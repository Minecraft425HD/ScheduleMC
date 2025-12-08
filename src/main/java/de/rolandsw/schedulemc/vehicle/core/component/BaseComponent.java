package de.rolandsw.schedulemc.vehicle.core.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * Abstract base implementation of IVehicleComponent.
 * Handles common functionality like type identification.
 */
public abstract class BaseComponent implements IVehicleComponent {

    private final ResourceLocation componentType;
    private boolean attached = false;

    protected BaseComponent(ResourceLocation componentType) {
        this.componentType = componentType;
    }

    @Override
    public ResourceLocation getComponentType() {
        return componentType;
    }

    @Override
    public void onAttached() {
        this.attached = true;
    }

    @Override
    public void onDetached() {
        this.attached = false;
    }

    public boolean isAttached() {
        return attached;
    }

    /**
     * Helper method to write component type to NBT.
     */
    protected void writeTypeToNbt(CompoundTag tag) {
        tag.putString("ComponentType", componentType.toString());
    }

    /**
     * Helper method to verify component type from NBT.
     */
    protected boolean verifyTypeFromNbt(CompoundTag tag) {
        if (!tag.contains("ComponentType")) {
            return false;
        }
        String typeStr = tag.getString("ComponentType");
        return componentType.toString().equals(typeStr);
    }
}
