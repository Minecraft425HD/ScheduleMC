package de.rolandsw.schedulemc.managers;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NPCEntityRegistry - Central Registry für O(1) NPC Lookups
 *
 * Performance-Optimierung:
 * VORHER: getAllEntities() durchläuft ALLE Entities (O(n) - kann 1000+ Entities sein)
 * NACHHER: UUID → NPC Lookup in O(1) via HashMap
 *
 * Features:
 * - UUID → CustomNPCEntity Mapping (O(1))
 * - Automatische Registrierung beim Spawn
 * - Automatische Deregistrierung beim Despawn
 * - Thread-safe Operations
 * - Welt-übergreifende Suche
 *
 * Usage:
 * - NPCs registrieren sich automatisch in CustomNPCEntity.onAddedToWorld()
 * - NPCs deregistrieren sich automatisch in CustomNPCEntity.remove()
 * - Lookups via NPCEntityRegistry.getNPCByUUID(uuid, level)
 */
public class NPCEntityRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    // UUID → CustomNPCEntity Mapping (per Welt)
    // Key: Level dimension (z.B. "minecraft:overworld")
    // Value: UUID → CustomNPCEntity Map
    private static final Map<String, Map<UUID, CustomNPCEntity>> npcsByWorld = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════
    // REGISTRIERUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert einen NPC im Registry
     * Wird automatisch von CustomNPCEntity.onAddedToWorld() aufgerufen
     *
     * @param npc Der zu registrierende NPC
     */
    public static void registerNPC(CustomNPCEntity npc) {
        if (npc == null || npc.level() == null) {
            return;
        }

        String worldKey = getWorldKey(npc.level());
        UUID npcUUID = npc.getUUID();

        // Hole oder erstelle Map für diese Welt
        Map<UUID, CustomNPCEntity> worldNPCs = npcsByWorld.computeIfAbsent(
            worldKey, k -> new ConcurrentHashMap<>()
        );

        worldNPCs.put(npcUUID, npc);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[NPCEntityRegistry] Registered NPC: {} (UUID: {}, World: {})",
                npc.getNpcName(), npcUUID, worldKey);
        }
    }

    /**
     * Entfernt einen NPC aus dem Registry
     * Wird automatisch von CustomNPCEntity.remove() aufgerufen
     *
     * @param npc Der zu entfernende NPC
     */
    public static void unregisterNPC(CustomNPCEntity npc) {
        if (npc == null || npc.level() == null) {
            return;
        }

        String worldKey = getWorldKey(npc.level());
        UUID npcUUID = npc.getUUID();

        Map<UUID, CustomNPCEntity> worldNPCs = npcsByWorld.get(worldKey);
        if (worldNPCs != null) {
            worldNPCs.remove(npcUUID);

            // Cleanup: Entferne leere World-Maps
            if (worldNPCs.isEmpty()) {
                npcsByWorld.remove(worldKey);
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[NPCEntityRegistry] Unregistered NPC: {} (UUID: {}, World: {})",
                    npc.getNpcName(), npcUUID, worldKey);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SUCHE & LOOKUP (O(1))
    // ═══════════════════════════════════════════════════════════

    /**
     * Findet einen NPC anhand seiner UUID in einem spezifischen ServerLevel
     * Performance: O(1) statt O(n)
     *
     * @param uuid Die UUID des NPCs
     * @param level Das ServerLevel
     * @return Der gefundene CustomNPCEntity oder null
     */
    public static CustomNPCEntity getNPCByUUID(UUID uuid, ServerLevel level) {
        if (uuid == null || level == null) {
            return null;
        }

        String worldKey = getWorldKey(level);
        Map<UUID, CustomNPCEntity> worldNPCs = npcsByWorld.get(worldKey);

        if (worldNPCs != null) {
            return worldNPCs.get(uuid);
        }

        return null;
    }

    /**
     * Findet einen NPC anhand seiner UUID in ALLEN geladenen Welten
     * Performance: O(w) wobei w = Anzahl Welten (typisch 3: Overworld, Nether, End)
     *
     * WICHTIG: Verwende getNPCByUUID(uuid, level) wenn du die Welt kennst!
     *
     * @param uuid Die UUID des NPCs
     * @return Der gefundene CustomNPCEntity oder null
     */
    public static CustomNPCEntity getNPCByUUID(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        // Durchsuche alle Welten
        for (Map<UUID, CustomNPCEntity> worldNPCs : npcsByWorld.values()) {
            CustomNPCEntity npc = worldNPCs.get(uuid);
            if (npc != null) {
                return npc;
            }
        }

        return null;
    }

    /**
     * Gibt alle registrierten NPCs in einem ServerLevel zurück
     *
     * @param level Das ServerLevel
     * @return Unmodifiable Collection aller NPCs
     */
    public static Collection<CustomNPCEntity> getAllNPCs(ServerLevel level) {
        if (level == null) {
            return Collections.emptyList();
        }

        String worldKey = getWorldKey(level);
        Map<UUID, CustomNPCEntity> worldNPCs = npcsByWorld.get(worldKey);

        if (worldNPCs != null) {
            return Collections.unmodifiableCollection(worldNPCs.values());
        }

        return Collections.emptyList();
    }

    /**
     * Gibt alle registrierten NPCs in ALLEN Welten zurück
     *
     * @return Unmodifiable Collection aller NPCs
     */
    public static Collection<CustomNPCEntity> getAllNPCs() {
        List<CustomNPCEntity> allNPCs = new ArrayList<>();

        for (Map<UUID, CustomNPCEntity> worldNPCs : npcsByWorld.values()) {
            allNPCs.addAll(worldNPCs.values());
        }

        return Collections.unmodifiableCollection(allNPCs);
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTIKEN & UTILITIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt die Anzahl registrierter NPCs in einem ServerLevel zurück
     *
     * @param level Das ServerLevel
     * @return Anzahl NPCs
     */
    public static int getNPCCount(ServerLevel level) {
        if (level == null) {
            return 0;
        }

        String worldKey = getWorldKey(level);
        Map<UUID, CustomNPCEntity> worldNPCs = npcsByWorld.get(worldKey);

        return worldNPCs != null ? worldNPCs.size() : 0;
    }

    /**
     * Gibt die Gesamtanzahl registrierter NPCs in ALLEN Welten zurück
     *
     * @return Gesamtanzahl NPCs
     */
    public static int getTotalNPCCount() {
        int total = 0;
        for (Map<UUID, CustomNPCEntity> worldNPCs : npcsByWorld.values()) {
            total += worldNPCs.size();
        }
        return total;
    }

    /**
     * Löscht alle registrierten NPCs (für Tests/Debug)
     */
    public static void clear() {
        int totalCleared = getTotalNPCCount();
        npcsByWorld.clear();
        LOGGER.warn("[NPCEntityRegistry] Cleared all registrations ({} NPCs)", totalCleared);
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt einen eindeutigen Key für eine Welt
     *
     * @param level Das Level
     * @return World Key (z.B. "minecraft:overworld")
     */
    private static String getWorldKey(net.minecraft.world.level.Level level) {
        return level.dimension().location().toString();
    }

    /**
     * Gibt Debug-Informationen über das Registry aus
     */
    public static void printDebugInfo() {
        LOGGER.info("=== NPCEntityRegistry Debug Info ===");
        LOGGER.info("Total NPCs: {}", getTotalNPCCount());

        for (Map.Entry<String, Map<UUID, CustomNPCEntity>> entry : npcsByWorld.entrySet()) {
            LOGGER.info("World '{}': {} NPCs", entry.getKey(), entry.getValue().size());

            if (LOGGER.isDebugEnabled()) {
                for (CustomNPCEntity npc : entry.getValue().values()) {
                    LOGGER.debug("  - {} (UUID: {})", npc.getNpcName(), npc.getUUID());
                }
            }
        }
    }
}
