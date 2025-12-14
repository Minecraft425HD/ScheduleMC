package de.rolandsw.schedulemc.vehicle.items;

import net.minecraft.world.item.Item;

public abstract class AbstractItemVehiclePart extends Item implements IVehiclePart {

    public AbstractItemVehiclePart() {
        super(new Item.Properties());
    }

}