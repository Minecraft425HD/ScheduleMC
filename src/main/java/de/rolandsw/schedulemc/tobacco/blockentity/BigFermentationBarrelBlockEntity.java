package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.config.ModConfigHandler;
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
 * Großes Fermentierungsfass BlockEntity
 * Kapazität und Fermentierungszeit werden aus der Config gelesen.
 */
public class BigFermentationBarrelBlockEntity extends AbstractFermentationBarrelBlockEntity implements MenuProvider {

    public BigFermentationBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.BIG_FERMENTATION_BARREL.get(), pos, state,
                ModConfigHandler.TOBACCO.BIG_FERMENTATION_BARREL_CAPACITY::get,
                ModConfigHandler.TOBACCO.TOBACCO_FERMENTING_TIME::get);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.big_fermentation_barrel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new de.rolandsw.schedulemc.tobacco.menu.BigFermentationBarrelMenu(id, inv, this);
    }
}
