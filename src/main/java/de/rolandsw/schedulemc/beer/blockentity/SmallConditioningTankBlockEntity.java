package de.rolandsw.schedulemc.beer.blockentity;

import de.rolandsw.schedulemc.beer.menu.SmallConditioningTankMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Small Conditioning Tank
 * - Slots: 4 (2x2)
 * - Speed: 1.0x (base aging speed)
 */
public class SmallConditioningTankBlockEntity extends AbstractConditioningTankBlockEntity {
    public SmallConditioningTankBlockEntity(BlockPos pos, BlockState state) {
        super(BeerBlockEntities.SMALL_CONDITIONING_TANK.get(), pos, state);
    }

    @Override
    protected int getSlotCount() {
        return 4; // 2x2 grid
    }

    @Override
    protected double getSpeedMultiplier() {
        return 1.0;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.small_conditioning_tank");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new SmallConditioningTankMenu(id, inv, this);
    }
}
