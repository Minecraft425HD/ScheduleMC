package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.vehicle.IVehicleAPI;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.VehicleFactory;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartRegistry;
import de.rolandsw.schedulemc.vehicle.items.InternalVehiclePartItem;
import de.rolandsw.schedulemc.vehicle.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Implementation of IVehicleAPI
 *
 * Wrapper für Vehicle-System mit vollständiger Thread-Safety.
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public class VehicleAPIImpl implements IVehicleAPI {

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public EntityGenericVehicle spawnVehicle(ServerLevel level, BlockPos position, String vehicleType) {
        if (level == null || position == null || vehicleType == null) {
            throw new IllegalArgumentException("level, position and vehicleType cannot be null");
        }
        List<ItemStack> parts = new ArrayList<>();
        switch (vehicleType.toLowerCase()) {
            case "limousine" -> {
                parts.add(InternalVehiclePartItem.create(PartRegistry.LIMOUSINE_CHASSIS));
                parts.add(InternalVehiclePartItem.create(PartRegistry.NORMAL_MOTOR));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(InternalVehiclePartItem.create(PartRegistry.FENDER_BASIC));
            }
            case "van" -> {
                parts.add(InternalVehiclePartItem.create(PartRegistry.VAN_CHASSIS));
                parts.add(InternalVehiclePartItem.create(PartRegistry.NORMAL_MOTOR));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(InternalVehiclePartItem.create(PartRegistry.FENDER_BASIC));
            }
            case "truck" -> {
                parts.add(InternalVehiclePartItem.create(PartRegistry.TRUCK_CHASSIS));
                parts.add(InternalVehiclePartItem.create(PartRegistry.NORMAL_MOTOR));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            }
            case "suv" -> {
                parts.add(InternalVehiclePartItem.create(PartRegistry.OFFROAD_CHASSIS));
                parts.add(InternalVehiclePartItem.create(PartRegistry.NORMAL_MOTOR));
                parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
                parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
                parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
                parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
                parts.add(InternalVehiclePartItem.create(PartRegistry.FENDER_BASIC));
            }
            case "sports_car" -> {
                parts.add(InternalVehiclePartItem.create(PartRegistry.LUXUS_CHASSIS));
                parts.add(InternalVehiclePartItem.create(PartRegistry.NORMAL_MOTOR));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
                parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            }
            default -> { return null; }
        }
        parts.add(InternalVehiclePartItem.create(PartRegistry.TANK_15L));
        parts.add(InternalVehiclePartItem.create(PartRegistry.LICENSE_PLATE_HOLDER));

        EntityGenericVehicle vehicle = VehicleFactory.createVehicle(level, parts);
        if (vehicle == null) return null;

        vehicle.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        vehicle.setFuelAmount(vehicle.getMaxFuel() / 4);
        vehicle.setBatteryLevel(500);
        level.addFreshEntity(vehicle);
        vehicle.setIsSpawned(true);
        return vehicle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVehicleOwner(EntityGenericVehicle vehicle, UUID ownerUUID) {
        if (vehicle == null || ownerUUID == null) {
            throw new IllegalArgumentException("vehicle and ownerUUID cannot be null");
        }
        vehicle.setOwnerId(ownerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public UUID getVehicleOwner(EntityGenericVehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle cannot be null");
        }
        return vehicle.getOwnerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean refuelVehicle(EntityGenericVehicle vehicle, double amount) {
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle cannot be null");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive, got: " + amount);
        }

        double currentFuel = getFuelLevel(vehicle);
        double capacity = getFuelCapacity(vehicle);

        if (currentFuel >= capacity) {
            return false; // Tank already full
        }

        double newFuel = Math.min(currentFuel + amount, capacity);
        vehicle.setFuelAmount((int) newFuel);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getFuelLevel(EntityGenericVehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle cannot be null");
        }
        return vehicle.getFuelAmount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getFuelCapacity(EntityGenericVehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle cannot be null");
        }
        return vehicle.getMaxFuel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntityGenericVehicle> getPlayerVehicles(ServerLevel level, UUID ownerUUID) {
        if (level == null || ownerUUID == null) {
            throw new IllegalArgumentException("level and ownerUUID cannot be null");
        }

        return Collections.unmodifiableList(
            StreamSupport.stream(level.getAllEntities().spliterator(), false)
                .filter(entity -> entity instanceof EntityGenericVehicle)
                .map(entity -> (EntityGenericVehicle) entity)
                .filter(vehicle -> ownerUUID.equals(vehicle.getOwnerId()))
                .collect(Collectors.toList())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeVehicle(EntityGenericVehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle cannot be null");
        }
        vehicle.discard();
    }

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntityGenericVehicle> getAllVehicles(ServerLevel level) {
        if (level == null) {
            throw new IllegalArgumentException("level cannot be null");
        }
        return Collections.unmodifiableList(
            StreamSupport.stream(level.getAllEntities().spliterator(), false)
                .filter(entity -> entity instanceof EntityGenericVehicle)
                .map(entity -> (EntityGenericVehicle) entity)
                .collect(Collectors.toList())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getVehicleCount(ServerLevel level) {
        if (level == null) {
            throw new IllegalArgumentException("level cannot be null");
        }
        return getAllVehicles(level).size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVehicleOwner(EntityGenericVehicle vehicle, UUID playerUUID) {
        if (vehicle == null || playerUUID == null) {
            throw new IllegalArgumentException("vehicle and playerUUID cannot be null");
        }
        return playerUUID.equals(vehicle.getOwnerId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void repairVehicle(EntityGenericVehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle cannot be null");
        }
        vehicle.getDamageComponent().setDamage(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getVehicleSpeed(EntityGenericVehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle cannot be null");
        }
        return vehicle.getDeltaMovement().length();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLicensePlate(EntityGenericVehicle vehicle, String plate) {
        if (vehicle == null || plate == null) {
            throw new IllegalArgumentException("vehicle and plate cannot be null");
        }
        vehicle.setLicensePlate(plate);
    }
}
