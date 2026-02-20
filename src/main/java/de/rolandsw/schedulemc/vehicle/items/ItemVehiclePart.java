package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.Part;
import net.minecraft.world.item.ItemStack;

/**
 * Item for tire parts shown in the creative tab.
 * Stores the Part instance directly (not via NBT) since tires are distinct items.
 */
public class ItemVehiclePart extends AbstractItemVehiclePart {

    private final Part part;

    public ItemVehiclePart(Part part) {
        super();
        this.part = part;
    }

    @Override
    public Part getPart(ItemStack stack) {
        return part;
    }

}
