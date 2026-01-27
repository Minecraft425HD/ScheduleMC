package de.rolandsw.schedulemc.honey.blockentity;

import de.rolandsw.schedulemc.honey.menu.LargeAgingChamberMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Large Aging Chamber - 16 slots, 2.0x speed
 */
public class LargeAgingChamberBlockEntity extends AbstractAgingChamberBlockEntity implements MenuProvider {
    public LargeAgingChamberBlockEntity(BlockPos pos, BlockState state) {
        super(HoneyBlockEntities.LARGE_AGING_CHAMBER.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 16;
    }

    @Override
    protected float getSpeedMultiplier() {
        return 2.0f;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.large_aging_chamber");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new LargeAgingChamberMenu(id, inv, this);
    }
}
