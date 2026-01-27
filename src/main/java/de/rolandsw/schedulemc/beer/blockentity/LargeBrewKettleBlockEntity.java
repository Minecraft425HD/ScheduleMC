package de.rolandsw.schedulemc.beer.blockentity;

import de.rolandsw.schedulemc.beer.menu.LargeBrewKettleMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Large Brew Kettle
 * - Capacity: 32 items
 * - Speed: 2.0x (fastest)
 */
public class LargeBrewKettleBlockEntity extends AbstractBrewKettleBlockEntity {
    public LargeBrewKettleBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.LARGE_BREW_KETTLE.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 32;
    }

    @Override
    protected double getSpeedMultiplier() {
        return 2.0;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.large_brew_kettle");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new LargeBrewKettleMenu(id, inv, this);
    }
}
