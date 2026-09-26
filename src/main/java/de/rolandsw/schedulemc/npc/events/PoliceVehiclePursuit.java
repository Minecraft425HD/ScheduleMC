package de.rolandsw.schedulemc.npc.events;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feature 1: Polizei-Fahrzeugverfolgung (Teil 19: echtes Fahrzeug statt Illusion)
 *
 * Polizei-NPCs erhalten ein echtes {@code EntityGenericVehicle} (siehe
 * {@code PoliceVehicleAI}), sobald ein Spieler tatsächlich per Fahrzeug flieht.
 * - Aktiviert wenn Spieler in Fahrzeug flieht (siehe {@code PoliceAIHandler})
 * - Steuerung per gezieltem PIT-Manöver statt direkter Zielverfolgung
 * - Fahrzeug ist per Schuss/Kollision zerstörbar wie ein Spielerfahrzeug
 * - Beendet Verfolgung wenn Spieler aussteigt, gefangen wird oder das Fahrzeug zerstört wird
 */
public class PoliceVehiclePursuit {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Aktive Fahrzeugverfolgungen: Polizei-UUID -> Ziel-Spieler-UUID */
    private static final Map<UUID, UUID> activeVehiclePursuits = new ConcurrentHashMap<>();

    /** Letzte bekannte Spieler-Position fuer Pfad-Update */
    private static final Map<UUID, BlockPos> lastKnownTargetPos = new ConcurrentHashMap<>();

    /** Mindestabstand fuer Fahrzeugverfolgung (Bloecke) */
    private static final double MIN_PURSUIT_DISTANCE = 30.0;

    /** Maximaler Abstand bevor Verfolgung abgebrochen wird */
    private static final double MAX_PURSUIT_DISTANCE = 200.0;

    /** Intervall fuer Sirenensound/Cleanup-Pruefungen (Ticks) - die Fahrzeugsteuerung
     *  selbst (PoliceVehicleAI.tick) laeuft jeden Tick, da sie echte Fahrphysik steuert. */
    private static final int PATH_UPDATE_INTERVAL = 60; // 3 Sekunden

    /** Tick-Counter fuer die 3-Sekunden-Pruefungen */
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

        // Feature: echtes Polizei-Fahrzeug statt reiner Fahr-Illusion (siehe PoliceVehicleAI) -
        // erscheint bewusst erst hier, also nur wenn der Spieler tatsächlich per Fahrzeug flieht.
        boolean started = PoliceVehicleAI.spawnAndMount(police);

        if (started) {
            activeVehiclePursuits.put(policeUUID, targetUUID);
            lastKnownTargetPos.put(policeUUID, target.blockPosition());

            // Sirene aktivieren
            if (ModConfigHandler.COMMON.POLICE_SIREN_ENABLED.get()) {
                police.setSirenActive(true);
            }

            LOGGER.info("[VEHICLE PURSUIT] {} startet Verfolgung von {}",
                police.getNpcName(), target.getName().getString());
            return true;
        }

