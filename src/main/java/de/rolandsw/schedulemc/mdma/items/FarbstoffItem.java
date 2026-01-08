package de.rolandsw.schedulemc.mdma.items;

import de.rolandsw.schedulemc.mdma.PillColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Farbstoff - Für farbige Pillen
 */
public class FarbstoffItem extends Item {

    public FarbstoffItem() {
        super(new Properties().stacksTo(64));
    }

    public static ItemStack create(PillColor color, int count) {
        ItemStack stack = new ItemStack(MDMAItems.FARBSTOFF.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Color", color.name());
        return stack;
    }

    public static PillColor getColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Color")) {
            try {
                return PillColor.valueOf(tag.getString("Color"));
            } catch (IllegalArgumentException e) {
                return PillColor.WEISS;
            }
        }
        return PillColor.WEISS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        PillColor color = getColor(stack);
        tooltip.add(Component.translatable("tooltip.farbstoff.color_label").append(color.getColoredName()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal(color.getColorCode()).append(Component.translatable("tooltip.farbstoff.food_dye")));
        tooltip.add(Component.translatable("tooltip.farbstoff.use_press"));
    }

    @Override
    public Component getName(ItemStack stack) {
        PillColor color = getColor(stack);
        return Component.literal(color.getColorCode())
            .append(Component.translatable("item.farbstoff.name", color.getDisplayName()));
    }
}
