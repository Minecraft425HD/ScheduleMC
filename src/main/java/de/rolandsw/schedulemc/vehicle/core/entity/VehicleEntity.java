package de.rolandsw.schedulemc.vehicle.core.entity;

import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import de.rolandsw.schedulemc.vehicle.core.registry.ComponentRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Core vehicle entity using ECS architecture.
 * This entity is just a container for components - all logic is in systems.
 */
public class VehicleEntity extends Entity {

    private static final Logger LOGGER = LogManager.getLogger();

    // Component storage
    private final Map<ResourceLocation, IVehicleComponent> components = new HashMap<>();

    // Synced data
    private static final EntityDataAccessor<String> VEHICLE_TYPE_ID =
        SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.STRING);

    private String vehicleTypeIdentifier = "default";

    public VehicleEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(VEHICLE_TYPE_ID, "default");
    }

    /**
     * Adds a component to this vehicle.
     */
    public void addComponent(IVehicleComponent component) {
        ResourceLocation type = component.getComponentType();

        if (components.containsKey(type)) {
            LOGGER.warn("Replacing existing component of type: {}", type);
            components.get(type).onDetached();
        }

        components.put(type, component);
        component.onAttached();
        LOGGER.debug("Added component {} to vehicle", type);
    }

    /**
     * Removes a component from this vehicle.
     */
    @Nullable
    public IVehicleComponent removeComponent(ResourceLocation type) {
        IVehicleComponent component = components.remove(type);
        if (component != null) {
            component.onDetached();
            LOGGER.debug("Removed component {} from vehicle", type);
        }
        return component;
    }

    /**
     * Gets a component by its type.
     */
    @Nullable
    public <T extends IVehicleComponent> T getComponent(ResourceLocation type, Class<T> componentClass) {
        IVehicleComponent component = components.get(type);
        if (component != null && componentClass.isInstance(component)) {
            return componentClass.cast(component);
        }
        return null;
    }

    /**
     * Checks if this vehicle has a component of the given type.
     */
    public boolean hasComponent(ResourceLocation type) {
        return components.containsKey(type);
    }

    /**
     * Gets all components attached to this vehicle.
     */
    public Collection<IVehicleComponent> getAllComponents() {
        return Collections.unmodifiableCollection(components.values());
    }

    /**
     * Gets all component types attached to this vehicle.
     */
    public Set<ResourceLocation> getComponentTypes() {
        return Collections.unmodifiableSet(components.keySet());
    }

    /**
     * Sets the vehicle type identifier (for registration/spawning).
     */
    public void setVehicleType(String identifier) {
        this.vehicleTypeIdentifier = identifier;
        this.entityData.set(VEHICLE_TYPE_ID, identifier);
    }

    /**
     * Gets the vehicle type identifier.
     */
    public String getVehicleType() {
        return this.entityData.get(VEHICLE_TYPE_ID);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // Read vehicle type
        if (tag.contains("VehicleType")) {
            this.vehicleTypeIdentifier = tag.getString("VehicleType");
            this.entityData.set(VEHICLE_TYPE_ID, vehicleTypeIdentifier);
        }

        // Read components
        if (tag.contains("Components", Tag.TAG_LIST)) {
            ListTag componentList = tag.getList("Components", Tag.TAG_COMPOUND);

            for (int i = 0; i < componentList.size(); i++) {
                CompoundTag componentTag = componentList.getCompound(i);
                IVehicleComponent component = ComponentRegistry.createFromNbt(componentTag);

                if (component != null) {
                    addComponent(component);
                } else {
                    LOGGER.warn("Failed to load component from NBT");
                }
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // Write vehicle type
        tag.putString("VehicleType", vehicleTypeIdentifier);

        // Write components
        ListTag componentList = new ListTag();
        for (IVehicleComponent component : components.values()) {
            CompoundTag componentTag = new CompoundTag();
            component.writeToNbt(componentTag);
            componentList.add(componentTag);
        }
        tag.put("Components", componentList);
    }

    @Override
    public void tick() {
        super.tick();
        // System ticking is handled by SystemManager, not here
    }
}
