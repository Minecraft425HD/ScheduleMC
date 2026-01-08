package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.vehicle.VehicleSpawnRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Tool zum Setzen von Fahrzeug-Spawn-Punkten für Autohändler
 */
public class VehicleSpawnTool extends Item {

    public VehicleSpawnTool() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrCreateTag();

        // Rechtsklick + Shift = Entferne Händler-Verknüpfung
        if (player.isShiftKeyDown()) {
            if (tag.contains("DealerId")) {
                tag.remove("DealerId");
                player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.dealer_unlinked").withStyle(ChatFormatting.YELLOW));
            } else {
                player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.link_dealer_first").withStyle(ChatFormatting.GOLD));
            }
            return InteractionResult.SUCCESS;
        }

        // Rechtsklick auf Block = Info-Nachricht
        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.help_header").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.help_title").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.help_left_dealer").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.help_left_block").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.help_shift_right").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.help_footer").withStyle(ChatFormatting.GOLD));

        return InteractionResult.SUCCESS;
    }

    /**
     * Handler für Linksklick auf Block (wird von ScheduleMC.java aufgerufen)
     */
    public static void handleLeftClick(Player player, ItemStack stack, BlockPos pos) {
        Level level = player.level();
        CompoundTag tag = stack.getOrCreateTag();

        // Prüfe ob Händler verknüpft ist
        if (!tag.contains("DealerId")) {
            player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.no_dealer").withStyle(ChatFormatting.RED));
            return;
        }

        UUID dealerId = tag.getUUID("DealerId");
        float yaw = player.getYRot();

        // Setze Spawn-Punkt
        VehicleSpawnRegistry.addSpawnPoint(dealerId, pos, yaw);
        VehicleSpawnRegistry.saveIfNeeded();

        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.spawn_header").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.spawn_title").withStyle(ChatFormatting.YELLOW)
            .withStyle(ChatFormatting.BOLD));
        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.position_label").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(pos.toShortString()).withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.rotation_label").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.format("%.1f°", yaw)).withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.spawn_footer").withStyle(ChatFormatting.GOLD));
    }

    /**
     * Verknüpft das Tool mit einem Händler-NPC (wird von CustomNPCEntity.hurt() aufgerufen)
     */
    public static void linkToDealer(ItemStack stack, UUID dealerId, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID("DealerId", dealerId);

        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.linked_header").withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.linked_title").withStyle(ChatFormatting.GREEN)
            .withStyle(ChatFormatting.BOLD));
        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.linked_instruction").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable("item.vehicle_spawn_tool.linked_footer").withStyle(ChatFormatting.GREEN));
    }
}
