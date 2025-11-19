package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.tobacco.TobaccoType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Tabak-Pflanze Block - wächst in 8 Stufen, wird ab Stufe 4 zwei Blöcke hoch
 */
public class TobaccoPlantBlock extends Block {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_7; // 0-7
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    // X-förmige Collision-Box (wie Pflanzen in Minecraft)
    private static final VoxelShape SHAPE_SMALL = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D); // Stufe 0-3
    private static final VoxelShape SHAPE_MEDIUM = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D); // Stufe 4-6 unten
    private static final VoxelShape SHAPE_TALL_BOTTOM = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D); // Stufe 7 unten
    private static final VoxelShape SHAPE_TALL_TOP = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D); // Stufe 4+ oben

    private final TobaccoType tobaccoType;

    public TobaccoPlantBlock(TobaccoType type) {
        super(BlockBehaviour.Properties.copy(Blocks.WHEAT)
                .noCollission() // Keine Kollision!
                .randomTicks()
                .strength(0.0F)
                .sound(net.minecraft.world.level.block.SoundType.CROP));

        this.tobaccoType = type;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public TobaccoType getTobaccoType() {
        return tobaccoType;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        DoubleBlockHalf half = state.getValue(HALF);

        if (age < 4) {
            return SHAPE_SMALL; // Stufe 0-3: klein, 1 Block hoch
        } else if (half == DoubleBlockHalf.UPPER) {
            return SHAPE_TALL_TOP; // Oberer Block
        } else if (age == 7) {
            return SHAPE_TALL_BOTTOM; // Stufe 7 unten (voll ausgewachsen)
        } else {
            return SHAPE_MEDIUM; // Stufe 4-6 unten
        }
    }

    /**
     * Entfernt Pflanze wenn unterer Block zerstört wird
     */
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                    LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (direction.getAxis() == Direction.Axis.Y) {
            if (half == DoubleBlockHalf.LOWER && direction == Direction.UP) {
                // Unterer Block: prüfe ob oben noch der obere Teil ist
                if (state.getValue(AGE) >= 4 && neighborState.getBlock() != this) {
                    return Blocks.AIR.defaultBlockState();
                }
            } else if (half == DoubleBlockHalf.UPPER && direction == Direction.DOWN) {
                // Oberer Block: muss unterer Teil vorhanden sein
                if (neighborState.getBlock() != this || neighborState.getValue(HALF) != DoubleBlockHalf.LOWER) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        }

        // Untere Hälfte: muss auf solidem Block stehen (Topf)
        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN) {
            return !canSurvive(state, level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }

        // Unterer Block: muss auf TobaccoPotBlock stehen
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return belowState.getBlock() instanceof TobaccoPotBlock;
    }

    /**
     * Wächst zur nächsten Stufe (wird vom TobaccoPotBlockEntity aufgerufen)
     */
    public static void growToStage(Level level, BlockPos potPos, int newAge, TobaccoType type) {
        BlockPos plantPos = potPos.above();

        // Finde den richtigen Pflanzen-Block für diesen Typ
        Block plantBlock = getPlantBlockForType(type);

        if (newAge <= 7) {
            // Setze unteren Block
            BlockState lowerState = plantBlock.defaultBlockState()
                    .setValue(AGE, newAge)
                    .setValue(HALF, DoubleBlockHalf.LOWER);
            level.setBlock(plantPos, lowerState, 3);

            // Ab Stufe 4: setze oberen Block
            if (newAge >= 4) {
                BlockState upperState = plantBlock.defaultBlockState()
                        .setValue(AGE, newAge)
                        .setValue(HALF, DoubleBlockHalf.UPPER);
                level.setBlock(plantPos.above(), upperState, 3);
            } else {
                // Entferne oberen Block falls vorhanden (downgrade)
                BlockState above = level.getBlockState(plantPos.above());
                if (above.getBlock() instanceof TobaccoPlantBlock) {
                    level.setBlock(plantPos.above(), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    /**
     * Entfernt die Pflanze (beim Ernten)
     */
    public static void removePlant(Level level, BlockPos potPos) {
        BlockPos plantPos = potPos.above();
        BlockState state = level.getBlockState(plantPos);

        if (state.getBlock() instanceof TobaccoPlantBlock) {
            level.setBlock(plantPos, Blocks.AIR.defaultBlockState(), 3);

            // Entferne oberen Block falls vorhanden
            BlockState above = level.getBlockState(plantPos.above());
            if (above.getBlock() instanceof TobaccoPlantBlock) {
                level.setBlock(plantPos.above(), Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    /**
     * Gibt den Pflanzen-Block für einen Tabak-Typ zurück
     */
    private static Block getPlantBlockForType(TobaccoType type) {
        return switch (type) {
            case VIRGINIA -> TobaccoBlocks.VIRGINIA_PLANT.get();
            case BURLEY -> TobaccoBlocks.BURLEY_PLANT.get();
            case ORIENTAL -> TobaccoBlocks.ORIENTAL_PLANT.get();
            case HAVANA -> TobaccoBlocks.HAVANA_PLANT.get();
        };
    }
}
