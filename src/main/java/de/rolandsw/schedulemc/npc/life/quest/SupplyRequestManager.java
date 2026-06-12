package de.rolandsw.schedulemc.npc.life.quest;

import de.rolandsw.schedulemc.managers.NPCAcquaintanceManager;
import de.rolandsw.schedulemc.messaging.NPCMessageTemplates;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.SupplyRequestNoticePacket;
import de.rolandsw.schedulemc.npc.personality.NPCRelationship;
import de.rolandsw.schedulemc.npc.personality.NPCRelationshipManager;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Proaktive Warenanfragen: NPCs mit guter Beziehung (Level >= 25) bitten
 * Spieler aktiv um Waren und vereinbaren einen Treffpunkt.
 *
 * - Nah: Chat-Hinweis + "!"-Indikator über dem Namen
 * - Fern: Smartphone-Nachricht über das Messaging-System
 * - Annahme im Dialog -> wird zur regulären Quest (QuestType.SUPPLY)
 */
public class SupplyRequestManager extends AbstractPersistenceManager<SupplyRequestManager.Data> {

    private static volatile SupplyRequestManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    /** Beziehungslevel, ab dem NPCs Anfragen stellen. */
    public static final int MIN_RELATIONSHIP_LEVEL = 25;
    /** Cooldown in Spieltagen pro (NPC, Spieler) nach Angebot/Ablehnung. */
    public static final int OFFER_COOLDOWN_DAYS = 2;
    /** Offene Angebote verfallen nach so vielen Tagen (nur bei Online-Spieler). */
    public static final int PENDING_EXPIRY_DAYS = 3;
    public static final int MAX_OPEN_PER_PLAYER = 3;
    /** Chance pro Prüfintervall, dass ein berechtigter NPC fragt. */
    public static final double OFFER_CHANCE = 0.15;
    /** Distanz für den Nah-Hinweis. */
    public static final double NEARBY_DISTANCE = 12.0;

    /** Key "npcId:playerId" -> Anfrage (offen oder angenommen). */
    private final Map<String, SupplyRequest> requests = new ConcurrentHashMap<>();
    /** Key "npcId:playerId" -> Spieltag des letzten Angebots (Cooldown). */
    private final Map<String, Long> lastOfferDay = new ConcurrentHashMap<>();

    private final MinecraftServer server;

    @Nullable
    public static SupplyRequestManager getInstance() {
        return instance;
    }

