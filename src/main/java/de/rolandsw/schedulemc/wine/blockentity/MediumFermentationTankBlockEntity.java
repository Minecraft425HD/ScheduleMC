package de.rolandsw.schedulemc.wine.blockentity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
public class MediumFermentationTankBlockEntity extends AbstractFermentationTankBlockEntity {
    public MediumFermentationTankBlockEntity(BlockPos pos, BlockState state) {
        super(WineBlockEntities.MEDIUM_FERMENTATION_TANK.get(), pos, state);
    }
    @Override protected int getCapacity() { return 32; }
    @Override protected int getFermentationTimePerItem() { return 1000; } // 50 seconds
}
