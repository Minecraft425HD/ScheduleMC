package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ContainerVehicleInventory extends ContainerBase {

    private EntityGenericVehicle vehicle;

    public ContainerVehicleInventory(int id, EntityGenericVehicle vehicle, Inventory playerInventory) {
        super(Main.VEHICLE_INVENTORY_CONTAINER_TYPE.get(), id, playerInventory, vehicle.getExternalInventory());
        this.vehicle = vehicle;

        int rows = getRows();

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < 9; y++) {
                addSlot(new Slot(vehicle.getExternalInventory(), y + x * 9, 8 + y * 18, 18 + x * 18));
            }
        }

        addPlayerInventorySlots();
    }

    public int getRows() {
        return vehicle.getExternalInventory().getContainerSize() / 9;
    }

    @Override
    public int getInvOffset() {
        return getRows() != 3 ? 56 : 0;
    }

    public EntityGenericVehicle getVehicle() {
        return vehicle;
    }

}
