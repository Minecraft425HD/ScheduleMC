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
 * Small Conching Machine
 * - 2 Input-Slots (1 Cocoa Mass + 1 Ingredient)
 * - 1.0x Geschwindigkeit (2400 Ticks = 2 Minuten)
 * - Ideal für kleine Schokoladen-Chargen
 */
public class SmallConchingMachineBlockEntity extends AbstractConchingMachineBlockEntity implements MenuProvider {
    public SmallConchingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ChocolateBlockEntities.SMALL_CONCHING_MACHINE.get(), pos, state);
    }

    @Override
    protected int getIngredientSlots() {
        return 1; // 1 ingredient slot
    }

    @Override
    protected double getSpeedMultiplier() {
        return 1.0; // Normal speed
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.small_conching_machine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new de.rolandsw.schedulemc.chocolate.menu.SmallConchingMachineMenu(id, inv, this);
    }
}
