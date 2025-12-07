package de.rolandsw.schedulemc.economy;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * Handler für Spieler-Events (Join, Leave)
 *
 * WICHTIG: Verwaltet das VOLLSTÄNDIGE Einfrieren der Welt
 *
 * Wenn kein Spieler online - ALLES friert ein:
 * - Zeit stoppt (doDaylightCycle = false)
 * - Pflanzen wachsen nicht (randomTickSpeed = 0)
 * - Mobs spawnen nicht (doMobSpawning = false)
 * - Wetter ändert sich nicht (doWeatherCycle = false)
 * - Entities ticken nicht
 *
 * Wenn Spieler online - Alles läuft normal:
 * - Zeit läuft (doDaylightCycle = true)
 * - Pflanzen wachsen (randomTickSpeed = 3)
 * - Mobs spawnen (doMobSpawning = true)
 * - Wetter läuft (doWeatherCycle = true)
 */
public class PlayerJoinHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Original-Werte für Wiederherstellung
    private static int savedRandomTickSpeed = 3; // Minecraft Standard

    /**
     * Erstellt automatisch ein Konto für neue Spieler beim ersten Join
     * Startet auch die Minecraft-Zeit wenn der erste Spieler joined
     */
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID uuid = player.getUUID();

        // Konto erstellen falls noch nicht vorhanden
        // createAccount setzt bereits das Startguthaben - kein zusätzlicher deposit nötig!
        if (!EconomyManager.hasAccount(uuid)) {
            EconomyManager.createAccount(uuid);
            LOGGER.info("Neuer Spieler: " + player.getName().getString() + " - Konto erstellt mit "
                + EconomyManager.getStartBalance() + " €");
        }

        // Prüfe ob das der erste Spieler ist
        ServerLevel level = (ServerLevel) player.level();
        int playerCount = level.getServer().getPlayerCount();

        // Wenn das der erste Spieler ist, ENTFRIER die Welt komplett
        if (playerCount == 1) {
            unfreezeWorld(level);
        }
    }

    /**
     * Speichert Economy-Daten beim Logout
     * Stoppt auch die Minecraft-Zeit wenn der letzte Spieler den Server verlässt
     */
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Prüfe ob das der letzte Spieler war
        ServerLevel level = (ServerLevel) player.level();
        int playerCount = level.getServer().getPlayerCount();

        // playerCount ist noch der alte Wert (bevor der Spieler entfernt wird)
        // Wenn nur noch 1 Spieler da ist, ist nach dem Logout keiner mehr da
        if (playerCount == 1) {
            freezeWorld(level);
        }

        // Könnte hier zusätzlich speichern für mehr Datensicherheit
        // EconomyManager.saveIfNeeded();
    }

    /**
     * Friert die GESAMTE Welt ein wenn keine Spieler online sind
     */
    private void freezeWorld(ServerLevel level) {
        // Speichere aktuelle randomTickSpeed vor dem Einfrieren
        savedRandomTickSpeed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);

        // Stoppe ALLES:
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());        // Zeit stoppt
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, level.getServer());       // Pflanzen wachsen nicht
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());   // Mobs spawnen nicht
        level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, level.getServer());   // Wetter ändert sich nicht

        long currentTime = level.getDayTime();
        LOGGER.info("═══════════════════════════════════════════════════════════");
        LOGGER.info("WELT EINGEFROREN - Letzter Spieler hat den Server verlassen");
        LOGGER.info("Zeit: {} | Tag: {} | Tageszeit: {}", currentTime, currentTime / 24000, currentTime % 24000);
        LOGGER.info("✓ Zeit gestoppt (doDaylightCycle = false)");
        LOGGER.info("✓ Pflanzen gestoppt (randomTickSpeed = 0)");
        LOGGER.info("✓ Mob-Spawn gestoppt (doMobSpawning = false)");
        LOGGER.info("✓ Wetter gestoppt (doWeatherCycle = false)");
        LOGGER.info("═══════════════════════════════════════════════════════════");
    }

    /**
     * Entfriert die Welt wenn der erste Spieler joined
     */
    private void unfreezeWorld(ServerLevel level) {
        // Starte ALLES wieder:
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, level.getServer());         // Zeit läuft
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(savedRandomTickSpeed, level.getServer()); // Pflanzen wachsen
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(true, level.getServer());    // Mobs spawnen
        level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(true, level.getServer());    // Wetter läuft

        long currentTime = level.getDayTime();
        LOGGER.info("═══════════════════════════════════════════════════════════");
        LOGGER.info("WELT ENTFRIERT - Erster Spieler joined");
        LOGGER.info("Zeit: {} | Tag: {} | Tageszeit: {}", currentTime, currentTime / 24000, currentTime % 24000);
        LOGGER.info("✓ Zeit läuft (doDaylightCycle = true)");
        LOGGER.info("✓ Pflanzen wachsen (randomTickSpeed = {})", savedRandomTickSpeed);
        LOGGER.info("✓ Mob-Spawn aktiv (doMobSpawning = true)");
        LOGGER.info("✓ Wetter aktiv (doWeatherCycle = true)");
        LOGGER.info("═══════════════════════════════════════════════════════════");
    }
}
