package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.tileentity.TileEntityFuelStation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ContainerFuelStation extends ContainerBase {

    private TileEntityFuelStation fuelStation;

    public ContainerFuelStation(int id, TileEntityFuelStation fuelStation, Inventory playerInv) {
        super(Main.FUEL_STATION_CONTAINER_TYPE.get(), id, playerInv, null);
        this.fuelStation = fuelStation;

        addSlot(new SlotPresent(fuelStation.getTradingInventory().getItem(0), 18, 99));
        addSlot(new Slot(fuelStation.getTradingInventory(), 1, 38, 99));

        addPlayerInventorySlots();
    }

    public TileEntityFuelStation getFuelStation() {
        return fuelStation;
    }

    @Override
    public int getInvOffset() {
        return 51;
    }

}
