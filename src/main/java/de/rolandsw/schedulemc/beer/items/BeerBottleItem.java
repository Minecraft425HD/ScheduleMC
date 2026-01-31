package de.rolandsw.schedulemc.beer.items;

import de.rolandsw.schedulemc.beer.BeerAgeLevel;
import de.rolandsw.schedulemc.beer.BeerProcessingMethod;
import de.rolandsw.schedulemc.beer.BeerQuality;
import de.rolandsw.schedulemc.beer.BeerType;
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
 * Bierflasche mit vollständigen NBT-Daten:
 * - BeerType (Sorte)
 * - BeerQuality (Qualität)
 * - BeerAgeLevel (Reifegrad)
 * - BeerProcessingMethod (Fass/Flasche/Dose)
 * - Volumen in Litern
 * - Produktionsdatum (Minecraft-Tick)
 * - Alkoholgehalt (ABV)
 */
public class BeerBottleItem extends Item {
    private final double volumeLiters;

    public BeerBottleItem(double volumeLiters, Properties properties) {
        super(properties);
        this.volumeLiters = volumeLiters;
    }

    public double getVolumeLiters() {
        return volumeLiters;
    }

    /**
     * Erstellt Bierflasche mit allen Attributen
     */
    public static ItemStack create(BeerType type, BeerQuality quality, BeerAgeLevel ageLevel,
                                   BeerProcessingMethod method, double volumeLiters,
                                   long productionDate, int count) {
        Item bottleItem = BeerItems.BEER_BOTTLE.get();

        ItemStack stack = new ItemStack(bottleItem, count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("BeerType", type.name());
        tag.putString("Quality", quality.name());
        tag.putString("AgeLevel", ageLevel.name());
        tag.putString("ProcessingMethod", method.name());
        tag.putDouble("VolumeLiters", volumeLiters);
        tag.putLong("ProductionDate", productionDate);
        tag.putDouble("ABV", type.getAlcoholPercentage());

        return stack;
    }

    /**
     * Erstellt Bierflasche mit aktueller Zeit als Produktionsdatum
     */
    public static ItemStack create(BeerType type, BeerQuality quality, BeerAgeLevel ageLevel,
                                   BeerProcessingMethod method, double volumeLiters, int count) {
        return create(type, quality, ageLevel, method, volumeLiters, System.currentTimeMillis(), count);
    }

    // Getter für NBT-Daten
    public static BeerType getBeerType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("BeerType")) {
            try {
                return BeerType.valueOf(tag.getString("BeerType"));
            } catch (IllegalArgumentException e) {
                return BeerType.PILSNER;
            }
        }
        return BeerType.PILSNER;
    }

    public static BeerQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            try {
                return BeerQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException e) {
                return BeerQuality.BASIC;
            }
        }
        return BeerQuality.BASIC;
    }

    public static BeerAgeLevel getAgeLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("AgeLevel")) {
            try {
                return BeerAgeLevel.valueOf(tag.getString("AgeLevel"));
            } catch (IllegalArgumentException e) {
                return BeerAgeLevel.YOUNG;
            }
        }
        return BeerAgeLevel.YOUNG;
    }

    public static BeerProcessingMethod getProcessingMethod(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ProcessingMethod")) {
            try {
                return BeerProcessingMethod.valueOf(tag.getString("ProcessingMethod"));
            } catch (IllegalArgumentException e) {
                return BeerProcessingMethod.BOTTLED;
            }
        }
        return BeerProcessingMethod.BOTTLED;
    }

    public static long getProductionDate(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ProductionDate")) {
            return tag.getLong("ProductionDate");
        }
        return 0L;
    }

    public static double getABV(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ABV")) {
            return tag.getDouble("ABV");
        }
        BeerType type = getBeerType(stack);
        return type.getAlcoholPercentage();
    }

    /**
     * Berechnet Verkaufspreis basierend auf allen Faktoren
     */
    public static double calculatePrice(ItemStack stack) {
        BeerType type = getBeerType(stack);
        BeerQuality quality = getQuality(stack);
        BeerAgeLevel ageLevel = getAgeLevel(stack);
        BeerProcessingMethod method = getProcessingMethod(stack);

        CompoundTag tag = stack.getTag();
        double volume = tag != null && tag.contains("VolumeLiters") ? tag.getDouble("VolumeLiters") : 0.5;

        double basePrice = type.getBasePricePerLiter();
        double totalPrice = basePrice * volume;
        totalPrice *= quality.getPriceMultiplier();
        totalPrice *= ageLevel.getPriceMultiplier();
        totalPrice *= method.getPriceMultiplier();

        return totalPrice;
    }

    /**
     * Berechnet die Anzahl der Tage seit Produktion
     */
    public static int getDaysSinceProduction(ItemStack stack) {
        long productionDate = getProductionDate(stack);
        if (productionDate == 0L) return 0;

        long currentTime = System.currentTimeMillis();
        long millisSinceProduction = currentTime - productionDate;
        // Convert milliseconds to Minecraft days (assuming 20 minutes per MC day = 1200000 ms)
        return (int) (millisSinceProduction / 1200000);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        BeerType type = getBeerType(stack);
        BeerQuality quality = getQuality(stack);
        BeerAgeLevel ageLevel = getAgeLevel(stack);
        BeerProcessingMethod method = getProcessingMethod(stack);
        double abv = getABV(stack);
        int daysSinceProduction = getDaysSinceProduction(stack);

        tooltip.add(Component.translatable("tooltip.beer.type", type.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.beer.quality", quality.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.beer.age", ageLevel.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.beer.method", method.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.beer.volume", volumeLiters));
        tooltip.add(Component.translatable("tooltip.beer.abv", abv));

        if (daysSinceProduction > 0) {
            tooltip.add(Component.translatable("tooltip.beer.days_aged", daysSinceProduction));
        }

        double price = calculatePrice(stack);
        tooltip.add(Component.translatable("tooltip.beer.value", price));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        BeerType type = getBeerType(stack);
        BeerQuality quality = getQuality(stack);
        BeerProcessingMethod method = getProcessingMethod(stack);

        return Component.translatable("item.schedulemc.beer_bottle.display",
                quality.getColorCode(), type.getDisplayName(), method.getDisplayName(), volumeLiters);
    }
}
