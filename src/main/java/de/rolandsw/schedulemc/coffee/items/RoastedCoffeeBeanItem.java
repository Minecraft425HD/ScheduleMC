package de.rolandsw.schedulemc.coffee.items;

import de.rolandsw.schedulemc.coffee.CoffeeQuality;
import de.rolandsw.schedulemc.coffee.CoffeeRoastLevel;
import de.rolandsw.schedulemc.coffee.CoffeeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Geröstete Kaffeebohnen
 * Kann direkt verkauft oder gemahlen werden
 */
public class RoastedCoffeeBeanItem extends Item {

    public RoastedCoffeeBeanItem() {
        super(new Properties().stacksTo(64));
    }

    /**
     * Erstellt ItemStack mit Typ, Qualität und Röstgrad
     */
    public static ItemStack create(CoffeeType type, CoffeeQuality quality,
                                   CoffeeRoastLevel roastLevel, int count) {
        ItemStack stack = new ItemStack(CoffeeItems.ROASTED_COFFEE_BEANS.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("CoffeeType", type.name());
        tag.putString("Quality", quality.name());
        tag.putString("RoastLevel", roastLevel.name());
        return stack;
    }

    /**
     * Liest Kaffeesorte aus ItemStack
     */
    public static CoffeeType getType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("CoffeeType")) {
            try {
                return CoffeeType.valueOf(tag.getString("CoffeeType"));
            } catch (IllegalArgumentException ignored) {}
        }
        return CoffeeType.ARABICA;
    }

    /**
     * Liest Qualität aus ItemStack
     */
    public static CoffeeQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            try {
                return CoffeeQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException ignored) {}
        }
        return CoffeeQuality.SEHR_GUT;
    }

    /**
     * Liest Röstgrad aus ItemStack
     */
    public static CoffeeRoastLevel getRoastLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("RoastLevel")) {
            try {
                return CoffeeRoastLevel.valueOf(tag.getString("RoastLevel"));
            } catch (IllegalArgumentException ignored) {}
        }
        return CoffeeRoastLevel.MEDIUM;
    }

    /**
     * Berechnet Verkaufspreis
     */
    public static double getPrice(ItemStack stack) {
        CoffeeType type = getType(stack);
        CoffeeQuality quality = getQuality(stack);
        CoffeeRoastLevel roastLevel = getRoastLevel(stack);

        try {
            double dynamicBase = type.calculateDynamicPrice(quality, stack.getCount(), null);
            return dynamicBase * roastLevel.getPriceMultiplier();
        } catch (Exception e) {
            double basePrice = type.calculatePrice(quality, stack.getCount());
            return basePrice * roastLevel.getPriceMultiplier();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CoffeeType type = getType(stack);
        CoffeeQuality quality = getQuality(stack);
        CoffeeRoastLevel roastLevel = getRoastLevel(stack);

        tooltip.add(Component.translatable("tooltip.coffee.type_label").append(type.getColoredName()));
        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));
        tooltip.add(Component.translatable("tooltip.coffee.roast_level").append(roastLevel.getColoredName()));
        tooltip.add(Component.translatable(roastLevel.getFlavorProfile()));
        tooltip.add(Component.literal(""));

        double pricePerGram;
        try {
            pricePerGram = type.calculateDynamicPrice(quality, 1, null) * roastLevel.getPriceMultiplier();
        } catch (Exception e) {
            pricePerGram = type.calculatePrice(quality, 1) * roastLevel.getPriceMultiplier();
        }
        tooltip.add(Component.translatable("tooltip.roasted_coffee.sale_price_per", String.format("%.2f", pricePerGram)));

        if (stack.getCount() > 1) {
            double totalPrice = getPrice(stack);
            tooltip.add(Component.translatable("tooltip.roasted_coffee.total", String.format("%.2f", totalPrice)));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.roasted_coffee_beans.name");
    }
}
