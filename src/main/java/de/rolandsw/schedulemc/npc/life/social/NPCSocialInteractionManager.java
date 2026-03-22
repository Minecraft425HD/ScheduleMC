package de.rolandsw.schedulemc.npc.life.social;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NPC-zu-NPC Interaktions-System.
 *
 * NPCs interagieren untereinander:
 * - Handeln (beeinflusst Marktpreise)
 * - Streiten (Spieler koennen schlichten)
 * - Freundschaften (beeinflusst Geruechte-Verbreitung)
 * - Warnen (vor Spielern mit hohem Wanted-Level)
 *
 * Wird alle 200 Ticks (10 Sekunden) fuer nahegelegene NPCs ausgefuehrt.
 */
public class NPCSocialInteractionManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile NPCSocialInteractionManager instance;

    private static final double INTERACTION_RANGE = 8.0; // Bloecke
    private static final int TICK_INTERVAL = 200; // 10 Sekunden

    // NPC-Beziehungen: NPC1-UUID -> NPC2-UUID -> Beziehungswert (-100 bis 100)
    private final Map<UUID, Map<UUID, Integer>> npcRelations = new ConcurrentHashMap<>();

    // Aktive NPC-Interaktionen (damit sie nicht doppelt stattfinden)
    private final Set<String> activeInteractions = ConcurrentHashMap.newKeySet();

    public enum NPCInteractionType {
        TRADING("Handeln", 5),
        CHATTING("Plaudern", 2),
        ARGUING("Streiten", -3),
        WARNING("Warnen", 0),
        GREETING("Gruessen", 1);

        private final String displayName;
        private final int relationChange;

        NPCInteractionType(String displayName, int relationChange) {
            this.displayName = displayName;
            this.relationChange = relationChange;
        }

        public String getDisplayName() { return displayName; }
        public int getRelationChange() { return relationChange; }
    }

    public static class NPCInteraction {
        private final UUID npc1;
        private final UUID npc2;
        private final NPCInteractionType type;
        private final long timestamp;

        public NPCInteraction(UUID npc1, UUID npc2, NPCInteractionType type) {
            this.npc1 = npc1;
            this.npc2 = npc2;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }

        public UUID getNpc1() { return npc1; }
        public UUID getNpc2() { return npc2; }
        public NPCInteractionType getType() { return type; }
        public long getTimestamp() { return timestamp; }
    }

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private NPCSocialInteractionManager() {}

    public static NPCSocialInteractionManager getInstance() {
        if (instance == null) {
            synchronized (NPCSocialInteractionManager.class) {
                if (instance == null) {
                    instance = new NPCSocialInteractionManager();
                }
            }
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // TICK - Automatische NPC-Interaktionen
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird alle TICK_INTERVAL Ticks aufgerufen.
     * Findet nahegelegene NPC-Paare und loest Interaktionen aus.
     */
    public void tick(ServerLevel level) {
        net.minecraft.world.level.border.WorldBorder border = level.getWorldBorder();
        net.minecraft.world.phys.AABB worldArea = new net.minecraft.world.phys.AABB(
            border.getMinX(), level.getMinBuildHeight(), border.getMinZ(),
            border.getMaxX(), level.getMaxBuildHeight(), border.getMaxZ()
        );
        List<CustomNPCEntity> npcs = level.getEntitiesOfClass(
            CustomNPCEntity.class,
            worldArea
        );

        // Fuer jedes NPC-Paar in Reichweite: Interaktion auswuerfeln
        for (int i = 0; i < npcs.size(); i++) {
            for (int j = i + 1; j < npcs.size(); j++) {
                CustomNPCEntity npc1 = npcs.get(i);
                CustomNPCEntity npc2 = npcs.get(j);

                if (npc1.distanceTo(npc2) <= INTERACTION_RANGE) {
                    String pairKey = createPairKey(npc1.getUUID(), npc2.getUUID());

                    // Nur wenn nicht bereits interagierend
                    if (activeInteractions.add(pairKey)) {
                        processInteraction(npc1, npc2);
                        // Nach kurzer Zeit wieder freigeben
                        activeInteractions.remove(pairKey);
                    }
                }
            }
        }
    }

    private void processInteraction(CustomNPCEntity npc1, CustomNPCEntity npc2) {
        int relation = getRelation(npc1.getUUID(), npc2.getUUID());
        NPCInteractionType type = determineInteractionType(relation);

        // Beziehung aktualisieren
        modifyRelation(npc1.getUUID(), npc2.getUUID(), type.getRelationChange());

        LOGGER.debug("NPC interaction: {} {} {} (relation: {} -> {})",
            npc1.getNpcName(), type.getDisplayName(), npc2.getNpcName(),
            relation, relation + type.getRelationChange());
    }

    private NPCInteractionType determineInteractionType(int relation) {
        java.util.concurrent.ThreadLocalRandom rng = java.util.concurrent.ThreadLocalRandom.current();
        if (relation > 30) {
            // Gute Beziehung: meist positiv
            return rng.nextFloat() < 0.7f ? NPCInteractionType.CHATTING : NPCInteractionType.TRADING;
        } else if (relation < -30) {
            // Schlechte Beziehung: oft Streit
            return rng.nextFloat() < 0.6f ? NPCInteractionType.ARGUING : NPCInteractionType.GREETING;
        } else {
            // Neutral: gemischt
            float roll = rng.nextFloat();
            if (roll < 0.4f) return NPCInteractionType.GREETING;
            if (roll < 0.7f) return NPCInteractionType.CHATTING;
            return NPCInteractionType.TRADING;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // RELATION MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    public int getRelation(UUID npc1, UUID npc2) {
        Map<UUID, Integer> relations = npcRelations.get(npc1);
        if (relations != null) {
            Integer val = relations.get(npc2);
            if (val != null) return val;
        }
        return 0; // Neutral
    }

    public void modifyRelation(UUID npc1, UUID npc2, int change) {
        // Bidirektional
        applyRelationChange(npc1, npc2, change);
        applyRelationChange(npc2, npc1, change);
    }

    private void applyRelationChange(UUID from, UUID to, int change) {
        npcRelations.computeIfAbsent(from, k -> new ConcurrentHashMap<>())
            .merge(to, change, (old, delta) ->
                Math.max(-100, Math.min(100, old + delta))
            );
    }

    /**
     * Spieler schlichtet einen Streit zwischen zwei NPCs.
     * Belohnung: Beide NPCs verbessern Beziehung, Spieler bekommt Rep.
     */
    public boolean mediateConflict(UUID npc1, UUID npc2) {
        int relation = getRelation(npc1, npc2);
        if (relation >= 0) return false; // Kein Streit

        // Beziehung verbessern
        modifyRelation(npc1, npc2, 20);
        return true;
    }

    private String createPairKey(UUID a, UUID b) {
        // Konsistente Reihenfolge
        if (a.compareTo(b) < 0) {
            return a.toString() + ":" + b.toString();
        }
        return b.toString() + ":" + a.toString();
    }
}
