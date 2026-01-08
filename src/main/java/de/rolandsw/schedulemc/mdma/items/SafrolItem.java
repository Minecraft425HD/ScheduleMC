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
public class SafrolItem extends Item {

    public SafrolItem() {
        super(new Properties().stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Aromatisches Öl"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§eGelbliche Flüssigkeit"));
        tooltip.add(Component.literal("§8Riecht nach Anis"));
        tooltip.add(Component.literal("§8Verwendung: Reaktions-Kessel"));
    }
}
