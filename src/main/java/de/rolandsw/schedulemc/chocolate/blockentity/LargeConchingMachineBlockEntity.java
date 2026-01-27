package de.rolandsw.schedulemc.chocolate.blockentity;

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
 * Large Conching Machine
 * - 6 Input-Slots (1 Cocoa Mass + 5 Ingredients)
 * - 2.0x Geschwindigkeit (1200 Ticks = 60 Sekunden)
 * - Beste Option für große Produktionen
 */
public class LargeConchingMachineBlockEntity extends AbstractConchingMachineBlockEntity implements MenuProvider {
    public LargeConchingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ChocolateBlockEntities.LARGE_CONCHING_MACHINE.get(), pos, state);
    }

    @Override
    protected int getIngredientSlots() {
        return 5; // 5 ingredient slots
    }

    @Override
    protected double getSpeedMultiplier() {
        return 2.0; // 2x faster
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.large_conching_machine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return null; // TODO: Create menu in Part 3
    }
}
