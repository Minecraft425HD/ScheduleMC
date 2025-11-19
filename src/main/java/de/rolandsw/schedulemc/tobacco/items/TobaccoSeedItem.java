package de.rolandsw.schedulemc.tobacco.items;

import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Tabaksamen - platziert Pflanzen auf dem Boden
 */
public class TobaccoSeedItem extends ItemNameBlockItem {

    private final TobaccoType tobaccoType;

    public TobaccoSeedItem(TobaccoType type, Block plantBlock) {
        super(plantBlock, new Properties().stacksTo(64));
        this.tobaccoType = type;
    }

    public TobaccoType getTobaccoType() {
        return tobaccoType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Sorte: " + tobaccoType.getColoredName()));
        tooltip.add(Component.literal("§7Wachstumszeit: §e~" + (tobaccoType.getGrowthTicks() / 20) + "s"));
        tooltip.add(Component.literal("§7Ertrag: §e~" + tobaccoType.getBaseYield() + " Blätter"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§8Rechtsklick auf Erde zum Pflanzen"));
    }
}
