import de.rolandsw.schedulemc.util.EventHelper;
package de.rolandsw.schedulemc.utility;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event Handler für das Utility-System
 *
 * Reagiert auf:
 * - Block-Platzierung
 * - Block-Entfernung
 * - Server-Ticks (für periodische Updates)
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public class UtilityEventHandler {

    // Counter für periodische Updates
    private static int tickCounter = 0;
    private static final int UPDATE_INTERVAL = 1000; // Alle 1000 Ticks (~50 Sekunden)
    private static final int SAVE_INTERVAL = 6000;   // Alle 6000 Ticks (~5 Minuten)

    // ═══════════════════════════════════════════════════════════════════════════
    // BLOCK EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Wird aufgerufen wenn ein Block platziert wird
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        EventHelper.handleBlockPlace(event, player -> {
            BlockPos pos = event.getPos();
            Block block = event.getPlacedBlock().getBlock();
            Level level = (Level) event.getLevel();

            // Registriere Block im Utility-System
            PlotUtilityManager.onBlockPlaced(pos, block, level);
        });
    }

    /**
     * Wird aufgerufen wenn ein Block abgebaut wird
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        EventHelper.handleBlockBreak(event, player -> {
            BlockPos pos = event.getPos();
            Block block = event.getState().getBlock();

            // Entferne Block aus Utility-System
            PlotUtilityManager.onBlockRemoved(pos, block);
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SERVER TICK
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Server-Tick für periodische Updates
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        EventHelper.handleServerTickEnd(event, server -> {
            tickCounter++;

            // Periodisches Update der Verbrauchswerte
            if (tickCounter % UPDATE_INTERVAL == 0) {
                ServerLevel overworld = server.overworld();
                if (overworld != null) {
                    // Tageswechsel-Check
                    PlotUtilityManager.onServerTick(overworld);

                    // Aktualisiere alle Verbrauchswerte
                    PlotUtilityManager.updateAllConsumption(overworld);
                }
            }

            // Periodisches Speichern
            if (tickCounter % SAVE_INTERVAL == 0) {
                PlotUtilityManager.saveIfNeeded();
            }

            // Counter Reset (verhindert Overflow)
            if (tickCounter >= Integer.MAX_VALUE - 10000) {
                tickCounter = 0;
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY-CONSUMER CALLBACK
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Wird von BlockEntities aufgerufen die IUtilityConsumer implementieren
     * um ihren Aktivitätsstatus zu melden
     *
     * @param pos Position des Blocks
     * @param isActive Aktueller Aktivitätsstatus
     */
    public static void reportActivityStatus(BlockPos pos, boolean isActive) {
        PlotUtilityManager.updateActiveStatus(pos, isActive);
    }

    /**
     * Hilfsmethode für BlockEntities die in tick() ihren Status melden wollen
     * Sollte nicht jeden Tick aufgerufen werden, sondern nur bei Statusänderung
     *
     * @param blockEntity Das BlockEntity
     * @param isActive Aktueller Status
     */
    public static void reportBlockEntityActivity(BlockEntity blockEntity, boolean isActive) {
        if (blockEntity.getLevel() == null || blockEntity.getLevel().isClientSide()) {
            return;
        }

        reportActivityStatus(blockEntity.getBlockPos(), isActive);
    }
}
