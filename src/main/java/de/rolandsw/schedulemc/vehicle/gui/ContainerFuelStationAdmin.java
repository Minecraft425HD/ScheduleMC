package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.tileentity.TileEntityFuelStation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ContainerFuelStationAdmin extends ContainerBase {

    private TileEntityFuelStation fuelStation;

    public ContainerFuelStationAdmin(int id, TileEntityFuelStation fuelStation, Inventory playerInv) {
        super(Main.FUEL_STATION_ADMIN_CONTAINER_TYPE.get(), id, playerInv, null);
        this.fuelStation = fuelStation;
        addSlot(new Slot(fuelStation.getTradingInventory(), 0, 26, 22));

        for (int j = 0; j < 3; j++) {
            for (int k = 0; k < 9; k++) {
                addSlot(new Slot(fuelStation.getInventory(), k + j * 9, 8 + k * 18, 49 + j * 18));
            }
        }

        addPlayerInventorySlots();
    }

    public TileEntityFuelStation getFuelStation() {
        return fuelStation;
    }

    @Override
    public int getInvOffset() {
        return 31;
    }

}
