package de.rolandsw.schedulemc.beer.blockentity;

import de.rolandsw.schedulemc.beer.menu.SmallBeerFermentationTankMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Small Beer Fermentation Tank
 * - Capacity: 8 items
 * - Speed: 1.0x (base speed)
 */
public class SmallBeerFermentationTankBlockEntity extends AbstractBeerFermentationTankBlockEntity {
    public SmallBeerFermentationTankBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.SMALL_BEER_FERMENTATION_TANK.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 8;
    }

    @Override
    protected double getSpeedMultiplier() {
        return 1.0;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.small_beer_fermentation_tank");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new SmallBeerFermentationTankMenu(id, inv, this);
    }
}
