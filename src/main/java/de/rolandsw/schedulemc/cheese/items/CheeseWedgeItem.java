package de.rolandsw.schedulemc.cheese.items;

import de.rolandsw.schedulemc.cheese.CheeseAgeLevel;
import de.rolandsw.schedulemc.cheese.CheeseProcessingMethod;
import de.rolandsw.schedulemc.cheese.CheeseQuality;
import de.rolandsw.schedulemc.cheese.CheeseType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Kasestuck (Wedge) zum Konsumieren mit vollstandigen NBT-Daten:
 * - CheeseType (Sorte)
 * - CheeseQuality (Qualitat)
 * - CheeseAgeLevel (Reifegrad)
 * - CheeseProcessingMethod (Verarbeitungsmethode)
 * - Gewicht in Gramm
 */
public class CheeseWedgeItem extends Item {
    public CheeseWedgeItem(Properties properties) {
        super(properties);
    }

    /**
     * Erstellt Kasestuck mit allen Attributen
     */
    public static ItemStack create(CheeseType type, CheeseQuality quality, CheeseAgeLevel ageLevel,
                                   CheeseProcessingMethod method, double weightGrams, int count) {
        ItemStack stack = new ItemStack(CheeseItems.CHEESE_WEDGE.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("CheeseType", type.name());
        tag.putString("Quality", quality.name());
        tag.putString("AgeLevel", ageLevel.name());
        tag.putString("ProcessingMethod", method.name());
        tag.putDouble("WeightGrams", weightGrams);

        return stack;
    }

    // Getter fur NBT-Daten
    public static CheeseType getCheeseType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("CheeseType")) {
            try {
                return CheeseType.valueOf(tag.getString("CheeseType"));
            } catch (IllegalArgumentException e) {
                return CheeseType.GOUDA;
            }
        }
        return CheeseType.GOUDA;
    }

    public static CheeseQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            try {
                return CheeseQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException e) {
                return CheeseQuality.STANDARD;
            }
        }
        return CheeseQuality.STANDARD;
    }

    public static CheeseAgeLevel getAgeLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("AgeLevel")) {
            try {
                return CheeseAgeLevel.valueOf(tag.getString("AgeLevel"));
            } catch (IllegalArgumentException e) {
                return CheeseAgeLevel.FRESH;
            }
        }
        return CheeseAgeLevel.FRESH;
    }

    public static CheeseProcessingMethod getProcessingMethod(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ProcessingMethod")) {
            try {
                return CheeseProcessingMethod.valueOf(tag.getString("ProcessingMethod"));
            } catch (IllegalArgumentException e) {
                return CheeseProcessingMethod.NATURAL;
            }
        }
        return CheeseProcessingMethod.NATURAL;
    }

    public static double getWeightGrams(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("WeightGrams") ? tag.getDouble("WeightGrams") : 100.0;
    }

    /**
     * Berechnet Verkaufspreis basierend auf allen Faktoren
     */
    public static double calculatePrice(ItemStack stack) {
        CheeseType type = getCheeseType(stack);
        CheeseQuality quality = getQuality(stack);
        CheeseAgeLevel ageLevel = getAgeLevel(stack);
        CheeseProcessingMethod method = getProcessingMethod(stack);
        double weightGrams = getWeightGrams(stack);

        // Convert grams to kg for price calculation
        double weightKg = weightGrams / 1000.0;

        double basePrice = type.getBasePricePerKg();
        double totalPrice = basePrice * weightKg;
        totalPrice *= quality.getPriceMultiplier();
        totalPrice *= ageLevel.getPriceMultiplier();
        totalPrice *= method.getPriceMultiplier();

        return totalPrice;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CheeseType type = getCheeseType(stack);
        CheeseQuality quality = getQuality(stack);
        CheeseAgeLevel ageLevel = getAgeLevel(stack);
        CheeseProcessingMethod method = getProcessingMethod(stack);
        double weight = getWeightGrams(stack);

        tooltip.add(Component.translatable("tooltip.cheese.type", type.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.cheese.quality", quality.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.cheese.age_level", ageLevel.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.cheese.processing", method.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.cheese.weight_g", weight));

        double price = calculatePrice(stack);
        tooltip.add(Component.translatable("tooltip.cheese.value", price));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        CheeseType type = getCheeseType(stack);
        CheeseQuality quality = getQuality(stack);
        return Component.translatable("item.schedulemc.cheese_wedge.display", quality.getColorCode(), type.getDisplayName());
    }
}
