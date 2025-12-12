package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.tileentity.TileEntityGasStation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ContainerGasStation extends ContainerBase {

    private TileEntityGasStation gasStation;

    public ContainerGasStation(int id, TileEntityGasStation gasStation, Inventory playerInv) {
        super(Main.GAS_STATION_CONTAINER_TYPE.get(), id, playerInv, null);
        this.gasStation = gasStation;

        addSlot(new SlotPresent(gasStation.getTradingInventory().getItem(0), 18, 99));
        addSlot(new Slot(gasStation.getTradingInventory(), 1, 38, 99));

        addPlayerInventorySlots();
    }

    public TileEntityGasStation getGasStation() {
        return gasStation;
    }

    @Override
    public int getInvOffset() {
        return 51;
    }

}
