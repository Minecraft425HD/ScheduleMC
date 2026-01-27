package de.rolandsw.schedulemc.wine.blockentity;

import de.rolandsw.schedulemc.wine.menu.LargeAgingBarrelMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LargeAgingBarrelBlockEntity extends AbstractAgingBarrelBlockEntity implements MenuProvider {
    public LargeAgingBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(WineBlockEntities.LARGE_AGING_BARREL.get(), pos, state);
    }
    @Override protected int getCapacity() { return 64; }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.large_aging_barrel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new LargeAgingBarrelMenu(id, inv, this);
    }
}
