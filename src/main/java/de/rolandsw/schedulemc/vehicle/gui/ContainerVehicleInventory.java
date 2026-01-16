package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ContainerVehicleInventory extends ContainerBase {

    private EntityGenericVehicle vehicle;
    private int internalSlots;
    private int externalSlots;

    public ContainerVehicleInventory(int id, EntityGenericVehicle vehicle, Inventory playerInventory) {
        super(Main.VEHICLE_INVENTORY_CONTAINER_TYPE.get(), id, playerInventory, vehicle.getInternalInventory());
        this.vehicle = vehicle;
        this.internalSlots = vehicle.getInternalInventory().getContainerSize();
        this.externalSlots = vehicle.getExternalInventory().getContainerSize();

        int slotIndex = 0;
        int yOffset = 18;

        // Add INTERNAL inventory slots (chassis-specific: 4/6/0/3/6 slots)
        if (internalSlots > 0) {
            int internalRows = (int) Math.ceil(internalSlots / 9.0);
            for (int row = 0; row < internalRows; row++) {
                for (int col = 0; col < 9; col++) {
                    if (slotIndex < internalSlots) {
                        addSlot(new Slot(vehicle.getInternalInventory(), slotIndex, 8 + col * 18, yOffset + row * 18));
                        slotIndex++;
                    }
                }
            }
            yOffset += internalRows * 18 + 4; // 4px spacing between internal and external
        }

        // Add EXTERNAL inventory slots (container: 0 or 12 slots)
        if (externalSlots > 0) {
            slotIndex = 0;
            int externalRows = (int) Math.ceil(externalSlots / 9.0);
            for (int row = 0; row < externalRows; row++) {
                for (int col = 0; col < 9; col++) {
                    if (slotIndex < externalSlots) {
                        addSlot(new Slot(vehicle.getExternalInventory(), slotIndex, 8 + col * 18, yOffset + row * 18));
                        slotIndex++;
                    }
                }
            }
        }

        addPlayerInventorySlots();
    }

    public int getRows() {
        // Calculate total rows needed for both internal and external inventories
        int internalRows = internalSlots > 0 ? (int) Math.ceil(internalSlots / 9.0) : 0;
        int externalRows = externalSlots > 0 ? (int) Math.ceil(externalSlots / 9.0) : 0;
        return internalRows + externalRows;
    }

    @Override
    public int getInvOffset() {
        int rows = getRows();
        return rows != 3 ? 56 : 0;
    }

    public EntityGenericVehicle getVehicle() {
        return vehicle;
    }

    public int getInternalSlots() {
        return internalSlots;
    }

    public int getExternalSlots() {
        return externalSlots;
    }

}
