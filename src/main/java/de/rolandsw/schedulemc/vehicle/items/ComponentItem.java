package de.rolandsw.schedulemc.vehicle.items;

import net.minecraft.world.item.Item;

/**
 * Item representing a vehicle component (engine, wheel, body, etc.).
 * Used for crafting and modifying vehicles.
 */
public class ComponentItem extends Item {

    public enum ComponentItemType {
        ENGINE,
        WHEELS,
        BODY,
        FUEL_TANK,
        BATTERY
    }

    private final ComponentItemType componentType;
    private final String specificationId;

    public ComponentItem(ComponentItemType type, String specId, Properties properties) {
        super(properties);
        this.componentType = type;
        this.specificationId = specId;
    }

    public ComponentItemType getComponentType() {
        return componentType;
    }

    public String getSpecificationId() {
        return specificationId;
    }
}
