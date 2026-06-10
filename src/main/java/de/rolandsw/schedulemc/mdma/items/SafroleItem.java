package de.rolandsw.schedulemc.mdma.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Safrol - Grundzutat für MDMA
 * Gewonnen aus Sassafras-Öl
 */
public class SafroleItem extends Item {

    public SafroleItem() {
        super(new Properties().stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.safrole.aromatic_oil"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.safrole.yellowish_liquid"));
        tooltip.add(Component.translatable("tooltip.safrole.smells_anise"));
        tooltip.add(Component.translatable("tooltip.safrole.use_kettle"));
    }
}
