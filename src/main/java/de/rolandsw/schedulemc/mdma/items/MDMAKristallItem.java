package de.rolandsw.schedulemc.mdma.items;

import de.rolandsw.schedulemc.mdma.MDMAQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * MDMA-Kristalle - Getrocknete Kristalle aus dem Trocknungs-Ofen
 * Bereit für die Pillen-Presse
 */
public class MDMAKristallItem extends Item {

    public MDMAKristallItem() {
        super(new Properties().stacksTo(32));
    }

    public static ItemStack create(MDMAQuality quality, int count) {
        ItemStack stack = new ItemStack(MDMAItems.MDMA_KRISTALL.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Quality", quality.name());
        return stack;
    }

    public static MDMAQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            try {
                return MDMAQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException e) {
                return MDMAQuality.STANDARD;
            }
        }
        return MDMAQuality.STANDARD;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        MDMAQuality quality = getQuality(stack);
        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.mdma.white_crystals"));
        tooltip.add(Component.translatable("tooltip.mdma.next_step_press"));
    }

    @Override
    public Component getName(ItemStack stack) {
        MDMAQuality quality = getQuality(stack);
        return Component.literal(quality.getColorCode() + "MDMA-Kristalle");
    }
}
