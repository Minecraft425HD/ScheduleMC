package de.rolandsw.schedulemc.economy;

import de.rolandsw.schedulemc.player.PlayerSettingsManager;
import de.rolandsw.schedulemc.player.network.PlayerSettingsNetworkHandler;
import de.rolandsw.schedulemc.player.network.SyncPlayerSettingsPacket;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
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
     *
     * WICHTIG: Verwendet EventPriority.HIGHEST um sicherzustellen, dass Konten
     * erstellt werden BEVOR andere Handler (wie AchievementTracker) darauf zugreifen!
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        EventHelper.handlePlayerJoin(event, player -> {
            UUID uuid = player.getUUID();

            LOGGER.info("=== PLAYER JOIN: {} (UUID: {}) ===", player.getName().getString(), uuid);

            // ✅ MEMORY LEAK PREVENTION: Entferne Offline-Markierung bei Reconnect
            MemoryCleanupManager.markPlayerOnline(uuid);

            // Konto erstellen falls noch nicht vorhanden
            // createAccount setzt bereits das Startguthaben - kein zusätzlicher deposit nötig!
            boolean hadAccount = EconomyManager.hasAccount(uuid);
            LOGGER.info("Player {} has existing account: {}", player.getName().getString(), hadAccount);

            if (!hadAccount) {
                LOGGER.info("Creating new account for player {} with start balance of {} €",
                    player.getName().getString(), EconomyManager.getStartBalance());
                EconomyManager.createAccount(uuid);
                LOGGER.info("Account created! New balance: {} €", EconomyManager.getBalance(uuid));
            } else {
                LOGGER.info("Player {} already has account with balance: {} €",
                    player.getName().getString(), EconomyManager.getBalance(uuid));
            }

            // Synchronisiere Spieler-Einstellungen zum Client
            var settings = PlayerSettingsManager.getSettings(uuid);
            PlayerSettingsNetworkHandler.sendToClient(player, new SyncPlayerSettingsPacket(settings));
            LOGGER.info("Synced player settings to client: Warnings={}, Electricity={} kWh, Water={} L",
                settings.isUtilityWarningsEnabled(),
                settings.getElectricityWarningThreshold(),
                settings.getWaterWarningThreshold());

            // Prüfe ob das der erste Spieler ist
            ServerLevel level = (ServerLevel) player.level();
            int playerCount = level.getServer().getPlayerCount();

            // Wenn das der erste Spieler ist, ENTFRIER die Welt komplett
            if (playerCount == 1) {
                unfreezeWorld(level);
            }

            LOGGER.info("=== PLAYER JOIN COMPLETE: {} ===", player.getName().getString());
        });
    }

    /**
     * Speichert Economy-Daten beim Logout
     * Stoppt auch die Minecraft-Zeit wenn der letzte Spieler den Server verlässt
     */
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        EventHelper.handlePlayerLogout(event, player -> {
            UUID uuid = player.getUUID();

            // ✅ MEMORY LEAK PREVENTION: Markiere Spieler für Cleanup
            MemoryCleanupManager.markPlayerOffline(uuid);

            // Prüfe ob das der letzte Spieler war
            ServerLevel level = (ServerLevel) player.level();
            int playerCount = level.getServer().getPlayerCount();

            // playerCount ist noch der alte Wert (bevor der Spieler entfernt wird)
            // Wenn nur noch 1 Spieler da ist, ist nach dem Logout keiner mehr da
            if (playerCount == 1) {
                freezeWorld(level);
            }

            // Speichere Economy-Daten beim Logout für Datensicherheit
            EconomyManager.saveAccounts();
        });
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
