package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.Part;
import net.minecraft.world.item.ItemStack;

public interface IVehiclePart {

    Part getPart(ItemStack stack);

}
