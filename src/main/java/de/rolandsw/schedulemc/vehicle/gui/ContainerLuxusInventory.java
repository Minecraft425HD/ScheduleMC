package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Container for Luxus vehicle inventory - 3 internal slots
 */
public class ContainerLuxusInventory extends ContainerBase {

    private EntityGenericVehicle vehicle;

    public ContainerLuxusInventory(int id, EntityGenericVehicle vehicle, Inventory playerInventory) {
        super(Main.VEHICLE_INVENTORY_CONTAINER_TYPE.get(), id, playerInventory, vehicle.getInternalInventory());
        this.vehicle = vehicle;

        // Add 3 internal inventory slots (partial row)
        for (int i = 0; i < 3; i++) {
            addSlot(new Slot(vehicle.getInternalInventory(), i, 8 + i * 18, 18));
        }

        addPlayerInventorySlots();
    }

    public int getRows() {
        return 1; // Fixed: 1 row for luxus
    }

    @Override
    public int getInvOffset() {
        return 56; // Offset for 1 partial row
    }

    public EntityGenericVehicle getVehicle() {
        return vehicle;
    }
}
