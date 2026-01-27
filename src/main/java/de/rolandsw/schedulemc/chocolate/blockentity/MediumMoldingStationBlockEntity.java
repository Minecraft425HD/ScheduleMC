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
 * Medium Molding Station
 * - Produziert 200g Schokoladentafeln
 * - 1.5x Geschwindigkeit (400 Ticks = 20 Sekunden)
 * - Standard-Tafelgröße für den Verkauf
 */
public class MediumMoldingStationBlockEntity extends AbstractMoldingStationBlockEntity implements MenuProvider {
    public MediumMoldingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ChocolateBlockEntities.MEDIUM_MOLDING_STATION.get(), pos, state);
    }

    @Override
    protected double getBarSize() {
        return 0.2; // 200g
    }

    @Override
    protected double getSpeedMultiplier() {
        return 1.5; // 1.5x faster
    }

    @Override
    protected Item getChocolateBarItem() {
        return ChocolateItems.CHOCOLATE_BAR_200G.get();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.medium_molding_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new de.rolandsw.schedulemc.chocolate.menu.MediumMoldingStationMenu(id, inv, this);
    }
}
