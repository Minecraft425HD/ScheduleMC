package de.rolandsw.schedulemc.vehicle.events;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * Handles player login/logout while sitting in a vehicle.
 *
 * On logout: dismounts the player and saves the vehicle UUID.
 * On login: searches for the vehicle nearby and remounts the player,
 * or lets them spawn at their saved position if the vehicle is gone.
 */
public class VehicleSessionHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_VEHICLE_UUID = "LastRiddenVehicleUUID";
    private static final double SEARCH_RADIUS = 100.0;

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof EntityGenericVehicle genericVehicle)) return;

        // Save vehicle UUID so we can find it on rejoin
        CompoundTag data = player.getPersistentData();
        data.putUUID(TAG_VEHICLE_UUID, genericVehicle.getUUID());

        // Dismount player safely - this prevents vanilla from saving
        // the player's position inside the vehicle bounding box
        player.stopRiding();

        LOGGER.debug("[VehicleSession] Player {} logged out while in vehicle {}, saved UUID for rejoin",
                player.getName().getString(), genericVehicle.getUUID());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag data = player.getPersistentData();
        if (!data.hasUUID(TAG_VEHICLE_UUID)) return;

        UUID vehicleUUID = data.getUUID(TAG_VEHICLE_UUID);
        data.remove(TAG_VEHICLE_UUID);

        // Search for the vehicle in the world
        Vec3 playerPos = player.position();
        AABB searchBox = new AABB(
                playerPos.subtract(SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS),
                playerPos.add(SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS)
        );

        EntityGenericVehicle vehicle = player.serverLevel().getEntitiesOfClass(
                EntityGenericVehicle.class,
                searchBox,
                v -> v.getUUID().equals(vehicleUUID)
        ).stream().findFirst().orElse(null);

        if (vehicle == null) {
            LOGGER.debug("[VehicleSession] Player {} rejoined but vehicle {} not found nearby",
                    player.getName().getString(), vehicleUUID);
            return;
        }

        // Teleport player to the vehicle and remount
        player.teleportTo(vehicle.getX(), vehicle.getY(), vehicle.getZ());
        player.startRiding(vehicle);

        LOGGER.debug("[VehicleSession] Player {} rejoined and remounted vehicle {} at ({}, {}, {})",
                player.getName().getString(), vehicleUUID,
                (int) vehicle.getX(), (int) vehicle.getY(), (int) vehicle.getZ());
    }
}
