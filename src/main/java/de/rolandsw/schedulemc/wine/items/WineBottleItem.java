package de.rolandsw.schedulemc.wine.items;

import de.rolandsw.schedulemc.wine.WineAgeLevel;
import de.rolandsw.schedulemc.wine.WineProcessingMethod;
import de.rolandsw.schedulemc.wine.WineQuality;
import de.rolandsw.schedulemc.wine.WineType;
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
 * Weinflasche mit vollständigen NBT-Daten:
 * - WineType (Sorte)
 * - WineQuality (Qualität)
 * - WineAgeLevel (Reifegrad)
 * - WineProcessingMethod (Trocken/Süß)
 * - Volumen in Litern
 */
public class WineBottleItem extends Item {
    private final double volumeLiters;

    public WineBottleItem(double volumeLiters, Properties properties) {
        super(properties);
        this.volumeLiters = volumeLiters;
    }

    public double getVolumeLiters() {
        return volumeLiters;
    }

    /**
     * Erstellt Weinflasche mit allen Attributen
     */
    public static ItemStack create(WineType type, WineQuality quality, WineAgeLevel ageLevel,
                                   WineProcessingMethod method, double volumeLiters, int count) {
        Item bottleItem;
        if (volumeLiters <= 0.375) {
            bottleItem = WineItems.WINE_BOTTLE_375ML.get();
        } else if (volumeLiters <= 0.75) {
            bottleItem = WineItems.WINE_BOTTLE_750ML.get();
        } else {
            bottleItem = WineItems.WINE_BOTTLE_1500ML.get();
        }

        ItemStack stack = new ItemStack(bottleItem, count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("WineType", type.name());
        tag.putString("Quality", quality.name());
        tag.putString("AgeLevel", ageLevel.name());
        tag.putString("ProcessingMethod", method.name());
        tag.putDouble("VolumeLiters", volumeLiters);

        return stack;
    }

    // Getter für NBT-Daten
    public static WineType getWineType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("WineType")) {
            try {
                return WineType.valueOf(tag.getString("WineType"));
            } catch (IllegalArgumentException e) {
                return WineType.RIESLING;
            }
        }
        return WineType.RIESLING;
    }

    public static WineQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            try {
                return WineQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException e) {
                return WineQuality.SCHLECHT;
            }
        }
        return WineQuality.SCHLECHT;
    }

    public static WineAgeLevel getAgeLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("AgeLevel")) {
            try {
                return WineAgeLevel.valueOf(tag.getString("AgeLevel"));
            } catch (IllegalArgumentException e) {
                return WineAgeLevel.YOUNG;
            }
        }
        return WineAgeLevel.YOUNG;
    }

    public static WineProcessingMethod getProcessingMethod(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ProcessingMethod")) {
            try {
                return WineProcessingMethod.valueOf(tag.getString("ProcessingMethod"));
            } catch (IllegalArgumentException e) {
                return WineProcessingMethod.DRY;
            }
        }
        return WineProcessingMethod.DRY;
    }

    /**
     * Berechnet Verkaufspreis basierend auf allen Faktoren
     */
    public static double calculatePrice(ItemStack stack) {
        WineType type = getWineType(stack);
        WineQuality quality = getQuality(stack);
        WineAgeLevel ageLevel = getAgeLevel(stack);
        WineProcessingMethod method = getProcessingMethod(stack);

        CompoundTag tag = stack.getTag();
        double volume = tag != null && tag.contains("VolumeLiters") ? tag.getDouble("VolumeLiters") : 0.75;

        try {
            // Dynamischer Preis: EconomyController-Basis + produktspezifische Multiplikatoren
            double dynamicBase = EconomyController.getInstance().getSellPrice(
                    type.getProductId(), quality.getPriceMultiplier(), 1, null);
            return dynamicBase * volume * ageLevel.getPriceMultiplier() * method.getPriceMultiplier();
        } catch (Exception e) {
            // Fallback auf alte Formel
            double basePrice = type.getBasePricePerLiter();
            double totalPrice = basePrice * volume;
            totalPrice *= quality.getPriceMultiplier();
            totalPrice *= ageLevel.getPriceMultiplier();
            totalPrice *= method.getPriceMultiplier();
            return totalPrice;
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        WineType type = getWineType(stack);
        WineQuality quality = getQuality(stack);
        WineAgeLevel ageLevel = getAgeLevel(stack);
        WineProcessingMethod method = getProcessingMethod(stack);

        tooltip.add(Component.translatable("tooltip.wine.type", type.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.wine.quality", quality.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.wine.age_level", ageLevel.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.wine.style", method.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.wine.volume", volumeLiters));

        double price = calculatePrice(stack);
        tooltip.add(Component.translatable("tooltip.wine.value", price));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        WineType type = getWineType(stack);
        String formattedVolume = String.format("%.2f", volumeLiters);
        return Component.translatable("tooltip.wine.name", type.getDisplayName(), formattedVolume);
    }
}
