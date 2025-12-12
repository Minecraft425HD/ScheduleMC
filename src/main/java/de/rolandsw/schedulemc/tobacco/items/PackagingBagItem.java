package de.rolandsw.schedulemc.tobacco.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Verpackungstüte für 1g Tabak
 * Kostet 1€ beim Kaufen
 */
public class PackagingBagItem extends Item {

    public PackagingBagItem() {
        super(new Properties()
                .stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Für 1g Tabak"));
        tooltip.add(Component.literal("§7Preis: §e1€"));
    }
}
