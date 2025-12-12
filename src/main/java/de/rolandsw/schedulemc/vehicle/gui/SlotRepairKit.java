package de.rolandsw.schedulemc.vehicle.gui;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.items.ModItems;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotRepairKit extends Slot {

    private EntityGenericVehicle vehicle;
    private Player player;

    public SlotRepairKit(EntityGenericVehicle vehicle, int index, int xPosition, int yPosition, Player player) {
        super(new SimpleContainer(1), index, xPosition, yPosition);
        this.vehicle = vehicle;
        this.player = player;
    }

    @Override
    public void set(ItemStack stack) {
        if (!stack.getItem().equals(ModItems.REPAIR_KIT.get())) {
            return;
        }

        if (vehicle.getDamageComponent().getDamage() >= 90) {

            stack.shrink(1);

            float damage = vehicle.getDamageComponent().getDamage() - ModConfigHandler.CAR_SERVER.repairKitRepairAmount.get().floatValue();
            if (damage >= 0) {
                vehicle.getDamageComponent().setDamage(damage);
            }
            ModSounds.playSound(ModSounds.RATCHET.get(), vehicle.level(), vehicle.blockPosition(), null, SoundSource.BLOCKS);
        }

        if (!player.getInventory().add(stack)) {
            Containers.dropItemStack(vehicle.level(), vehicle.getX(), vehicle.getY(), vehicle.getZ(), stack);
        }
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem().equals(ModItems.REPAIR_KIT.get());
    }

}
