package de.rolandsw.schedulemc.car.items;

import de.rolandsw.schedulemc.car.entity.car.parts.Part;
import net.minecraft.world.item.ItemStack;

public interface ICarPart {

    Part getPart(ItemStack stack);

}
