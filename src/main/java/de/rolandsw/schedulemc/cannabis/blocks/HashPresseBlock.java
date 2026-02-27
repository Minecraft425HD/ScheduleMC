package de.rolandsw.schedulemc.cannabis.blocks;

import de.rolandsw.schedulemc.cannabis.blockentity.HashPresseBlockEntity;
import de.rolandsw.schedulemc.cannabis.blockentity.CannabisBlockEntities;
import de.rolandsw.schedulemc.cannabis.items.TrimItem;
import de.rolandsw.schedulemc.cannabis.menu.HashPresseMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.network.NetworkHooks;
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

        // Hash entnehmen (jedes Gramm als eigenes Item)
        if (presse.hasOutput()) {
            ItemStack hash = presse.extractHash();
            if (!hash.isEmpty()) {
                while (!hash.isEmpty()) {
                    ItemStack gram = hash.split(1);
                    if (!player.addItem(gram)) Block.popResource(level, presse.getBlockPos(), gram);
                }
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

        // GUI öffnen (leere Hand)
        if (heldItem.isEmpty() && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new HashPresseMenu.Provider(presse), presse.getBlockPos());
            return InteractionResult.CONSUME;
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
