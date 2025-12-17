package de.rolandsw.schedulemc.cannabis.items;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Cannabis-Samen verschiedener Sorten
 */
public class CannabisSeedItem extends Item {

    public CannabisSeedItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(CannabisStrain strain, int count) {
        ItemStack stack = new ItemStack(CannabisItems.CANNABIS_SEED.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Strain", strain.name());
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

    @Override
    public Component getName(ItemStack stack) {
        CannabisStrain strain = getStrain(stack);
        return Component.literal(strain.getColorCode() + strain.getDisplayName() + " Samen");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CannabisStrain strain = getStrain(stack);

        tooltip.add(Component.literal("§7Sorte: " + strain.getColoredName()));
        tooltip.add(Component.literal("§7THC: §f" + strain.getThcContent() + "%"));
        tooltip.add(Component.literal("§7CBD: §f" + strain.getCbdContent() + "%"));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal(strain.getEffectDescription()));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§8Blütezeit: " + strain.getFloweringDays() + " Tage"));
        tooltip.add(Component.literal("§8Ertrag: ~" + strain.getBaseYield() + "g"));
    }
}
