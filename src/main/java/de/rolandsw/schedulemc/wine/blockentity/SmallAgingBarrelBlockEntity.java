package de.rolandsw.schedulemc.wine.blockentity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
public class SmallAgingBarrelBlockEntity extends AbstractAgingBarrelBlockEntity {
    public SmallAgingBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(WineBlockEntities.SMALL_AGING_BARREL.get(), pos, state);
    }
    @Override protected int getCapacity() { return 16; }
}
