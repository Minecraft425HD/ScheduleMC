package de.rolandsw.schedulemc.coffee.blockentity;
import de.rolandsw.schedulemc.coffee.menu.LargeCoffeeRoasterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class LargeCoffeeRoasterBlockEntity extends AbstractCoffeeRoasterBlockEntity implements MenuProvider {
    public LargeCoffeeRoasterBlockEntity(BlockPos pos, BlockState state) {
        super(CoffeeBlockEntities.LARGE_COFFEE_ROASTER.get(), pos, state);
    }
    @Override protected int getCapacity() { return 64; }
    @Override protected int getRoastingTimePerBean() { return 200; }
    @Override public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.large_coffee_roaster");
    }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new LargeCoffeeRoasterMenu(id, inv, this);
    }
}
