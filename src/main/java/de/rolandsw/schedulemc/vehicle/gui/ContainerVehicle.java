package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ContainerVehicle extends ContainerBase {

    protected EntityGenericVehicle vehicle;
    private int invOffset;

    public ContainerVehicle(int id, EntityGenericVehicle vehicle, Inventory playerInv) {
        super(Main.VEHICLE_CONTAINER_TYPE.get(), id, playerInv, vehicle);
        this.vehicle = vehicle;

        // CRITICAL: Ensure inventories have correct sizes BEFORE adding slots!
        // Use synced sizes from entityData (guaranteed to be in sync between client/server)
        int internalSlots = vehicle.getSyncedInternalInventorySize();
        int externalSlots = vehicle.getSyncedExternalInventorySize();

        // Resize inventories if needed (ensures inventory size matches synced size)
        vehicle.getInventoryComponent().setInternalInventorySize(internalSlots);
        vehicle.getInventoryComponent().setExternalInventorySize(externalSlots);

        int slotY = 98; // Start Y position for inventory slots

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
            slotY += internalRows * 18 + 2; // Move down for external slots
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

        // Add special slots (Fuel, Battery, Repair Kit) - always at fixed position
        addSlot(new SlotFuel(vehicle, 0, 98, 66, playerInv.player));
        addSlot(new SlotBattery(vehicle, 0, 116, 66, playerInv.player));
        addSlot(new SlotRepairKit(vehicle, 0, 134, 66, playerInv.player));

        // Calculate offset so player inventory appears below vehicle content
        int invHeight = internalRows * 18;
        if (internalRows > 0 && externalRows > 0) invHeight += 2;
        invHeight += externalRows * 18;
        this.invOffset = invHeight + 28;

        addPlayerInventorySlots();
    }

    @Override
    public int getInvOffset() {
        return invOffset;
    }

    public EntityGenericVehicle getVehicle() {
        return vehicle;
    }

}
