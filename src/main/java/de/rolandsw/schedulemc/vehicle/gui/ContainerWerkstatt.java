package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.tileentity.TileEntityWerkstatt;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class ContainerWerkstatt extends ContainerBase {

    private final EntityGenericVehicle vehicle;
    private final TileEntityWerkstatt werkstatt;

    // Server-side constructor
    public ContainerWerkstatt(int id, EntityGenericVehicle vehicle, TileEntityWerkstatt werkstatt, Inventory playerInv) {
        super(Main.WERKSTATT_CONTAINER_TYPE.get(), id, playerInv, null);
        this.vehicle = vehicle;
        this.werkstatt = werkstatt;

        // Add werkstatt data slots
        addDataSlots(werkstatt.getFields());

        // No player inventory slots needed in werkstatt GUI
    }

    // Client-side constructor
    public ContainerWerkstatt(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        super(Main.WERKSTATT_CONTAINER_TYPE.get(), id, playerInv, null);

        // Read block position (from default TileEntityContainerProvider)
        extraData.readBlockPos();

        // Read vehicle UUID
        UUID vehicleUUID = extraData.readUUID();

        // Find vehicle in client world
        Level level = playerInv.player.level();
        this.vehicle = findVehicleByUUID(level, vehicleUUID);

        // Get werkstatt tile entity
        this.werkstatt = null; // Werkstatt reference not needed on client

        // Add dummy data slots to match server-side (1 slot for isActive)
        addDataSlots(new net.minecraft.world.inventory.SimpleContainerData(1));

        // No player inventory slots needed in werkstatt GUI
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

    public TileEntityWerkstatt getWerkstatt() {
        return werkstatt;
    }

    @Override
    public int getInvOffset() {
        return 0; // No inventory in werkstatt GUI
    }
}
