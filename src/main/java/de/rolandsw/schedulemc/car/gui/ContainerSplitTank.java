package de.rolandsw.schedulemc.car.gui;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.blocks.tileentity.TileEntitySplitTank;
import net.minecraft.world.entity.player.Inventory;

public class ContainerSplitTank extends ContainerBase {

    private TileEntitySplitTank splitTank;

    public ContainerSplitTank(int id, TileEntitySplitTank splitTank, Inventory playerInv) {
        super(Main.SPLIT_TANK_CONTAINER_TYPE.get(), id, playerInv, splitTank);
        this.splitTank = splitTank;
        addPlayerInventorySlots();
    }


    public TileEntitySplitTank getSplitTank() {
        return splitTank;
    }

}
