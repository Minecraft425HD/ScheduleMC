package de.rolandsw.schedulemc.production.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Abstrakte Basis-Klasse für alle Verarbeitungs-Blöcke
 * Blöcke die einen Prozess durchführen und ein BlockEntity + GUI haben
 *
 * Beispiele:
 * - Trocknungsgestelle (Drying Racks)
 * - Fermentationsfässer (Fermentation Barrels)
 * - Verpackungstische (Packaging Tables)
 * - Extraktionsbottiche (Extraction Vats)
 * - Raffinerie (Refineries)
 * - etc.
 *
 * Features:
 * - BlockEntity Support
 * - Automatisches GUI-Opening
 * - Inventar-Drop beim Zerstören
 * - MenuProvider Integration
 */
public abstract class AbstractProcessingBlock extends Block implements EntityBlock {

    protected AbstractProcessingBlock(Properties properties) {
        super(properties);
    }

    /**
     * Handle Rechtsklick - Öffne GUI wenn BlockEntity MenuProvider ist
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MenuProvider menuProvider) {
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, menuProvider, pos);
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    /**
     * Droppe Inventar-Inhalt beim Zerstören
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                // Droppe Items aus dem Inventar
                blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(i));
                    }
                });
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    /**
     * Erstellt das BlockEntity
     * Override in Subklasse
     */
    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);
}
