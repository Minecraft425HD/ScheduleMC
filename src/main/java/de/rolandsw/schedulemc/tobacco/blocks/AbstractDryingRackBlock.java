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
 * Basis-Klasse für Multi-Block Trocknungsgestelle (3 Blöcke breit: links, mitte, rechts)
 */
public abstract class AbstractDryingRackBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<RackPart> PART = EnumProperty.create("part", RackPart.class);

    // Collision Shape (voller Block)
    private static final VoxelShape SHAPE = Shapes.block();

    public AbstractDryingRackBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, RackPart.CENTER));
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

        // Prüfe ob alle 3 Positionen frei sind (links, mitte, rechts)
        BlockPos leftPos = pos.relative(facing.getClockWise());
        BlockPos rightPos = pos.relative(facing.getCounterClockWise());

        if (level.getBlockState(pos).canBeReplaced(context) &&
            level.getBlockState(leftPos).canBeReplaced(context) &&
            level.getBlockState(rightPos).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(PART, RackPart.CENTER);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide) {
            Direction facing = state.getValue(FACING);

            // Positionen der anderen 2 Blöcke
            BlockPos leftPos = pos.relative(facing.getClockWise());
            BlockPos rightPos = pos.relative(facing.getCounterClockWise());

            // Setze die anderen 2 Teile
            level.setBlock(leftPos, state.setValue(PART, RackPart.LEFT), 3);
            level.setBlock(rightPos, state.setValue(PART, RackPart.RIGHT), 3);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                   LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        RackPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);

        // Prüfe ob der zugehörige Master-Block noch existiert
        BlockPos masterPos = getMasterPos(pos, part, facing);
        if (!masterPos.equals(pos)) {
            BlockState masterState = level.getBlockState(masterPos);
            if (!masterState.is(this) || masterState.getValue(PART) != RackPart.CENTER) {
                return Blocks.AIR.defaultBlockState();
            }
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            RackPart part = state.getValue(PART);
            Direction facing = state.getValue(FACING);

            // Finde die Master-Position
            BlockPos masterPos = getMasterPos(pos, part, facing);

            // Entferne alle 3 Blöcke
            removeAllParts(level, masterPos, facing);
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Berechnet die Position des Master-Blocks (CENTER) basierend auf dem aktuellen Teil
     */
    protected BlockPos getMasterPos(BlockPos pos, RackPart part, Direction facing) {
        return switch (part) {
            case CENTER -> pos;
            case LEFT -> pos.relative(facing.getCounterClockWise());
            case RIGHT -> pos.relative(facing.getClockWise());
        };
    }

    /**
     * Entfernt alle 3 Teile des Gestells
     */
    protected void removeAllParts(Level level, BlockPos masterPos, Direction facing) {
        BlockPos leftPos = masterPos.relative(facing.getClockWise());
        BlockPos rightPos = masterPos.relative(facing.getCounterClockWise());

        // Entferne die Blöcke
        removePartIfPresent(level, masterPos);
        removePartIfPresent(level, leftPos);
        removePartIfPresent(level, rightPos);
    }

    private void removePartIfPresent(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(this)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
        }
    }

    /**
     * Nur der Master-Block (CENTER) hat ein BlockEntity
     */
    protected boolean isMasterBlock(BlockState state) {
        return state.getValue(PART) == RackPart.CENTER;
    }

    /**
     * Enum für die 3 Teile des Multi-Block Gestells
     */
    public enum RackPart implements StringRepresentable {
        LEFT("left"),
        CENTER("center"),
        RIGHT("right");

        private final String name;

        RackPart(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
