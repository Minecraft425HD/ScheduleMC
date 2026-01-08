package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.tobacco.blockentity.MediumDryingRackBlockEntity;
import de.rolandsw.schedulemc.tobacco.menu.MediumDryingRackMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mittleres Trocknungsgestell (2 Blöcke breit: links, rechts)
 * Kapazität: 8 Tabakblätter
 */
public class MediumDryingRackBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<RackPart> PART = EnumProperty.create("part", RackPart.class);

    // Collision Shapes basierend auf Modellkoordinaten (Z: 3-14 Pixel = 0.1875-0.875 Blöcke)
    // Die Modelle sind 2 Blöcke hoch (Y: 0-31 Pixel = 0-1.9375 Blöcke)
    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 2, 16, 16, 14);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 2, 16, 16, 14);
    private static final VoxelShape SHAPE_WEST = Block.box(2, 0, 0, 14, 16, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(2, 0, 0, 14, 16, 16);

    public MediumDryingRackBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, RackPart.BOTTOM_LEFT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite();

        // Prüfe ob alle 4 Positionen frei sind (2x2 Grid) - nach RECHTS vom Facing
        BlockPos bottomRight = pos.relative(facing.getClockWise());
        BlockPos topLeft = pos.above();
        BlockPos topRight = pos.above().relative(facing.getClockWise());

        if (level.getBlockState(pos).canBeReplaced(context) &&
            level.getBlockState(bottomRight).canBeReplaced(context) &&
            level.getBlockState(topLeft).canBeReplaced(context) &&
            level.getBlockState(topRight).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(PART, RackPart.BOTTOM_LEFT);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide) {
            Direction facing = state.getValue(FACING);

            // Positionen der anderen 3 Blöcke (2x2 Grid) - nach RECHTS vom Facing
            BlockPos bottomRight = pos.relative(facing.getClockWise());
            BlockPos topLeft = pos.above();
            BlockPos topRight = pos.above().relative(facing.getClockWise());

            // Setze die anderen 3 Teile
            level.setBlock(bottomRight, state.setValue(PART, RackPart.BOTTOM_RIGHT), 3);
            level.setBlock(topLeft, state.setValue(PART, RackPart.TOP_LEFT), 3);
            level.setBlock(topRight, state.setValue(PART, RackPart.TOP_RIGHT), 3);
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
            if (!masterState.is(this) || masterState.getValue(PART) != RackPart.BOTTOM_LEFT) {
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

            // Entferne alle 4 Blöcke
            removeAllParts(level, masterPos, facing);
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Berechnet die Position des Master-Blocks (BOTTOM_LEFT) basierend auf dem aktuellen Teil
     */
    protected BlockPos getMasterPos(BlockPos pos, RackPart part, Direction facing) {
        return switch (part) {
            case BOTTOM_LEFT -> pos;
            case BOTTOM_RIGHT -> pos.relative(facing.getCounterClockWise());
            case TOP_LEFT -> pos.below();
            case TOP_RIGHT -> pos.below().relative(facing.getCounterClockWise());
        };
    }

    /**
     * Entfernt alle 4 Teile des Gestells (2x2 Grid)
     */
    protected void removeAllParts(Level level, BlockPos masterPos, Direction facing) {
        BlockPos bottomRight = masterPos.relative(facing.getClockWise());
        BlockPos topLeft = masterPos.above();
        BlockPos topRight = masterPos.above().relative(facing.getClockWise());

        // Entferne die Blöcke
        removePartIfPresent(level, masterPos);
        removePartIfPresent(level, bottomRight);
        removePartIfPresent(level, topLeft);
        removePartIfPresent(level, topRight);
    }

    private void removePartIfPresent(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(this)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
        }
    }

    /**
     * Nur der Master-Block (BOTTOM_LEFT) hat ein BlockEntity
     */
    protected boolean isMasterBlock(BlockState state) {
        return state.getValue(PART) == RackPart.BOTTOM_LEFT;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Nur der BOTTOM_LEFT-Block hat ein BlockEntity
        if (isMasterBlock(state)) {
            return new MediumDryingRackBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || !isMasterBlock(state)) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof MediumDryingRackBlockEntity rackBE) {
                rackBE.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        // Finde das Master-BlockEntity (BOTTOM_LEFT)
        RackPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        BlockPos masterPos = getMasterPos(pos, part, facing);

        BlockEntity be = level.getBlockEntity(masterPos);
        if (!(be instanceof MediumDryingRackBlockEntity rackBE)) {
            return InteractionResult.PASS;
        }

        // GUI öffnen
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return Component.translatable("block.medium_drying_rack.name");
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                    return new MediumDryingRackMenu(containerId, playerInventory, rackBE);
                }
            }, masterPos);
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Enum für die 4 Teile des 2x2 Multi-Block Gestells (2 hoch, 2 breit)
     */
    public enum RackPart implements StringRepresentable {
        BOTTOM_LEFT("bottom_left"),
        BOTTOM_RIGHT("bottom_right"),
        TOP_LEFT("top_left"),
        TOP_RIGHT("top_right");

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
