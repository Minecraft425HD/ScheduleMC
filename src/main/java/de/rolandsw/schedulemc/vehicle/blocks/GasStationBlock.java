package de.rolandsw.schedulemc.vehicle.blocks;

import de.rolandsw.schedulemc.vehicle.blocks.entity.GasStationBlockEntity;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import de.rolandsw.schedulemc.vehicle.fuel.GasStationRegistry;
import de.rolandsw.schedulemc.vehicle.system.FuelingSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Main gas station block (bottom part).
 * Stores fuel and allows players to fuel their vehicles.
 */
public class GasStationBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public GasStationBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GasStationBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos abovePos = context.getClickedPos().above();
        Level level = context.getLevel();

        // Check if there's space above for the top block
        if (!level.getBlockState(abovePos).canBeReplaced(context)) {
            return null;
        }

        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        // Place top block
        BlockPos abovePos = pos.above();
        level.setBlock(abovePos, VehicleBlocks.GAS_STATION_TOP.get().defaultBlockState()
                .setValue(GasStationTopBlock.FACING, state.getValue(FACING)), 3);

        // Register gas station if placed by player
        if (!level.isClientSide && placer instanceof ServerPlayer player) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof GasStationBlockEntity gasStation) {
                UUID stationId = GasStationRegistry.registerGasStation(
                    "Gas Station",
                    pos,
                    level.dimension().location().toString(),
                    player.getUUID(),
                    0.01 // Default price: 0.01 per mB
                );
                gasStation.setStationId(stationId);
                gasStation.setOwner(player.getUUID());
                gasStation.setFuelAmount(10000); // Start with 10,000 mB (10 buckets)
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Unregister from registry
            if (!level.isClientSide) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof GasStationBlockEntity gasStation) {
                    UUID stationId = gasStation.getStationId();
                    if (stationId != null) {
                        GasStationRegistry.unregisterGasStation(stationId);
                    }
                }
            }

            // Remove top block
            BlockPos abovePos = pos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            if (aboveState.getBlock() instanceof GasStationTopBlock) {
                level.removeBlock(abovePos, false);
            }

            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof GasStationBlockEntity gasStation) {
                // Check if player is in a vehicle
                if (player.getVehicle() instanceof VehicleEntity vehicle) {
                    // Try to fuel the vehicle
                    return FuelingSystem.tryFuelVehicle(vehicle, gasStation, player);
                } else {
                    // Open gas station GUI for management (owner only)
                    if (gasStation.isOwner(player.getUUID())) {
                        NetworkHooks.openScreen((ServerPlayer) player, gasStation, pos);
                        return InteractionResult.SUCCESS;
                    } else {
                        player.sendSystemMessage(Component.literal("You are not the owner of this gas station!"));
                        return InteractionResult.FAIL;
                    }
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Remove top block when bottom is broken
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        if (aboveState.getBlock() instanceof GasStationTopBlock) {
            level.destroyBlock(abovePos, false);
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}
