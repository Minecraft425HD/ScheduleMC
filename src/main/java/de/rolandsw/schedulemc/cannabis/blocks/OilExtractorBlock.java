package de.rolandsw.schedulemc.cannabis.blocks;

import de.rolandsw.schedulemc.cannabis.blockentity.OilExtractorBlockEntity;
import de.rolandsw.schedulemc.cannabis.blockentity.CannabisBlockEntities;
import de.rolandsw.schedulemc.cannabis.items.TrimmedBudItem;
import de.rolandsw.schedulemc.cannabis.items.TrimItem;
import de.rolandsw.schedulemc.cannabis.items.CannabisItems;
import de.rolandsw.schedulemc.cannabis.menu.OilExtractorMenu;
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
 * Öl-Extractor - extrahiert Cannabis-Öl
 */
public class OilExtractorBlock extends BaseEntityBlock {

    public OilExtractorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OilExtractorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, CannabisBlockEntities.OIL_EXTRACTOR.get(),
                (lvl, pos, st, be) -> be.tick());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof OilExtractorBlockEntity extractor)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Öl entnehmen (jedes ml als eigenes Item)
        if (extractor.hasOutput()) {
            ItemStack oil = extractor.extractOil();
            if (!oil.isEmpty()) {
                while (!oil.isEmpty()) {
                    ItemStack ml = oil.split(1);
                    if (!player.addItem(ml)) Block.popResource(level, extractor.getBlockPos(), ml);
                }
                player.displayClientMessage(Component.translatable("block.oil_extractor.oil_removed"), true);
                return InteractionResult.CONSUME;
            }
        }

        // Material hinzufügen (Blüten oder Trim)
        if ((heldItem.getItem() instanceof TrimmedBudItem || heldItem.getItem() instanceof TrimItem) && !extractor.isExtracting()) {
            if (extractor.addMaterial(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
                String typeKey = extractor.isFromBuds() ? "block.oil_extractor.buds_label" : "block.oil_extractor.trim_label";
                player.displayClientMessage(Component.translatable("block.oil_extractor.material_added").append(
                        Component.translatable(typeKey)).append(Component.translatable("block.oil_extractor.material_added_suffix")).append(
                        Component.translatable("block.oil_extractor.material_grams", extractor.getMaterialWeight())
                ), true);
                return InteractionResult.CONSUME;
            }
        }

        // Lösungsmittel hinzufügen
        if (heldItem.is(CannabisItems.EXTRACTION_SOLVENT.get()) && !extractor.isExtracting()) {
            if (extractor.addSolvent(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(heldItem.getCount());
                }
                player.displayClientMessage(Component.translatable("block.oil_extractor.solvent_added").append(
                        Component.translatable("block.oil_extractor.solvent_count", extractor.getSolventCount())
                ), true);
                return InteractionResult.CONSUME;
            }
        }

        // GUI öffnen (leere Hand)
        if (heldItem.isEmpty() && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new OilExtractorMenu.Provider(extractor), extractor.getBlockPos());
            return InteractionResult.CONSUME;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof OilExtractorBlockEntity extractor && extractor.hasOutput()) {
                ItemStack oil = extractor.extractOil();
                if (!oil.isEmpty()) {
                    Block.popResource(level, pos, oil);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
