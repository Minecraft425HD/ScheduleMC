package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handler für Spieler-Events im Zusammenhang mit dem Smartphone
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public class SmartphonePlayerHandler {

    /**
     * Cleanup wenn Spieler den Server verlässt
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SmartphoneTracker.removePlayer(event.getEntity().getUUID());
    }
}
