package de.rolandsw.schedulemc.vehicle.core.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * Base interface for all vehicle components in the ECS architecture.
 * Components are pure data containers with minimal logic.
 */
public interface IVehicleComponent {

    /**
     * Gets the unique type identifier for this component.
     * @return ResourceLocation identifying the component type
     */
    ResourceLocation getComponentType();

    /**
     * Serializes component data to NBT for saving/networking.
     * @param tag The compound tag to write to
     */
    void writeToNbt(CompoundTag tag);

    /**
     * Deserializes component data from NBT.
     * @param tag The compound tag to read from
     */
    void readFromNbt(CompoundTag tag);

    /**
     * Creates a deep copy of this component.
     * @return A new instance with copied data
     */
    IVehicleComponent duplicate();

    /**
     * Validates if this component's current state is valid.
     * @return true if valid, false otherwise
     */
    default boolean isValid() {
        return true;
    }

    /**
     * Called when this component is added to a vehicle.
     */
    default void onAttached() {}

    /**
     * Called when this component is removed from a vehicle.
     */
    default void onDetached() {}
}
