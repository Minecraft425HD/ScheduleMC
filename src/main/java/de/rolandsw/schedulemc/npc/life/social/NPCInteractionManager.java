package de.rolandsw.schedulemc.npc.life.social;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * NPCInteractionManager - Verwaltet Interaktionen zwischen NPCs mit JSON-Persistenz
 *
 * Features:
 * - Warnsystem (NPCs warnen sich vor Gefahren)
 * - Handel zwischen NPCs
 * - Soziale Interaktionen (Gespräche, Grüße)
 * - Gerüchte verbreiten
 * - Ambiente, automatische Interaktionen zwischen nahen NPCs samt langfristiger
 *   NPC-zu-NPC-Beziehungswerte (siehe {@link #autoTriggerNearbyInteractions}) —
 *   übernommen aus dem ehemals separaten, nie getickten
 *   {@code NPCSocialInteractionManager} (siehe CLAUDE.md "NPCInteractionManager/
 *   NPCSocialInteractionManager Merge").
 */
public class NPCInteractionManager extends AbstractPersistenceManager<NPCInteractionManager.InteractionManagerData> {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private static volatile NPCInteractionManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    @Nullable
    public static NPCInteractionManager getInstance() {
        return instance;
    }

    public static NPCInteractionManager initialize(MinecraftServer server) {
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

    /**
     * Helper method for level-based access.
     * Note: Manager is server-wide, not per-level.
     */
    public static NPCInteractionManager getManager(ServerLevel level) {
        return getInstance();
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

    /** Aktive Interaktionen: NPC UUID -> InteractionContext (TRANSIENT - nicht persistiert) */
    private final Map<UUID, InteractionContext> activeInteractions = new ConcurrentHashMap<>();

    /** Cooldowns: NPC UUID Pair -> Ticks until can interact again (TRANSIENT - nicht persistiert) */
    private final Map<String, Integer> interactionCooldowns = new ConcurrentHashMap<>();

    /**
     * Langfristige NPC-zu-NPC-Beziehungswerte (-100 bis 100), PERSISTIERT.
     * NPC1-UUID -> NPC2-UUID -> Beziehungswert. Übernommen aus NPCSocialInteractionManager.
     */
    private final Map<UUID, Map<UUID, Integer>> npcRelations = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private NPCInteractionManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("npc_life_interactions.json").toFile(),
            GsonHelper.get()
        );
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
        // Expliziter Iterator statt removeIf+setValue: setValue innerhalb von removeIf ist
        // ein spezifizierter Seiteneffekt, der von ConcurrentHashMap nicht unterstützt wird.
        Iterator<Map.Entry<String, Integer>> cooldownIter = interactionCooldowns.entrySet().iterator();
        while (cooldownIter.hasNext()) {
            Map.Entry<String, Integer> e = cooldownIter.next();
            int newVal = e.getValue() - 1;
            if (newVal <= 0) {
                cooldownIter.remove();
            } else {
                e.setValue(newVal);
            }
        }

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

        if (ThreadLocalRandom.current().nextDouble() > targetTrust) {
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
                targetLife.getMemory().addPlayerTag(dangerUUID, "Dangerous");
                targetLife.getEmotions().trigger(EmotionState.FEARFUL, 50.0f);
                targetLife.getNeeds().modifySafety(-20);
            }
            case "thief" -> {
                targetLife.getMemory().addPlayerTag(dangerUUID, "Dieb");
                targetLife.getEmotions().trigger(EmotionState.SUSPICIOUS, 30.0f);
            }
            default -> {}
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
        if (activeInteractions.containsKey(npc1.getNpcData().getNpcUUID()) ||  // NOPMD
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
    // AMBIENT NPC-NPC RELATIONS (übernommen aus NPCSocialInteractionManager)
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt den Beziehungswert zwischen zwei NPCs zurück (-100 bis 100, 0 = neutral).
     */
    public int getRelation(UUID npc1, UUID npc2) {
        Map<UUID, Integer> relations = npcRelations.get(npc1);
        if (relations != null) {
            Integer val = relations.get(npc2);
            if (val != null) return val;
        }
        return 0;
    }

    /**
     * Verändert den Beziehungswert zwischen zwei NPCs (bidirektional, geclampt auf -100..100).
     */
    public void modifyRelation(UUID npc1, UUID npc2, int change) {
        applyRelationChange(npc1, npc2, change);
        applyRelationChange(npc2, npc1, change);
        markDirty();
    }

    private void applyRelationChange(UUID from, UUID to, int change) {
        npcRelations.computeIfAbsent(from, k -> new ConcurrentHashMap<>())
            .merge(to, change, (old, delta) -> Math.max(-100, Math.min(100, old + delta)));
    }

    /**
     * Spieler schlichtet einen Streit zwischen zwei NPCs (verbessert die Beziehung).
     *
     * @return true wenn tatsächlich ein Streit vorlag (Beziehung war negativ)
     */
    public boolean mediateConflict(UUID npc1, UUID npc2) {
        if (getRelation(npc1, npc2) >= 0) return false;
        modifyRelation(npc1, npc2, 20);
        return true;
    }

    /**
     * Sucht alle NPC-Paare in Reichweite und löst pro Paar (falls {@link #canInteract} es
     * erlaubt) eine zufällige, vom aktuellen Beziehungswert abhängige ambiente Interaktion aus
     * (Begrüßung, Gespräch inkl. Gerüchte-Austausch, Handelsanbahnung, oder - bei schlechter
     * Beziehung - ein Streit). Gedacht für einen gedrosselten Aufruf (z.B. alle 200 Ticks),
     * NICHT jeden Tick - die Methode scannt alle NPCs der Level.
     */
    public void autoTriggerNearbyInteractions(ServerLevel level) {
        List<CustomNPCEntity> npcs = level.getEntitiesOfClass(CustomNPCEntity.class,
            new AABB(level.getWorldBorder().getMinX(), level.getMinBuildHeight(), level.getWorldBorder().getMinZ(),
                     level.getWorldBorder().getMaxX(), level.getMaxBuildHeight(), level.getWorldBorder().getMaxZ()));

        for (int i = 0; i < npcs.size(); i++) {
            for (int j = i + 1; j < npcs.size(); j++) {
                CustomNPCEntity npc1 = npcs.get(i);
                CustomNPCEntity npc2 = npcs.get(j);

                if (npc1.distanceTo(npc2) > INTERACTION_RANGE) continue;
                if (!canInteract(npc1, npc2)) continue;

                triggerAmbientInteraction(npc1, npc2, level);
            }
        }
    }

    private void triggerAmbientInteraction(CustomNPCEntity npc1, CustomNPCEntity npc2, ServerLevel level) {
        UUID id1 = npc1.getNpcData().getNpcUUID();
        UUID id2 = npc2.getNpcData().getNpcUUID();
        int relation = getRelation(id1, id2);
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        if (relation > 30) {
            // Gute Beziehung: meist Gespräch, manchmal Handel
            if (rng.nextFloat() < 0.7f) {
                converse(npc1, npc2, level);
            } else {
                initiateNPCTrade(npc1, npc2);
            }
            modifyRelation(id1, id2, 5);
        } else if (relation < -30) {
            // Schlechte Beziehung: meist Streit (keine Kontext-Warnung möglich, daher nur Beziehungsabbau),
            // manchmal doch eine (kurze) Begrüßung
            if (rng.nextFloat() < 0.6f) {
                modifyRelation(id1, id2, -3);
                setCooldown(npc1, npc2);
            } else {
                greet(npc1, npc2);
                modifyRelation(id1, id2, 1);
            }
        } else {
            // Neutral: gemischt
            float roll = rng.nextFloat();
            if (roll < 0.4f) {
                greet(npc1, npc2);
                modifyRelation(id1, id2, 1);
            } else if (roll < 0.7f) {
                converse(npc1, npc2, level);
                modifyRelation(id1, id2, 2);
            } else {
                initiateNPCTrade(npc1, npc2);
                modifyRelation(id1, id2, 5);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<InteractionManagerData>(){}.getType();
    }

    @Override
    protected void onDataLoaded(InteractionManagerData data) {
        // Aktive Interaktionen und Cooldowns werden bei Level-Load bewusst zurückgesetzt
        // (transient, ergeben nach einem Neustart keinen Sinn mehr).
        activeInteractions.clear();
        interactionCooldowns.clear();
        npcRelations.clear();

        int invalidCount = 0;
        int correctedCount = 0;

        if (data == null || data.npcRelations == null) {
            if (data == null) {
                LOGGER.warn("Null data loaded, using defaults");
                invalidCount++;
            }
        } else {
            if (data.npcRelations.size() > 10000) {
                LOGGER.warn("NPC relation map size ({}) exceeds limit, potential corruption",
                    data.npcRelations.size());
                correctedCount++;
            }
            for (Map.Entry<String, Map<String, Integer>> outer : data.npcRelations.entrySet()) {
                UUID npc1;
                try {
                    npc1 = UUID.fromString(outer.getKey());
                } catch (IllegalArgumentException e) {
                    invalidCount++;
                    continue;
                }
                Map<UUID, Integer> inner = new ConcurrentHashMap<>();
                if (outer.getValue() != null) {
                    for (Map.Entry<String, Integer> entry : outer.getValue().entrySet()) {
                        try {
                            UUID npc2 = UUID.fromString(entry.getKey());
                            if (entry.getValue() == null) {
                                invalidCount++;
                                continue;
                            }
                            inner.put(npc2, Math.max(-100, Math.min(100, entry.getValue())));
                        } catch (IllegalArgumentException e) {
                            invalidCount++;
                        }
                    }
                }
                npcRelations.put(npc1, inner);
            }
        }

        // SUMMARY
        if (invalidCount > 0 || correctedCount > 0) {
            LOGGER.warn("Data validation: {} invalid entries, {} corrected entries",
                invalidCount, correctedCount);
            if (correctedCount > 0) {
                markDirty(); // Re-save corrected data
            }
        }
    }

    @Override
    protected InteractionManagerData getCurrentData() {
        InteractionManagerData data = new InteractionManagerData();
        data.npcRelations = new HashMap<>();
        for (Map.Entry<UUID, Map<UUID, Integer>> outer : npcRelations.entrySet()) {
            Map<String, Integer> inner = new HashMap<>();
            for (Map.Entry<UUID, Integer> entry : outer.getValue().entrySet()) {
                inner.put(entry.getKey().toString(), entry.getValue());
            }
            data.npcRelations.put(outer.getKey().toString(), inner);
        }
        return data;
    }

    @Override
    protected String getComponentName() {
        return "NPCInteractionManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("%d active, %d cooldowns, %d NPC relations tracked",
            activeInteractions.size(), interactionCooldowns.size(), npcRelations.size());
    }

    @Override
    protected void onCriticalLoadFailure() {
        activeInteractions.clear();
        interactionCooldowns.clear();
        npcRelations.clear();
    }

    @Override
    public String toString() {
        return String.format("NPCInteractionManager{activeInteractions=%d, cooldowns=%d, relations=%d}",
            activeInteractions.size(), interactionCooldowns.size(), npcRelations.size());
    }

    /**
     * Serialisierbare Daten (nur die langfristigen NPC-zu-NPC-Beziehungswerte;
     * aktive Interaktionen/Cooldowns sind bewusst transient).
     */
    public static class InteractionManagerData {
        public Map<String, Map<String, Integer>> npcRelations;
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
