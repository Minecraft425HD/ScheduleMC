package de.rolandsw.schedulemc.car.gui;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ContainerCarInventory extends ContainerBase {

    private EntityGenericCar car;

    public ContainerCarInventory(int id, EntityGenericCar car, Inventory playerInventory) {
        super(Main.CAR_INVENTORY_CONTAINER_TYPE.get(), id, playerInventory, car.getExternalInventory());
        this.car = car;

        int rows = getRows();

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < 9; y++) {
                addSlot(new Slot(car.getExternalInventory(), y + x * 9, 8 + y * 18, 18 + x * 18));
            }
        }

        addPlayerInventorySlots();
    }

    public int getRows() {
        return car.getExternalInventory().getContainerSize() / 9;
    }

    @Override
    public int getInvOffset() {
        return getRows() != 3 ? 56 : 0;
    }

    public EntityGenericCar getCar() {
        return car;
    }

}
