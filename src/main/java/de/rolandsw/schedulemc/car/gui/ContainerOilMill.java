package de.rolandsw.schedulemc.car.gui;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.blocks.tileentity.TileEntityEnergyFluidProducer;
import net.minecraft.world.entity.player.Inventory;

public class ContainerOilMill extends ContainerEnergyFluidProducer {

    public ContainerOilMill(int id, TileEntityEnergyFluidProducer tile, Inventory playerInv) {
        super(Main.OIL_MILL_CONTAINER_TYPE.get(), id, tile, playerInv);
    }

}
