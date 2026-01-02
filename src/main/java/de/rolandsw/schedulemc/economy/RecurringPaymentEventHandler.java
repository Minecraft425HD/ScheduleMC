package de.rolandsw.schedulemc.economy;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Event Handler für automatische Ausführung von Daueraufträgen
 * Läuft täglich und verarbeitet alle fälligen Daueraufträge
 */
@Mod.EventBusSubscriber
public class RecurringPaymentEventHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        EventHelper.handleServerTickEnd(event, server -> {
            // Überspringe wenn keine Spieler online sind (Zeit läuft nicht)
            if (server.getPlayerCount() == 0) {
                return;
            }

            ServerLevel level = server.overworld();
            long dayTime = level.getDayTime();

            // Übergebe DayTime an RecurringPaymentManager
            RecurringPaymentManager manager = RecurringPaymentManager.getInstance(server);
            manager.tick(dayTime);
        });
    }
}
