package de.rolandsw.schedulemc.cheese.items;

import de.rolandsw.schedulemc.cheese.CheeseAgeLevel;
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
 * Kaselaib (Wheel) mit vollstandigen NBT-Daten:
 * - CheeseType (Sorte)
 * - CheeseQuality (Qualitat)
 * - CheeseAgeLevel (Reifegrad)
 * - Gewicht in Kilogramm
 */
public class CheeseWheelItem extends Item {
    public CheeseWheelItem(Properties properties) {
        super(properties);
    }

    /**
     * Erstellt Kaselaib mit allen Attributen
     */
    public static ItemStack create(CheeseType type, CheeseQuality quality, CheeseAgeLevel ageLevel,
                                   double weightKg, int count) {
        ItemStack stack = new ItemStack(CheeseItems.CHEESE_WHEEL.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("CheeseType", type.name());
        tag.putString("Quality", quality.name());
        tag.putString("AgeLevel", ageLevel.name());
        tag.putDouble("WeightKg", weightKg);
        tag.putInt("AgingTicks", 0);

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
                return CheeseQuality.SCHLECHT;
            }
        }
        return CheeseQuality.SCHLECHT;
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

    public static double getWeightKg(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("WeightKg") ? tag.getDouble("WeightKg") : 1.0;
    }

    public static int getAgingTicks(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("AgingTicks") ? tag.getInt("AgingTicks") : 0;
    }

    public static void setAgingTicks(ItemStack stack, int ticks) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("AgingTicks", ticks);

        // Update age level based on ticks
        CheeseAgeLevel newAgeLevel = CheeseAgeLevel.determineAgeLevel(ticks);
        tag.putString("AgeLevel", newAgeLevel.name());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CheeseType type = getCheeseType(stack);
        CheeseQuality quality = getQuality(stack);
        CheeseAgeLevel ageLevel = getAgeLevel(stack);

        tooltip.add(Component.translatable("tooltip.cheese.type", type.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.cheese.quality", quality.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.cheese.age_level", ageLevel.getDisplayName()));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return Component.translatable("item.schedulemc.cheese_wheel.name");
    }
}
