package de.rolandsw.schedulemc.tobacco.business;
nimport de.rolandsw.schedulemc.util.GameConstants;

import de.rolandsw.schedulemc.managers.NPCEntityRegistry;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

/**
 * Täglich aktualisiert NPC Business-Metriken
 */
@Mod.EventBusSubscriber
public class BusinessMetricsUpdateHandler {

    private static long lastUpdateDay = -1;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        EventHelper.handleServerTickEnd(event, server -> {
            // Überspringe wenn keine Spieler online sind (Zeit läuft nicht)
            if (server.getPlayerCount() == 0) {
                return;
            }

            ServerLevel level = server.overworld();
            long dayTime = level.getDayTime() % GameConstants.TICKS_PER_DAY;

            // Um Mitternacht (Zeit 0)
            if (dayTime == 0) {
                long currentDay = level.getDayTime() / GameConstants.TICKS_PER_DAY;

                // Nur einmal pro Tag
                if (currentDay != lastUpdateDay) {
                    lastUpdateDay = currentDay;
                    updateAllNPCMetrics(level, currentDay);
                }
            }
        });
    }

    /**
     * Performance-Optimierung: Nutze NPCEntityRegistry statt getAllEntities()
     */
    private static void updateAllNPCMetrics(ServerLevel level, long currentDay) {
        for (CustomNPCEntity npc : NPCEntityRegistry.getAllNPCs(level)) {
            if (!npc.getNpcData().hasInventoryAndWallet()) {
                continue; // Nur BEWOHNER und VERKÄUFER
            }

                NPCBusinessMetrics metrics = new NPCBusinessMetrics(npc);

                // 1. Zufriedenheits-Decay
                long daysSinceLastPurchase = currentDay - metrics.getLastPurchaseDay();

                if (daysSinceLastPurchase >= 3) {
                    int decay = 2 + Math.max(0, (int)(daysSinceLastPurchase - 3) * 2);
                    metrics.modifySatisfaction(-decay);
                }

                // 2. Nachfrage-Regeneration
                if (daysSinceLastPurchase >= 1) {
                    metrics.regenerateDemand();
                }

            // 3. Speichern
            metrics.save();
        }
    }
}
