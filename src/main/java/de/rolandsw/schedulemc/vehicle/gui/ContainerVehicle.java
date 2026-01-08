package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ContainerVehicle extends ContainerBase {

    protected EntityGenericVehicle vehicle;

    public ContainerVehicle(int id, EntityGenericVehicle vehicle, Inventory playerInv) {
        super(Main.VEHICLE_CONTAINER_TYPE.get(), id, playerInv, vehicle);
        this.vehicle = vehicle;

        int numRows = vehicle.getContainerSize() / 9;

        for (int j = 0; j < numRows; j++) {
            for (int k = 0; k < 9; k++) {
                addSlot(new Slot(vehicle, k + j * 9, 8 + k * 18, 98 + j * 18));
            }
        }

        addSlot(new SlotFuel(vehicle, 0, 98, 66, playerInv.player));

        addSlot(new SlotBattery(vehicle, 0, 116, 66, playerInv.player));

        addSlot(new SlotRepairKit(vehicle, 0, 134, 66, playerInv.player));

        // Player inventory removed - not needed for vehicle GUI
    }

    public EntityGenericVehicle getVehicle() {
        return vehicle;
    }

}
