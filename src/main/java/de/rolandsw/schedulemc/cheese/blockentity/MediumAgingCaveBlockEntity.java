package de.rolandsw.schedulemc.cheese.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MediumAgingCaveBlockEntity extends AbstractAgingCaveBlockEntity implements MenuProvider {
    public MediumAgingCaveBlockEntity(BlockPos pos, BlockState state) {
        super(CheeseBlockEntities.MEDIUM_AGING_CAVE.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 9; // 9 cheese wheels
    }

    @Override
    protected double getAgingSpeedMultiplier() {
        return 1.2; // 20% faster aging
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.medium_aging_cave");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return null; // Menu will be created later
    }
}
