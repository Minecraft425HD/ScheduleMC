package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.tobacco.blockentity.SinkBlockEntity;
import de.rolandsw.schedulemc.tobacco.blockentity.TobaccoBlockEntities;
import de.rolandsw.schedulemc.tobacco.items.WateringCanItem;
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
 * Waschbecken-Block
 * Füllt Gießkannen mit Wasser auf
 * Integriert mit Utility-System für Wasserverbrauch
 */
public class SinkBlock extends Block implements EntityBlock {
    
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

            // Melde Wasserverbrauch an Utility-System
            if (level.getBlockEntity(pos) instanceof SinkBlockEntity sinkBE) {
                sinkBE.onWaterUsed();
            }

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

    // ═══════════════════════════════════════════════════════════════════════════
    // EntityBlock Implementation
    // ═══════════════════════════════════════════════════════════════════════════

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SinkBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == TobaccoBlockEntities.SINK.get()
            ? (lvl, pos, st, be) -> ((SinkBlockEntity) be).tick()
            : null;
    }
}
