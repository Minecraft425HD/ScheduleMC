package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.tobacco.blockentity.MediumFermentationBarrelBlockEntity;
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
 * Mittleres Fermentierungsfass
 * Kapazität: 8 Tabakblätter
 */
public class MediumFermentationBarrelBlock extends Block implements EntityBlock {

    public MediumFermentationBarrelBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MediumFermentationBarrelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof MediumFermentationBarrelBlockEntity barrelBE) {
                barrelBE.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MediumFermentationBarrelBlockEntity barrelBE)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        // ═══════════════════════════════════════════════════════════
        // 1. GETROCKNETE BLÄTTER HINZUFÜGEN
        // ═══════════════════════════════════════════════════════════
        if (heldItem.getItem() instanceof DriedTobaccoLeafItem) {
            if (barrelBE.isFull()) {
                player.displayClientMessage(Component.literal(
                    "§c✗ Fass ist voll! (8/8)"
                ), true);
                return InteractionResult.FAIL;
            }

            if (barrelBE.addDriedLeaves(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }

                int count = barrelBE.getInputCount();
                player.displayClientMessage(Component.literal(
                    "§a✓ Blatt hinzugefügt! (" + count + "/8)\n" +
                    "§7Dauer: §e~10 Minuten\n" +
                    "§630% Chance auf Qualitätsverbesserung!"
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

                player.displayClientMessage(Component.literal(
                    "§a✓ Fermentierung abgeschlossen!\n" +
                    "§7Erhalten: §e" + fermented.getCount() + "x §7fermentierten Tabak"
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

            player.displayClientMessage(Component.literal(
                "§6═══ Mittleres Fermentierungsfass ═══\n" +
                "§7Kapazität: §e" + inputCount + "/8\n" +
                "§7Fortschritt: " + bar + " §e" + String.format("%.1f", progress) + "%\n" +
                "§7Fertig: §e" + outputCount + "x\n" +
                (barrelBE.hasOutput() ? "§a✓ Shift+Rechtsklick zum Entnehmen" : "§7Fermentierung läuft...")
            ), false);
        } else {
            player.displayClientMessage(Component.literal(
                "§6═══ Mittleres Fermentierungsfass ═══\n" +
                "§7Kapazität: §e8 Blätter\n" +
                "§7Leer - Lege getrocknete Tabakblätter hinein\n" +
                "§830% Chance auf Qualitätsverbesserung"
            ), false);
        }

        return InteractionResult.SUCCESS;
    }

    private String createProgressBar(float progress) {
        int filled = (int) (progress * 10);
        int empty = 10 - filled;
        return "§6" + "▰".repeat(Math.max(0, filled)) + "§7" + "▱".repeat(Math.max(0, empty));
    }
}
