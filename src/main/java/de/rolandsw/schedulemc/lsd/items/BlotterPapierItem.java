package de.rolandsw.schedulemc.lsd.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Blotter-Papier - Leeres Löschpapier für LSD
 * Wird in der Perforations-Presse mit LSD-Lösung versehen
 */
public class BlotterPapierItem extends Item {

    public BlotterPapierItem() {
        super(new Properties().stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.blotter_paper.absorbent_paper"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.blotter_paper.white_perforated"));
        tooltip.add(Component.translatable("tooltip.blotter_paper.ready_for_lsd"));
        tooltip.add(Component.translatable("tooltip.blotter_paper.use_press"));
    }
}
