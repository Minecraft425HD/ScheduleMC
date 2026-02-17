package de.rolandsw.schedulemc.chocolate.blockentity;

import de.rolandsw.schedulemc.chocolate.items.ChocolateItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Large Molding Station
 * - Produziert 500g Schokoladentafeln
 * - 2.0x Geschwindigkeit (300 Ticks = 15 Sekunden)
 * - Premium-Tafelgröße für Geschenksets
 */
public class LargeMoldingStationBlockEntity extends AbstractMoldingStationBlockEntity implements MenuProvider {
    public LargeMoldingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ChocolateBlockEntities.LARGE_MOLDING_STATION.get(), pos, state);
    }

    @Override
    protected double getBarSize() {
        return 0.5; // 500g
    }

    @Override
    protected double getSpeedMultiplier() {
        return 2.0; // 2x faster
    }

    @Override
    protected Item getChocolateBarItem() {
        return ChocolateItems.CHOCOLATE_BAR.get();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.large_molding_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new de.rolandsw.schedulemc.chocolate.menu.LargeMoldingStationMenu(id, inv, this);
    }
}
