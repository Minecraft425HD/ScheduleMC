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
 * Medium Conching Machine
 * - 4 Input-Slots (1 Cocoa Mass + 3 Ingredients)
 * - 1.5x Geschwindigkeit (1600 Ticks = 80 Sekunden)
 * - Gut für mittlere Produktionen
 */
public class MediumConchingMachineBlockEntity extends AbstractConchingMachineBlockEntity implements MenuProvider {
    public MediumConchingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ChocolateBlockEntities.MEDIUM_CONCHING_MACHINE.get(), pos, state);
    }

    @Override
    protected int getIngredientSlots() {
        return 3; // 3 ingredient slots
    }

    @Override
    protected double getSpeedMultiplier() {
        return 1.5; // 1.5x faster
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.medium_conching_machine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return null; // TODO: Create menu in Part 3
    }
}
