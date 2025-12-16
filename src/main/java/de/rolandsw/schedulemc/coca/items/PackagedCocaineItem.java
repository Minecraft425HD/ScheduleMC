package de.rolandsw.schedulemc.coca.items;

import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Verpacktes Kokain - Verkaufsfertig
 * Verschiedene Gewichte: 50g, 100g, 250g, 500g
 */
public class PackagedCocaineItem extends Item {

    public PackagedCocaineItem() {
        super(new Properties()
                .stacksTo(1)); // Verpackte Ware nicht stapelbar
    }

    /**
     * Erstellt verpacktes Kokain mit allen Eigenschaften
     */
    public static ItemStack create(CocaType type, TobaccoQuality quality, int weight) {
        ItemStack stack = new ItemStack(CocaItems.PACKAGED_COCAINE.get(), 1);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("CocaType", type.name());
        tag.putString("Quality", quality.name());
        tag.putInt("Weight", weight);
        tag.putLong("PackagedDate", System.currentTimeMillis());
        return stack;
    }

    public static CocaType getType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("CocaType")) {
            return CocaType.valueOf(tag.getString("CocaType"));
        }
        return CocaType.BOLIVIANISCH;
    }

    public static TobaccoQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            return TobaccoQuality.valueOf(tag.getString("Quality"));
        }
        return TobaccoQuality.GUT;
    }

    public static int getWeight(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Weight")) {
            return tag.getInt("Weight");
        }
        return 50; // Default: 50g
    }

    /**
     * Berechnet den Verkaufspreis
     */
    public static double calculatePrice(ItemStack stack) {
        CocaType type = getType(stack);
        TobaccoQuality quality = getQuality(stack);
        int weight = getWeight(stack);

        double basePrice = type.getBasePrice();
        double qualityMultiplier = quality.getPriceMultiplier();

        return basePrice * qualityMultiplier * weight;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CocaType type = getType(stack);
        TobaccoQuality quality = getQuality(stack);
        int weight = getWeight(stack);
        double price = calculatePrice(stack);

        tooltip.add(Component.literal("§7Sorte: " + type.getColoredName()));
        tooltip.add(Component.literal("§7Qualität: " + quality.getColoredName()));
        tooltip.add(Component.literal("§7Gewicht: §f" + weight + "g"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§6Verkaufspreis: §e" + String.format("%.2f", price) + "€"));
    }

    @Override
    public Component getName(ItemStack stack) {
        CocaType type = getType(stack);
        int weight = getWeight(stack);
        return Component.literal(type.getDisplayName() + " Kokain (" + weight + "g)");
    }
}
