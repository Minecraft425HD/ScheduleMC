package de.rolandsw.schedulemc.beer.blockentity;

import de.rolandsw.schedulemc.beer.menu.MediumBeerFermentationTankMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Medium Beer Fermentation Tank
 * - Capacity: 16 items
 * - Speed: 1.5x (faster)
 */
public class MediumBeerFermentationTankBlockEntity extends AbstractBeerFermentationTankBlockEntity {
    public MediumBeerFermentationTankBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.MEDIUM_BEER_FERMENTATION_TANK.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 16;
    }

    @Override
    protected double getSpeedMultiplier() {
        return 1.5;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.medium_beer_fermentation_tank");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new MediumBeerFermentationTankMenu(id, inv, this);
    }
}
