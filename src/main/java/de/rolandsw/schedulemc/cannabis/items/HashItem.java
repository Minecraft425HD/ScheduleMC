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
 * Haschisch - konzentriertes Cannabis-Produkt
 * Hergestellt aus Trim (Blätterreste)
 */
public class HashItem extends Item {

    public HashItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(CannabisStrain strain, CannabisQuality quality, int weight) {
        ItemStack stack = new ItemStack(CannabisItems.HASH.get(), 1);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Strain", strain.name());
        tag.putString("Quality", quality.name());
        tag.putInt("Weight", weight);
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
                return CannabisQuality.MIDS;
            }
        }
        return CannabisQuality.MIDS;
    }

    public static int getWeight(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getInt("Weight");
        }
        return 5;
    }

    public static double calculatePrice(ItemStack stack) {
        CannabisStrain strain = getStrain(stack);
        CannabisQuality quality = getQuality(stack);
        int weight = getWeight(stack);
        // Hash ist pro Gramm teurer als Blüten
        return strain.calculatePrice(quality) * 1.5 * (weight / 10.0);
    }

    @Override
    public Component getName(ItemStack stack) {
        CannabisStrain strain = getStrain(stack);
        CannabisQuality quality = getQuality(stack);
        return Component.literal("§6🟤 ")
            .append(Component.literal(quality.getColorCode()))
            .append(Component.literal(strain.getDisplayName()))
            .append(Component.translatable("item.hash.suffix"));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CannabisStrain strain = getStrain(stack);
        CannabisQuality quality = getQuality(stack);
        int weight = getWeight(stack);
        double price = calculatePrice(stack);

        tooltip.add(Component.translatable("tooltip.cannabis.strain_label").append(strain.getColoredName()));
        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));
        tooltip.add(Component.translatable("tooltip.cannabis.thc_label").append(Component.literal("§f~" + (strain.getThcContent() * 1.5) + "%")));
        tooltip.add(Component.translatable("tooltip.cannabis.weight_label").append(Component.literal("§f" + weight + "g")));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.cannabis.value_label").append(Component.literal("§f" + String.format("%.2f", price) + "€")));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.hash.concentrated_product"));
        tooltip.add(Component.translatable("tooltip.hash.made_from_trim"));
    }
}
