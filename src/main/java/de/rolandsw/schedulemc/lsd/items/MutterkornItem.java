package de.rolandsw.schedulemc.lsd.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Mutterkorn (Ergot) - Grundzutat für LSD
 * Enthält Ergot-Alkaloide die zu Lysergsäure verarbeitet werden
 */
public class MutterkornItem extends Item {

    public MutterkornItem() {
        super(new Properties().stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ergot.fungal_infection"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.ergot.dark_grains"));
        tooltip.add(Component.translatable("tooltip.ergot.contains_alkaloids"));
        tooltip.add(Component.translatable("tooltip.ergot.use_fermentation"));
    }
}
