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
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().above();

        if (player == null) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrCreateTag();

        // Rechtsklick + Shift = Speichere Händler-UUID
        if (player.isShiftKeyDown()) {
            if (tag.contains("DealerId")) {
                tag.remove("DealerId");
                player.sendSystemMessage(Component.literal("Händler-Verknüpfung entfernt").withStyle(ChatFormatting.YELLOW));
            } else {
                player.sendSystemMessage(Component.literal("Rechtsklicke einen Autohändler-NPC, um ihn zu verknüpfen").withStyle(ChatFormatting.GOLD));
            }
            return InteractionResult.SUCCESS;
        }

        // Normaler Rechtsklick = Setze Spawn-Punkt
        if (!tag.contains("DealerId")) {
            player.sendSystemMessage(Component.literal("⚠ Kein Händler verknüpft! Shift+Rechtsklick auf einen Autohändler-NPC").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        UUID dealerId = tag.getUUID("DealerId");
        float yaw = player.getYRot();

        if (!level.isClientSide()) {
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

        return InteractionResult.SUCCESS;
    }

    /**
     * Verknüpft das Tool mit einem Händler-NPC
     */
    public static void linkToDealer(ItemStack stack, UUID dealerId, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID("DealerId", dealerId);

        player.sendSystemMessage(Component.literal("✓ ").withStyle(ChatFormatting.GREEN)
            .append(Component.literal("Tool mit Autohändler verknüpft").withStyle(ChatFormatting.GOLD)));
        player.sendSystemMessage(Component.literal("Rechtsklicke nun auf den Boden, um Spawn-Punkte zu setzen").withStyle(ChatFormatting.GRAY));
    }
}
