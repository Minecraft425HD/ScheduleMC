package de.rolandsw.schedulemc.npc.life.social;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NPCInteractionManager - Verwaltet Interaktionen zwischen NPCs mit JSON-Persistenz
 *
 * Features:
 * - Warnsystem (NPCs warnen sich vor Gefahren)
 * - Handel zwischen NPCs
 * - Soziale Interaktionen (Gespräche, Grüße)
 * - Gerüchte verbreiten
 */
public class NPCInteractionManager extends AbstractPersistenceManager<Map<String, Object>> {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private static volatile NPCInteractionManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    @Nullable
    public static NPCInteractionManager getInstance() {
        return instance;
    }

    public static NPCInteractionManager getInstance(MinecraftServer server) {
        NPCInteractionManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new NPCInteractionManager(server);
                }
            }
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Maximale Distanz für NPC-NPC Interaktionen (in Blöcken) */
    public static final double INTERACTION_RANGE = 8.0;

    /** Cooldown zwischen Interaktionen (in Ticks) */
    public static final int INTERACTION_COOLDOWN = 600; // 30 Sekunden

    /** Maximale gleichzeitige Interaktionen pro NPC */
    public static final int MAX_CONCURRENT_INTERACTIONS = 1;

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private MinecraftServer server;

    /** Aktive Interaktionen: NPC UUID -> InteractionContext (TRANSIENT - nicht persistiert) */
    private final Map<UUID, InteractionContext> activeInteractions = new ConcurrentHashMap<>();

    /** Cooldowns: NPC UUID Pair -> Ticks until can interact again (TRANSIENT - nicht persistiert) */
    private final Map<String, Integer> interactionCooldowns = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private NPCInteractionManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("npc_life_interactions.json").toFile(),
            GsonHelper.get()
        );
        this.server = server;
        load();
    }

    // ═══════════════════════════════════════════════════════════
    // TICK / UPDATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird jeden Tick aufgerufen
     */
    public void tick() {
        // Cooldowns verringern
        interactionCooldowns.entrySet().removeIf(e -> {
            e.setValue(e.getValue() - 1);
            return e.getValue() <= 0;
        });

        // Aktive Interaktionen ticken
        activeInteractions.entrySet().removeIf(e -> {
            e.getValue().tick();
            return e.getValue().isFinished();
        });
    }

    // ═══════════════════════════════════════════════════════════
    // WARNING SYSTEM
    // ═══════════════════════════════════════════════════════════

    /**
     * Ein NPC warnt einen anderen vor einer Gefahr
     */
    public boolean warnNPC(CustomNPCEntity warner, CustomNPCEntity target, UUID dangerUUID, String warningType) {
        if (!canInteract(warner, target)) return false;

        // Cooldown setzen
        setCooldown(warner, target);

        NPCLifeData targetLife = target.getLifeData();
        NPCLifeData warnerLife = warner.getLifeData();

        if (targetLife == null || warnerLife == null) return false;

        // Warnung nur annehmen wenn Warner vertrauenswürdig
        float warnerHonesty = warnerLife.getTraits().getHonesty();
        float targetTrust = 0.5f + (warnerHonesty / 200.0f); // 0.0 - 1.0

        if (Math.random() > targetTrust) {
            // Target glaubt nicht
            return false;
        }

        // Warnung verarbeiten
        switch (warningType) {
            case "criminal" -> {
                targetLife.getMemory().addPlayerTag(dangerUUID, "Kriminell");
                targetLife.getEmotions().trigger(EmotionState.SUSPICIOUS, 40.0f);
            }
            case "dangerous" -> {
                targetLife.getMemory().addPlayerTag(dangerUUID, "Gefährlich");
                targetLife.getEmotions().trigger(EmotionState.FEARFUL, 50.0f);
                targetLife.getNeeds().modifySafety(-20);
            }
            case "thief" -> {
                targetLife.getMemory().addPlayerTag(dangerUUID, "Dieb");
                targetLife.getEmotions().trigger(EmotionState.SUSPICIOUS, 30.0f);
            }
        }

        // Erinnerung an Warnung speichern
        targetLife.getMemory().addMemory(
            warner.getNpcData().getNpcUUID(),
            MemoryType.RUMOR_HEARD,
            "Warnung erhalten: " + warningType,
            5
        );

        return true;
    }

    /**
     * Broadcast-Warnung an alle NPCs in der Nähe
     */
    public void broadcastWarning(CustomNPCEntity warner, ServerLevel level, UUID dangerUUID, String warningType, double range) {
        level.getEntities(warner, warner.getBoundingBox().inflate(range), e -> e instanceof CustomNPCEntity)
            .stream()
            .filter(e -> e != warner)
            .map(e -> (CustomNPCEntity) e)
            .forEach(npc -> warnNPC(warner, npc, dangerUUID, warningType));
    }

    // ═══════════════════════════════════════════════════════════
    // NPC TRADING
    // ═══════════════════════════════════════════════════════════

    /**
     * Initiiert Handel zwischen zwei NPCs
     */
    public boolean initiateNPCTrade(CustomNPCEntity buyer, CustomNPCEntity seller) {
        if (!canInteract(buyer, seller)) return false;

        NPCLifeData buyerLife = buyer.getLifeData();
        NPCLifeData sellerLife = seller.getLifeData();

        if (buyerLife == null || sellerLife == null) return false;

        // Prüfen ob beide handeln wollen
        if (!buyerLife.isWillingToTrade() || !sellerLife.isWillingToTrade()) {
            return false;
        }

        // Interaktion starten
        InteractionContext context = new InteractionContext(
            buyer.getNpcData().getNpcUUID(),
            seller.getNpcData().getNpcUUID(),
            InteractionType.TRADE,
            200 // 10 Sekunden
        );

        activeInteractions.put(buyer.getNpcData().getNpcUUID(), context);
        activeInteractions.put(seller.getNpcData().getNpcUUID(), context);

        setCooldown(buyer, seller);

        return true;
    }

    /**
     * Führt den eigentlichen Handel aus (vereinfacht - nur Wallet-Transfer)
     */
    public boolean executeNPCTrade(CustomNPCEntity buyer, CustomNPCEntity seller, int amount) {
        // Prüfen ob Handel aktiv
        InteractionContext context = activeInteractions.get(buyer.getNpcData().getNpcUUID());
        if (context == null || context.type != InteractionType.TRADE) {
            return false;
        }

        // Wallet-Transfer
        int buyerWallet = buyer.getNpcData().getWallet();
        if (buyerWallet < amount) {
            return false; // Nicht genug Geld
        }

        buyer.getNpcData().setWallet(buyerWallet - amount);
        seller.getNpcData().setWallet(seller.getNpcData().getWallet() + amount);

        // Emotion bei erfolgreichem Handel
        if (seller.getLifeData() != null) {
            seller.getLifeData().getEmotions().trigger(EmotionState.HAPPY, 20.0f, 600);
        }

        // Interaktion beenden
        context.finish();

        return true;
    }

    // ═══════════════════════════════════════════════════════════
    // SOCIAL INTERACTIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * NPCs begrüßen sich
     */
    public void greet(CustomNPCEntity npc1, CustomNPCEntity npc2) {
        if (!canInteract(npc1, npc2)) return;

        NPCLifeData life1 = npc1.getLifeData();
        NPCLifeData life2 = npc2.getLifeData();

        if (life1 == null || life2 == null) return;

        // Kleine positive Emotion
        life1.getEmotions().trigger(EmotionState.HAPPY, 10.0f, 200);
        life2.getEmotions().trigger(EmotionState.HAPPY, 10.0f, 200);

        // NPCs schauen sich an
        npc1.getLookControl().setLookAt(npc2);
        npc2.getLookControl().setLookAt(npc1);

        setCooldown(npc1, npc2);
    }

    /**
     * NPCs unterhalten sich (und tauschen dabei möglicherweise Gerüchte)
     */
    public void converse(CustomNPCEntity npc1, CustomNPCEntity npc2, ServerLevel level) {
        if (!canInteract(npc1, npc2)) return;

        NPCLifeData life1 = npc1.getLifeData();
        NPCLifeData life2 = npc2.getLifeData();

        if (life1 == null || life2 == null) return;

        // Interaktion starten
        InteractionContext context = new InteractionContext(
            npc1.getNpcData().getNpcUUID(),
            npc2.getNpcData().getNpcUUID(),
            InteractionType.CONVERSATION,
            400 // 20 Sekunden
        );

        activeInteractions.put(npc1.getNpcData().getNpcUUID(), context);
        activeInteractions.put(npc2.getNpcData().getNpcUUID(), context);

        // Gerüchte austauschen
        RumorNetwork.getNetwork(level).spreadRumorsBetweenNPCs(npc1, npc2, level.getDayTime() / 24000);

        setCooldown(npc1, npc2);
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob zwei NPCs miteinander interagieren können
     */
    public boolean canInteract(CustomNPCEntity npc1, CustomNPCEntity npc2) {
        if (npc1 == npc2) return false;
        if (npc1.distanceTo(npc2) > INTERACTION_RANGE) return false;

        // Cooldown prüfen
        String cooldownKey = createCooldownKey(npc1, npc2);
        if (interactionCooldowns.containsKey(cooldownKey)) return false;

        // Prüfen ob bereits in Interaktion
        if (activeInteractions.containsKey(npc1.getNpcData().getNpcUUID()) ||
            activeInteractions.containsKey(npc2.getNpcData().getNpcUUID())) {
            return false;
        }

        return true;
    }

    /**
     * Setzt einen Cooldown zwischen zwei NPCs
     */
    private void setCooldown(CustomNPCEntity npc1, CustomNPCEntity npc2) {
        String key = createCooldownKey(npc1, npc2);
        interactionCooldowns.put(key, INTERACTION_COOLDOWN);
    }

    /**
     * Erstellt einen eindeutigen Key für ein NPC-Paar
     */
    private String createCooldownKey(CustomNPCEntity npc1, CustomNPCEntity npc2) {
        UUID uuid1 = npc1.getNpcData().getNpcUUID();
        UUID uuid2 = npc2.getNpcData().getNpcUUID();
        // Sortieren für konsistenten Key
        if (uuid1.compareTo(uuid2) < 0) {
            return uuid1 + "_" + uuid2;
        }
        return uuid2 + "_" + uuid1;
    }

    /**
     * Findet nahegelegene NPCs
     */
    public List<CustomNPCEntity> findNearbyNPCs(CustomNPCEntity npc, ServerLevel level, double range) {
        return level.getEntities(npc, npc.getBoundingBox().inflate(range), e -> e instanceof CustomNPCEntity)
            .stream()
            .filter(e -> e != npc)
            .map(e -> (CustomNPCEntity) e)
            .toList();
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<String, Object>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<String, Object> data) {
        // Keine persistenten Daten - aktive Interaktionen und Cooldowns
        // werden bei Level-Load automatisch zurückgesetzt
        activeInteractions.clear();
        interactionCooldowns.clear();
    }

    @Override
    protected Map<String, Object> getCurrentData() {
        // Keine persistenten Daten
        return new HashMap<>();
    }

    @Override
    protected String getComponentName() {
        return "NPCInteractionManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("%d active, %d cooldowns",
            activeInteractions.size(), interactionCooldowns.size());
    }

    @Override
    protected void onCriticalLoadFailure() {
        activeInteractions.clear();
        interactionCooldowns.clear();
    }

    @Override
    public String toString() {
        return String.format("NPCInteractionManager{activeInteractions=%d, cooldowns=%d}",
            activeInteractions.size(), interactionCooldowns.size());
    }

    // ═══════════════════════════════════════════════════════════
    // INNER CLASSES
    // ═══════════════════════════════════════════════════════════

    /**
     * Typ der Interaktion
     */
    public enum InteractionType {
        GREETING,
        CONVERSATION,
        TRADE,
        WARNING,
        HELP_REQUEST
    }

    /**
     * Kontext einer laufenden Interaktion
     */
    public static class InteractionContext {
        public final UUID participant1;
        public final UUID participant2;
        public final InteractionType type;
        public int ticksRemaining;
        private boolean finished = false;

        public InteractionContext(UUID p1, UUID p2, InteractionType type, int duration) {
            this.participant1 = p1;
            this.participant2 = p2;
            this.type = type;
            this.ticksRemaining = duration;
        }

        public void tick() {
            ticksRemaining--;
            if (ticksRemaining <= 0) {
                finished = true;
            }
        }

        public void finish() {
            finished = true;
        }

        public boolean isFinished() {
            return finished;
        }
    }
}
