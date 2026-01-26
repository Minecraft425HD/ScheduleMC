package de.rolandsw.schedulemc.coffee.items;

import de.rolandsw.schedulemc.coffee.CoffeeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Kaffee-Setzlinge - verschiedene Sorten (zum Pflanzen in Töpfe)
 */
public class CoffeeSeedlingItem extends Item {

    private final CoffeeType coffeeType;

    public CoffeeSeedlingItem(CoffeeType type) {
        super(new Properties().stacksTo(64));
        this.coffeeType = type;
    }

    public CoffeeType getCoffeeType() {
        return coffeeType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.coffee.type_label").append(coffeeType.getColoredName()));
        tooltip.add(Component.translatable("tooltip.coffee_seedling.growth_time", (coffeeType.getGrowthTicks() / 20)));
        tooltip.add(Component.translatable("tooltip.coffee_seedling.yield", coffeeType.getBaseYield()));
        tooltip.add(Component.translatable("tooltip.coffee_seedling.optimal_altitude", coffeeType.getOptimalAltitude()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.coffee_seedling.plant_in_pot"));
    }
}
