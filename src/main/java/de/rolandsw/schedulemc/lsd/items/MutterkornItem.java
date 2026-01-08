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
        tooltip.add(Component.translatable("tooltip.mutterkorn.fungal_infection"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.mutterkorn.dark_grains"));
        tooltip.add(Component.translatable("tooltip.mutterkorn.contains_alkaloids"));
        tooltip.add(Component.translatable("tooltip.mutterkorn.use_fermentation"));
    }
}
