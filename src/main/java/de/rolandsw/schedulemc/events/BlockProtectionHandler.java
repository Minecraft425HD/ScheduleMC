package de.rolandsw.schedulemc.events;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.items.NPCLocationTool;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;

/**
 * ScheduleMC 3.0 - Block-Schutz mit Trusted Players Support
 */
public class BlockProtectionHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Verhindert das Abbauen von Blöcken in fremden Plots oder NPC-Arbeitsorten
     */
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        BlockPos pos = event.getPos();

        // Prüfe ob Block ein NPC Arbeitsort ist
        if (isNPCWorkLocation(player, pos)) {
            event.setCanceled(true);
            return;
        }

        if (!checkPlotPermission(player, pos, "abbauen")) {
            event.setCanceled(true);
        }
    }

    /**
     * Behandelt Linksklick auf Blöcke für das LocationTool (Arbeitsort setzen)
     */
    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = player.getItemInHand(event.getHand());

        // Prüfe ob Spieler das LocationTool hält
        if (!(stack.getItem() instanceof NPCLocationTool)) {
            return;
        }

        BlockPos clickedPos = event.getPos();

        if (!player.level().isClientSide) {
            // Prüfe ob ein NPC ausgewählt wurde
            Integer npcId = NPCLocationTool.getSelectedNPC(player.getUUID());
            if (npcId == null) {
                player.sendSystemMessage(
                    Component.literal("Kein NPC ausgewählt! Linksklick auf einen NPC.")
                        .withStyle(ChatFormatting.RED)
                );
                event.setCanceled(true);
                event.setUseBlock(Event.Result.DENY);
                event.setUseItem(Event.Result.DENY);
                return;
            }

            Entity entity = player.level().getEntity(npcId);

            if (!(entity instanceof CustomNPCEntity npc)) {
                player.sendSystemMessage(
                    Component.literal("Ausgewählter NPC nicht mehr verfügbar!")
                        .withStyle(ChatFormatting.RED)
                );
                NPCLocationTool.clearSelectedNPC(player.getUUID());
                event.setCanceled(true);
                event.setUseBlock(Event.Result.DENY);
                event.setUseItem(Event.Result.DENY);
                return;
            }

            // Setze Arbeitsort
            npc.getNpcData().setWorkLocation(clickedPos);
            player.sendSystemMessage(
                Component.literal("Arbeitsstätte gesetzt für ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(npc.getNpcName())
                        .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" bei " + clickedPos.toShortString())
                        .withStyle(ChatFormatting.WHITE))
            );

            // Verhindere Block-Abbau
            event.setCanceled(true);
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
        }
    }

    /**
     * Prüft ob eine Position ein NPC Arbeitsort ist und schützt sie
     */
    private boolean isNPCWorkLocation(Player player, BlockPos pos) {
        // Admin darf Arbeitsorte abbauen
        if (player.hasPermissions(2)) {
            return false;
        }

        // Suche alle NPCs im Level
        WorldBorder border = player.level().getWorldBorder();
        AABB searchArea = new AABB(
            border.getMinX(), player.level().getMinBuildHeight(), border.getMinZ(),
            border.getMaxX(), player.level().getMaxBuildHeight(), border.getMaxZ()
        );
        List<CustomNPCEntity> npcs = player.level().getEntitiesOfClass(
            CustomNPCEntity.class,
            searchArea
        );

        for (CustomNPCEntity npc : npcs) {
            BlockPos workLocation = npc.getNpcData().getWorkLocation();
            if (workLocation != null && workLocation.equals(pos)) {
                player.displayClientMessage(
                    Component.literal("§c✗ Dies ist der Arbeitsort von ")
                        .append(Component.literal(npc.getNpcName()).withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal("!")),
                    true
                );
                return true;
            }
        }

        return false;
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
     * OPTIMIERT: Nutzt Spatial Index für O(1) statt O(n) Plot-Lookup
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

        // OPTIMIERT: Direkter Lookup statt Iteration über alle Plots
        PlotRegion plot = PlotManager.getPlotAt(pos);

        // Nicht in einem Plot = erlaubt
        if (plot == null) {
            return true;
        }

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

    /**
     * Erlaubt Interaktion in öffentlichen Plots (Truhen, GUIs, etc.)
     *
     * OPTIMIERT: Nutzt Spatial Index für schnellen Plot-Lookup
     */
    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getPos();

        // Admin darf immer!
        if (player.hasPermissions(2)) {
            return;
        }

        // OPTIMIERT: Direkter Lookup statt Iteration
        PlotRegion plot = PlotManager.getPlotAt(pos);

        // Nicht in einem Plot = erlaubt
        if (plot == null) {
            return;
        }

        // Öffentlicher Plot: Interaktion ERLAUBT!
        if (plot.isPublic()) {
            return;
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
        }
    }
}
