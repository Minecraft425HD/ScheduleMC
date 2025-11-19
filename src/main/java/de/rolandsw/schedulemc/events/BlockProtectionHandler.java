package de.rolandsw.schedulemc.events;

import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * ScheduleMC 3.0 - Block-Schutz mit Trusted Players Support
 */
public class BlockProtectionHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Verhindert das Abbauen von Blöcken in fremden Plots
     */
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        BlockPos pos = event.getPos();

        if (!checkPlotPermission(player, pos, "abbauen")) {
            event.setCanceled(true);
        }
    }

    /**
     * Verhindert das Platzieren von Blöcken in fremden Plots
     */
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        BlockPos pos = event.getPos();

        if (!checkPlotPermission(player, pos, "platzieren")) {
            event.setCanceled(true);
        }
    }

    /**
     * Zentrale Berechtigungsprüfung
     * 
     * @param player Der Spieler
     * @param pos Die Position
     * @param action Die Aktion (für Fehlermeldung)
     * @return true wenn erlaubt, false wenn verboten
     */
    private boolean checkPlotPermission(Player player, BlockPos pos, String action) {
        UUID playerUUID = player.getUUID();

        // Admin hat IMMER Zugriff!
        if (player.hasPermissions(2)) {
            return true;
        }

        // Prüfe alle Plots
        for (PlotRegion plot : PlotManager.getPlots()) {
            if (plot.contains(pos)) {
                
                // Öffentlicher Plot: KEIN Bauen/Abbauen erlaubt!
                if (plot.isPublic()) {
                    player.displayClientMessage(Component.literal(
                        "§cÖffentlicher Plot - Bauen/Abbauen verboten!"
                    ), true);
                    return false;
                }
                
                // Plot hat keinen Besitzer = frei
                if (!plot.hasOwner()) {
                    return true;
                }

                // Besitzer oder Trusted?
                if (plot.hasAccess(playerUUID)) {
                    return true;
                }

                // Keine Berechtigung - zeige Fehlermeldung
                String ownerInfo;
                if (plot.isRented()) {
                    ownerInfo = "Dieser Plot ist vermietet";
                } else {
                    ownerInfo = "Dieser Plot gehört jemand anderem";
                }
                
                player.displayClientMessage(
                    Component.literal(
                        "§c✗ Du darfst hier keine Blöcke " + action + "!\n" +
                        "§7" + ownerInfo
                    ), 
                    true
                );
                
                LOGGER.debug("Plot-Schutz: {} versuchte Block zu {} bei {} (Plot: {})",
                    player.getName().getString(), action, pos, plot.getPlotName());
                
                return false;
            }
        }

        // Nicht in einem Plot = erlaubt
        return true;
    }

    /**
     * Erlaubt Interaktion in öffentlichen Plots (Truhen, GUIs, etc.)
     */
    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        
        // Admin darf immer!
        if (player.hasPermissions(2)) {
            return;
        }

        for (PlotRegion plot : PlotManager.getPlots()) {
            if (plot.contains(pos)) {
                
                // Öffentlicher Plot: Interaktion ERLAUBT!
                if (plot.isPublic()) {
                    return;  // Erlauben!
                }
                
                // Privater Plot ohne Besitzer: Erlauben
                if (!plot.hasOwner()) {
                    return;
                }
                
                // Privater Plot: Nur Besitzer + Trusted
                if (!plot.hasAccess(player.getUUID())) {
                    event.setCanceled(true);
                    player.displayClientMessage(Component.literal(
                        "§c✗ Du darfst hier nichts benutzen!"
                    ), true);
                    return;
                }
            }
        }
    }
}
