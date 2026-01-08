package de.rolandsw.schedulemc.tobacco.items;

import de.rolandsw.schedulemc.tobacco.TobaccoType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Tabaksamen - verschiedene Sorten (zum Pflanzen in Töpfe)
 */
public class TobaccoSeedItem extends Item {

    private final TobaccoType tobaccoType;

    public TobaccoSeedItem(TobaccoType type) {
        super(new Properties().stacksTo(64));
        this.tobaccoType = type;
    }

    public TobaccoType getTobaccoType() {
        return tobaccoType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tobacco.type_label").append(tobaccoType.getColoredName()));
        tooltip.add(Component.translatable("tooltip.tobacco_seed.growth_time", (tobaccoType.getGrowthTicks() / 20)));
        tooltip.add(Component.translatable("tooltip.tobacco_seed.yield", tobaccoType.getBaseYield()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.tobacco_seed.plant_in_pot"));
    }
}
