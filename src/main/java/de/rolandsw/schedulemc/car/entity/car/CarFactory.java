package de.rolandsw.schedulemc.car.entity.car;

import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Factory class for creating car entities from parts
 */
public class CarFactory {

    /**
     * Creates a car entity from a list of part ItemStacks
     *
     * @param world The world to create the car in
     * @param parts List of ItemStacks representing car parts
     * @return The created EntityGenericCar, or null if creation failed
     */
    @Nullable
    public static EntityGenericCar createCar(Level world, List<ItemStack> parts) {
        if (world == null || parts == null || parts.isEmpty()) {
            return null;
        }

        EntityGenericCar car = new EntityGenericCar(world);

        // Add all parts to the car's part inventory
        for (int i = 0; i < parts.size() && i < 15; i++) {
            ItemStack stack = parts.get(i);
            if (!stack.isEmpty()) {
                car.getPartInventory().setItem(i, stack.copy());
            }
        }

        // Synchronize the parts
        car.setPartSerializer();
        car.initParts();

        return car;
    }
}
