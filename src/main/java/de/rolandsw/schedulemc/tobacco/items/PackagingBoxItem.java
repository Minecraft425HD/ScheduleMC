package de.rolandsw.schedulemc.tobacco.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Verpackungsschachtel für 10g Tabak
 * Kostet 6€ beim Kaufen
 */
public class PackagingBoxItem extends Item {

    public PackagingBoxItem() {
        super(new Properties()
                .stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Für 10g Tabak"));
        tooltip.add(Component.literal("§7Preis: §e6€"));
    }
}
