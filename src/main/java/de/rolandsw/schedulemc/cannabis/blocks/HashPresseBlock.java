package de.rolandsw.schedulemc.cannabis.blocks;

import de.rolandsw.schedulemc.cannabis.blockentity.HashPresseBlockEntity;
import de.rolandsw.schedulemc.cannabis.blockentity.CannabisBlockEntities;
import de.rolandsw.schedulemc.cannabis.items.TrimItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Hash-Presse - presst Trim zu Haschisch
 */
public class HashPresseBlock extends BaseEntityBlock {

    public HashPresseBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HashPresseBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, CannabisBlockEntities.HASH_PRESSE.get(),
                (lvl, pos, st, be) -> be.tick());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof HashPresseBlockEntity presse)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Hash entnehmen
        if (presse.hasOutput()) {
            ItemStack hash = presse.extractHash();
            if (!hash.isEmpty()) {
                player.addItem(hash);
                player.displayClientMessage(Component.translatable("block.hash_press.hash_removed"), true);
                return InteractionResult.CONSUME;
            }
        }

        // Trim hinzufügen
        if (heldItem.getItem() instanceof TrimItem && !presse.isPressing()) {
            if (presse.addTrim(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
                player.displayClientMessage(Component.translatable("block.hash_press.trim_added").append(
                        Component.translatable("block.hash_press.trim_grams", presse.getTrimWeight())
                ), true);
                return InteractionResult.CONSUME;
            }
        }

        // Pressen starten (leere Hand + Shift)
        if (heldItem.isEmpty() && player.isShiftKeyDown() && presse.canStart()) {
            if (presse.startPressing()) {
                player.displayClientMessage(Component.translatable("block.hash_press.pressing_started"), true);
                return InteractionResult.CONSUME;
            }
        }

        // Status anzeigen
        if (presse.isPressing()) {
            int progress = (int) (presse.getPressProgress() * 100);
            player.displayClientMessage(Component.translatable("block.hash_press.pressing").append(
                    Component.translatable("block.hash_press.pressing_percent", progress)
            ), true);
        } else if (presse.getTrimWeight() > 0) {
            player.displayClientMessage(Component.translatable("block.hash_press.status_trim", presse.getTrimWeight()).append(
                    Component.translatable("block.hash_press.status_expected", presse.getExpectedHashWeight())
            ), true);
            if (presse.canStart()) {
                player.displayClientMessage(Component.translatable("block.hash_press.shift_to_start"), false);
            } else {
                player.displayClientMessage(Component.translatable("block.hash_press.min_trim"), false);
            }
        } else {
            player.displayClientMessage(Component.translatable("block.hash_press.empty"), true);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof HashPresseBlockEntity presse && presse.hasOutput()) {
                ItemStack hash = presse.extractHash();
                if (!hash.isEmpty()) {
                    Block.popResource(level, pos, hash);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
