package de.rolandsw.schedulemc.coffee.items;

import de.rolandsw.schedulemc.coffee.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Gemahlener Kaffee
 * Endprodukt - bereit für Verkauf
 */
public class GroundCoffeeItem extends Item {

    public GroundCoffeeItem() {
        super(new Properties().stacksTo(64));
    }

    /**
     * Erstellt ItemStack mit allen Eigenschaften
     */
    public static ItemStack create(CoffeeType type, CoffeeQuality quality,
                                   CoffeeRoastLevel roastLevel, CoffeeGrindSize grindSize,
                                   int count) {
        ItemStack stack = new ItemStack(CoffeeItems.GROUND_COFFEE.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("CoffeeType", type.name());
        tag.putString("Quality", quality.name());
        tag.putString("RoastLevel", roastLevel.name());
        tag.putString("GrindSize", grindSize.name());
        return stack;
    }

    /**
     * Liest Kaffeesorte aus ItemStack
     */
    public static CoffeeType getType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("CoffeeType")) {
            return CoffeeType.valueOf(tag.getString("CoffeeType"));
        }
        return CoffeeType.ARABICA;
    }

    /**
     * Liest Qualität aus ItemStack
     */
    public static CoffeeQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            return CoffeeQuality.valueOf(tag.getString("Quality"));
        }
        return CoffeeQuality.GOOD;
    }

    /**
     * Liest Röstgrad aus ItemStack
     */
    public static CoffeeRoastLevel getRoastLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("RoastLevel")) {
            return CoffeeRoastLevel.valueOf(tag.getString("RoastLevel"));
        }
        return CoffeeRoastLevel.MEDIUM;
    }

    /**
     * Liest Mahlgrad aus ItemStack
     */
    public static CoffeeGrindSize getGrindSize(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("GrindSize")) {
            return CoffeeGrindSize.valueOf(tag.getString("GrindSize"));
        }
        return CoffeeGrindSize.MEDIUM;
    }

    /**
     * Berechnet Verkaufspreis (höher als ganze Bohnen)
     */
    public static double getPrice(ItemStack stack) {
        CoffeeType type = getType(stack);
        CoffeeQuality quality = getQuality(stack);
        CoffeeRoastLevel roastLevel = getRoastLevel(stack);

        double basePrice = type.calculatePrice(quality, stack.getCount());
        return basePrice * roastLevel.getPriceMultiplier() * 1.2; // +20% für Mahlung
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CoffeeType type = getType(stack);
        CoffeeQuality quality = getQuality(stack);
        CoffeeRoastLevel roastLevel = getRoastLevel(stack);
        CoffeeGrindSize grindSize = getGrindSize(stack);

        tooltip.add(Component.translatable("tooltip.coffee.type_label").append(type.getColoredName()));
        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));
        tooltip.add(Component.translatable("tooltip.coffee.roast_level").append(roastLevel.getColoredName()));
        tooltip.add(Component.translatable("tooltip.coffee.grind_size").append(grindSize.getColoredName()));
        tooltip.add(Component.translatable(grindSize.getBestFor()));
        tooltip.add(Component.literal(""));

        double pricePerGram = getPrice(new ItemStack(this, 1));
        tooltip.add(Component.translatable("tooltip.ground_coffee.sale_price_per", String.format("%.2f", pricePerGram)));

        if (stack.getCount() > 1) {
            double totalPrice = getPrice(stack);
            tooltip.add(Component.translatable("tooltip.ground_coffee.total", String.format("%.2f", totalPrice)));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        CoffeeType type = getType(stack);
        CoffeeQuality quality = getQuality(stack);
        CoffeeRoastLevel roastLevel = getRoastLevel(stack);
        CoffeeGrindSize grindSize = getGrindSize(stack);

        return Component.empty()
            .append(quality.getColoredName())
            .append(" ")
            .append(grindSize.getColoredName())
            .append(" ")
            .append(roastLevel.getColoredName())
            .append(" ")
            .append(Component.translatable("item.ground_coffee.name", type.getDisplayName()));
    }
}
