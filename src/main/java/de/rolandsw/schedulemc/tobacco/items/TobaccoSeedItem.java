package de.rolandsw.schedulemc.tobacco.items;

import de.rolandsw.schedulemc.tobacco.TobaccoType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Tabaksamen - verschiedene Sorten
 */
public class TobaccoSeedItem extends Item {
    
    private final TobaccoType tobaccoType;
    
    public TobaccoSeedItem(TobaccoType type) {
        super(new Properties()
                .stacksTo(64));
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
        tooltip.add(Component.literal("§8Pflanze in einen Topf mit Erde"));
    }
}
