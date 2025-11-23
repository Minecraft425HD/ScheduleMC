package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.tobacco.blockentity.FermentationBarrelBlockEntity;
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
 * Fermentierungsfass-Block
 * Fermentiert getrocknete Tabakblätter und verbessert Qualität
 */
public class FermentationBarrelBlock extends Block implements EntityBlock {
    
    public FermentationBarrelBlock(Properties properties) {
        super(properties);
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FermentationBarrelBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof FermentationBarrelBlockEntity barrelBE) {
                barrelBE.tick();
            }
        };
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, 
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FermentationBarrelBlockEntity barrelBE)) {
            return InteractionResult.PASS;
        }
        
        ItemStack heldItem = player.getItemInHand(hand);
        
        // ═══════════════════════════════════════════════════════════
        // 1. GETROCKNETE BLÄTTER HINZUFÜGEN
        // ═══════════════════════════════════════════════════════════
        if (heldItem.getItem() instanceof DriedTobaccoLeafItem) {
            if (barrelBE.hasInput()) {
                player.displayClientMessage(Component.literal(
                    "§c✗ Fass ist bereits belegt!"
                ), true);
                return InteractionResult.FAIL;
            }
            
            if (barrelBE.addDriedLeaves(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(heldItem.getCount());
                }
                
                player.displayClientMessage(Component.literal(
                    "§a✓ Fermentierung gestartet!\n" +
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
            ItemStack fermented = barrelBE.extractFermentedLeaves();
            if (!fermented.isEmpty()) {
                player.getInventory().add(fermented);
                
                player.displayClientMessage(Component.literal(
                    "§a✓ Fermentierung abgeschlossen!\n" +
                    "§7Erhalten: §e" + fermented.getCount() + "x §7fermentierten Tabak\n" +
                    "§7Verkaufe im Shop!"
                ), true);
                return InteractionResult.SUCCESS;
            }
        }
        
        // ═══════════════════════════════════════════════════════════
        // 3. FORTSCHRITT ANZEIGEN
        // ═══════════════════════════════════════════════════════════
        if (barrelBE.hasInput()) {
            float progress = barrelBE.getFermentationPercentage() * 100;
            String bar = createProgressBar(barrelBE.getFermentationPercentage());
            
            player.displayClientMessage(Component.literal(
                "§6═══ Fermentierungsfass ═══\n" +
                "§7Fortschritt: " + bar + " §e" + String.format("%.1f", progress) + "%\n" +
                (barrelBE.hasOutput() ? "§a✓ Fertig! Shift+Rechtsklick zum Entnehmen" : "§7Fermentierung läuft...")
            ), false);
        } else {
            player.displayClientMessage(Component.literal(
                "§6═══ Fermentierungsfass ═══\n" +
                "§7Leer\n" +
                "§7Lege getrocknete Tabakblätter hinein\n" +
                "§830% Chance auf Qualitätsverbesserung"
            ), false);
        }
        
        return InteractionResult.SUCCESS;
    }
    
    /**
     * Erstellt visuellen Fortschrittsbalken
     */
    private String createProgressBar(float progress) {
        int filled = (int) (progress * 10);
        int empty = 10 - filled;
        return "§6" + "▰".repeat(Math.max(0, filled)) + "§7" + "▱".repeat(Math.max(0, empty));
    }
}
