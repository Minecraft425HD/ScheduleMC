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
 * Getrimmte Cannabis-Blüten
 * Bereit zum Curing oder Verkauf
 */
public class TrimmedBudItem extends Item {

    public TrimmedBudItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(CannabisStrain strain, CannabisQuality quality, int weight) {
        ItemStack stack = new ItemStack(CannabisItems.TRIMMED_BUD.get(), 1);
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
        return 10;
    }

    public static double calculatePrice(ItemStack stack) {
        CannabisStrain strain = getStrain(stack);
        CannabisQuality quality = getQuality(stack);
        int weight = getWeight(stack);
        return strain.calculatePrice(quality) * (weight / 10.0);
    }

    @Override
    public Component getName(ItemStack stack) {
        CannabisStrain strain = getStrain(stack);
        CannabisQuality quality = getQuality(stack);
        return Component.literal("§a✂ " + quality.getColorCode() + strain.getDisplayName() + " §7(Getrimmt)");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CannabisStrain strain = getStrain(stack);
        CannabisQuality quality = getQuality(stack);
        int weight = getWeight(stack);
        double price = calculatePrice(stack);

        tooltip.add(Component.literal("§7Sorte: " + strain.getColoredName()));
        tooltip.add(Component.literal("§7Qualität: " + quality.getColoredName()));
        tooltip.add(Component.literal("§7THC: §f" + strain.getThcContent() + "%"));
        tooltip.add(Component.literal("§7Gewicht: §f" + weight + "g"));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§6💰 Wert: §f" + String.format("%.2f", price) + "€"));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§a🫙 Kann gecured werden"));
        tooltip.add(Component.literal("§8Curing verbessert Qualität!"));
    }
}
