package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.component.control.OwnershipComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Key item for locking/unlocking vehicles.
 */
public class VehicleKeyItem extends Item {

    public VehicleKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                 net.minecraft.world.entity.LivingEntity entity,
                                                 InteractionHand hand) {
        if (entity instanceof VehicleEntity vehicle) {
            OwnershipComponent ownership = vehicle.getComponent(
                    ComponentType.SECURITY, OwnershipComponent.class);

            if (ownership != null) {
                // Link key to vehicle if not already linked
                UUID keyVehicleId = getLinkedVehicleId(stack);
                if (keyVehicleId == null) {
                    setLinkedVehicleId(stack, vehicle.getUUID());
                    player.displayClientMessage(
                            Component.translatable("message.vehicle.key_linked"), true);
                    return InteractionResult.SUCCESS;
                }

                // Toggle lock if key matches vehicle
                if (keyVehicleId.equals(vehicle.getUUID())) {
                    ownership.toggleLock();
                    player.displayClientMessage(
                            Component.translatable(ownership.isLocked() ?
                                    "message.vehicle.locked" : "message.vehicle.unlocked"),
                            true);
                    return InteractionResult.SUCCESS;
                } else {
                    player.displayClientMessage(
                            Component.translatable("message.vehicle.wrong_key"), true);
                    return InteractionResult.FAIL;
                }
            }
        }

        return super.interactLivingEntity(stack, player, entity, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world,
                               List<Component> tooltip, TooltipFlag flag) {
        UUID vehicleId = getLinkedVehicleId(stack);
        if (vehicleId != null) {
            tooltip.add(Component.translatable("tooltip.vehicle.key_linked",
                    vehicleId.toString().substring(0, 8)));
        } else {
            tooltip.add(Component.translatable("tooltip.vehicle.key_unlinked"));
        }

        super.appendHoverText(stack, world, tooltip, flag);
    }

    @Nullable
    private UUID getLinkedVehicleId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.hasUUID("VehicleId")) {
            return tag.getUUID("VehicleId");
        }
        return null;
    }

    private void setLinkedVehicleId(ItemStack stack, UUID vehicleId) {
        stack.getOrCreateTag().putUUID("VehicleId", vehicleId);
    }
}
