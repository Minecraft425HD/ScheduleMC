package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.tileentity.TileEntityGarage;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.world.entity.player.Inventory;

public class ContainerGarage extends ContainerBase {

    private final EntityGenericVehicle vehicle;
    private final TileEntityGarage garage;

    public ContainerGarage(int id, EntityGenericVehicle vehicle, TileEntityGarage garage, Inventory playerInv) {
        super(Main.GARAGE_CONTAINER_TYPE.get(), id, playerInv, null);
        this.vehicle = vehicle;
        this.garage = garage;

        // Add garage data slots
        addDataSlots(garage.getFields());

        // Add player inventory slots
        addPlayerInventorySlots();
    }

    public EntityGenericVehicle getVehicle() {
        return vehicle;
    }

    public TileEntityGarage getGarage() {
        return garage;
    }

    @Override
    public int getInvOffset() {
        return 84; // Offset for player inventory in GUI
    }
}
