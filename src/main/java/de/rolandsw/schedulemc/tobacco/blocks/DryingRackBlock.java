package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.tobacco.blockentity.DryingRackBlockEntity;
import de.rolandsw.schedulemc.tobacco.items.FreshTobaccoLeafItem;
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
 * Trocknungsgestell-Block
 * Trocknet frische Tabakblätter über Zeit
 */
public class DryingRackBlock extends Block implements EntityBlock {
    
    public DryingRackBlock(Properties properties) {
        super(properties);
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof DryingRackBlockEntity rackBE) {
                rackBE.tick();
            }
        };
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, 
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof DryingRackBlockEntity rackBE)) {
            return InteractionResult.PASS;
        }
        
        ItemStack heldItem = player.getItemInHand(hand);
        
        // ═══════════════════════════════════════════════════════════
        // 1. FRISCHE BLÄTTER HINZUFÜGEN
        // ═══════════════════════════════════════════════════════════
        if (heldItem.getItem() instanceof FreshTobaccoLeafItem) {
            if (rackBE.hasInput()) {
                player.displayClientMessage(Component.literal(
                    "§c✗ Trocknungsgestell ist bereits belegt!"
                ), true);
                return InteractionResult.FAIL;
            }
            
            if (rackBE.addFreshLeaves(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(heldItem.getCount());
                }
                
                player.displayClientMessage(Component.literal(
                    "§a✓ Trocknung gestartet!\n" +
                    "§7Dauer: §e~5 Minuten\n" +
                    "§7Komm später wieder"
                ), true);
                return InteractionResult.SUCCESS;
            }
        }
        
        // ═══════════════════════════════════════════════════════════
        // 2. GETROCKNETE BLÄTTER ENTNEHMEN
        // ═══════════════════════════════════════════════════════════
        if (player.isShiftKeyDown() && rackBE.hasOutput()) {
            ItemStack dried = rackBE.extractDriedLeaves();
            if (!dried.isEmpty()) {
                player.getInventory().add(dried);
                
                player.displayClientMessage(Component.literal(
                    "§a✓ Trocknung abgeschlossen!\n" +
                    "§7Erhalten: §e" + dried.getCount() + "x §7getrocknete Blätter"
                ), true);
                return InteractionResult.SUCCESS;
            }
        }
        
        // ═══════════════════════════════════════════════════════════
        // 3. FORTSCHRITT ANZEIGEN
        // ═══════════════════════════════════════════════════════════
        if (rackBE.hasInput()) {
            float progress = rackBE.getDryingPercentage() * 100;
            String bar = createProgressBar(rackBE.getDryingPercentage());
            
            player.displayClientMessage(Component.literal(
                "§6═══ Trocknungsgestell ═══\n" +
                "§7Fortschritt: " + bar + " §e" + String.format("%.1f", progress) + "%\n" +
                (rackBE.hasOutput() ? "§a✓ Fertig! Shift+Rechtsklick zum Entnehmen" : "§7Trocknung läuft...")
            ), false);
        } else {
            player.displayClientMessage(Component.literal(
                "§6═══ Trocknungsgestell ═══\n" +
                "§7Leer\n" +
                "§7Lege frische Tabakblätter hinein"
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
        return "§a" + "▰".repeat(Math.max(0, filled)) + "§7" + "▱".repeat(Math.max(0, empty));
    }
}
