package de.rolandsw.schedulemc.lsd.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Lysergsäure - Destilliertes Ergot-Extrakt
 * Grundsubstanz für LSD-Synthese
 */
public class LysergsaeureItem extends Item {

    public LysergsaeureItem() {
        super(new Properties().stacksTo(16));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.lysergsaeure.pure_lysergic"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.lysergsaeure.white_powder"));
        tooltip.add(Component.translatable("tooltip.lysergsaeure.highly_concentrated"));
        tooltip.add(Component.translatable("tooltip.lysergsaeure.next_microdosing"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.lysergsaeure.light_sensitive"));
    }
}
