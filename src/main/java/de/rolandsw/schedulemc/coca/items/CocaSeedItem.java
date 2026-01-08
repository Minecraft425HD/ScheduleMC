package de.rolandsw.schedulemc.coca.items;

import de.rolandsw.schedulemc.coca.CocaType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Koka-Samen - verschiedene Sorten (zum Pflanzen in Töpfe)
 */
public class CocaSeedItem extends Item {

    private final CocaType cocaType;

    public CocaSeedItem(CocaType type) {
        super(new Properties().stacksTo(64));
        this.cocaType = type;
    }

    public CocaType getCocaType() {
        return cocaType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.coca.type_label").append(cocaType.getColoredName()));
        tooltip.add(Component.translatable("tooltip.coca_seed.growth_time", (cocaType.getGrowthTicks() / 20)));
        tooltip.add(Component.translatable("tooltip.coca_seed.yield", cocaType.getBaseYield()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.coca_seed.plant_in_pot"));
    }
}
