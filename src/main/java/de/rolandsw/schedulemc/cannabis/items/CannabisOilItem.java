package de.rolandsw.schedulemc.cannabis.items;
nimport de.rolandsw.schedulemc.util.StringUtils;

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
 * Cannabis-Öl - hochkonzentriertes Extrakt
 * Höchster THC-Gehalt, teuerste Variante
 */
public class CannabisOilItem extends Item {

    public CannabisOilItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(CannabisStrain strain, CannabisQuality quality, int milliliters) {
        ItemStack stack = new ItemStack(CannabisItems.CANNABIS_OIL.get(), 1);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Strain", strain.name());
        tag.putString("Quality", quality.name());
        tag.putInt("Milliliters", milliliters);
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

    public static int getMilliliters(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getInt("Milliliters");
        }
        return 10;
    }

    public static double calculatePrice(ItemStack stack) {
        CannabisStrain strain = getStrain(stack);
        CannabisQuality quality = getQuality(stack);
        int ml = getMilliliters(stack);
        // Öl ist am teuersten - 3x Basispreis
        return strain.calculatePrice(quality) * 3.0 * (ml / 10.0);
    }

    @Override
    public Component getName(ItemStack stack) {
        CannabisStrain strain = getStrain(stack);
        CannabisQuality quality = getQuality(stack);
        return Component.literal("§e🧪 " + quality.getColorCode() + strain.getDisplayName() + " Öl");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CannabisStrain strain = getStrain(stack);
        CannabisQuality quality = getQuality(stack);
        int ml = getMilliliters(stack);
        double price = calculatePrice(stack);

        tooltip.add(Component.literal("§7Sorte: " + strain.getColoredName()));
        tooltip.add(Component.literal("§7Qualität: " + quality.getColoredName()));
        tooltip.add(Component.literal("§7THC: §f~" + (strain.getThcContent() * 3) + "%"));
        tooltip.add(Component.literal("§7Menge: §f" + ml + "ml"));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§6💰 Wert: §f" + StringUtils.formatMoney(price)));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§d✨ Hochkonzentriert"));
        tooltip.add(Component.literal("§8Extrahiert aus Blüten"));
    }
}
