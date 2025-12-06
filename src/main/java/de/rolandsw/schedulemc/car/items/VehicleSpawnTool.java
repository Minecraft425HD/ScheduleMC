package de.rolandsw.schedulemc.car.items;

import de.rolandsw.schedulemc.car.vehicle.VehicleSpawnRegistry;
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
                player.sendSystemMessage(Component.literal("Händler-Verknüpfung entfernt").withStyle(ChatFormatting.YELLOW));
            } else {
                player.sendSystemMessage(Component.literal("Linksklick auf Autohändler-NPC, um ihn zu verknüpfen").withStyle(ChatFormatting.GOLD));
            }
            return InteractionResult.SUCCESS;
        }

        // Rechtsklick auf Block = Info-Nachricht
        player.sendSystemMessage(Component.literal("══════════════════════════════").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("🚗 Vehicle Spawn Tool").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("Linksklick auf AUTOHAENDLER = Tool verknüpfen").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("Linksklick auf Block = Spawn-Punkt setzen").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("Shift+Rechtsklick = Verknüpfung entfernen").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("══════════════════════════════").withStyle(ChatFormatting.GOLD));

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
            player.sendSystemMessage(Component.literal("⚠ Kein Händler verknüpft! Rechtsklick auf einen Autohändler-NPC").withStyle(ChatFormatting.RED));
            return;
        }

        UUID dealerId = tag.getUUID("DealerId");
        float yaw = player.getYRot();

        // Setze Spawn-Punkt
        VehicleSpawnRegistry.addSpawnPoint(dealerId, pos, yaw);
        VehicleSpawnRegistry.saveIfNeeded();

        player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("🚗 ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal("FAHRZEUG-SPAWN-PUNKT").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
        player.sendSystemMessage(Component.literal("Position: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(pos.toShortString()).withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.literal("Rotation: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.format("%.1f°", yaw)).withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));
    }

    /**
     * Verknüpft das Tool mit einem Händler-NPC (wird von CustomNPCEntity.hurt() aufgerufen)
     */
    public static void linkToDealer(ItemStack stack, UUID dealerId, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID("DealerId", dealerId);

        player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("✓ ").withStyle(ChatFormatting.GREEN)
            .append(Component.literal("TOOL VERKNÜPFT").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
        player.sendSystemMessage(Component.literal("Linksklicke nun auf den Boden, um Spawn-Punkte zu setzen").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));
    }
}
