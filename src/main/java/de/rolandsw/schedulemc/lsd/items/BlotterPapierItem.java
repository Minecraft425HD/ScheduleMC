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
        tooltip.add(Component.literal("§7Saugfähiges Spezialpapier"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§fWeißes, perforiertes Papier"));
        tooltip.add(Component.literal("§8Bereit für LSD-Lösung"));
        tooltip.add(Component.literal("§8Verwendung: Perforations-Presse"));
    }
}
