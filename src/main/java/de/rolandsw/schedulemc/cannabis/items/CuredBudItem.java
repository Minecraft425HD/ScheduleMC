package de.rolandsw.schedulemc.cannabis.items;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Gecurte Cannabis-Blüten
 * Höchste Qualitätsstufe, verkaufsfertig
 */
public class CuredBudItem extends Item {

    public CuredBudItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(CannabisStrain strain, CannabisQuality quality, int count, int curingDays) {
        ItemStack stack = new ItemStack(CannabisItems.CURED_BUD.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Strain", strain.name());
        tag.putString("Quality", quality.name());
        tag.putInt("Weight", 1); // Jedes Item = 1g
        tag.putInt("CuringDays", curingDays);
        return stack;
    }

    public static CannabisStrain getStrain(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Strain")) {
            try {
                return CannabisStrain.valueOf(stack.getTag().getString("Strain"));
            } catch (IllegalArgumentException e) {
                return CannabisStrain.HYBRID;
            }
        }
        return CannabisStrain.HYBRID;
    }

    public static CannabisQuality getQuality(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Quality")) {
            try {
                return CannabisQuality.valueOf(stack.getTag().getString("Quality"));
            } catch (IllegalArgumentException e) {
                return CannabisQuality.GUT;
            }
        }
        return CannabisQuality.GUT;
    }

    public static int getWeight(ItemStack stack) {
        // Jedes Item = 1g, Gesamtgewicht = Stack-Count
        return 1;
    }

    public static int getCuringDays(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getInt("CuringDays");
        }
        return 0;
    }

    public static double calculatePrice(ItemStack stack) {
        CannabisStrain strain = getStrain(stack);
        CannabisQuality quality = getQuality(stack);

        double basePrice;
        try {
            basePrice = strain.calculateDynamicPrice(quality, stack.getCount(), null);
        } catch (Exception e) {
            basePrice = strain.calculatePrice(quality) * stack.getCount() / 10.0;
        }
        float priceBonus = (stack.hasTag() && stack.getTag().contains("PriceBonus"))
                ? stack.getTag().getFloat("PriceBonus") : 0.0f;
        return basePrice * (1.0 + priceBonus);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("§6🫙 ")
            .append(Component.translatable("item.cured_bud.name"));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CannabisStrain strain = getStrain(stack);
        CannabisQuality quality = getQuality(stack);
        int weight = getWeight(stack);
        int curingDays = getCuringDays(stack);
        double price = calculatePrice(stack);

        tooltip.add(Component.translatable("tooltip.cannabis.strain_label").append(strain.getColoredName()));
        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));
        tooltip.add(Component.translatable("tooltip.cannabis.thc_label").append(Component.translatable("tooltip.cannabis.thc_value", strain.getThcContent())));
        tooltip.add(Component.translatable("tooltip.cannabis.weight_label").append(Component.translatable("tooltip.cannabis.weight_value", (weight * stack.getCount()), stack.getCount())));
        tooltip.add(Component.translatable("tooltip.cured_bud.curing_time").append(Component.translatable("tooltip.cured_bud.curing_days_value", curingDays)));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.cannabis.value_label").append(Component.translatable("tooltip.cannabis.value", String.format("%.2f", price))));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.cured_bud.premium_product"));

        if (curingDays >= 28) {
            tooltip.add(Component.translatable("tooltip.cured_bud.perfectly_cured"));
        } else if (curingDays >= 14) {
            tooltip.add(Component.translatable("tooltip.cured_bud.well_cured"));
        }
    }
}
