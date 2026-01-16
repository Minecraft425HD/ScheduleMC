package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Container for Truck vehicle with item container - 12 external slots
 */
public class ContainerTruckWithContainer extends ContainerBase {

    private EntityGenericVehicle vehicle;

    public ContainerTruckWithContainer(int id, EntityGenericVehicle vehicle, Inventory playerInventory) {
        super(Main.VEHICLE_INVENTORY_CONTAINER_TYPE.get(), id, playerInventory, vehicle.getExternalInventory());
        this.vehicle = vehicle;

        // Add 12 external inventory slots (2 full rows from container)
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 6; col++) {
                int slotIndex = col + row * 6;
                addSlot(new Slot(vehicle.getExternalInventory(), slotIndex, 8 + col * 18, 18 + row * 18));
            }
        }

        addPlayerInventorySlots();
    }

    public int getRows() {
        return 2; // Fixed: 2 full rows for truck container
    }

    @Override
    public int getInvOffset() {
        return 0; // No offset for 2 full rows
    }

    public EntityGenericVehicle getVehicle() {
        return vehicle;
    }
}
