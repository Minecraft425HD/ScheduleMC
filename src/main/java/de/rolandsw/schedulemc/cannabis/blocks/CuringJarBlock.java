package de.rolandsw.schedulemc.cannabis.blocks;

import de.rolandsw.schedulemc.cannabis.blockentity.CuringJarBlockEntity;
import de.rolandsw.schedulemc.cannabis.blockentity.CannabisBlockEntities;
import de.rolandsw.schedulemc.cannabis.items.TrimmedBudItem;
import de.rolandsw.schedulemc.cannabis.menu.CuringJarMenu;
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
 * Curing-Glas für Cannabis
 */
public class CuringJarBlock extends BaseEntityBlock {

    public CuringJarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CuringJarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, CannabisBlockEntities.CURING_JAR.get(),
                (lvl, pos, st, be) -> be.tick());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CuringJarBlockEntity glas)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Getrimmte Buds hinzufügen (bis zu MAX_WEIGHT g, gleiche Sorte/Qualität)
        if (heldItem.getItem() instanceof TrimmedBudItem) {
            if (glas.addTrimmedBud(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
                player.displayClientMessage(Component.translatable("block.curing_glas.buds_added"), true);
                return InteractionResult.CONSUME;
            }
        }

        // Gecurte Buds entnehmen (Shift+Click), jedes Gramm als eigenes Item
        if (glas.hasContent() && heldItem.isEmpty() && player.isShiftKeyDown()) {
            ItemStack cured = glas.extractCuredBud();
            if (!cured.isEmpty()) {
                while (!cured.isEmpty()) {
                    ItemStack gram = cured.split(1);
                    if (!player.addItem(gram)) Block.popResource(level, glas.getBlockPos(), gram);
                }
                if (glas.isOptimallyCured()) {
                    player.displayClientMessage(Component.translatable("block.curing_glas.perfect_buds"), true);
                } else if (glas.isReadyForExtraction()) {
                    player.displayClientMessage(Component.translatable("block.curing_glas.cured_buds"), true);
                } else {
                    player.displayClientMessage(Component.translatable("block.curing_glas.early_removal"), true);
                }
                return InteractionResult.CONSUME;
            }
        }

        // GUI öffnen (leere Hand, normaler Rechtsklick)
        if (heldItem.isEmpty() && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new CuringJarMenu.Provider(glas), glas.getBlockPos());
            return InteractionResult.CONSUME;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CuringJarBlockEntity glas && glas.hasContent()) {
                ItemStack cured = glas.extractCuredBud();
                if (!cured.isEmpty()) {
                    Block.popResource(level, pos, cured);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
