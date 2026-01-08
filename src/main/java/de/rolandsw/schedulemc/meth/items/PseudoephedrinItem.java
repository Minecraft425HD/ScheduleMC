package de.rolandsw.schedulemc.meth.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Pseudoephedrin - Alternative Grundzutat für Meth-Herstellung
 * Gibt bessere Qualität als Ephedrin
 */
public class PseudoephedrinItem extends Item {

    public PseudoephedrinItem() {
        super(new Properties()
                .stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Hochwertige Grundsubstanz"));
        tooltip.add(Component.literal("§a+10% Qualitätsbonus"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§fWeißes kristallines Pulver"));
        tooltip.add(Component.literal("§8Verwendung: Chemie-Mixer"));
    }
}
