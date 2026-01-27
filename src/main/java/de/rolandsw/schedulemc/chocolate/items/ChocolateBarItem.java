package de.rolandsw.schedulemc.chocolate.items;

import de.rolandsw.schedulemc.chocolate.ChocolateAgeLevel;
import de.rolandsw.schedulemc.chocolate.ChocolateProcessingMethod;
import de.rolandsw.schedulemc.chocolate.ChocolateQuality;
import de.rolandsw.schedulemc.chocolate.ChocolateType;
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
 * Schokoladentafel mit vollständigen NBT-Daten:
 * - ChocolateType (Sorte)
 * - ChocolateQuality (Qualität)
 * - ChocolateAgeLevel (Reifegrad)
 * - ChocolateProcessingMethod (Verarbeitungsmethode)
 * - Gewicht in Kilogramm
 * - Produktionsdatum (Ticks)
 */
public class ChocolateBarItem extends Item {
    private final double weightKg;

    public ChocolateBarItem(double weightKg, Properties properties) {
        super(properties);
        this.weightKg = weightKg;
    }

    public double getWeightKg() {
        return weightKg;
    }

    /**
     * Erstellt Schokoladentafel mit allen Attributen
     */
    public static ItemStack create(ChocolateType type, ChocolateQuality quality, ChocolateAgeLevel ageLevel,
                                   ChocolateProcessingMethod method, double weightKg, long productionDate, int count) {
        Item barItem;
        if (weightKg <= 0.1) {
            barItem = ChocolateItems.CHOCOLATE_BAR_100G.get();
        } else if (weightKg <= 0.2) {
            barItem = ChocolateItems.CHOCOLATE_BAR_200G.get();
        } else {
            barItem = ChocolateItems.CHOCOLATE_BAR_500G.get();
        }

        ItemStack stack = new ItemStack(barItem, count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("ChocolateType", type.name());
        tag.putString("Quality", quality.name());
        tag.putString("AgeLevel", ageLevel.name());
        tag.putString("ProcessingMethod", method.name());
        tag.putDouble("WeightKg", weightKg);
        tag.putLong("ProductionDate", productionDate);

        return stack;
    }

    // Getter für NBT-Daten
    public static ChocolateType getChocolateType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ChocolateType")) {
            try {
                return ChocolateType.valueOf(tag.getString("ChocolateType"));
            } catch (IllegalArgumentException e) {
                return ChocolateType.MILK;
            }
        }
        return ChocolateType.MILK;
    }

    public static ChocolateQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            try {
                return ChocolateQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException e) {
                return ChocolateQuality.BASIC;
            }
        }
        return ChocolateQuality.BASIC;
    }

    public static ChocolateAgeLevel getAgeLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("AgeLevel")) {
            try {
                return ChocolateAgeLevel.valueOf(tag.getString("AgeLevel"));
            } catch (IllegalArgumentException e) {
                return ChocolateAgeLevel.FRESH;
            }
        }
        return ChocolateAgeLevel.FRESH;
    }

    public static ChocolateProcessingMethod getProcessingMethod(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ProcessingMethod")) {
            try {
                return ChocolateProcessingMethod.valueOf(tag.getString("ProcessingMethod"));
            } catch (IllegalArgumentException e) {
                return ChocolateProcessingMethod.PLAIN;
            }
        }
        return ChocolateProcessingMethod.PLAIN;
    }

    public static long getProductionDate(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ProductionDate")) {
            return tag.getLong("ProductionDate");
        }
        return 0;
    }

    public static double getWeightKg(ItemStack stack) {
        if (stack.getItem() instanceof ChocolateBarItem barItem) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("WeightKg")) {
                return tag.getDouble("WeightKg");
            }
            return barItem.getWeightKg();
        }
        return 0.1; // Default 100g
    }

    /**
     * Berechnet das Alter in Tagen basierend auf aktuellem Zeitpunkt
     */
    public static int calculateAgeDays(ItemStack stack, long currentTick) {
        long productionDate = getProductionDate(stack);
        long ageTicks = currentTick - productionDate;
        return ChocolateAgeLevel.ticksToDays(ageTicks);
    }

    /**
     * Aktualisiert den Reifegrad basierend auf verstrichener Zeit
     */
    public static void updateAgeLevel(ItemStack stack, long currentTick) {
        int ageDays = calculateAgeDays(stack, currentTick);
        ChocolateAgeLevel newAgeLevel = ChocolateAgeLevel.determineAgeLevel(ageDays);

        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("AgeLevel", newAgeLevel.name());
    }

    /**
     * Berechnet Verkaufspreis basierend auf allen Faktoren
     */
    public static double calculatePrice(ItemStack stack) {
        ChocolateType type = getChocolateType(stack);
        ChocolateQuality quality = getQuality(stack);
        ChocolateAgeLevel ageLevel = getAgeLevel(stack);
        ChocolateProcessingMethod method = getProcessingMethod(stack);
        double weight = getWeightKg(stack);

        double basePrice = type.getBasePricePerKg();
        double totalPrice = basePrice * weight;
        totalPrice *= quality.getPriceMultiplier();
        totalPrice *= ageLevel.getPriceMultiplier();
        totalPrice *= method.getPriceMultiplier();

        return totalPrice;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        ChocolateType type = getChocolateType(stack);
        ChocolateQuality quality = getQuality(stack);
        ChocolateAgeLevel ageLevel = getAgeLevel(stack);
        ChocolateProcessingMethod method = getProcessingMethod(stack);
        double weight = getWeightKg(stack);

        tooltip.add(Component.literal("§7Type: " + type.getColorCode() + type.getDisplayName()));
        tooltip.add(Component.literal("§7Quality: " + quality.getColoredName()));
        tooltip.add(Component.literal("§7Age: " + ageLevel.getDisplayName()));
        tooltip.add(Component.literal("§7Method: " + method.getDisplayName()));
        tooltip.add(Component.literal("§7Weight: §f" + (weight * 1000) + "g"));
        tooltip.add(Component.literal("§7Cocoa: §f" + type.getCocoaPercentage() + "%"));

        // Show age in days if available
        if (level != null && getProductionDate(stack) > 0) {
            int ageDays = calculateAgeDays(stack, level.getGameTime());
            tooltip.add(Component.literal("§7Age Days: §f" + ageDays));

            // Show days until next age level
            if (ageLevel.canAge()) {
                int daysUntilNext = ageLevel.getDaysUntilNext(ageDays);
                tooltip.add(Component.literal("§7Next Stage: §e" + daysUntilNext + " days"));
            }
        }

        double price = calculatePrice(stack);
        tooltip.add(Component.literal("§6Value: §f" + String.format("%.2f€", price)));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        ChocolateType type = getChocolateType(stack);
        ChocolateQuality quality = getQuality(stack);
        double weight = getWeightKg(stack);
        int weightGrams = (int) (weight * 1000);

        return Component.literal(quality.getColorCode() + type.getDisplayName() + " (" + weightGrams + "g)");
    }
}
