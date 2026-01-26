package de.rolandsw.schedulemc.coffee.blockentity;
import de.rolandsw.schedulemc.coffee.menu.LargeDryingTrayMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class LargeDryingTrayBlockEntity extends AbstractCoffeeDryingTrayBlockEntity implements MenuProvider {
    public LargeDryingTrayBlockEntity(BlockPos pos, BlockState state) {
        super(CoffeeBlockEntities.LARGE_DRYING_TRAY.get(), pos, state);
    }
    @Override protected int getCapacity() { return 30; }
    @Override protected int getDryingTimePerCherry() { return 400; }
    @Override public @NotNull Component getDisplayName() {
        return Component.translatable("block.large_coffee_drying_tray.name");
    }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new LargeDryingTrayMenu(id, inv, this);
    }
}
