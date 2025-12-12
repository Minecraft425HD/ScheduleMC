package de.rolandsw.schedulemc.vehicle.entity.vehicle;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Factory class for creating vehicle entities from parts
 */
public class VehicleFactory {

    /**
     * Creates a vehicle entity from a list of part ItemStacks
     *
     * @param world The world to create the vehicle in
     * @param parts List of ItemStacks representing vehicle parts
     * @return The created EntityGenericVehicle, or null if creation failed
     */
    @Nullable
    public static EntityGenericVehicle createCar(Level world, List<ItemStack> parts) {
        if (world == null || parts == null || parts.isEmpty()) {
            return null;
        }

        EntityGenericVehicle vehicle = new EntityGenericVehicle(world);

        // Add all parts to the vehicle's part inventory
        for (int i = 0; i < parts.size() && i < 15; i++) {
            ItemStack stack = parts.get(i);
            if (!stack.isEmpty()) {
                vehicle.getPartInventory().setItem(i, stack.copy());
            }
        }

        // Synchronize the parts
        vehicle.setPartSerializer();
        vehicle.initParts();

        return vehicle;
    }
}
