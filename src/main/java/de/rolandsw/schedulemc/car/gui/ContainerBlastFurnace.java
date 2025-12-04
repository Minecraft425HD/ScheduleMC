package de.rolandsw.schedulemc.car.gui;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.blocks.tileentity.TileEntityBlastFurnace;
import net.minecraft.world.entity.player.Inventory;

public class ContainerBlastFurnace extends ContainerEnergyFluidProducer {

    public ContainerBlastFurnace(int id, TileEntityBlastFurnace tile, Inventory playerInv) {
        super(Main.BLAST_FURNACE_CONTAINER_TYPE.get(), id, tile, playerInv);
    }
}
