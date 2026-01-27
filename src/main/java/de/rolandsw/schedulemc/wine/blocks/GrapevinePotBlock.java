package de.rolandsw.schedulemc.wine.blocks;

import de.rolandsw.schedulemc.wine.WineType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class GrapevinePotBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 8.0, 14.0);
    private final WineType wineType;

    public GrapevinePotBlock(WineType wineType, Properties properties) {
        super(properties);
        this.wineType = wineType;
    }

    public WineType getWineType() {
        return wineType;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                        @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }
}
