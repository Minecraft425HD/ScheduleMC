package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Container for Limousine vehicle inventory - 4 internal slots
 */
public class ContainerLimousineInventory extends ContainerBase {

    private EntityGenericVehicle vehicle;

    public ContainerLimousineInventory(int id, EntityGenericVehicle vehicle, Inventory playerInventory) {
        super(Main.LIMOUSINE_INVENTORY_CONTAINER_TYPE.get(), id, playerInventory, vehicle.getInternalInventory());
        this.vehicle = vehicle;

        // Add 4 internal inventory slots (partial row)
        for (int i = 0; i < 4; i++) {
            addSlot(new Slot(vehicle.getInternalInventory(), i, 8 + i * 18, 18));
        }

        addPlayerInventorySlots();
    }

    public int getRows() {
        return 1; // Fixed: 1 row for limousine
    }

    @Override
    public int getInvOffset() {
        return 56; // Offset for 1 partial row
    }

    public EntityGenericVehicle getVehicle() {
        return vehicle;
    }
}
