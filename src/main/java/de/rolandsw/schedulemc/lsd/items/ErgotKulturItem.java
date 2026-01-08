package de.rolandsw.schedulemc.lsd.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Ergot-Kultur - Fermentiertes Mutterkorn
 * Zwischenprodukt aus dem Fermentations-Tank
 */
public class ErgotKulturItem extends Item {

    public ErgotKulturItem() {
        super(new Properties().stacksTo(32));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ergot_kultur.fermented_culture"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.ergot_kultur.dark_mass"));
        tooltip.add(Component.translatable("tooltip.ergot_kultur.rich_alkaloids"));
        tooltip.add(Component.translatable("tooltip.ergot_kultur.next_distillation"));
    }
}
