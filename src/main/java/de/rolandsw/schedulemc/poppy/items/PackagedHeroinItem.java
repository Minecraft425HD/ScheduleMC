package de.rolandsw.schedulemc.poppy.items;

import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Verpacktes Heroin in verschiedenen Größen (50g, 100g, 250g, 500g)
 */
public class PackagedHeroinItem extends Item {

    private final int grams;

    public PackagedHeroinItem(int grams) {
        super(new Properties().stacksTo(64));
        this.grams = grams;
    }

    public int getGrams() {
        return grams;
    }

    public static ItemStack create(Item item, PoppyType type, TobaccoQuality quality, int count) {
        ItemStack stack = new ItemStack(item, count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("PoppyType", type.name());
        tag.putString("Quality", quality.name());
        return stack;
    }

    public static PoppyType getType(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("PoppyType")) {
            try {
                return PoppyType.valueOf(stack.getTag().getString("PoppyType"));
            } catch (IllegalArgumentException e) {
                return PoppyType.TUERKISCH;
            }
        }
        return PoppyType.TUERKISCH;
    }

    public static TobaccoQuality getQuality(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Quality")) {
            try {
                return TobaccoQuality.valueOf(stack.getTag().getString("Quality"));
            } catch (IllegalArgumentException e) {
                return TobaccoQuality.SCHLECHT;
            }
        }
        return TobaccoQuality.SCHLECHT;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        PoppyType type = getType(stack);
        TobaccoQuality quality = getQuality(stack);

        tooltip.add(Component.literal("§7Sorte: " + type.getColoredName()));
        tooltip.add(Component.literal("§7Qualität: " + quality.getColoredName()));
        tooltip.add(Component.literal("§7Menge: §e" + grams + "g"));
        tooltip.add(Component.literal("§7Potenz: §c" + String.format("%.0f%%", type.getPotencyMultiplier() * 100)));
    }

    @Override
    public Component getName(ItemStack stack) {
        PoppyType type = getType(stack);
        return Component.literal("§f" + grams + "g Heroin §7(" + type.getDisplayName() + ")");
    }
}
