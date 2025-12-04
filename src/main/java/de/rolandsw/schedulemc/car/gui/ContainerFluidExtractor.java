package de.rolandsw.schedulemc.car.gui;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.blocks.tileentity.TileEntityFluidExtractor;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;

public class ContainerFluidExtractor extends ContainerBase {

    protected TileEntityFluidExtractor tile;
    protected Inventory playerInventory;

    public ContainerFluidExtractor(int id, TileEntityFluidExtractor tile, Inventory playerInventory) {
        super(Main.FLUID_EXTRACTOR_CONTAINER_TYPE.get(), id, playerInventory, new SimpleContainer(1));
        this.tile = tile;
        this.playerInventory = playerInventory;

        addSlot(new SlotFluidFilter(inventory, 0, 26, 25, tile));

        addPlayerInventorySlots();
    }

    public TileEntityFluidExtractor getTile() {
        return tile;
    }

    @Override
    public int getInvOffset() {
        return -27;
    }

}
