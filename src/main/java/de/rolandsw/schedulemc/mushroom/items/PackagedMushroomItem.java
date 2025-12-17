package de.rolandsw.schedulemc.mushroom.items;

import de.rolandsw.schedulemc.mushroom.MushroomType;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Verpackte Pilze in verschiedenen Größen
 */
public class PackagedMushroomItem extends Item {

    private final int grams;

    public PackagedMushroomItem(int grams) {
        super(new Properties().stacksTo(16));
        this.grams = grams;
    }

    public int getGrams() {
        return grams;
    }

    /**
     * Erstellt verpackte Pilze
     */
    public static ItemStack create(int grams, MushroomType type, TobaccoQuality quality) {
        ItemStack stack = new ItemStack(getItemForGrams(grams));
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("MushroomType", type.name());
        tag.putString("Quality", quality.name());
        return stack;
    }

    public static MushroomType getMushroomType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("MushroomType")) {
            return MushroomType.valueOf(tag.getString("MushroomType"));
        }
        return MushroomType.CUBENSIS;
    }

    public static TobaccoQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            return TobaccoQuality.valueOf(tag.getString("Quality"));
        }
        return TobaccoQuality.GUT;
    }

    private static Item getItemForGrams(int grams) {
        return switch (grams) {
            case 1 -> MushroomItems.PACKAGED_1G.get();
            case 4 -> MushroomItems.PACKAGED_3_5G.get(); // 3.5g als "Eighth"
            case 7 -> MushroomItems.PACKAGED_7G.get();
            case 14 -> MushroomItems.PACKAGED_14G.get();
            default -> MushroomItems.PACKAGED_1G.get();
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        MushroomType type = getMushroomType(stack);
        TobaccoQuality quality = getQuality(stack);
        double potency = type.getPotencyMultiplier() * quality.getYieldMultiplier();

        tooltip.add(Component.literal("§7Sorte: " + type.getColoredName()));
        tooltip.add(Component.literal("§7Qualität: " + quality.getColoredName()));
        tooltip.add(Component.literal("§7Potenz: §d" + String.format("%.1f", potency * 100) + "%"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§a✓ Verkaufsfertig"));

        // Preis-Schätzung basierend auf Sorte, Qualität und Gramm
        double basePrice = type.getSporePrice() * 0.5 * grams;
        double qualityMultiplier = quality.getYieldMultiplier();
        double estimatedPrice = basePrice * qualityMultiplier * potency;
        tooltip.add(Component.literal("§7Schätzwert: §a$" + String.format("%.2f", estimatedPrice)));
    }

    @Override
    public Component getName(ItemStack stack) {
        MushroomType type = getMushroomType(stack);
        String gramLabel = grams == 4 ? "3.5g" : grams + "g";
        return Component.literal(type.getColorCode() + "Verpackte Pilze §7[" + gramLabel + "]");
    }
}
