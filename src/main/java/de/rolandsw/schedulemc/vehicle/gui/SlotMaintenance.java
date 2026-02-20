package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.items.ItemDieselCanister;
import de.rolandsw.schedulemc.vehicle.items.ModItems;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Universal maintenance slot that accepts diesel canisters, batteries, and repair kits.
 * Detects the item type and executes the appropriate maintenance action.
 */
public class SlotMaintenance extends Slot {

    private EntityGenericVehicle vehicle;
    private Player player;

    public SlotMaintenance(EntityGenericVehicle vehicle, int index, int xPosition, int yPosition, Player player) {
        super(new SimpleContainer(1), index, xPosition, yPosition);
        this.vehicle = vehicle;
        this.player = player;
    }

    @Override
    public void set(ItemStack stack) {
        if (stack.getItem().equals(ModItems.DIESEL_CANISTER.get())) {
            handleFuel(stack);
        } else if (stack.getItem().equals(ModItems.BATTERY.get())) {
            handleBattery(stack);
        } else if (stack.getItem().equals(ModItems.REPAIR_KIT.get())) {
            handleRepairKit(stack);
        } else {
            return;
        }

        if (!player.getInventory().add(stack)) {
            Containers.dropItemStack(vehicle.level(), vehicle.getX(), vehicle.getY(), vehicle.getZ(), stack);
        }
    }

    private void handleFuel(ItemStack stack) {
        boolean success = ItemDieselCanister.fuelFluidHandler(stack, vehicle);
        if (success) {
            ModSounds.playSound(SoundEvents.BREWING_STAND_BREW, vehicle.level(), vehicle.blockPosition(), null, SoundSource.MASTER);
        }
    }

    private void handleBattery(ItemStack stack) {
        int energy = stack.getMaxDamage() - stack.getDamageValue();
        int energyToFill = vehicle.getBatteryComponent().getMaxBatteryLevel() - vehicle.getBatteryComponent().getBatteryLevel();
        int fill = Math.min(energy, energyToFill);
        stack.setDamageValue(stack.getMaxDamage() - (energy - fill));
        vehicle.getBatteryComponent().setBatteryLevel(vehicle.getBatteryComponent().getBatteryLevel() + fill);
    }

    private void handleRepairKit(ItemStack stack) {
        if (vehicle.getDamageComponent().getDamage() >= 90) {
            stack.shrink(1);
            float damage = vehicle.getDamageComponent().getDamage() - ModConfigHandler.VEHICLE_SERVER.repairKitRepairAmount.get().floatValue();
            if (damage >= 0) {
                vehicle.getDamageComponent().setDamage(damage);
            }
            ModSounds.playSound(ModSounds.RATCHET.get(), vehicle.level(), vehicle.blockPosition(), null, SoundSource.BLOCKS);
        }
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem().equals(ModItems.DIESEL_CANISTER.get())
            || stack.getItem().equals(ModItems.BATTERY.get())
            || stack.getItem().equals(ModItems.REPAIR_KIT.get());
    }
}
