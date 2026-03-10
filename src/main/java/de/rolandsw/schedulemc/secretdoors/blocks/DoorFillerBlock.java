package de.rolandsw.schedulemc.secretdoors.blocks;

import de.rolandsw.schedulemc.secretdoors.SecretDoors;
import de.rolandsw.schedulemc.secretdoors.blockentity.DoorFillerBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blockentity.SecretDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Interner Füller-Block für Multi-Block-Türen.
 * - Kein Item, nicht im Creative-Tab
 * - Speichert Referenz auf Controller-Block (via BlockEntity)
 * - Rechtsklick delegiert an Controller
 * - Abbau löst Controller-Entfernung aus
 * - Ist vollständig solid (verhindert Durchgehen solange Tür zu ist)
 */
public class DoorFillerBlock extends BaseEntityBlock {

    public DoorFillerBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    // ─────────────────────────────────────────────────────────────────
    // Rendering: immer unsichtbar (wird vom Controller gesteuert)
    // ─────────────────────────────────────────────────────────────────

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.block();
    }

    // ─────────────────────────────────────────────────────────────────
    // Interaktion → delegiere an Controller
    // ─────────────────────────────────────────────────────────────────

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos controllerPos = getControllerPos(level, pos);
        if (controllerPos == null) {
            // Verwaister Füller-Block: selbst entfernen
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return InteractionResult.SUCCESS;
        }

        BlockState controllerState = level.getBlockState(controllerPos);
        if (controllerState.getBlock() instanceof AbstractSecretDoorBlock) {
            return controllerState.getBlock().use(controllerState, level, controllerPos, player, hand, hit);
        }

        return InteractionResult.PASS;
    }

    // ─────────────────────────────────────────────────────────────────
    // Abbau → Controller entfernen (Kaskade)
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            BlockPos controllerPos = getControllerPos(level, pos);
            if (controllerPos != null) {
                BlockState controllerState = level.getBlockState(controllerPos);
                if (controllerState.getBlock() instanceof AbstractSecretDoorBlock) {
                    if (level.getBlockEntity(controllerPos) instanceof SecretDoorBlockEntity be) {
                        // Alle anderen Füller entfernen
                        AbstractSecretDoorBlock.removeAllFillers(level, controllerPos, be);
                    }
                    // Controller-Block entfernen (droppt Item)
                    level.destroyBlock(controllerPos, true);
                }
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    // ─────────────────────────────────────────────────────────────────
    // BlockEntity
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DoorFillerBlockEntity(pos, state);
    }

    // ─────────────────────────────────────────────────────────────────
    // Hilfsmethoden
    // ─────────────────────────────────────────────────────────────────

    @Nullable
    private BlockPos getControllerPos(Level level, BlockPos fillerPos) {
        if (level.getBlockEntity(fillerPos) instanceof DoorFillerBlockEntity fbe) {
            return fbe.getControllerPos();
        }
        return null;
    }
}
