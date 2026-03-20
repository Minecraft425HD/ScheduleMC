package de.rolandsw.schedulemc.cheese.blocks;

import de.rolandsw.schedulemc.cheese.blockentity.MediumAgingCaveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Mittlerer Reifekeller - Lagert Käse zur Reifung
 * Kapazität: 9 Käselaibe
 * Reifungsgeschwindigkeit: 1.2x
 */
public class MediumAgingCaveBlock extends Block implements EntityBlock {
    public MediumAgingCaveBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MediumAgingCaveBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof MediumAgingCaveBlockEntity cave) {
                cave.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof MediumAgingCaveBlockEntity cave && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, cave, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
