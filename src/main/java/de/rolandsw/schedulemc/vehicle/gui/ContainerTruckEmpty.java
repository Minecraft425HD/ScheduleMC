package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.world.entity.player.Inventory;

/**
 * Container for Truck vehicle without container - 0 slots
 */
public class ContainerTruckEmpty extends ContainerBase {

    private EntityGenericVehicle vehicle;

    public ContainerTruckEmpty(int id, EntityGenericVehicle vehicle, Inventory playerInventory) {
        super(Main.VEHICLE_INVENTORY_CONTAINER_TYPE.get(), id, playerInventory, vehicle.getInternalInventory());
        this.vehicle = vehicle;

        // No vehicle inventory slots - truck has 0 internal slots without container

        addPlayerInventorySlots();
    }

    public int getRows() {
        return 0; // Fixed: 0 rows for empty truck
    }

    @Override
    public int getInvOffset() {
        return 56; // Offset for 0 rows
    }

    public EntityGenericVehicle getVehicle() {
        return vehicle;
    }
}
