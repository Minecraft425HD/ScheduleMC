package de.rolandsw.schedulemc.vehicle.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Voucher item representing a vehicle that can be purchased from a dealer.
 * The vehicle type and price are stored in NBT data.
 */
public class VehicleVoucher extends Item {

    public enum VehicleType {
        SEDAN("Limousine", 5000),
        SPORT("Sportwagen", 15000),
        SUV("SUV", 12000),
        TRUCK("LKW", 18000),
        TRANSPORTER("Transporter", 10000);

        private final String displayName;
        private final int defaultPrice;

        VehicleType(String displayName, int defaultPrice) {
            this.displayName = displayName;
            this.defaultPrice = defaultPrice;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getDefaultPrice() {
            return defaultPrice;
        }
    }

    public VehicleVoucher(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        VehicleType type = getVehicleType(stack);
        if (type != null) {
            return Component.literal(type.getDisplayName());
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        VehicleType type = getVehicleType(stack);
        if (type != null) {
            tooltip.add(Component.literal("Fahrzeugtyp: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(type.getDisplayName()).withStyle(ChatFormatting.AQUA)));

            int price = getPrice(stack);
            tooltip.add(Component.literal("Preis: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(price + "€").withStyle(ChatFormatting.GOLD)));

            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("✓ Automatische Lieferung").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal("Fahrzeug spawnt am gesetzten Spawnpunkt").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    /**
     * Create a vehicle voucher for a specific type
     */
    public static ItemStack create(VehicleType type) {
        ItemStack stack = new ItemStack(VehicleItems.VEHICLE_VOUCHER.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("VehicleType", type.name());
        tag.putInt("Price", type.getDefaultPrice());
        return stack;
    }

    /**
     * Create a vehicle voucher with custom price
     */
    public static ItemStack create(VehicleType type, int price) {
        ItemStack stack = new ItemStack(VehicleItems.VEHICLE_VOUCHER.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("VehicleType", type.name());
        tag.putInt("Price", price);
        return stack;
    }

    /**
     * Get vehicle type from voucher
     */
    public static VehicleType getVehicleType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("VehicleType")) {
            try {
                return VehicleType.valueOf(tag.getString("VehicleType"));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Get price from voucher
     */
    public static int getPrice(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Price")) {
            return tag.getInt("Price");
        }
        VehicleType type = getVehicleType(stack);
        return type != null ? type.getDefaultPrice() : 0;
    }

    /**
     * Check if item is a vehicle voucher
     */
    public static boolean isVehicleVoucher(ItemStack stack) {
        return stack.getItem() instanceof VehicleVoucher && getVehicleType(stack) != null;
    }
}
