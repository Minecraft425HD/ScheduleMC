package de.rolandsw.schedulemc.honey.blockentity;

import de.rolandsw.schedulemc.honey.menu.MediumAgingChamberMenu;
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
 * Medium Aging Chamber - 9 slots, 1.5x speed
 */
public class MediumAgingChamberBlockEntity extends AbstractAgingChamberBlockEntity implements MenuProvider {
    public MediumAgingChamberBlockEntity(BlockPos pos, BlockState state) {
        super(HoneyBlockEntities.MEDIUM_AGING_CHAMBER.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 9;
    }

    @Override
    protected float getSpeedMultiplier() {
        return 1.5f;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.medium_aging_chamber");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new MediumAgingChamberMenu(id, inv, this);
    }
}
