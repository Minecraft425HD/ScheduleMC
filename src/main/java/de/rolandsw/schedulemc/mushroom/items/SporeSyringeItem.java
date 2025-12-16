package de.rolandsw.schedulemc.mushroom.items;

import de.rolandsw.schedulemc.mushroom.MushroomType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Sporen-Spritze zum Impfen von Substrat
 */
public class SporeSyringeItem extends Item {

    private final MushroomType mushroomType;

    public SporeSyringeItem(MushroomType mushroomType) {
        super(new Properties().stacksTo(16));
        this.mushroomType = mushroomType;
    }

    public MushroomType getMushroomType() {
        return mushroomType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Sorte: " + mushroomType.getColoredName()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§7Potenz: §e" + (int)(mushroomType.getPotencyMultiplier() * 100) + "%"));
        tooltip.add(Component.literal("§7Flushes: §e" + mushroomType.getMaxFlushes()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§8Rechtsklick auf Topf mit Mist"));
        tooltip.add(Component.literal("§8um Myzel zu impfen"));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(mushroomType.getColorCode() + "Sporen-Spritze (" + mushroomType.getDisplayName() + ")");
    }
}
