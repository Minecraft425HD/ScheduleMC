package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.Part;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Single internal item that represents any non-tire vehicle part.
 * Parts are identified by their string ID stored in NBT ("part_id").
 * This item is never shown in the creative tab and has no player-facing texture.
 */
public class InternalVehiclePartItem extends AbstractItemVehiclePart {

    private static final String TAG_PART_ID = "part_id";
    private static final String TAG_PLATE_TEXT = "plate_text";

    public InternalVehiclePartItem() {
        super();
    }

    @Override
    public Part getPart(ItemStack stack) {
        if (!stack.hasTag()) return null;
        String id = stack.getTag().getString(TAG_PART_ID);
        return PartRegistry.getById(id);
    }

    /**
     * Creates an ItemStack for the given Part, storing its ID in NBT.
     */
    public static ItemStack create(Part part) {
        String id = part.getId();
        if (id == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(ModItems.INTERNAL_VEHICLE_PART.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_PART_ID, id);
        return stack;
    }

    /**
     * Creates a license plate ItemStack with the given text stored in NBT.
     */
    public static ItemStack createLicensePlate(String plateText) {
        ItemStack stack = new ItemStack(ModItems.INTERNAL_VEHICLE_PART.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_PART_ID, "license_sign");
        tag.putString(TAG_PLATE_TEXT, plateText != null ? plateText : "");
        return stack;
    }

    /**
     * Reads the license plate text from an InternalVehiclePartItem stack.
     */
    public static String getPlateText(ItemStack stack) {
        if (!stack.hasTag()) return "";
        CompoundTag tag = stack.getTag();
        if (!tag.contains(TAG_PLATE_TEXT)) return "";
        return tag.getString(TAG_PLATE_TEXT);
    }

    /**
     * Returns true if this stack represents the license plate part.
     */
    public static boolean isLicensePlate(ItemStack stack) {
        if (!(stack.getItem() instanceof InternalVehiclePartItem)) return false;
        if (!stack.hasTag()) return false;
        return "license_sign".equals(stack.getTag().getString(TAG_PART_ID));
    }
}
