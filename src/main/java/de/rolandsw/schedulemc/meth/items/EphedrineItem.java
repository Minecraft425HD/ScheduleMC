package de.rolandsw.schedulemc.meth.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Ephedrine - Grundzutat für Meth-Herstellung
 * Wird im Chemie-Mixer verarbeitet
 */
public class EphedrineItem extends Item {

    public EphedrineItem() {
        super(new Properties()
                .stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ephedrin.chemical_base"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.ephedrin.white_powder"));
        tooltip.add(Component.translatable("tooltip.ephedrin.use_mixer"));
    }
}
