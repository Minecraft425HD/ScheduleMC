package de.rolandsw.schedulemc.wine.blockentity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
public class LargeAgingBarrelBlockEntity extends AbstractAgingBarrelBlockEntity {
    public LargeAgingBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(WineBlockEntities.LARGE_AGING_BARREL.get(), pos, state);
    }
    @Override protected int getCapacity() { return 64; }
}
