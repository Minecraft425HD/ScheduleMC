package de.rolandsw.schedulemc.region;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.util.EventHelper;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Plot-Schutz-System
 *
 * Verhindert:
 * - Block-Abbau außerhalb von Plots oder ohne Rechte
 * - Block-Platzierung ohne Rechte
 * - Interaktionen ohne Rechte
 * - Explosionen in Plots
 *
 * Ausnahmen:
 * - OPs (Permission Level 2+) dürfen alles
 * - Besitzer, Trusted Players und Mieter haben Zugriff
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public class PlotProtectionHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean ENABLE_WORLD_PROTECTION = true; // In Config verschieben später

    /**
     * Verhindert Block-Abbau ohne Rechte
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        EventHelper.handleBlockBreak(event, player -> {
            if (!ENABLE_WORLD_PROTECTION) return;
            if (!(player instanceof ServerPlayer serverPlayer)) return;

            // OPs dürfen immer
            if (serverPlayer.hasPermissions(2)) {
                return;
            }

            BlockPos pos = event.getPos();
        PlotRegion plot = PlotManager.getPlotAt(pos);

        if (plot == null) {
            // Kein Plot → Weltschutz aktiv
            event.setCanceled(true);
            player.sendSystemMessage(Component.translatable("message.plot.cannot_break_buy_plot"));
            LOGGER.debug("[PLOT-PROTECTION] {} attempted to break outside of a plot at {}",
                player.getName().getString(), pos.toShortString());
            return;
        }

        // Prüfe Rechte im Plot (mit Apartment-Unterstützung)
        if (!plot.hasAccess(player.getUUID(), pos)) {
            event.setCanceled(true);

            // Prüfe ob Position in Apartment liegt
            PlotArea apartment = plot.getSubAreaAt(pos);

            if (apartment != null) {
                // In Apartment
                if (apartment.isRented()) {
                    player.sendSystemMessage(Component.translatable("message.plot.apartment_rented"));
                } else if (apartment.isForRent()) {
                    player.sendSystemMessage(Component.translatable("message.plot.apartment_rent_command", apartment.getId()));
                } else {
                    player.sendSystemMessage(Component.translatable("message.plot.no_apartment_rights"));
                }

                LOGGER.debug("[PLOT-PROTECTION] {} versuchte ohne Rechte in Apartment {} (Plot: {}) abzubauen",
                    player.getName().getString(), apartment.getId(), plot.getPlotId());
            } else {
                // Normaler Plot-Bereich
                String ownerName = plot.getOwnerName();
                if (ownerName == null || ownerName.equals("Niemand")) {
                    player.sendSystemMessage(Component.translatable("message.plot.unowned_buy"));
                } else {
                    player.sendSystemMessage(Component.translatable("message.plot.no_rights_owner", ownerName));
                }

                LOGGER.debug("[PLOT-PROTECTION] {} versuchte ohne Rechte in Plot {} (Besitzer: {}) abzubauen",
                    serverPlayer.getName().getString(), plot.getPlotId(), ownerName);
            }
        }
        });
    }

    /**
     * Verhindert Block-Platzierung ohne Rechte
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        EventHelper.handleBlockPlace(event, player -> {
            if (!ENABLE_WORLD_PROTECTION) return;

            if (!(player instanceof ServerPlayer serverPlayer)) return;

            // OPs dürfen immer
            if (serverPlayer.hasPermissions(2)) {
                return;
            }

            BlockPos pos = event.getPos();
            PlotRegion plot = PlotManager.getPlotAt(pos);

            if (plot == null) {
                // Kein Plot → Weltschutz aktiv
                event.setCanceled(true);
                serverPlayer.sendSystemMessage(Component.translatable("message.plot.cannot_build_buy_plot"));
                return;
            }

            // Prüfe Rechte im Plot (mit Apartment-Unterstützung)
            if (!plot.hasAccess(serverPlayer.getUUID(), pos)) {
                event.setCanceled(true);

                // Prüfe ob Position in Apartment liegt
                PlotArea apartment = plot.getSubAreaAt(pos);

                if (apartment != null) {
                    serverPlayer.sendSystemMessage(Component.translatable("message.plot.no_apartment_rights"));
                } else {
                    String ownerName = plot.getOwnerName();
                    serverPlayer.sendSystemMessage(Component.translatable("message.plot.no_rights_owner", ownerName));
                }
            }
        });
    }

    /**
     * Verhindert Interaktionen ohne Rechte
     * (Türen, Truhen, Knöpfe, etc.)
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EventHelper.handleRightClickBlock(event, player -> {
            if (!ENABLE_WORLD_PROTECTION) return;
            if (!(player instanceof ServerPlayer serverPlayer)) return;

            // OPs dürfen immer
            if (serverPlayer.hasPermissions(2)) {
                return;
            }

            BlockPos pos = event.getPos();
            PlotRegion plot = PlotManager.getPlotAt(pos);

            // Wenn kein Plot → Öffentlicher Bereich, Interaktion erlaubt
            // (z.B. Shops, Spawn-NPCs)
            if (plot == null) {
                return;
            }

            // Wenn Plot öffentlich → Interaktion erlaubt
            if (plot.isPublic()) {
                return;
            }

            // Prüfe Rechte im Plot
            if (!plot.hasAccess(serverPlayer.getUUID())) {
                // Nur bei bestimmten Blöcken blocken (Türen, Truhen, etc.)
                String blockName = event.getLevel().getBlockState(pos).getBlock().getDescriptionId();

                if (blockName.contains("door") ||
                    blockName.contains("chest") ||
                    blockName.contains("furnace") ||
                    blockName.contains("button") ||
                    blockName.contains("lever") ||
                    blockName.contains("trapdoor") ||
                    blockName.contains("fence_gate")) {

                    event.setCanceled(true);
                    serverPlayer.sendSystemMessage(Component.translatable("message.plot.belongs_to_owner", plot.getOwnerName()));
                }
            }
        });
    }

    /**
     * Verhindert Explosionen in Plots (optional)
     */
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        EventHelper.handleEvent(() -> {
            if (!ENABLE_WORLD_PROTECTION) return;

            BlockPos explosionPos = BlockPos.containing(event.getExplosion().getPosition());
            PlotRegion plot = PlotManager.getPlotAt(explosionPos);

            if (plot != null) {
                // Explosion in Plot → Verhindere Block-Schaden
                event.getAffectedBlocks().clear();

                LOGGER.debug("[PLOT-PROTECTION] Explosion in Plot {} verhindert bei {}",
                    plot.getPlotId(), explosionPos.toShortString());
            }
        }, "onExplosion");
    }

    /**
     * Hilfsmethode: Gibt Plot-Info für Spieler zurück
     */
    public static String getPlotInfo(BlockPos pos) {
        PlotRegion plot = PlotManager.getPlotAt(pos);

        if (plot == null) {
            return Component.translatable("message.plot.info_no_plot").getString();
        }

        StringBuilder info = new StringBuilder();
        info.append("§e").append(plot.getPlotName()).append("\n");
        info.append(Component.translatable("message.plot.info_owner", plot.getOwnerName()).getString()).append("\n");

        if (plot.isForSale()) {
            info.append(Component.translatable("message.plot.info_for_sale", plot.getSalePrice()).getString()).append("\n");
        }

        if (plot.isForRent()) {
            info.append(Component.translatable("message.plot.info_for_rent", plot.getRentPricePerDay()).getString()).append("\n");
        }

        if (plot.isRented()) {
            info.append(Component.translatable("message.plot.info_rented_days", plot.getRentDaysLeft()).getString()).append("\n");
        }

        info.append(Component.translatable("message.plot.info_rating", plot.getRatingStars(), plot.getRatingCount()).getString()).append("\n");
        info.append(Component.translatable("message.plot.info_size", plot.getVolume()).getString());

        return info.toString();
    }
}
