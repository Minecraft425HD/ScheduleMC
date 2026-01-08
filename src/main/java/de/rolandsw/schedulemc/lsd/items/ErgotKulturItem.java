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
        tooltip.add(Component.literal("§7Fermentierte Pilzkultur"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§5Dunkle, klebrige Masse"));
        tooltip.add(Component.literal("§8Reich an Ergot-Alkaloiden"));
        tooltip.add(Component.literal("§8Nächster Schritt: Destillations-Apparat"));
    }
}
