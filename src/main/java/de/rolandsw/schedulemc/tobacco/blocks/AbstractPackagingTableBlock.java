package de.rolandsw.schedulemc.tobacco.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Basis-Klasse für Multi-Block Packtische (2x2 Blöcke: 2 breit, 2 hoch)
 */
public abstract class AbstractPackagingTableBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<TablePart> PART = EnumProperty.create("part", TablePart.class);

    // Collision Shapes für jeden Teil (volle Blöcke)
    private static final VoxelShape SHAPE = Shapes.block();

    public AbstractPackagingTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, TablePart.LOWER_LEFT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite();

        // Prüfe ob alle 4 Positionen frei sind (Tisch erstreckt sich nach rechts)
        BlockPos rightPos = pos.relative(facing.getCounterClockWise());
        BlockPos upperPos = pos.above();
        BlockPos upperRightPos = upperPos.relative(facing.getCounterClockWise());

        if (pos.getY() < level.getMaxBuildHeight() - 1 &&
            level.getBlockState(pos).canBeReplaced(context) &&
            level.getBlockState(rightPos).canBeReplaced(context) &&
            level.getBlockState(upperPos).canBeReplaced(context) &&
            level.getBlockState(upperRightPos).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(PART, TablePart.LOWER_LEFT);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide) {
            Direction facing = state.getValue(FACING);

            // Positionen der anderen 3 Blöcke (Tisch erstreckt sich nach rechts)
            BlockPos rightPos = pos.relative(facing.getCounterClockWise());
            BlockPos upperPos = pos.above();
            BlockPos upperRightPos = upperPos.relative(facing.getCounterClockWise());

            // Setze die anderen 3 Teile
            level.setBlock(rightPos, state.setValue(PART, TablePart.LOWER_RIGHT), 3);
            level.setBlock(upperPos, state.setValue(PART, TablePart.UPPER_LEFT), 3);
            level.setBlock(upperRightPos, state.setValue(PART, TablePart.UPPER_RIGHT), 3);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                   LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        TablePart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);

        // Prüfe ob der zugehörige Master-Block noch existiert
        BlockPos masterPos = getMasterPos(pos, part, facing);
        if (!masterPos.equals(pos)) {
            BlockState masterState = level.getBlockState(masterPos);
            if (!masterState.is(this) || masterState.getValue(PART) != TablePart.LOWER_LEFT) {
                return Blocks.AIR.defaultBlockState();
            }
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            TablePart part = state.getValue(PART);
            Direction facing = state.getValue(FACING);

            // Finde die Master-Position
            BlockPos masterPos = getMasterPos(pos, part, facing);

            // Entferne alle 4 Blöcke
            removeAllParts(level, masterPos, facing, player);
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Berechnet die Position des Master-Blocks (LOWER_LEFT) basierend auf dem aktuellen Teil
     */
    protected BlockPos getMasterPos(BlockPos pos, TablePart part, Direction facing) {
        return switch (part) {
            case LOWER_LEFT -> pos;
            case LOWER_RIGHT -> pos.relative(facing.getClockWise());
            case UPPER_LEFT -> pos.below();
            case UPPER_RIGHT -> pos.below().relative(facing.getClockWise());
        };
    }

    /**
     * Entfernt alle 4 Teile des Tisches
     */
    protected void removeAllParts(Level level, BlockPos masterPos, Direction facing, Player player) {
        BlockPos rightPos = masterPos.relative(facing.getCounterClockWise());
        BlockPos upperPos = masterPos.above();
        BlockPos upperRightPos = upperPos.relative(facing.getCounterClockWise());

        // Entferne die Blöcke (außer dem gerade zerstörten)
        removePartIfPresent(level, masterPos);
        removePartIfPresent(level, rightPos);
        removePartIfPresent(level, upperPos);
        removePartIfPresent(level, upperRightPos);
    }

    private void removePartIfPresent(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(this)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
        }
    }

    /**
     * Nur der Master-Block (LOWER_LEFT) hat ein BlockEntity
     */
    protected boolean isMasterBlock(BlockState state) {
        return state.getValue(PART) == TablePart.LOWER_LEFT;
    }

    /**
     * Enum für die 4 Teile des Multi-Block Tisches
     */
    public enum TablePart implements StringRepresentable {
        LOWER_LEFT("lower_left"),
        LOWER_RIGHT("lower_right"),
        UPPER_LEFT("upper_left"),
        UPPER_RIGHT("upper_right");

        private final String name;

        TablePart(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
