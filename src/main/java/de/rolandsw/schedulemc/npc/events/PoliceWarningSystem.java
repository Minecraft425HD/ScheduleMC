package de.rolandsw.schedulemc.npc.events;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.witness.CrimeType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feature 8: Verwarnungssystem (vor Verhaftung)
 *
 * Bei Wanted Level 1-2 gibt die Polizei erst eine Verwarnung,
 * bevor eine Verfolgung/Festnahme beginnt.
 *
 * Ablauf:
 * 1. Polizei sieht Spieler mit Wanted 1-2
 * 2. Verwarnung wird ausgesprochen (Chat + Sound)
 * 3. 10 Sekunden Frist
 * 4a. Spieler bleibt stehen -> Nur Geldstrafe, kein Gefaengnis
 * 4b. Spieler flieht -> Normale Verfolgung + extra Stern
 *
 * Ab Wanted 3+: Sofortige Verfolgung (wie bisher)
 */
public class PoliceWarningSystem {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Verwarnungs-Timeout in Ticks (10 Sekunden) */
    public static final int WARNING_TIMEOUT_TICKS = 200;

    /** Maximale Bewegung waehrend Verwarnung (Bloecke) */
    private static final double MAX_MOVEMENT_DURING_WARNING = 3.0;

    /** Status einer Verwarnung */
    public enum WarningStatus {
        NONE,
        WARNING_ISSUED,     // Verwarnung ausgesprochen
        COMPLIANCE_TIMER,   // Spieler steht still, Timer laeuft
        FLED,               // Spieler ist geflohen
        COMPLIED            // Spieler hat sich ergeben
    }

    /** Aktive Verwarnungen: Spieler-UUID -> WarningData */
    private static final Map<UUID, WarningData> activeWarnings = new ConcurrentHashMap<>();

    /**
     * Daten einer aktiven Verwarnung
     */
    private static class WarningData {
        final UUID policeUUID;
        final long issuedTick;
        final double startX;
        final double startY;
        final double startZ;
        WarningStatus status;

        WarningData(UUID policeUUID, long issuedTick, double x, double y, double z) {
            this.policeUUID = policeUUID;
            this.issuedTick = issuedTick;
            this.startX = x;
            this.startY = y;
            this.startZ = z;
            this.status = WarningStatus.WARNING_ISSUED;
        }
    }

    /**
     * Prueft ob Verwarnung statt Verfolgung angemessen ist
     *
     * @param wantedLevel Aktuelles Wanted-Level des Spielers
     * @return true wenn Verwarnung angemessen (Level 1-2)
     */
    public static boolean shouldWarnInsteadOfPursue(int wantedLevel) {
        return wantedLevel >= 1 && wantedLevel <= 2;
    }

    /**
     * Spricht eine Verwarnung aus
     *
     * @param police Der Polizei-NPC
     * @param player Der Spieler
     * @param currentTick Aktueller Game-Tick
     * @return true wenn Verwarnung ausgesprochen wurde
     */
    public static boolean issueWarning(CustomNPCEntity police, ServerPlayer player, long currentTick) {
        UUID playerUUID = player.getUUID();

        // Bereits verwarnt?
        if (activeWarnings.containsKey(playerUUID)) {
            return false;
        }

        WarningData data = new WarningData(
            police.getUUID(),
            currentTick,
            player.getX(), player.getY(), player.getZ()
        );
        activeWarnings.put(playerUUID, data);

        // Warnungen senden
        player.sendSystemMessage(Component.translatable("event.police.warning_stop"));
        player.sendSystemMessage(Component.translatable("event.police.warning_timer",
            WARNING_TIMEOUT_TICKS / 20));
        player.sendSystemMessage(Component.translatable("event.police.warning_comply"));

        // Polizei bleibt stehen
        police.getNavigation().stop();

        LOGGER.info("[WARNING] {} verwarnt {} - 10 Sekunden Frist",
            police.getNpcName(), player.getName().getString());
        return true;
    }

    /**
     * Prueft den Status einer Verwarnung
     *
     * @param player Der Spieler
     * @param currentTick Aktueller Game-Tick
     * @return Aktueller Status
     */
    public static WarningStatus checkWarningStatus(ServerPlayer player, long currentTick) {
        UUID playerUUID = player.getUUID();
        WarningData data = activeWarnings.get(playerUUID);

        if (data == null) {
            return WarningStatus.NONE;
        }

        long elapsed = currentTick - data.issuedTick;

        // Pruefe ob Spieler sich bewegt hat
        double movedDistance = Math.sqrt(
            Math.pow(player.getX() - data.startX, 2) +
            Math.pow(player.getY() - data.startY, 2) +
            Math.pow(player.getZ() - data.startZ, 2)
        );

        if (movedDistance > MAX_MOVEMENT_DURING_WARNING) {
            // Spieler ist geflohen!
            data.status = WarningStatus.FLED;
            activeWarnings.remove(playerUUID);

            player.sendSystemMessage(Component.translatable("event.police.warning_fled"));

            // Extra Stern fuer Flucht
            long currentDay = player.level().getDayTime() / 24000;
            CrimeManager.addWantedLevel(playerUUID, 1, currentDay,
                CrimeType.EVADING_POLICE, player.blockPosition());

            LOGGER.info("[WARNING] {} ist geflohen - extra Stern", player.getName().getString());
            return WarningStatus.FLED;
        }

        if (elapsed >= WARNING_TIMEOUT_TICKS) {
            // Timer abgelaufen, Spieler hat sich ergeben
            data.status = WarningStatus.COMPLIED;
            activeWarnings.remove(playerUUID);

            // Nur Geldstrafe, kein Gefaengnis
            int wantedLevel = CrimeManager.getWantedLevel(playerUUID);
            double fine = wantedLevel * 500.0;

            player.sendSystemMessage(Component.translatable("event.police.warning_complied"));
            player.sendSystemMessage(Component.translatable("event.police.warning_fine_only",
                String.format("%.2f", fine)));

            // Wanted-Level reduzieren (nicht komplett loeschen)
            CrimeManager.setWantedLevel(playerUUID, Math.max(0, wantedLevel - 1));

            LOGGER.info("[WARNING] {} hat sich ergeben - nur Geldstrafe ({}€)",
                player.getName().getString(), fine);
            return WarningStatus.COMPLIED;
        }

        // Timer laeuft noch
        if (elapsed % 40 == 0) { // Alle 2 Sekunden
            int remaining = (int) ((WARNING_TIMEOUT_TICKS - elapsed) / 20);
            player.sendSystemMessage(Component.translatable("event.police.warning_remaining", remaining));
        }

        return WarningStatus.WARNING_ISSUED;
    }

    /**
     * Prueft ob ein Spieler aktuell verwarnt wird
     */
    public static boolean isWarned(UUID playerUUID) {
        return activeWarnings.containsKey(playerUUID);
    }

    /**
     * Entfernt Verwarnung (z.B. bei Festnahme oder Wanted-Clear)
     */
    public static void clearWarning(UUID playerUUID) {
        activeWarnings.remove(playerUUID);
    }

    /**
     * Bereinigt abgelaufene Verwarnungen
     */
    public static void cleanup(long currentTick) {
        activeWarnings.entrySet().removeIf(entry -> {
            long elapsed = currentTick - entry.getValue().issuedTick;
            return elapsed > WARNING_TIMEOUT_TICKS * 2; // Safety-Cleanup
        });
    }
}
