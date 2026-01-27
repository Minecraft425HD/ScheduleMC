package de.rolandsw.schedulemc.coffee.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Verpackter Kaffee - bereit für Verkauf
 * Verschiedene Größen: 250g, 500g, 1kg
 */
public class PackagedCoffeeItem extends Item {

    public PackagedCoffeeItem() {
        super(new Properties().stacksTo(16));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.packaged_coffee.description"));
        tooltip.add(Component.translatable("tooltip.packaged_coffee.sell_hint"));
    }
}
