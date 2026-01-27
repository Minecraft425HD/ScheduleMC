package de.rolandsw.schedulemc.wine.items;

import de.rolandsw.schedulemc.wine.WineType;
import de.rolandsw.schedulemc.wine.blocks.WineBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Traubensetzling zum Pflanzen in Weinreben-Töpfe
 */
public class GrapeSeedlingItem extends Item {
    private final WineType wineType;

    public GrapeSeedlingItem(WineType wineType, Properties properties) {
        super(properties);
        this.wineType = wineType;
    }

    public WineType getWineType() {
        return wineType;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.literal("§7Typ: " + wineType.getDisplayName()));
        tooltip.add(Component.literal("§7Wachstumszeit: §f" + wineType.getGrowthTimeDays() + " Tage"));
        tooltip.add(Component.literal("§7Ertrag: §f" + wineType.getYieldPerPlant() + " Trauben"));
        tooltip.add(Component.literal("§8Zum Pflanzen in Topf rechtsklicken"));
    }
}
