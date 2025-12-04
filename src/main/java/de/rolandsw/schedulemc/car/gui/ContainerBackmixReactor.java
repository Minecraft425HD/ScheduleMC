package de.rolandsw.schedulemc.car.gui;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.blocks.tileentity.TileEntityBackmixReactor;
import net.minecraft.world.entity.player.Inventory;

public class ContainerBackmixReactor extends ContainerBase {

    private TileEntityBackmixReactor backmixReactor;

    public ContainerBackmixReactor(int id, TileEntityBackmixReactor backmixReactor, Inventory playerInv) {
        super(Main.BACKMIX_REACTOR_CONTAINER_TYPE.get(), id, playerInv, backmixReactor);
        this.backmixReactor = backmixReactor;
        addPlayerInventorySlots();
    }

    public TileEntityBackmixReactor getBackmixReactor() {
        return backmixReactor;
    }

}
