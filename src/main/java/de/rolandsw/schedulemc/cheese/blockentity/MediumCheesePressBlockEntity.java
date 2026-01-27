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

public class MediumCheesePressBlockEntity extends AbstractCheesePressBlockEntity implements MenuProvider {
    public MediumCheesePressBlockEntity(BlockPos pos, BlockState state) {
        super(CheeseBlockEntities.MEDIUM_CHEESE_PRESS.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 32; // 32 curd items = 8kg cheese wheel
    }

    @Override
    protected int getPressingTimePerItem() {
        return 250; // 12.5 seconds per item
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.medium_cheese_press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return null; // Menu will be created later
    }
}
