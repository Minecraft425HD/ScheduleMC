package de.rolandsw.schedulemc.meth.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Jod - Katalysator für Meth-Herstellung
 * Wird im Chemie-Mixer benötigt
 */
public class JodItem extends Item {

    public JodItem() {
        super(new Properties()
                .stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.jod.catalyst"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.jod.dark_violet"));
        tooltip.add(Component.translatable("tooltip.jod.use_mixer"));
    }
}
