package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.tileentity.TileEntityGarage;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class ContainerGarage extends ContainerBase {

    private final EntityGenericVehicle vehicle;
    private final TileEntityGarage garage;

    // Server-side constructor
    public ContainerGarage(int id, EntityGenericVehicle vehicle, TileEntityGarage garage, Inventory playerInv) {
        super(Main.GARAGE_CONTAINER_TYPE.get(), id, playerInv, null);
        this.vehicle = vehicle;
        this.garage = garage;

        // Add garage data slots
        addDataSlots(garage.getFields());

        // Add player inventory slots
        addPlayerInventorySlots();
    }

    // Client-side constructor
    public ContainerGarage(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        super(Main.GARAGE_CONTAINER_TYPE.get(), id, playerInv, null);

        // Read block position (from default TileEntityContainerProvider)
        extraData.readBlockPos();

        // Read vehicle UUID
        UUID vehicleUUID = extraData.readUUID();

        // Find vehicle in client world
        Level level = playerInv.player.level();
        this.vehicle = findVehicleByUUID(level, vehicleUUID);

        // Get garage tile entity
        this.garage = null; // Garage reference not needed on client

        // Add player inventory slots
        addPlayerInventorySlots();
    }

    @Nullable
    private EntityGenericVehicle findVehicleByUUID(Level level, UUID uuid) {
        // Iterate through all entities in the level
        Iterable<Entity> entities = level.getAllEntities();
        for (Entity entity : entities) {
            if (entity instanceof EntityGenericVehicle genericVehicle) {
                if (genericVehicle.getUUID().equals(uuid)) {
                    return genericVehicle;
                }
            }
        }
        return null;
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
