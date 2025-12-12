package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.Part;
import net.minecraft.world.item.ItemStack;

public class ItemVehiclePart extends AbstractItemVehiclePart {

    private final Part part;

    public ItemVehiclePart(Part part) {
        this.part = part;
    }

    @Override
    public Part getPart(ItemStack stack) {
        return part;
    }
}
