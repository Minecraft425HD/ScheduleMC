package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.tobacco.items.WateringCanItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Waschbecken-Block
 * Füllt Gießkannen mit Wasser auf
 */
public class SinkBlock extends Block {
    
    public SinkBlock(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, 
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        
        ItemStack heldItem = player.getItemInHand(hand);
        
        // ═══════════════════════════════════════════════════════════
        // GIESSKANNEN AUFFÜLLEN
        // ═══════════════════════════════════════════════════════════
        if (heldItem.getItem() instanceof WateringCanItem) {
            if (WateringCanItem.isFull(heldItem)) {
                player.displayClientMessage(Component.literal(
                    "§e⚠ Gießkanne ist bereits voll!"
                ), true);
                return InteractionResult.FAIL;
            }
            
            // Fülle komplett auf
            WateringCanItem.setWaterLevel(heldItem, 1000);
            
            player.displayClientMessage(Component.literal(
                "§b✓ Gießkanne aufgefüllt!\n" +
                "§7Wasser: §b1000/1000"
            ), true);
            
            // Spiele Sound ab
            player.playSound(net.minecraft.sounds.SoundEvents.BUCKET_FILL, 1.0f, 1.0f);
            
            return InteractionResult.SUCCESS;
        }
        
        // ═══════════════════════════════════════════════════════════
        // INFO ANZEIGEN
        // ═══════════════════════════════════════════════════════════
        player.displayClientMessage(Component.literal(
            "§b═══ Waschbecken ═══\n" +
            "§7Rechtsklick mit Gießkanne\n" +
            "§7zum Auffüllen"
        ), false);
        
        return InteractionResult.SUCCESS;
    }
}
