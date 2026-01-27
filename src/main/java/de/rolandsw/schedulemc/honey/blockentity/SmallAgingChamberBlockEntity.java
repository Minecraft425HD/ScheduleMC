package de.rolandsw.schedulemc.honey.blockentity;

import de.rolandsw.schedulemc.honey.menu.SmallAgingChamberMenu;
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
 * Small Aging Chamber - 4 slots, 1.0x speed
 */
public class SmallAgingChamberBlockEntity extends AbstractAgingChamberBlockEntity implements MenuProvider {
    public SmallAgingChamberBlockEntity(BlockPos pos, BlockState state) {
        super(HoneyBlockEntities.SMALL_AGING_CHAMBER.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 4;
    }

    @Override
    protected float getSpeedMultiplier() {
        return 1.0f;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.small_aging_chamber");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new SmallAgingChamberMenu(id, inv, this);
    }
}
