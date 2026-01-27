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
 * Small Molding Station
 * - Produziert 100g Schokoladentafeln
 * - 1.0x Geschwindigkeit (600 Ticks = 30 Sekunden)
 * - Ideal für kleine Chargen und Tests
 */
public class SmallMoldingStationBlockEntity extends AbstractMoldingStationBlockEntity implements MenuProvider {
    public SmallMoldingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ChocolateBlockEntities.SMALL_MOLDING_STATION.get(), pos, state);
    }

    @Override
    protected double getBarSize() {
        return 0.1; // 100g
    }

    @Override
    protected double getSpeedMultiplier() {
        return 1.0; // Normal speed
    }

    @Override
    protected Item getChocolateBarItem() {
        return ChocolateItems.CHOCOLATE_BAR_100G.get();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.small_molding_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new de.rolandsw.schedulemc.chocolate.menu.SmallMoldingStationMenu(id, inv, this);
    }
}
