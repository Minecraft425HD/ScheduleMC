package de.rolandsw.schedulemc.wine.blockentity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
public class SmallFermentationTankBlockEntity extends AbstractFermentationTankBlockEntity {
    public SmallFermentationTankBlockEntity(BlockPos pos, BlockState state) {
        super(WineBlockEntities.SMALL_FERMENTATION_TANK.get(), pos, state);
    }
    @Override protected int getCapacity() { return 16; }
    @Override protected int getFermentationTimePerItem() { return 1200; } // 60 seconds
}
