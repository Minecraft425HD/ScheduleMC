package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.tobacco.blockentity.SmallFermentationBarrelBlockEntity;
import de.rolandsw.schedulemc.tobacco.items.DriedTobaccoLeafItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Kleines Fermentierungsfass
 * Kapazität: 6 Tabakblätter
 */
public class SmallFermentationBarrelBlock extends Block implements EntityBlock {

    public SmallFermentationBarrelBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmallFermentationBarrelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof SmallFermentationBarrelBlockEntity barrelBE) {
                barrelBE.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SmallFermentationBarrelBlockEntity barrelBE)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        // ═══════════════════════════════════════════════════════════
        // 1. GETROCKNETE BLÄTTER HINZUFÜGEN
        // ═══════════════════════════════════════════════════════════
        if (heldItem.getItem() instanceof DriedTobaccoLeafItem) {
            if (barrelBE.isFull()) {
                player.displayClientMessage(Component.translatable(
                    "block.fermentation.barrel_full", 6
                ), true);
                return InteractionResult.FAIL;
            }

            if (barrelBE.addDriedLeaves(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }

                int count = barrelBE.getInputCount();
                player.displayClientMessage(Component.translatable(
                    "block.fermentation.leaf_added", count, 6
                ), true);
                return InteractionResult.SUCCESS;
            }
        }

        // ═══════════════════════════════════════════════════════════
        // 2. FERMENTIERTE BLÄTTER ENTNEHMEN
        // ═══════════════════════════════════════════════════════════
        if (player.isShiftKeyDown() && barrelBE.hasOutput()) {
            ItemStack fermented = barrelBE.extractAllFermentedLeaves();
            if (!fermented.isEmpty()) {
                player.getInventory().add(fermented);

                player.displayClientMessage(Component.translatable(
                    "block.fermentation.fermentation_complete", fermented.getCount()
                ), true);
                return InteractionResult.SUCCESS;
            }
        }

        // ═══════════════════════════════════════════════════════════
        // 3. FORTSCHRITT ANZEIGEN
        // ═══════════════════════════════════════════════════════════
        if (barrelBE.hasInput()) {
            float progress = barrelBE.getAverageFermentationPercentage() * 100;
            String bar = createProgressBar(barrelBE.getAverageFermentationPercentage());
            int inputCount = barrelBE.getInputCount();
            int outputCount = barrelBE.getOutputCount();

            player.displayClientMessage(Component.translatable("gui.fermentation.small_header")
                .append(Component.translatable("block.fermentation.capacity", inputCount, 6))
                .append(Component.translatable("gui.fermentation.progress_line", bar, String.format("%.1f", progress)))
                .append(Component.translatable("block.fermentation.finished", outputCount))
                .append(Component.literal("\n"))
                .append(barrelBE.hasOutput() ? Component.translatable("block.fermentation.shift_extract") : Component.translatable("block.fermentation.processing"))
            , false);
        } else {
            player.displayClientMessage(Component.translatable("gui.fermentation.small_header")
                .append(Component.translatable("block.fermentation.capacity_max", 6))
                .append(Component.literal("\n"))
                .append(Component.translatable("block.fermentation.empty"))
            , false);
        }

        return InteractionResult.SUCCESS;
    }

    private String createProgressBar(float progress) {
        int filled = (int) (progress * 10);
        int empty = 10 - filled;
        return "§6" + "▰".repeat(Math.max(0, filled)) + "§7" + "▱".repeat(Math.max(0, empty));
    }
}