        return false;
    }

    /**
     * Stoppt eine Fahrzeugverfolgung
     */
    public static void stopVehiclePursuit(CustomNPCEntity police) {
        UUID policeUUID = police.getUUID();
        if (activeVehiclePursuits.remove(policeUUID) != null) {
            lastKnownTargetPos.remove(policeUUID);
            police.setSirenActive(false);

            // Nur despawnen wenn das Fahrzeug noch existiert (bei Zerstörung hat
            // PoliceVehicleAI.tick() bereits selbst despawn(asWreck=true) aufgerufen und
            // stopVehiclePursuit ausgelöst - ein erneuter despawn(false) hier wäre dann ein
            // no-op, da PoliceVehicleAI.policeToVehicle den Eintrag bereits entfernt hat).
            PoliceVehicleAI.despawn(police, false);

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
     * Wird jeden Server-Tick aufgerufen - steuert das echte Verfolgungsfahrzeug jeden Tick
     * (fuer fluessige Fahrphysik) und aktualisiert Sirene/Abbruch-Bedingungen alle 3 Sekunden.
     */
    public static void tick(net.minecraft.server.MinecraftServer server) {
        if (!activeVehiclePursuits.isEmpty()) {
            for (Map.Entry<UUID, UUID> entry : activeVehiclePursuits.entrySet()) {
                UUID policeUUID = entry.getKey();
                UUID targetUUID = entry.getValue();

                ServerPlayer target = server.getPlayerList().getPlayer(targetUUID);
                if (target == null) {
                    continue;
                }
                CustomNPCEntity police = PoliceAIHandler.findPoliceByUUID(policeUUID);
                if (police != null) {
                    PoliceVehicleAI.tick(police, target);
                }
            }
        }

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
                CustomNPCEntity police = PoliceAIHandler.findPoliceByUUID(policeUUID);
                if (police != null) {
                    PoliceVehicleAI.despawn(police, false);
                }
                continue;
            }

            // Prüfe ob Spieler noch Wanted ist
            if (CrimeManager.getWantedLevel(targetUUID) <= 0) {
                it.remove();
                lastKnownTargetPos.remove(policeUUID);
                CustomNPCEntity police = PoliceAIHandler.findPoliceByUUID(policeUUID);
                if (police != null) {
                    police.setSirenActive(false);
                    PoliceVehicleAI.despawn(police, false);
                }
                continue;
            }

            // Nicht mehr in Fahrzeugverfolgung (z. B. Fahrzeug wurde bereits zerstört und
            // PoliceVehicleAI hat den Polizisten abgezogen) - dann existiert der NPC evtl.
            // gar nicht mehr, einfach überspringen.
            CustomNPCEntity sirenPolice = PoliceAIHandler.findPoliceByUUID(policeUUID);
            if (sirenPolice == null) {
                continue;
            }

            // Feature 5: Periodischer Sirenensound (alle 3 Sekunden, gleicher Takt wie
            // dieser Pfad-Update-Zyklus) - unabhaengig davon ob sich der Spieler bewegt hat.
            // Nur bei aktiver Fahrzeugverfolgung (nicht mehr relevant fuer Fuss-Sirenenlicht).
            if (ModConfigHandler.COMMON.POLICE_SIREN_ENABLED.get()) {
                playSirenSound(sirenPolice);
            }

            lastKnownTargetPos.put(policeUUID, target.blockPosition());
        }
    }

    /**
     * Feature 5: Sirenensound während der Fahrzeugverfolgung. Nutzt bewusst den
     * Vanilla-Sound {@code RAID_HORN} zweckentfremdet statt eines eigenen Sound-Assets
     * (keine Audiodatei im Repo vorhanden, Nutzerentscheidung). Lautstärke wird aus dem
     * konfigurierten Hörradius abgeleitet (Vanilla-Lautstärke 1.0 ≈ 16 Blöcke Hörweite).
     */
    private static void playSirenSound(CustomNPCEntity police) {
        if (!(police.level() instanceof net.minecraft.server.level.ServerLevel level)) return;

        int radius = ModConfigHandler.COMMON.POLICE_SIREN_SOUND_RADIUS.get();
        float volume = Math.max(1.0f, radius / 16.0f);

        level.playSound(null, police.getX(), police.getY(), police.getZ(),
            net.minecraft.sounds.SoundEvents.RAID_HORN.value(), net.minecraft.sounds.SoundSource.NEUTRAL,
            volume, 1.0f);
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
        // Polizei muss POLICE-Typ sein
        if (police.getNpcType() != NPCType.POLICE) return false;

        // Polizei darf nicht bereits fahren
        if (police.isDriving() || police.isPassenger()) return false;

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
        PoliceVehicleAI.cleanup(policeUUID);
    }
}
