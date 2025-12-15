package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.tobacco.blockentity.LargePackagingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Großer Packtisch für 20g (kein Verpackungsmaterial)
 * Multi-Block: 2x2 (2 breit, 2 hoch)
 */
public class LargePackagingTableBlock extends AbstractPackagingTableBlock {

    public LargePackagingTableBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Nur der Master-Block (LOWER_LEFT) hat ein BlockEntity
        if (isMasterBlock(state)) {
            return new LargePackagingTableBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Finde die Master-Position
        TablePart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        BlockPos masterPos = getMasterPos(pos, part, facing);

        BlockEntity be = level.getBlockEntity(masterPos);
        if (be instanceof LargePackagingTableBlockEntity packagingTable) {
            NetworkHooks.openScreen((ServerPlayer) player, packagingTable, masterPos);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isMasterBlock(state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LargePackagingTableBlockEntity packagingTable) {
                packagingTable.drops();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
