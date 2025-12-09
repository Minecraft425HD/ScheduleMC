package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.builder.VehiclePresets;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Tool for spawning vehicles from dealers in the new ECS-based vehicle system.
 */
public class VehicleSpawnTool extends Item {

    private final VehicleType vehicleType;

    public enum VehicleType {
        SEDAN("Sedan"),
        SPORT("Sportwagen"),
        SUV("SUV"),
        TRUCK("LKW"),
        TRANSPORTER("Transporter");

        private final String displayName;

        VehicleType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public VehicleSpawnTool(Properties properties, VehicleType vehicleType) {
        super(properties);
        this.vehicleType = vehicleType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrCreateTag();

        // Check if linked to dealer
        if (!tag.contains("DealerUUID")) {
            player.sendSystemMessage(Component.literal("§cDieses Tool ist nicht mit einem Händler verknüpft!"));
            player.sendSystemMessage(Component.literal("§7Klicke mit dem Tool auf einen Autohändler-NPC."));
            return InteractionResult.FAIL;
        }

        // Spawn vehicle at clicked position
        BlockPos pos = context.getClickedPos().above();
        VehicleEntity vehicle = createVehicleByType(context.getLevel(), vehicleType);

        if (vehicle != null) {
            vehicle.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            vehicle.setOwner(player.getUUID(), player.getName().getString());
            context.getLevel().addFreshEntity(vehicle);

            player.sendSystemMessage(Component.literal("✓ ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal("Fahrzeug gespawnt: " + vehicleType.getDisplayName()).withStyle(ChatFormatting.AQUA)));

            // Consume item if not creative
            if (!player.isCreative()) {
                stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();

        tooltip.add(Component.literal("Fahrzeugtyp: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(vehicleType.getDisplayName()).withStyle(ChatFormatting.AQUA)));

        if (tag.contains("DealerUUID")) {
            tooltip.add(Component.literal("✓ Mit Händler verknüpft").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.literal("✗ Nicht verknüpft").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal("→ Auf Autohändler klicken").withStyle(ChatFormatting.GRAY));
        }
    }

    /**
     * Link this tool to a dealer NPC
     */
    public static void linkToDealer(ItemStack stack, UUID dealerUUID, ServerPlayer player) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID("DealerUUID", dealerUUID);

        player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("✓ ").withStyle(ChatFormatting.GREEN)
            .append(Component.literal("TOOL VERKNÜPFT").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        player.sendSystemMessage(Component.literal("Das Fahrzeug-Tool wurde mit diesem Händler verknüpft.").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("Rechtsklick auf Block: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal("Fahrzeug spawnen").withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));
    }

    /**
     * Handle left click on block (for dealer linking)
     */
    public static void handleLeftClick(Player player, ItemStack stack, BlockPos pos) {
        // This is called from ScheduleMC when player left-clicks a block with this tool
        // Currently unused but kept for compatibility
    }

    /**
     * Create vehicle by type
     */
    private VehicleEntity createVehicleByType(Level level, VehicleType type) {
        return switch (type) {
            case SEDAN -> VehiclePresets.createSedan(level);
            case SPORT -> VehiclePresets.createSportCar(level);
            case SUV -> VehiclePresets.createSUV(level);
            case TRUCK -> VehiclePresets.createTruck(level);
            case TRANSPORTER -> VehiclePresets.createTransporter(level);
        };
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }
}
