package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Polizei-Verstärkungen
 *
 * Limits:
 * - Max. 2 Polizisten bei normaler Verfolgung
 * - Max. 4 Polizisten wenn illegale Items gefunden wurden
 * SICHERHEIT: Thread-safe Maps für concurrent access
 */
public class PoliceBackupSystem {

    private static final Logger LOGGER = LogUtils.getLogger();

    // SICHERHEIT: ConcurrentHashMap für Thread-safe Zugriff
    // Player UUID -> Set of Police UUIDs currently pursuing
    private static final Map<UUID, Set<UUID>> activePolice = new ConcurrentHashMap<>();

    // Player UUID -> Is Raid (true = 4 max, false = 2 max)
    private static final Map<UUID, Boolean> isRaidPursuit = new ConcurrentHashMap<>();

    /**
     * Registriert eine Polizei als aktiv verfolgend
     */
    public static void registerPolice(UUID playerUUID, UUID policeUUID, boolean isRaid) {
        // SICHERHEIT: ConcurrentHashMap.newKeySet() für Thread-safe Set
        activePolice.computeIfAbsent(playerUUID, k -> ConcurrentHashMap.newKeySet()).add(policeUUID);
        isRaidPursuit.put(playerUUID, isRaid);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[BACKUP] Police {} pursues player {} (Raid: {}, Total: {})",
                policeUUID, playerUUID, isRaid, activePolice.get(playerUUID).size());
        }
    }

    /**
     * Entfernt eine Polizei von der Verfolgung
     */
    public static void unregisterPolice(UUID playerUUID, UUID policeUUID) {
        Set<UUID> police = activePolice.get(playerUUID);
        if (police != null) {
            police.remove(policeUUID);
            if (police.isEmpty()) {
                activePolice.remove(playerUUID);
                isRaidPursuit.remove(playerUUID);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[BACKUP] Police {} stops pursuit of player {}", policeUUID, playerUUID);
            }
        }
    }

    /**
     * Gibt die Anzahl der aktiven Polizisten für einen Spieler zurück
     */
    public static int getActivePoliceCount(UUID playerUUID) {
        Set<UUID> police = activePolice.get(playerUUID);
        return police != null ? police.size() : 0;
    }

    /**
     * Prüft ob weitere Polizisten hinzugezogen werden können
     */
    public static boolean canCallBackup(UUID playerUUID) {
        int currentCount = getActivePoliceCount(playerUUID);
        boolean isRaid = isRaidPursuit.getOrDefault(playerUUID, false);

        int maxPolice = isRaid ? 4 : 2;

        return currentCount < maxPolice;
    }

    /**
     * Ruft Verstärkung herbei
     * Findet verfügbare Polizisten in der Nähe und weist sie der Verfolgung zu
     */
    public static void callBackup(ServerPlayer player, CustomNPCEntity caller) {
        if (!canCallBackup(player.getUUID())) {
            LOGGER.info("[BACKUP] No backup possible - maximum reached");
            return;
        }

        boolean isRaid = isRaidPursuit.getOrDefault(player.getUUID(), false);
        int maxPolice = isRaid ? 4 : 2;
        int currentCount = getActivePoliceCount(player.getUUID());
        int needed = maxPolice - currentCount;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[BACKUP] Calling {} backup(s) for player {} (Raid: {})",
                needed, player.getName().getString(), isRaid);
        }

        // Suche Polizisten in 100-Block-Radius
        AABB searchArea = new AABB(
            caller.getX() - 100, caller.getY() - 20, caller.getZ() - 100,
            caller.getX() + 100, caller.getY() + 20, caller.getZ() + 100
        );

        List<CustomNPCEntity> nearbyPolice = caller.level().getEntitiesOfClass(
            CustomNPCEntity.class,
            searchArea,
            npc -> npc.getNpcType() == de.rolandsw.schedulemc.npc.data.NPCType.POLIZEI &&
                   !npc.equals(caller) &&
                   !isPoliceAssigned(npc.getUUID())
        );

        if (nearbyPolice.isEmpty()) {
            LOGGER.debug("[BACKUP] No available police found nearby");
            return;
        }

        // Sortiere nach Entfernung
        nearbyPolice.sort(Comparator.comparingDouble(npc ->
            npc.distanceToSqr(caller.getX(), caller.getY(), caller.getZ())
        ));

        // Weise die nächsten verfügbaren Polizisten zu
        int assigned = 0;
        for (CustomNPCEntity police : nearbyPolice) {
            if (assigned >= needed) {
                break;
            }

            // Registriere Polizist
            registerPolice(player.getUUID(), police.getUUID(), isRaid);

            // Setze Ziel für Verstärkung (wird in PoliceAIHandler übernommen)
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[BACKUP] Police {} assigned as backup (Distance: {:.1f} blocks)",
                    police.getNpcName(), Math.sqrt(police.distanceToSqr(caller.getX(), caller.getY(), caller.getZ())));
            }

            assigned++;
        }

        if (assigned > 0) {
            LOGGER.debug("[BACKUP] {} backup(s) successfully assigned", assigned);
        } else {
            LOGGER.debug("[BACKUP] No backup assigned");
        }
    }

    /**
     * Prüft ob eine Polizei bereits einer Verfolgung zugewiesen ist
     */
    private static boolean isPoliceAssigned(UUID policeUUID) {
        for (Set<UUID> policeSet : activePolice.values()) {
            if (policeSet.contains(policeUUID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt die Spieler-UUID zurück, die eine Polizei verfolgt (falls vorhanden)
     */
    @Nullable
    public static UUID getAssignedTarget(UUID policeUUID) {
        for (Map.Entry<UUID, Set<UUID>> entry : activePolice.entrySet()) {
            if (entry.getValue().contains(policeUUID)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Cleanup wenn Spieler den Server verlässt
     */
    public static void cleanup(UUID playerUUID) {
        if (playerUUID == null) {
            return;
        }

        activePolice.remove(playerUUID);
        isRaidPursuit.remove(playerUUID);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[BACKUP] Cleaned up backup data for player {}", playerUUID);
        }
    }

    /**
     * Cleanup wenn Polizei-NPC entfernt wird
     * Entfernt die Polizei aus allen aktiven Verfolgungen
     */
    public static void cleanupNPC(UUID policeUUID) {
        if (policeUUID == null) {
            return;
        }

        int removedFrom = 0;

        // Entferne NPC aus allen Verfolgungen
        for (Set<UUID> policeSet : activePolice.values()) {
            if (policeSet.remove(policeUUID)) {
                removedFrom++;
            }
        }

        // Entferne leere Sets
        activePolice.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        if (removedFrom > 0 && LOGGER.isDebugEnabled()) {
            LOGGER.debug("[BACKUP] Cleaned up NPC {} from {} pursuits", policeUUID, removedFrom);
        }
    }

    /**
     * Setzt eine Verfolgung als Raid (erhöht Limit auf 4 Polizisten)
     */
    public static void upgradeToRaid(UUID playerUUID) {
        if (activePolice.containsKey(playerUUID)) {
            isRaidPursuit.put(playerUUID, true);
            LOGGER.info("[BACKUP] Pursuit upgraded to raid - Max 4 police allowed");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Feature 4: Strategische Koordination (Flanking)
    // ═══════════════════════════════════════════════════════════

    /** Zugewiesene Flanking-Positionen: Polizei-UUID -> Zielposition */
    private static final Map<UUID, Vec3> assignedFlankPositions = new ConcurrentHashMap<>();

    /** Flanking-Abstand */
    private static final double FLANK_DISTANCE = 15.0;

    /**
     * Berechnet und weist Flanking-Positionen zu
     * Polizisten werden auf gegenueberliegende Seiten des Spielers verteilt.
     *
     * @param playerUUID UUID des verfolgten Spielers
     * @param playerPos Aktuelle Spielerposition
     * @param playerDir Bewegungsrichtung des Spielers
     */
    public static void assignFlankingPositions(UUID playerUUID, Vec3 playerPos, Vec3 playerDir) {
        Set<UUID> police = activePolice.get(playerUUID);
        if (police == null || police.size() < 2) return;

        // Normalisiere Spielerrichtung
        Vec3 dir = playerDir.normalize();
        if (dir.lengthSqr() < 0.01) {
            dir = new Vec3(1, 0, 0); // Fallback
        }

        // Berechne seitliche Richtungen
        Vec3 right = new Vec3(-dir.z, 0, dir.x).normalize();
        Vec3 left = right.scale(-1);
        Vec3 behind = dir.scale(-1);

        // Positionen: Links, Rechts, Hinten-Links, Hinten-Rechts
        Vec3[] flankPositions = new Vec3[]{
            playerPos.add(right.scale(FLANK_DISTANCE)),                                    // Rechts
            playerPos.add(left.scale(FLANK_DISTANCE)),                                     // Links
            playerPos.add(behind.scale(FLANK_DISTANCE)).add(right.scale(FLANK_DISTANCE * 0.5)), // Hinten-Rechts
            playerPos.add(behind.scale(FLANK_DISTANCE)).add(left.scale(FLANK_DISTANCE * 0.5))   // Hinten-Links
        };

        int i = 0;
        for (UUID policeUUID : police) {
            if (i < flankPositions.length) {
                assignedFlankPositions.put(policeUUID, flankPositions[i]);
                i++;
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[FLANKING] Assigned {} flanking positions for player {}", i, playerUUID);
        }
    }

    /**
     * Gibt die Flanking-Position fuer einen Polizisten zurueck
     *
     * @return Flanking-Position oder null wenn keine zugewiesen
     */
    public static Vec3 getFlankPosition(UUID policeUUID) {
        return assignedFlankPositions.get(policeUUID);
    }

    /**
     * Prueft ob ein Polizist eine Flanking-Position hat
     */
    public static boolean hasFlankPosition(UUID policeUUID) {
        return assignedFlankPositions.containsKey(policeUUID);
    }

    /**
     * Entfernt Flanking-Positionen fuer einen Spieler
     */
    public static void clearFlankPositions(UUID playerUUID) {
        Set<UUID> police = activePolice.get(playerUUID);
        if (police != null) {
            for (UUID policeUUID : police) {
                assignedFlankPositions.remove(policeUUID);
            }
        }
    }

    /**
     * Debug-Info
     */
    public static void printStatus() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[BACKUP] === Polizei-Status ===");
            for (Map.Entry<UUID, Set<UUID>> entry : activePolice.entrySet()) {
                boolean isRaid = isRaidPursuit.getOrDefault(entry.getKey(), false);
                LOGGER.debug("[BACKUP] Player {}: {} police (Raid: {})",
                    entry.getKey(), entry.getValue().size(), isRaid);
            }
        }
    }
}
