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
 * Morphin-Base - Zwischenprodukt aus gekochtem Rohopium
 */
public class MorphineItem extends Item {

    public MorphineItem() {
        super(new Properties().stacksTo(64));
    }

    public static ItemStack create(PoppyType type, TobaccoQuality quality, int count) {
        ItemStack stack = new ItemStack(PoppyItems.MORPHINE.get(), count);
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

        tooltip.add(Component.translatable("tooltip.poppy.type_label").append(type.getColoredName()));
        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.morphine.refine_to_heroin"));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("§dMorphin-Base");
    }
}
