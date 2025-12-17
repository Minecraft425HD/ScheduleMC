package de.rolandsw.schedulemc.cannabis.blocks;

import de.rolandsw.schedulemc.cannabis.blockentity.TrocknungsnetzBlockEntity;
import de.rolandsw.schedulemc.cannabis.blockentity.CannabisBlockEntities;
import de.rolandsw.schedulemc.cannabis.items.FreshBudItem;
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
 * Trocknungsnetz für Cannabis-Blüten
 */
public class TrocknungsnetzBlock extends BaseEntityBlock {

    public TrocknungsnetzBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrocknungsnetzBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, CannabisBlockEntities.TROCKNUNGSNETZ.get(),
                (lvl, pos, st, be) -> be.tick());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TrocknungsnetzBlockEntity netz)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Frische Buds hinzufügen
        if (heldItem.getItem() instanceof FreshBudItem) {
            for (int i = 0; i < 4; i++) {
                if (!netz.hasItem(i)) {
                    if (netz.addFreshBud(heldItem, i)) {
                        if (!player.isCreative()) {
                            heldItem.shrink(1);
                        }
                        player.displayClientMessage(Component.literal("§a🌿 Blüten zum Trocknen aufgehängt (Slot " + (i+1) + ")"), true);
                        return InteractionResult.CONSUME;
                    }
                }
            }
            player.displayClientMessage(Component.literal("§c✗ Trocknungsnetz ist voll!"), true);
            return InteractionResult.FAIL;
        }

        // Getrocknete Buds entnehmen
        if (heldItem.isEmpty()) {
            for (int i = 0; i < 4; i++) {
                if (netz.isDried(i)) {
                    ItemStack dried = netz.extractDriedBud(i);
                    if (!dried.isEmpty()) {
                        player.addItem(dried);
                        player.displayClientMessage(Component.literal("§e🍂 Getrocknete Blüten entnommen!"), true);
                        return InteractionResult.CONSUME;
                    }
                }
            }

            // Status anzeigen
            StringBuilder status = new StringBuilder("§7Trocknungsnetz: ");
            int filled = netz.getFilledSlots();
            if (filled == 0) {
                status.append("§8Leer");
            } else {
                for (int i = 0; i < 4; i++) {
                    if (netz.hasItem(i)) {
                        int progress = (int) (netz.getDryingProgress(i) * 100);
                        if (netz.isDried(i)) {
                            status.append("§a✓");
                        } else {
                            status.append("§e").append(progress).append("%");
                        }
                    } else {
                        status.append("§8-");
                    }
                    if (i < 3) status.append(" ");
                }
            }
            player.displayClientMessage(Component.literal(status.toString()), true);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TrocknungsnetzBlockEntity netz) {
                for (int i = 0; i < 4; i++) {
                    if (netz.isDried(i)) {
                        ItemStack dried = netz.extractDriedBud(i);
                        if (!dried.isEmpty()) {
                            Block.popResource(level, pos, dried);
                        }
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
