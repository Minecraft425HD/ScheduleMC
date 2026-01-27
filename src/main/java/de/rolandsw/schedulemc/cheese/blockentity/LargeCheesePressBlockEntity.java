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

public class LargeCheesePressBlockEntity extends AbstractCheesePressBlockEntity implements MenuProvider {
    public LargeCheesePressBlockEntity(BlockPos pos, BlockState state) {
        super(CheeseBlockEntities.LARGE_CHEESE_PRESS.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 64; // 64 curd items = 16kg cheese wheel
    }

    @Override
    protected int getPressingTimePerItem() {
        return 200; // 10 seconds per item
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.large_cheese_press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return null; // Menu will be created later
    }
}
