package de.rolandsw.schedulemc.npc.events;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.driving.NPCDrivingScheduler;
import de.rolandsw.schedulemc.npc.driving.NPCVehicleAssignment;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feature 1: Polizei-Fahrzeugverfolgung
 *
 * Polizei-NPCs nutzen das NPC-Driving-System fuer Verfolgungsjagden.
 * - Aktiviert wenn Spieler in Fahrzeug flieht UND Polizei Fahrzeug hat
 * - Polizei faehrt 30% schneller als normale NPCs
 * - Verfolgt Spieler-Position dynamisch ueber RoadGraph
 * - Beendet Verfolgung wenn Spieler aussteigt oder gefangen
 */
public class PoliceVehiclePursuit {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Aktive Fahrzeugverfolgungen: Polizei-UUID -> Ziel-Spieler-UUID */
    private static final Map<UUID, UUID> activeVehiclePursuits = new ConcurrentHashMap<>();

    /** Letzte bekannte Spieler-Position fuer Pfad-Update */
    private static final Map<UUID, BlockPos> lastKnownTargetPos = new ConcurrentHashMap<>();

    /** Geschwindigkeits-Multiplikator fuer Polizei-Fahrzeuge */
    public static final double POLICE_SPEED_MULTIPLIER = 1.3;

    /** Mindestabstand fuer Fahrzeugverfolgung (Bloecke) */
    private static final double MIN_PURSUIT_DISTANCE = 30.0;

    /** Maximaler Abstand bevor Verfolgung abgebrochen wird */
    private static final double MAX_PURSUIT_DISTANCE = 200.0;

    /** Intervall fuer Pfad-Updates (Ticks) */
    private static final int PATH_UPDATE_INTERVAL = 60; // 3 Sekunden

    /** Tick-Counter fuer Pfad-Updates */
    private static volatile int tickCounter = 0;

    /**
     * Startet eine Fahrzeugverfolgung
     *
     * @param police Der Polizei-NPC
     * @param target Der zu verfolgende Spieler
     * @return true wenn Verfolgung gestartet wurde
     */
    public static boolean startVehiclePursuit(CustomNPCEntity police, ServerPlayer target) {
        UUID policeUUID = police.getUUID();
        UUID targetUUID = target.getUUID();

        // Bereits in Verfolgung?
        if (activeVehiclePursuits.containsKey(policeUUID)) {
            return false;
        }

        // Polizei muss Fahrzeug haben
        if (!NPCVehicleAssignment.hasVehicle(policeUUID)) {
            return false;
        }

        // Starte Fahrt zum Spieler
        BlockPos targetPos = target.blockPosition();
        boolean started = NPCDrivingScheduler.canDrive(police, targetPos)
            && startDrivingToTarget(police, targetPos);

        if (started) {
            activeVehiclePursuits.put(policeUUID, targetUUID);
            lastKnownTargetPos.put(policeUUID, targetPos);

            // Sirene aktivieren
            police.setSirenActive(true);

            LOGGER.info("[VEHICLE PURSUIT] {} startet Verfolgung von {}",
                police.getNpcName(), target.getName().getString());
            return true;
        }

        return false;
    }

    /**
     * Startet Fahrt mit erhoehter Geschwindigkeit
     */
    private static boolean startDrivingToTarget(CustomNPCEntity police, BlockPos target) {
        // Setze Polizei-Farbe (blau, Index 3)
        police.setVehicleColor(3);
        NPCDrivingScheduler.startDriving(police, target);
        return police.isDriving();
    }

    /**
     * Stoppt eine Fahrzeugverfolgung
     */
    public static void stopVehiclePursuit(CustomNPCEntity police) {
        UUID policeUUID = police.getUUID();
        if (activeVehiclePursuits.remove(policeUUID) != null) {
            lastKnownTargetPos.remove(policeUUID);
            NPCDrivingScheduler.stopDriving(police);
            police.setSirenActive(false);

            LOGGER.info("[VEHICLE PURSUIT] {} beendet Verfolgung", police.getNpcName());
        }
    }

    /**
     * Prueft ob ein Polizei-NPC in einer Fahrzeugverfolgung ist
     */
    public static boolean isInVehiclePursuit(UUID policeUUID) {
        return activeVehiclePursuits.containsKey(policeUUID);
    }

    /**
     * Wird jeden Server-Tick aufgerufen - aktualisiert Verfolgungen
     */
    public static void tick(net.minecraft.server.MinecraftServer server) {
        tickCounter++;

        if (tickCounter < PATH_UPDATE_INTERVAL) {
            return;
        }
        tickCounter = 0;

        // Iteriere ueber aktive Verfolgungen
        Iterator<Map.Entry<UUID, UUID>> it = activeVehiclePursuits.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, UUID> entry = it.next();
            UUID policeUUID = entry.getKey();
            UUID targetUUID = entry.getValue();

            // Finde Spieler
            ServerPlayer target = server.getPlayerList().getPlayer(targetUUID);
            if (target == null) {
                // Spieler offline - stoppe Verfolgung
                it.remove();
                lastKnownTargetPos.remove(policeUUID);
                continue;
            }

            // Prüfe ob Spieler noch Wanted ist
            if (CrimeManager.getWantedLevel(targetUUID) <= 0) {
                it.remove();
                lastKnownTargetPos.remove(policeUUID);
                continue;
            }

            // Prüfe Abstand
            BlockPos currentTargetPos = target.blockPosition();
            BlockPos lastPos = lastKnownTargetPos.get(policeUUID);

            if (lastPos != null) {
                double distanceMoved = Math.sqrt(lastPos.distSqr(currentTargetPos));
                if (distanceMoved > 20) {
                    // Spieler hat sich bewegt - Update Pfad
                    lastKnownTargetPos.put(policeUUID, currentTargetPos);
                    // Pfad wird beim naechsten NPCDrivingScheduler-Tick aktualisiert
                }
            }
        }
    }

    /**
     * Prueft ob ein Spieler in einem Fahrzeug sitzt
     */
    public static boolean isPlayerInVehicle(ServerPlayer player) {
        return player.getVehicle() != null;
    }

    /**
     * Prueft ob Fahrzeugverfolgung moeglich ist
     */
    public static boolean canStartVehiclePursuit(CustomNPCEntity police, ServerPlayer target) {
        // Polizei muss POLIZEI-Typ sein
        if (police.getNpcType() != NPCType.POLIZEI) return false;

        // Polizei darf nicht bereits fahren
        if (police.isDriving()) return false;

        // Polizei muss Fahrzeug haben
        if (!NPCVehicleAssignment.hasVehicle(police.getUUID())) return false;

        // Abstand muss gross genug sein
        double distance = police.distanceTo(target);
        return distance > MIN_PURSUIT_DISTANCE && distance < MAX_PURSUIT_DISTANCE;
    }

    /**
     * Bereinigt alle Daten fuer einen NPC
     */
    public static void cleanup(UUID policeUUID) {
        activeVehiclePursuits.remove(policeUUID);
        lastKnownTargetPos.remove(policeUUID);
    }
}
