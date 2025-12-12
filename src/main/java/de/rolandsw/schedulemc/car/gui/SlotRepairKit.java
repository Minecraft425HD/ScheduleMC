package de.rolandsw.schedulemc.car.gui;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import de.rolandsw.schedulemc.car.items.ModItems;
import de.rolandsw.schedulemc.car.sounds.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotRepairKit extends Slot {

    private EntityGenericCar car;
    private Player player;

    public SlotRepairKit(EntityGenericCar car, int index, int xPosition, int yPosition, Player player) {
        super(new SimpleContainer(1), index, xPosition, yPosition);
        this.car = car;
        this.player = player;
    }

    @Override
    public void set(ItemStack stack) {
        if (!stack.getItem().equals(ModItems.REPAIR_KIT.get())) {
            return;
        }

        if (car.getDamageComponent().getDamage() >= 90) {

            stack.shrink(1);

            float damage = car.getDamageComponent().getDamage() - Main.SERVER_CONFIG.repairKitRepairAmount.get().floatValue();
            if (damage >= 0) {
                car.getDamageComponent().setDamage(damage);
            }
            ModSounds.playSound(ModSounds.RATCHET.get(), car.level(), car.blockPosition(), null, SoundSource.BLOCKS);
        }

        if (!player.getInventory().add(stack)) {
            Containers.dropItemStack(car.level(), car.getX(), car.getY(), car.getZ(), stack);
        }
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem().equals(ModItems.REPAIR_KIT.get());
    }

}
