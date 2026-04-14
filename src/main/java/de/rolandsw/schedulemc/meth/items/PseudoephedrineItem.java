package de.rolandsw.schedulemc.meth.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Pseudoephedrine - Alternative Grundzutat für Meth-Herstellung
 * Gibt bessere Qualität als Ephedrine
 */
public class PseudoephedrineItem extends Item {

    public PseudoephedrineItem() {
        super(new Properties()
                .stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.pseudoephedrin.high_quality"));
        tooltip.add(Component.translatable("tooltip.pseudoephedrin.quality_bonus"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.pseudoephedrin.white_powder"));
        tooltip.add(Component.translatable("tooltip.pseudoephedrin.use_mixer"));
    }
}
