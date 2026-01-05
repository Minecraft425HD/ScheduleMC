package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.items.ItemCanister;
import de.rolandsw.schedulemc.vehicle.items.VehicleItems;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotFuel extends Slot {

    private EntityGenericVehicle vehicle;
    private Player player;

    public SlotFuel(EntityGenericVehicle vehicle, int index, int xPosition, int yPosition, Player player) {
        super(new SimpleContainer(1), index, xPosition, yPosition);
        this.vehicle = vehicle;
        this.player = player;
    }

    @Override
    public void set(ItemStack stack) {
        if (!stack.getItem().equals(VehicleItems.CANISTER.get())) {
            return;
        }

        boolean success = ItemCanister.fuelFluidHandler(stack, vehicle);

        if (success) {
            ModSounds.playSound(SoundEvents.BREWING_STAND_BREW, vehicle.level(), vehicle.blockPosition(), null, SoundSource.MASTER);
        }

        if (!player.getInventory().add(stack)) {
            Containers.dropItemStack(vehicle.level(), vehicle.getX(), vehicle.getY(), vehicle.getZ(), stack);
        }
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem().equals(VehicleItems.CANISTER.get());
    }

}
