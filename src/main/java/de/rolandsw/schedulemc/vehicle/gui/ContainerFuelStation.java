package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.tileentity.TileEntityFuelStation;
import net.minecraft.world.entity.player.Inventory;

public class ContainerFuelStation extends ContainerBase {

    private TileEntityFuelStation fuelStation;

    public ContainerFuelStation(int id, TileEntityFuelStation fuelStation, Inventory playerInv) {
        super(Main.FUEL_STATION_CONTAINER_TYPE.get(), id, playerInv, null);
        this.fuelStation = fuelStation;
        // No slots - fuel station GUI is display-only with START/STOP buttons
    }

    public TileEntityFuelStation getFuelStation() {
        return fuelStation;
    }

}
