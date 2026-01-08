package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.Part;
import net.minecraft.world.item.ItemStack;

/**
 * Interface for items that represent vehicle parts (engines, wheels, batteries, etc.).
 * <p>
 * Implementing this interface allows an item to be recognized as a vehicle component
 * that can be installed, upgraded, or removed from vehicles. The item must be able
 * to provide its corresponding Part instance from an ItemStack.
 * </p>
 * <p>
 * <strong>Implementation Note:</strong> Most vehicle part items should extend
 * {@link AbstractItemVehiclePart} rather than implementing this interface directly.
 * </p>
 *
 * @see Part
 * @see AbstractItemVehiclePart
 * @see ItemVehiclePart
 */
public interface IVehiclePart {

    /**
     * Extracts the vehicle part data from the given ItemStack.
     * <p>
     * This method is called when a vehicle part item needs to be installed on
     * a vehicle or when its properties need to be accessed.
     * </p>
     *
     * @param stack the ItemStack containing the vehicle part item
     * @return the Part instance representing this vehicle component, or null if invalid
     */
    Part getPart(ItemStack stack);

}
