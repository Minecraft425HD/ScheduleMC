package de.rolandsw.schedulemc.tobacco.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Verpackungsglas für 5g Tabak
 * Kostet 3€ beim Kaufen
 */
public class PackagingJarItem extends Item {

    public PackagingJarItem() {
        super(new Properties()
                .stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Für 5g Tabak"));
        tooltip.add(Component.literal("§7Preis: §e3€"));
    }
}
