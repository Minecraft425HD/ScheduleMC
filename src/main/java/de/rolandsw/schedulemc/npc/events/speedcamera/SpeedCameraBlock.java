package de.rolandsw.schedulemc.npc.events.speedcamera;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Blitzer (Geschwindigkeits-Kontrollpunkt). Wird von Admins/Stadtplanung an markanten
 * Stellen im Straßennetz platziert - wie im echten Leben ist nicht jeder platzierte
 * Blitzer permanent aktiv, {@link SpeedCameraManager} rotiert die aktive Teilmenge
 * periodisch (siehe {@code police.speed_camera_active_count}/
 * {@code speed_camera_rotation_minutes}). Die Blickrichtung (FACING) ist rein
 * kosmetisch - die Geschwindigkeitserfassung selbst ist radiusbasiert und
 * richtungsunabhängig (siehe {@link SpeedCameraBlockEntity}).
 */
public class SpeedCameraBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public SpeedCameraBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpeedCameraBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof SpeedCameraBlockEntity camera && lvl instanceof ServerLevel serverLevel) {
                camera.tick(serverLevel);
            }
        };
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            SpeedCameraManager.getInstance().registerCamera(pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()) {
            SpeedCameraManager.getInstance().unregisterCamera(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SpeedCameraBlockEntity camera) {
            String statusKey = camera.isActive() ? "message.speed_camera.status_active" : "message.speed_camera.status_inactive";
            player.sendSystemMessage(Component.translatable(statusKey));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
