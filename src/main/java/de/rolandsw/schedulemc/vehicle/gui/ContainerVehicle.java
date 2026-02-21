package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ContainerVehicle extends ContainerBase {

    protected EntityGenericVehicle vehicle;
    private int hotbarY;

    public ContainerVehicle(int id, EntityGenericVehicle vehicle, Inventory playerInv) {
        super(Main.VEHICLE_CONTAINER_TYPE.get(), id, playerInv, vehicle);
        this.vehicle = vehicle;

        // CRITICAL: Ensure inventories have correct sizes BEFORE adding slots!
        int internalSlots = vehicle.getSyncedInternalInventorySize();
        int externalSlots = vehicle.getSyncedExternalInventorySize();

        vehicle.getInventoryComponent().setInternalInventorySize(internalSlots);
        vehicle.getInventoryComponent().setExternalInventorySize(externalSlots);

        int slotY = 129; // Start Y position for inventory slots (shifted down for tire/season info in status card)

        // Add INTERNAL inventory slots (chassis-specific: 0-6 slots)
        int internalRows = 0;
        if (internalSlots > 0) {
            internalRows = (int) Math.ceil(internalSlots / 9.0);
            int slotIndex = 0;
            for (int row = 0; row < internalRows; row++) {
                for (int col = 0; col < 9; col++) {
                    if (slotIndex < internalSlots) {
                        addSlot(new Slot(vehicle.getInternalInventory(), slotIndex, 8 + col * 18, slotY + row * 18));
                        slotIndex++;
                    }
                }
            }
            slotY += internalRows * 18 + 2;
        }

        // Add EXTERNAL inventory slots (container: 0 or 12 slots)
        int externalRows = 0;
        if (externalSlots > 0) {
            externalRows = (int) Math.ceil(externalSlots / 9.0);
            int slotIndex = 0;
            for (int row = 0; row < externalRows; row++) {
                for (int col = 0; col < 9; col++) {
                    if (slotIndex < externalSlots) {
                        addSlot(new Slot(vehicle.getExternalInventory(), slotIndex, 8 + col * 18, slotY + row * 18));
                        slotIndex++;
                    }
                }
            }
        }

        // Add single maintenance slot (accepts fuel, battery, repair kit)
        addSlot(new SlotMaintenance(vehicle, 0, 150, 97, playerInv.player));

        // Calculate hotbar position below vehicle content
        int invHeight = internalRows * 18;
        if (internalRows > 0 && externalRows > 0) invHeight += 2;
        invHeight += externalRows * 18;
        this.hotbarY = 129 + invHeight + 16;

        // Add ONLY hotbar slots (player inventory slots 0-8)
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, hotbarY));
        }
    }

    public int getHotbarY() {
        return hotbarY;
    }

    public EntityGenericVehicle getVehicle() {
        return vehicle;
    }

}
