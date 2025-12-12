package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.PredicateUUID;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

public class ItemKey extends Item {

    public ItemKey() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        UUID carUUID = getVehicle(stack);

        if (carUUID == null) {
            if (worldIn.isClientSide) {
                playerIn.displayClientMessage(Component.translatable("message.key_no_vehicle"), true);
            }
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        } else if (worldIn.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        List<EntityGenericVehicle> vehicles = worldIn.getEntitiesOfClass(EntityGenericVehicle.class, new AABB(playerIn.getX() - 25D, playerIn.getY() - 25D, playerIn.getZ() - 25D, playerIn.getX() + 25D, playerIn.getY() + 25D, playerIn.getZ() + 25D), new PredicateUUID(carUUID));

        if (vehicles.isEmpty()) {
            playerIn.displayClientMessage(Component.translatable("message.vehicle_out_of_range"), true);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        EntityGenericVehicle vehicle = vehicles.get(0);

        if (vehicle.getPassengers().stream().anyMatch(entity -> entity == playerIn)) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        vehicle.setLocked(!vehicle.isLocked(), true);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    public static void setVehicle(ItemStack stack, UUID carUUID) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }

        CompoundTag comp = stack.getTag();

        comp.putUUID("vehicle", carUUID);
    }

    public static UUID getVehicle(ItemStack stack) {
        if (stack == null) {
            return null;
        }

        if (!stack.hasTag()) {
            return null;
        }

        CompoundTag comp = stack.getTag();

        if (comp == null) {
            return null;
        }

        return comp.getUUID("vehicle");
    }

    public static ItemStack getKeyForVehicle(UUID vehicle) {
        ItemStack stack = new ItemStack(ModItems.KEY.get());

        setVehicle(stack, vehicle);

        return stack;
    }

}
