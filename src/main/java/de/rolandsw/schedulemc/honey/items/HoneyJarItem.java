package de.rolandsw.schedulemc.honey.items;

import de.rolandsw.schedulemc.honey.HoneyAgeLevel;
import de.rolandsw.schedulemc.honey.HoneyProcessingMethod;
import de.rolandsw.schedulemc.honey.HoneyQuality;
import de.rolandsw.schedulemc.honey.HoneyType;
import de.rolandsw.schedulemc.economy.EconomyController;
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
 * Honigglas mit vollständigen NBT-Daten:
 * - HoneyType (Sorte)
 * - HoneyQuality (Qualität)
 * - HoneyAgeLevel (Reifegrad)
 * - HoneyProcessingMethod (Verarbeitung)
 * - Gewicht in Kilogramm
 * - Produktionsdatum (in Ticks)
 * - Berechneter Verkaufspreis
 */
public class HoneyJarItem extends Item {
    private final double weightKg;

    public HoneyJarItem(double weightKg, Properties properties) {
        super(properties);
        this.weightKg = weightKg;
    }

    public double getWeightKg() {
        return weightKg;
    }

    /**
     * Erstellt Honigglas mit allen Attributen
     */
    public static ItemStack create(HoneyType type, HoneyQuality quality, HoneyAgeLevel ageLevel,
                                   HoneyProcessingMethod method, double weightKg, long productionDate, int count) {
        Item jarItem;
        if (weightKg <= 0.25) {
            jarItem = HoneyItems.HONEY_JAR_250G.get();
        } else if (weightKg <= 0.5) {
            jarItem = HoneyItems.HONEY_JAR_500G.get();
        } else {
            jarItem = HoneyItems.HONEY_JAR_1KG.get();
        }

        ItemStack stack = new ItemStack(jarItem, count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("HoneyType", type.name());
        tag.putString("Quality", quality.name());
        tag.putString("AgeLevel", ageLevel.name());
        tag.putString("ProcessingMethod", method.name());
        tag.putDouble("WeightKg", weightKg);
        tag.putLong("ProductionDate", productionDate);

        return stack;
    }

    /**
     * Erstellt Honigglas mit aktueller Weltzeit als Produktionsdatum
     */
    public static ItemStack create(HoneyType type, HoneyQuality quality, HoneyAgeLevel ageLevel,
                                   HoneyProcessingMethod method, double weightKg, int count) {
        return create(type, quality, ageLevel, method, weightKg, 0L, count);
    }

    // Getter für NBT-Daten
    public static HoneyType getHoneyType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("HoneyType")) {
            try {
                return HoneyType.valueOf(tag.getString("HoneyType"));
            } catch (IllegalArgumentException e) {
                return HoneyType.WILDFLOWER;
            }
        }
        return HoneyType.WILDFLOWER;
    }

    public static HoneyQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            try {
                return HoneyQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException e) {
                return HoneyQuality.GUT;
            }
        }
        return HoneyQuality.GUT;
    }

    public static HoneyAgeLevel getAgeLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("AgeLevel")) {
            try {
                return HoneyAgeLevel.valueOf(tag.getString("AgeLevel"));
            } catch (IllegalArgumentException e) {
                return HoneyAgeLevel.FRESH;
            }
        }
        return HoneyAgeLevel.FRESH;
    }

    public static HoneyProcessingMethod getProcessingMethod(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ProcessingMethod")) {
            try {
                return HoneyProcessingMethod.valueOf(tag.getString("ProcessingMethod"));
            } catch (IllegalArgumentException e) {
                return HoneyProcessingMethod.LIQUID;
            }
        }
        return HoneyProcessingMethod.LIQUID;
    }

    public static double getWeightKg(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("WeightKg")) {
            return tag.getDouble("WeightKg");
        }
        // Fallback basierend auf Item-Typ
        if (stack.getItem() instanceof HoneyJarItem jarItem) {
            return jarItem.getWeightKg();
        }
        return 0.5; // Default
    }

    public static long getProductionDate(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ProductionDate")) {
            return tag.getLong("ProductionDate");
        }
        return 0L;
    }

    /**
     * Berechnet das Alter des Honigs in Tagen basierend auf der aktuellen Weltzeit
     */
    public static int getAgeDays(ItemStack stack, long currentWorldTime) {
        long productionDate = getProductionDate(stack);
        if (productionDate == 0L) {
            return 0;
        }
        long ageTicks = currentWorldTime - productionDate;
        return (int) (ageTicks / 24000); // 24000 ticks = 1 MC day
    }

    /**
     * Aktualisiert das Reifungslevel basierend auf der aktuellen Weltzeit
     */
    public static void updateAgeLevel(ItemStack stack, long currentWorldTime) {
        int ageDays = getAgeDays(stack, currentWorldTime);
        HoneyAgeLevel newAgeLevel = HoneyAgeLevel.determineAgeLevel(ageDays);

        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("AgeLevel", newAgeLevel.name());
    }

    /**
     * Berechnet Verkaufspreis basierend auf allen Faktoren
     */
    public static double calculatePrice(ItemStack stack) {
        HoneyType type = getHoneyType(stack);
        HoneyQuality quality = getQuality(stack);
        HoneyAgeLevel ageLevel = getAgeLevel(stack);
        HoneyProcessingMethod method = getProcessingMethod(stack);
        double weight = getWeightKg(stack);

        try {
            // Dynamischer Preis: EconomyController-Basis + produktspezifische Multiplikatoren
            double dynamicBase = EconomyController.getInstance().getSellPrice(
                    type.getProductId(), quality.getPriceMultiplier(), 1, null);
            return dynamicBase * weight * ageLevel.getPriceMultiplier() * method.getPriceMultiplier();
        } catch (Exception e) {
            // Fallback auf alte Formel
            double basePrice = type.getBasePricePerKg();
            double totalPrice = basePrice * weight;
            totalPrice *= quality.getPriceMultiplier();
            totalPrice *= ageLevel.getPriceMultiplier();
            totalPrice *= method.getPriceMultiplier();
            return totalPrice;
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        HoneyType type = getHoneyType(stack);
        HoneyQuality quality = getQuality(stack);
        HoneyAgeLevel ageLevel = getAgeLevel(stack);
        HoneyProcessingMethod method = getProcessingMethod(stack);
        double weight = getWeightKg(stack);

        tooltip.add(Component.translatable("tooltip.honey.type", type.getColorCode() + type.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.honey.quality", quality.getColorCode() + quality.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.honey.age_level", ageLevel.getColorCode() + ageLevel.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.honey.processing", method.getColorCode() + method.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.honey.weight", weight));

        // Zeige Alter wenn Produktionsdatum gesetzt ist
        if (level != null && getProductionDate(stack) != 0L) {
            int ageDays = getAgeDays(stack, level.getGameTime());
            tooltip.add(Component.translatable("tooltip.honey.age_days", ageDays));
        }

        double price = calculatePrice(stack);
        tooltip.add(Component.translatable("tooltip.honey.value", price));

        // Zusätzliche Info im erweiterten Tooltip
        if (flag.isAdvanced()) {
            tooltip.add(Component.literal("§8" + quality.getDescription()));
            tooltip.add(Component.literal("§8" + ageLevel.getDescription()));
        }
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        double weight = getWeightKg(stack);

        String weightStr;
        if (weight < 1.0) {
            weightStr = String.format("%.0fg", weight * 1000);
        } else {
            weightStr = String.format("%.1fkg", weight);
        }

        return Component.translatable("tooltip.honey.name", weightStr);
    }
}
