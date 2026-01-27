package de.rolandsw.schedulemc.cheese.blockentity;

import de.rolandsw.schedulemc.cheese.menu.SmallAgingCaveMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SmallAgingCaveBlockEntity extends AbstractAgingCaveBlockEntity implements MenuProvider {
    public SmallAgingCaveBlockEntity(BlockPos pos, BlockState state) {
        super(CheeseBlockEntities.SMALL_AGING_CAVE.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 4; // 4 cheese wheels
    }

    @Override
    protected double getAgingSpeedMultiplier() {
        return 1.0; // Normal speed
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.small_aging_cave");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new SmallAgingCaveMenu(id, inv, this);
    }
}
