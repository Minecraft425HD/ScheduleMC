package de.rolandsw.schedulemc.coffee.blockentity;
import de.rolandsw.schedulemc.coffee.menu.MediumCoffeeRoasterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class MediumCoffeeRoasterBlockEntity extends AbstractCoffeeRoasterBlockEntity implements MenuProvider {
    public MediumCoffeeRoasterBlockEntity(BlockPos pos, BlockState state) {
        super(CoffeeBlockEntities.MEDIUM_COFFEE_ROASTER.get(), pos, state);
    }
    @Override protected int getCapacity() { return 32; }
    @Override protected int getRoastingTimePerBean() { return 250; }
    @Override public @NotNull Component getDisplayName() {
        return Component.translatable("block.medium_coffee_roaster.name");
    }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new MediumCoffeeRoasterMenu(id, inv, this);
    }
}
