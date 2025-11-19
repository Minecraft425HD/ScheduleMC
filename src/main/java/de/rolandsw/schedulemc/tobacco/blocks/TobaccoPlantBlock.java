package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.items.FreshTobaccoLeafItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
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

import java.util.List;

/**
 * Tabak-Pflanze Block - wächst in 8 Stufen, wird ab Stufe 4 zwei Blöcke hoch
 * Droppt Blätter beim Abbauen
 */
public class TobaccoPlantBlock extends Block {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_7; // 0-7
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    // X-förmige Collision-Box (wie Pflanzen in Minecraft)
    private static final VoxelShape SHAPE_SMALL = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);
    private static final VoxelShape SHAPE_MEDIUM = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape SHAPE_TALL = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

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
            return SHAPE_SMALL;
        } else if (half == DoubleBlockHalf.UPPER) {
            return SHAPE_TALL;
        } else {
            return SHAPE_MEDIUM;
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                    LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (direction.getAxis() == Direction.Axis.Y) {
            if (half == DoubleBlockHalf.LOWER && direction == Direction.UP) {
                if (state.getValue(AGE) >= 4 && neighborState.getBlock() != this) {
                    return Blocks.AIR.defaultBlockState();
                }
            } else if (half == DoubleBlockHalf.UPPER && direction == Direction.DOWN) {
                if (neighborState.getBlock() != this || neighborState.getValue(HALF) != DoubleBlockHalf.LOWER) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        }

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

        // Unterer Block: muss auf einem soliden Block stehen
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return belowState.isSolidRender(level, belowPos);
    }

    /**
     * Drops beim Abbauen
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        List<ItemStack> drops = new java.util.ArrayList<>();

        // Nur untere Hälfte droppt Items
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            int age = state.getValue(AGE);

            // Berechne Ertrag basierend auf Wachstumsstufe
            if (age >= 7) {
                // Voll ausgewachsen - voller Ertrag
                int yield = tobaccoType.getBaseYield();
                drops.add(FreshTobaccoLeafItem.create(tobaccoType, TobaccoQuality.GUT, yield));
            } else if (age >= 4) {
                // Teilweise gewachsen - reduzierter Ertrag
                int yield = (int) (tobaccoType.getBaseYield() * 0.5);
                drops.add(FreshTobaccoLeafItem.create(tobaccoType, TobaccoQuality.SCHLECHT, yield));
            }
            // Stufe 0-3: kein Drop
        }

        return drops;
    }

    /**
     * Zufälliges Wachstum
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER && !this.isMaxAge(state)) {
            if (random.nextInt(3) == 0) {
                this.grow(level, pos, state);
            }
        }
    }

    private boolean isMaxAge(BlockState state) {
        return state.getValue(AGE) >= 7;
    }

    private void grow(Level level, BlockPos pos, BlockState state) {
        int currentAge = state.getValue(AGE);
        int newAge = Math.min(7, currentAge + 1);

        // Update unterer Block
        level.setBlock(pos, state.setValue(AGE, newAge), 3);

        // Ab Stufe 4: setze/update oberen Block
        if (newAge >= 4) {
            BlockState upperState = this.defaultBlockState()
                    .setValue(AGE, newAge)
                    .setValue(HALF, DoubleBlockHalf.UPPER);
            level.setBlock(pos.above(), upperState, 3);
        } else {
            // Entferne oberen Block falls vorhanden
            BlockState above = level.getBlockState(pos.above());
            if (above.getBlock() instanceof TobaccoPlantBlock) {
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }
}
