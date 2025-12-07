package de.rolandsw.schedulemc.tobacco.business;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
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
        if (event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.END) {
            return;
        }

        // Überspringe wenn keine Spieler online sind (Zeit läuft nicht)
        if (event.getServer().getPlayerCount() == 0) {
            return;
        }

        ServerLevel level = event.getServer().overworld();
        long dayTime = level.getDayTime() % 24000;

        // Um Mitternacht (Zeit 0)
        if (dayTime == 0) {
            long currentDay = level.getDayTime() / 24000;

            // Nur einmal pro Tag
            if (currentDay != lastUpdateDay) {
                lastUpdateDay = currentDay;
                updateAllNPCMetrics(level, currentDay);
            }
        }
    }

    private static void updateAllNPCMetrics(ServerLevel level, long currentDay) {
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof CustomNPCEntity npc) {
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
}
