package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import java.util.*;

/**
 * Verwaltet Polizei-Verstärkungen
 *
 * Limits:
 * - Max. 2 Polizisten bei normaler Verfolgung
 * - Max. 4 Polizisten wenn illegale Items gefunden wurden
 */
public class PoliceBackupSystem {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Player UUID -> Set of Police UUIDs currently pursuing
    private static final Map<UUID, Set<UUID>> activePolice = new HashMap<>();

    // Player UUID -> Is Raid (true = 4 max, false = 2 max)
    private static final Map<UUID, Boolean> isRaidPursuit = new HashMap<>();

    /**
     * Registriert eine Polizei als aktiv verfolgend
     */
    public static void registerPolice(UUID playerUUID, UUID policeUUID, boolean isRaid) {
        activePolice.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(policeUUID);
        isRaidPursuit.put(playerUUID, isRaid);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[BACKUP] Polizei {} verfolgt Spieler {} (Raid: {}, Gesamt: {})",
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
                LOGGER.debug("[BACKUP] Polizei {} gibt Verfolgung auf von Spieler {}", policeUUID, playerUUID);
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
            LOGGER.info("[BACKUP] Keine Verstärkung möglich - Maximum erreicht");
            return;
        }

        boolean isRaid = isRaidPursuit.getOrDefault(player.getUUID(), false);
        int maxPolice = isRaid ? 4 : 2;
        int currentCount = getActivePoliceCount(player.getUUID());
        int needed = maxPolice - currentCount;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[BACKUP] Rufe {} Verstärkung(en) für Spieler {} (Raid: {})",
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
            LOGGER.debug("[BACKUP] Keine verfügbaren Polizisten in der Nähe gefunden");
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
                LOGGER.debug("[BACKUP] Polizist {} als Verstärkung zugewiesen (Entfernung: {:.1f} Blöcke)",
                    police.getNpcName(), Math.sqrt(police.distanceToSqr(caller.getX(), caller.getY(), caller.getZ())));
            }

            assigned++;
        }

        if (assigned > 0) {
            LOGGER.debug("[BACKUP] {} Verstärkung(en) erfolgreich zugewiesen", assigned);
        } else {
            LOGGER.debug("[BACKUP] Keine Verstärkung zugewiesen");
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
        activePolice.remove(playerUUID);
        isRaidPursuit.remove(playerUUID);
    }

    /**
     * Setzt eine Verfolgung als Raid (erhöht Limit auf 4 Polizisten)
     */
    public static void upgradeToRaid(UUID playerUUID) {
        if (activePolice.containsKey(playerUUID)) {
            isRaidPursuit.put(playerUUID, true);
            LOGGER.info("[BACKUP] Verfolgung zu Raid hochgestuft - Max 4 Polizisten erlaubt");
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
                LOGGER.debug("[BACKUP] Spieler {}: {} Polizisten (Raid: {})",
                    entry.getKey(), entry.getValue().size(), isRaid);
            }
        }
    }
}
