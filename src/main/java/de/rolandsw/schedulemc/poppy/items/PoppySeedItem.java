package de.rolandsw.schedulemc.poppy.items;

import de.rolandsw.schedulemc.poppy.PoppyType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Mohn-Samen zum Pflanzen
 */
public class PoppySeedItem extends Item {

    private final PoppyType poppyType;

    public PoppySeedItem(PoppyType type) {
        super(new Properties().stacksTo(64));
        this.poppyType = type;
    }

    public PoppyType getPoppyType() {
        return poppyType;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Sorte: " + poppyType.getColoredName()));
        tooltip.add(Component.literal("§7Wachstumszeit: §e" + (poppyType.getGrowthTicks() / 20) + "s"));
        tooltip.add(Component.literal("§7Potenz: §c" + String.format("%.0f%%", poppyType.getPotencyMultiplier() * 100)));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§8In einen Topf mit Erde pflanzen"));
    }
}
