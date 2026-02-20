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
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return Component.translatable("item.schedulemc.grape_seedling.name");
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.grape_seedling.type", wineType.getDisplayName()));
        tooltip.add(Component.translatable("tooltip.grape_seedling.growth_time", wineType.getGrowthTimeDays()));
        tooltip.add(Component.translatable("tooltip.grape_seedling.yield", wineType.getYieldPerPlant()));
        tooltip.add(Component.translatable("tooltip.grape_seedling.hint"));
    }
}
