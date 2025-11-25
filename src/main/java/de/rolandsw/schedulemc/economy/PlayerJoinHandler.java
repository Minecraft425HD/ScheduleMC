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
 * WICHTIG: Verwaltet auch das Stoppen/Starten der Minecraft-Zeit
 * - Wenn kein Spieler online: Zeit stoppt (doDaylightCycle = false)
 * - Wenn Spieler online: Zeit läuft (doDaylightCycle = true)
 */
public class PlayerJoinHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

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

        // Wenn das der erste Spieler ist, starte die Zeit
        if (playerCount == 1) {
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, level.getServer());
            long currentTime = level.getDayTime();
            LOGGER.info("Erster Spieler joined - Zeit läuft weiter (Zeit: {})", currentTime);
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
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
            long currentTime = level.getDayTime();
            LOGGER.info("Letzter Spieler hat den Server verlassen - Zeit gestoppt (Zeit: {})", currentTime);
        }

        // Könnte hier zusätzlich speichern für mehr Datensicherheit
        // EconomyManager.saveIfNeeded();
    }
}
