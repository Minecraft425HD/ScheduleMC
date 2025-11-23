package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
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

        System.out.println("[BACKUP] Polizei " + policeUUID + " verfolgt Spieler " + playerUUID +
            " (Raid: " + isRaid + ", Gesamt: " + activePolice.get(playerUUID).size() + ")");
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
            System.out.println("[BACKUP] Polizei " + policeUUID + " gibt Verfolgung auf von Spieler " + playerUUID);
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
            System.out.println("[BACKUP] Keine Verstärkung möglich - Maximum erreicht");
            return;
        }

        boolean isRaid = isRaidPursuit.getOrDefault(player.getUUID(), false);
        int maxPolice = isRaid ? 4 : 2;
        int currentCount = getActivePoliceCount(player.getUUID());
        int needed = maxPolice - currentCount;

        System.out.println("[BACKUP] Rufe " + needed + " Verstärkung(en) für Spieler " +
            player.getName().getString() + " (Raid: " + isRaid + ")");

        // Suche Polizisten in 100-Block-Radius
        AABB searchArea = new AABB(
            caller.getX() - 100, caller.getY() - 20, caller.getZ() - 100,
            caller.getX() + 100, caller.getY() + 20, caller.getZ() + 100
        );

        List<CustomNPCEntity> nearbyPolice = caller.level().getEntitiesOfClass(
            CustomNPCEntity.class,
            searchArea,
            npc -> npc.isPolice() && !npc.equals(caller) && !isPoliceAssigned(npc.getUUID())
        );

        if (nearbyPolice.isEmpty()) {
            System.out.println("[BACKUP] Keine verfügbaren Polizisten in der Nähe gefunden");
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
            System.out.println("[BACKUP] Polizist " + police.getNpcName() +
                " als Verstärkung zugewiesen (Entfernung: " +
                String.format("%.1f", Math.sqrt(police.distanceToSqr(caller.getX(), caller.getY(), caller.getZ()))) + " Blöcke)");

            assigned++;
        }

        if (assigned > 0) {
            System.out.println("[BACKUP] " + assigned + " Verstärkung(en) erfolgreich zugewiesen");
        } else {
            System.out.println("[BACKUP] Keine Verstärkung zugewiesen");
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
            System.out.println("[BACKUP] Verfolgung zu Raid hochgestuft - Max 4 Polizisten erlaubt");
        }
    }

    /**
     * Debug-Info
     */
    public static void printStatus() {
        System.out.println("[BACKUP] === Polizei-Status ===");
        for (Map.Entry<UUID, Set<UUID>> entry : activePolice.entrySet()) {
            boolean isRaid = isRaidPursuit.getOrDefault(entry.getKey(), false);
            System.out.println("[BACKUP] Spieler " + entry.getKey() + ": " +
                entry.getValue().size() + " Polizisten (Raid: " + isRaid + ")");
        }
    }
}
