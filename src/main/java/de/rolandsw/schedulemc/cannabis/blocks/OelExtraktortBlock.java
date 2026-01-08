package de.rolandsw.schedulemc.cannabis.blocks;

import de.rolandsw.schedulemc.cannabis.blockentity.OelExtraktortBlockEntity;
import de.rolandsw.schedulemc.cannabis.blockentity.CannabisBlockEntities;
import de.rolandsw.schedulemc.cannabis.items.TrimmedBudItem;
import de.rolandsw.schedulemc.cannabis.items.TrimItem;
import de.rolandsw.schedulemc.cannabis.items.CannabisItems;
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
 * Öl-Extraktor - extrahiert Cannabis-Öl
 */
public class OelExtraktortBlock extends BaseEntityBlock {

    public OelExtraktortBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OelExtraktortBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, CannabisBlockEntities.OEL_EXTRAKTOR.get(),
                (lvl, pos, st, be) -> be.tick());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof OelExtraktortBlockEntity extraktor)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Öl entnehmen
        if (extraktor.hasOutput()) {
            ItemStack oil = extraktor.extractOil();
            if (!oil.isEmpty()) {
                player.addItem(oil);
                player.displayClientMessage(Component.translatable("block.oil_extractor.oil_removed"), true);
                return InteractionResult.CONSUME;
            }
        }

        // Material hinzufügen (Blüten oder Trim)
        if ((heldItem.getItem() instanceof TrimmedBudItem || heldItem.getItem() instanceof TrimItem) && !extraktor.isExtracting()) {
            if (extraktor.addMaterial(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
                String typeKey = extraktor.isFromBuds() ? "block.oil_extractor.buds_label" : "block.oil_extractor.trim_label";
                player.displayClientMessage(Component.translatable("block.oil_extractor.material_added").append(
                        Component.translatable(typeKey)).append(Component.translatable("block.oil_extractor.material_added_suffix")).append(
                        Component.translatable("block.oil_extractor.material_grams", extraktor.getMaterialWeight())
                ), true);
                return InteractionResult.CONSUME;
            }
        }

        // Lösungsmittel hinzufügen
        if (heldItem.is(CannabisItems.EXTRACTION_SOLVENT.get()) && !extraktor.isExtracting()) {
            if (extraktor.addSolvent(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(heldItem.getCount());
                }
                player.displayClientMessage(Component.translatable("block.oil_extractor.solvent_added").append(
                        Component.translatable("block.oil_extractor.solvent_count", extraktor.getSolventCount())
                ), true);
                return InteractionResult.CONSUME;
            }
        }

        // Extraktion starten (leere Hand + Shift)
        if (heldItem.isEmpty() && player.isShiftKeyDown() && extraktor.canStart()) {
            if (extraktor.startExtraction()) {
                player.displayClientMessage(Component.translatable("block.oil_extractor.extraction_started"), true);
                return InteractionResult.CONSUME;
            }
        }

        // Status anzeigen
        if (extraktor.isExtracting()) {
            int progress = (int) (extraktor.getExtractionProgress() * 100);
            player.displayClientMessage(Component.translatable("block.oil_extractor.extracting").append(
                    Component.translatable("block.oil_extractor.extracting_percent", progress)
            ), true);
        } else if (extraktor.getMaterialWeight() > 0) {
            String type = extraktor.isFromBuds() ? "Blüten" : "Trim";
            player.displayClientMessage(Component.literal(
                    "§7" + type + ": §f" + extraktor.getMaterialWeight() + "g §7| Lösungsmittel: §f" + extraktor.getSolventCount()
            ), true);
            player.displayClientMessage(Component.literal(
                    "§7Erwartetes Öl: §f" + extraktor.getExpectedOilAmount() + "ml"
            ), false);
            if (extraktor.canStart()) {
                player.displayClientMessage(Component.literal("§8[Shift+Rechtsklick zum Starten]"), false);
            } else if (extraktor.getSolventCount() < 1) {
                player.displayClientMessage(Component.translatable("block.oil_extractor.solvent_needed"), false);
            } else {
                player.displayClientMessage(Component.translatable("block.oil_extractor.min_material"), false);
            }
        } else {
            player.displayClientMessage(Component.translatable("block.oil_extractor.empty"), true);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof OelExtraktortBlockEntity extraktor && extraktor.hasOutput()) {
                ItemStack oil = extraktor.extractOil();
                if (!oil.isEmpty()) {
                    Block.popResource(level, pos, oil);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
