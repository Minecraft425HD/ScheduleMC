package de.rolandsw.schedulemc.coffee.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Grüne Kaffeebohnen - nach Processing, vor Röstung
 * Können gelagert oder geröstet werden
 */
public class GreenCoffeeBeanItem extends Item {

    public GreenCoffeeBeanItem() {
        super(new Properties().stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.green_coffee_bean.description"));
        tooltip.add(Component.translatable("tooltip.green_coffee_bean.roast_hint"));
    }
}
