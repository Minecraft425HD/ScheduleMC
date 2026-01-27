package de.rolandsw.schedulemc.beer.blockentity;

import de.rolandsw.schedulemc.beer.menu.LargeConditioningTankMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Large Conditioning Tank
 * - Slots: 16 (4x4)
 * - Speed: 2.0x (fastest aging)
 */
public class LargeConditioningTankBlockEntity extends AbstractConditioningTankBlockEntity {
    public LargeConditioningTankBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.LARGE_CONDITIONING_TANK.get(), pos, state);
    }

    @Override
    protected int getSlotCount() {
        return 16; // 4x4 grid
    }

    @Override
    protected double getSpeedMultiplier() {
        return 2.0;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.large_conditioning_tank");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new LargeConditioningTankMenu(id, inv, this);
    }
}
