package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.items.ModItems;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotBattery extends Slot {

    private EntityGenericVehicle vehicle;
    private Player player;

    public SlotBattery(EntityGenericVehicle vehicle, int index, int xPosition, int yPosition, Player player) {
        super(new SimpleContainer(1), index, xPosition, yPosition);
        this.vehicle = vehicle;
        this.player = player;
    }

    @Override
    public void set(ItemStack stack) {
        if (!stack.getItem().equals(ModItems.BATTERY.get())) {
            return;
        }

        int energy = stack.getMaxDamage() - stack.getDamageValue();

        int energyToFill = vehicle.getBatteryComponent().getMaxBatteryLevel() - vehicle.getBatteryComponent().getBatteryLevel();

        int fill = Math.min(energy, energyToFill);

        stack.setDamageValue(stack.getMaxDamage() - (energy - fill));

        vehicle.getBatteryComponent().setBatteryLevel(vehicle.getBatteryComponent().getBatteryLevel() + fill);

        if (!player.getInventory().add(stack)) {
            Containers.dropItemStack(vehicle.level(), vehicle.getX(), vehicle.getY(), vehicle.getZ(), stack);
        }
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem().equals(ModItems.BATTERY.get());
    }

}
