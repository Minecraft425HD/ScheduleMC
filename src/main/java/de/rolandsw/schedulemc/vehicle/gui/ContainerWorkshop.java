package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.tileentity.TileEntityWorkshop;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class ContainerWorkshop extends ContainerBase {

    private final EntityGenericVehicle vehicle;
    private final TileEntityWorkshop workshop;

    // Server-side constructor
    public ContainerWorkshop(int id, EntityGenericVehicle vehicle, TileEntityWorkshop workshop, Inventory playerInv) {
        super(Main.WORKSHOP_CONTAINER_TYPE.get(), id, playerInv, null);
        this.vehicle = vehicle;
        this.workshop = workshop;

        // Add workshop data slots
        addDataSlots(workshop.getFields());

        // No player inventory slots needed in workshop GUI
    }

    // Client-side constructor
    public ContainerWorkshop(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        super(Main.WORKSHOP_CONTAINER_TYPE.get(), id, playerInv, null);

        // Read block position (from default TileEntityContainerProvider)
        extraData.readBlockPos();

        // Read vehicle UUID
        UUID vehicleUUID = extraData.readUUID();

        // Find vehicle in client world
        Level level = playerInv.player.level();
        this.vehicle = findVehicleByUUID(level, vehicleUUID);

        // Get workshop tile entity
        this.workshop = null; // Workshop reference not needed on client

        // Add dummy data slots to match server-side (1 slot for isActive)
        addDataSlots(new net.minecraft.world.inventory.SimpleContainerData(1));

        // No player inventory slots needed in workshop GUI
    }

    @Nullable
    private EntityGenericVehicle findVehicleByUUID(Level level, UUID uuid) {
        // Search in a large area around the player for the vehicle
        // Use a large AABB to ensure we find the vehicle
        net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(
            -30000, -500, -30000,
            30000, 500, 30000
        );

        return level.getEntitiesOfClass(EntityGenericVehicle.class, searchBox).stream()
            .filter(v -> v.getUUID().equals(uuid))
            .findFirst()
            .orElse(null);
    }

    public EntityGenericVehicle getVehicle() {
        return vehicle;
    }

    public TileEntityWorkshop getWorkshop() {
        return workshop;
    }

    @Override
    public int getInvOffset() {
        return 0; // No inventory in workshop GUI
    }
}
