package de.rolandsw.schedulemc.coffee.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Kaffeekirschen - frisch geerntet von Kaffeebäumen
 * Enthalten 2 Kaffeebohnen pro Kirsche
 */
public class CoffeeCherryItem extends Item {

    public CoffeeCherryItem() {
        super(new Properties().stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.coffee_cherry.description"));
        tooltip.add(Component.translatable("tooltip.coffee_cherry.process_hint"));
    }
}
