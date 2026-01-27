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

public class SmallCheesePressBlockEntity extends AbstractCheesePressBlockEntity implements MenuProvider {
    public SmallCheesePressBlockEntity(BlockPos pos, BlockState state) {
        super(CheeseBlockEntities.SMALL_CHEESE_PRESS.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 16; // 16 curd items = 4kg cheese wheel
    }

    @Override
    protected int getPressingTimePerItem() {
        return 300; // 15 seconds per item
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.small_cheese_press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new de.rolandsw.schedulemc.cheese.menu.SmallCheesePressMenu(id, inv, this);
    }
}
