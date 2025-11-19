package de.rolandsw.schedulemc.economy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * Handler für Spieler-Events (Join, Leave)
 */
public class PlayerJoinHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Erstellt automatisch ein Konto für neue Spieler beim ersten Join
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
    }

    /**
     * Speichert Economy-Daten beim Logout (optional für zusätzliche Sicherheit)
     */
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // Könnte hier zusätzlich speichern für mehr Datensicherheit
        // EconomyManager.saveIfNeeded();
    }
}
