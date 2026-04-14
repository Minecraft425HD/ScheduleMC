package de.rolandsw.schedulemc.meth.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Iodine - Katalysator für Meth-Herstellung
 * Wird im Chemie-Mixer benötigt
 */
public class IodineItem extends Item {

    public IodineItem() {
        super(new Properties()
                .stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.jod.catalyst"));
        tooltip.add(Component.translatable("tooltip.jod.use_mixer"));
    }
}
