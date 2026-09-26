package de.rolandsw.schedulemc.npc.events.speedcamera;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
 * Blitzer (Geschwindigkeits-Kontrollpunkt). Wird NICHT direkt vom Admin platziert, sondern
 * von {@link SpeedCameraManager} automatisch an einem gerade aktiven, zuvor per
 * {@link SpeedCameraMarkerItem} markierten Standort erzeugt und nach der 7-Tage-Rotation
 * wieder entfernt - wie im echten Leben taucht der Blitzer nur temporär auf. Die
 * Blickrichtung (FACING) ist rein kosmetisch - die Geschwindigkeitserfassung selbst ist
 * radiusbasiert und richtungsunabhängig (siehe {@link SpeedCameraBlockEntity}).
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel
                && SpeedCameraManager.getInstance() != null) {
            long daysLeft = SpeedCameraManager.getInstance().getDaysUntilNextRotation(serverLevel);
            player.sendSystemMessage(Component.translatable("message.speed_camera.status_active", daysLeft));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
