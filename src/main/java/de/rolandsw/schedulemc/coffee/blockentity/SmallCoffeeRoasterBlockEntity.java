package de.rolandsw.schedulemc.coffee.blockentity;
import de.rolandsw.schedulemc.coffee.menu.SmallCoffeeRoasterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class SmallCoffeeRoasterBlockEntity extends AbstractCoffeeRoasterBlockEntity implements MenuProvider {
    public SmallCoffeeRoasterBlockEntity(BlockPos pos, BlockState state) {
        super(CoffeeBlockEntities.SMALL_COFFEE_ROASTER.get(), pos, state);
    }
    @Override protected int getCapacity() { return 16; }
    @Override protected int getRoastingTimePerBean() { return 300; }
    @Override public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.small_coffee_roaster");
    }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new SmallCoffeeRoasterMenu(id, inv, this);
    }
}
