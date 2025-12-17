package de.rolandsw.schedulemc.mdma.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Bindemittel - Für die Pillen-Presse
 * Hält die Pille zusammen
 */
public class BindemittelItem extends Item {

    public BindemittelItem() {
        super(new Properties().stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Tablettierhilfsstoff"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§fWeißes Pulver"));
        tooltip.add(Component.literal("§8Verwendung: Pillen-Presse"));
    }
}