    public static SupplyRequestManager initialize(MinecraftServer server) {
        SupplyRequestManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new SupplyRequestManager(server);
                }
            }
        }
        return result;
    }

    private SupplyRequestManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("npc_supply_requests.json").toFile(),
            GsonHelper.get()
        );
        this.server = server;
        load();
    }

    private static String key(UUID npcId, UUID playerId) {
        return npcId + ":" + playerId;
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    public Optional<SupplyRequest> getPendingRequest(UUID npcDataId, UUID playerId) {
        SupplyRequest req = requests.get(key(npcDataId, playerId));
        return (req != null && !req.isAccepted()) ? Optional.of(req) : Optional.empty();
    }

    public boolean hasAnyPendingForNpc(UUID npcDataId) {
        return requests.values().stream().anyMatch(r -> r.npcId.equals(npcDataId) && !r.isAccepted());
    }

    private long openRequestCount(UUID playerId) {
        return requests.values().stream().filter(r -> r.playerId.equals(playerId)).count();
    }

    // ═══════════════════════════════════════════════════════════
    // TICK: Generierung, Benachrichtigung, Ablauf
    // ═══════════════════════════════════════════════════════════

    public void tick(ServerLevel level) {
        long currentDay = level.getDayTime() / 24000;

        for (ServerPlayer player : level.players()) {
            generateOffersNear(level, player, currentDay);
            notifyPlayer(level, player, currentDay);
        }
        expireRequests(level, currentDay);
    }

    private void generateOffersNear(ServerLevel level, ServerPlayer player, long currentDay) {
        if (openRequestCount(player.getUUID()) >= MAX_OPEN_PER_PLAYER) return;

        NPCAcquaintanceManager acquaintances = NPCAcquaintanceManager.getInstance();
        NPCRelationshipManager relationships = NPCRelationshipManager.getInstance();
        if (acquaintances == null || relationships == null) return;

        List<CustomNPCEntity> nearby = level.getEntitiesOfClass(
            CustomNPCEntity.class, new AABB(player.blockPosition()).inflate(64));

        for (CustomNPCEntity npc : nearby) {
            if (npc.getNpcData() == null || npc.getLifeData() == null) continue;
            UUID npcId = npc.getNpcData().getNpcUUID();
            String k = key(npcId, player.getUUID());

            if (requests.containsKey(k)) continue;
            if (hasAnyPendingForNpc(npcId)) continue;
            Long last = lastOfferDay.get(k);
            if (last != null && currentDay - last < OFFER_COOLDOWN_DAYS) continue;
            if (!acquaintances.knowsNPC(player.getUUID(), npcId)) continue;

            NPCRelationship rel = relationships.getRelationship(npcId, player.getUUID());
            if (rel == null || rel.getRelationshipLevel() < MIN_RELATIONSHIP_LEVEL) continue;

            EmotionState emotion = npc.getLifeData().getEmotions().getCurrentEmotion();
            if (emotion == EmotionState.FEARFUL || emotion == EmotionState.ANGRY) continue;
            if (npc.getPersistentData().getBoolean("IsKnockedOut")) continue;

            if (ThreadLocalRandom.current().nextDouble() >= OFFER_CHANCE) continue;

            SupplyRequestPlanner.PlannedRequest planned =
                SupplyRequestPlanner.plan(npc, ThreadLocalRandom.current());
            if (planned == null) continue;

            BlockPos meeting = resolveMeetingPoint(npc);
            SupplyRequest request = new SupplyRequest(
                npcId, player.getUUID(),
                SupplyRequestPlanner.itemRegistryId(planned.item()),
                planned.amount(), planned.payment(),
                meeting.getX(), meeting.getY(), meeting.getZ(), currentDay);

            requests.put(k, request);
            lastOfferDay.put(k, currentDay);
            markDirty();
            NPCNetworkHandler.sendToClient(new SupplyRequestNoticePacket(npcId, true), player);

            if (openRequestCount(player.getUUID()) >= MAX_OPEN_PER_PLAYER) return;
        }
    }

    private void notifyPlayer(ServerLevel level, ServerPlayer player, long currentDay) {
        for (SupplyRequest req : requests.values()) {
            if (!req.playerId.equals(player.getUUID()) || req.isAccepted()) continue;

            CustomNPCEntity npc = findNpc(level, req.npcId);
            Item item = SupplyRequestPlanner.itemFromRegistryId(req.itemId);
            String itemName = item != null
                ? Component.translatable(item.getDescriptionId()).getString() : req.itemId;
            String npcName = npc != null ? npc.getName().getString() : "?";

            boolean nearby = npc != null
                && npc.distanceToSqr(player) <= NEARBY_DISTANCE * NEARBY_DISTANCE;

            if (nearby && !req.noticedNearby) {
                req.noticedNearby = true;
                markDirty();
                player.sendSystemMessage(Component.literal("§6[" + npcName + "] §f")
                    .append(Component.translatable("supply.schedulemc.nearby_hint",
                        req.amount, itemName)));
                npc.getLookControl().setLookAt(player);
            } else if (!nearby && !req.notifiedRemotely && currentDay >= req.offerDay) {
                // Spieler nicht in der Nähe -> Smartphone-Nachricht
                req.notifiedRemotely = true;
                markDirty();
                String text = NPCMessageTemplates.getSupplyRequestMessage(
                    itemName, req.amount, req.payment,
                    req.meetingX, req.meetingY, req.meetingZ,
                    req.offerDay + PENDING_EXPIRY_DAYS);
                sendSmartphoneMessage(player, req.npcId, npcName, text);
            }
        }
    }

    private void expireRequests(ServerLevel level, long currentDay) {
        Iterator<Map.Entry<String, SupplyRequest>> it = requests.entrySet().iterator();
        while (it.hasNext()) {
            SupplyRequest req = it.next().getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(req.playerId);

            if (!req.isAccepted()) {
                // Offene Angebote verfallen nur, wenn der Spieler online ist
                if (player != null && currentDay - req.offerDay >= PENDING_EXPIRY_DAYS) {
                    it.remove();
                    markDirty();
                    notifyRemoved(player, req.npcId);
                }
                continue;
            }

            // Angenommene Anfragen: Quest-Status prüfen
            if (player == null) continue;
            QuestManager questManager = QuestManager.getInstance();
            if (questManager == null) continue;
            Quest active = questManager.getProgress(player).getActiveQuest(req.questId);
            if (active == null) {
                // Quest weg (abgeschlossen via onQuestCompleted-Hook bereits entfernt,
                // hier also: fehlgeschlagen/abgebrochen) -> NPC ist enttäuscht
                it.remove();
                markDirty();
                applyDisappointment(level, player, req);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // AKZEPTIEREN / ABLEHNEN / ABSCHLUSS
    // ═══════════════════════════════════════════════════════════

    /**
     * Wandelt die offene Anfrage in eine reguläre Quest (QuestType.SUPPLY) um.
     * @return Quest-Titel oder null bei Fehlschlag
     */
    @Nullable
    public String acceptRequest(ServerPlayer player, CustomNPCEntity npc) {
        if (npc.getNpcData() == null) return null;
        UUID npcId = npc.getNpcData().getNpcUUID();
        SupplyRequest req = requests.get(key(npcId, player.getUUID()));
        if (req == null || req.isAccepted()) return null;

        Item item = SupplyRequestPlanner.itemFromRegistryId(req.itemId);
        QuestManager questManager = QuestManager.getInstance();
        if (item == null || questManager == null) return null;

        String itemName = Component.translatable(item.getDescriptionId()).getString();
        String questId = "supply_" + UUID.randomUUID();
        String title = "Errand: " + req.amount + "x " + itemName;
        String description = npc.getName().getString() + " needs " + req.amount + "x " + itemName
            + ". Meeting point: " + req.meetingX + ", " + req.meetingY + ", " + req.meetingZ
            + " — payment: " + req.payment + "€";

        Quest quest = new Quest(questId, title, description, QuestType.SUPPLY, npcId,
            Faction.forNPCType(npc.getNpcType()));
        quest.addObjective(QuestObjective.collectItems("collect", item, req.amount,
            "Obtain " + req.amount + "x " + itemName));
        quest.addObjective(QuestObjective.visitLocation("meet", req.getMeetingPoint(), 5,
            "Go to the meeting point (" + req.meetingX + ", " + req.meetingY + ", " + req.meetingZ + ")"));
        quest.addObjective(QuestObjective.deliverToNPC("deliver", item, req.amount, npcId,
            "Hand over the goods"));
        quest.setReward(QuestReward.create().money(req.payment)
            .factionRep(Faction.forNPCType(npc.getNpcType()), 2));
        quest.setTimeLimit(2);

        if (!questManager.acceptQuest(player, quest)) {
            return null;
        }
        req.questId = questId;
        markDirty();
        notifyRemoved(player, npcId);
        return title;
    }

    /** Lehnt die offene Anfrage ab (kleiner Beziehungs-Malus, Cooldown läuft). */
    public void declineRequest(ServerPlayer player, CustomNPCEntity npc) {
        if (npc.getNpcData() == null) return;
        UUID npcId = npc.getNpcData().getNpcUUID();
        SupplyRequest removed = requests.remove(key(npcId, player.getUUID()));
        if (removed == null) return;
        markDirty();
        notifyRemoved(player, npcId);

        NPCRelationshipManager relationships = NPCRelationshipManager.getInstance();
        if (relationships != null) {
            relationships.getOrCreateRelationship(npcId, player.getUUID()).modifyRelationship(-1);
        }
    }

    /** Entfernt alle Anfragen eines zerstörten NPCs. */
    public void removeForNpc(UUID npcDataId) {
        boolean changed = requests.entrySet().removeIf(e -> e.getValue().npcId.equals(npcDataId));
        if (changed) {
            markDirty();
        }
    }

    /** Hook aus QuestManager.completeQuest: räumt erledigte SUPPLY-Anfragen auf. */
    public void onQuestCompleted(ServerPlayer player, Quest quest) {
        if (quest.getType() != QuestType.SUPPLY) return;
        requests.entrySet().removeIf(e -> {
            SupplyRequest req = e.getValue();
            if (quest.getId().equals(req.questId)) {
                markDirty();
                return true;
            }
            return false;
        });
    }

    private void applyDisappointment(ServerLevel level, ServerPlayer player, SupplyRequest req) {
        NPCRelationshipManager relationships = NPCRelationshipManager.getInstance();
        if (relationships != null) {
            relationships.getOrCreateRelationship(req.npcId, player.getUUID()).modifyRelationship(-5);
        }
        CustomNPCEntity npc = findNpc(level, req.npcId);
        String npcName = npc != null ? npc.getName().getString() : "?";
        if (npc != null && npc.getLifeData() != null) {
            npc.getLifeData().getEmotions().trigger(EmotionState.SAD, 40.0f);
        }
        sendSmartphoneMessage(player, req.npcId, npcName,
            NPCMessageTemplates.getSupplyRequestDisappointedMessage());
    }

    // ═══════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════

    private static BlockPos resolveMeetingPoint(CustomNPCEntity npc) {
        var loc = npc.getNpcData().getLocationData();
        if (loc.getWorkLocation() != null) return loc.getWorkLocation();
        if (loc.getHomeLocation() != null) return loc.getHomeLocation();
        return npc.blockPosition();
    }

    @Nullable
    private static CustomNPCEntity findNpc(ServerLevel level, UUID npcDataId) {
        for (var entity : level.getAllEntities()) {
            if (entity instanceof CustomNPCEntity npc
                && npc.getNpcData() != null
                && npcDataId.equals(npc.getNpcData().getNpcUUID())) {
                return npc;
            }
        }
        return null;
    }

    private void sendSmartphoneMessage(ServerPlayer player, UUID npcId, String npcName, String text) {
        de.rolandsw.schedulemc.messaging.MessageManager.sendMessage(
            npcId, npcName, false,
            player.getUUID(), player.getName().getString(), true,
            text);
        de.rolandsw.schedulemc.messaging.network.MessageNetworkHandler.sendToClient(
            new de.rolandsw.schedulemc.messaging.network.ReceiveMessagePacket(npcId, npcName, false, text),
            player);
    }

    private void notifyRemoved(ServerPlayer player, UUID npcId) {
        NPCNetworkHandler.sendToClient(new SupplyRequestNoticePacket(npcId, false), player);
    }

    /** Voll-Sync der "!"-Indikatoren beim Login. */
    public void syncToPlayer(ServerPlayer player) {
        for (SupplyRequest req : requests.values()) {
            if (req.playerId.equals(player.getUUID()) && !req.isAccepted()) {
                NPCNetworkHandler.sendToClient(new SupplyRequestNoticePacket(req.npcId, true), player);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE (AbstractPersistenceManager)
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return Data.class;
    }

    @Override
    protected Data getCurrentData() {
        Data data = new Data();
        data.requests = new HashMap<>(requests);
        data.lastOfferDay = new HashMap<>(lastOfferDay);
        return data;
    }

    @Override
    protected void onDataLoaded(Data data) {
        requests.clear();
        lastOfferDay.clear();
        if (data != null) {
            if (data.requests != null) requests.putAll(data.requests);
            if (data.lastOfferDay != null) lastOfferDay.putAll(data.lastOfferDay);
        }
    }

    @Override
    protected String getComponentName() {
        return "SupplyRequestManager";
    }

    @Override
    protected String getHealthDetails() {
        return requests.size() + " requests";
    }

    @Override
    protected void onCriticalLoadFailure() {
        requests.clear();
        lastOfferDay.clear();
    }

    public static class Data {
        public Map<String, SupplyRequest> requests;
        public Map<String, Long> lastOfferDay;
    }
}
