package de.rolandsw.schedulemc.wine.blockentity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
public class LargeFermentationTankBlockEntity extends AbstractFermentationTankBlockEntity {
    public LargeFermentationTankBlockEntity(BlockPos pos, BlockState state) {
        super(WineBlockEntities.LARGE_FERMENTATION_TANK.get(), pos, state);
    }
    @Override protected int getCapacity() { return 64; }
    @Override protected int getFermentationTimePerItem() { return 800; } // 40 seconds
}
