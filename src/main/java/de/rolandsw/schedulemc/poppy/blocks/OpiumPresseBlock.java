package de.rolandsw.schedulemc.poppy.blocks;

import de.rolandsw.schedulemc.coca.items.CocaItems;
import de.rolandsw.schedulemc.coca.items.DieselCanisterItem;
import de.rolandsw.schedulemc.poppy.blockentity.OpiumPresseBlockEntity;
import de.rolandsw.schedulemc.poppy.items.PoppyPodItem;
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
 * Opium-Presse - presst Mohnkapseln mit Diesel zu Rohopium
 * Höherer Ertrag als Ritzmaschine
 */
public class OpiumPresseBlock extends Block implements EntityBlock {

    public OpiumPresseBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OpiumPresseBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof OpiumPresseBlockEntity presseBE) {
                presseBE.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof OpiumPresseBlockEntity presseBE)) return InteractionResult.PASS;

        ItemStack handStack = player.getItemInHand(hand);

        // 1. Diesel hinzufügen
        if (handStack.getItem() instanceof DieselCanisterItem) {
            int dieselAmount = DieselCanisterItem.getDieselAmount(handStack);
            if (dieselAmount > 0) {
                if (presseBE.getDieselLevel() >= presseBE.getMaxDiesel()) {
                    player.displayClientMessage(Component.literal("§c✗ Diesel-Tank ist voll!"), true);
                    return InteractionResult.FAIL;
                }

                int toAdd = Math.min(dieselAmount, presseBE.getMaxDiesel() - presseBE.getDieselLevel());
                presseBE.addDiesel(toAdd);
                DieselCanisterItem.consumeDiesel(handStack, toAdd);

                player.displayClientMessage(Component.literal(
                        "§e✓ Diesel hinzugefügt!\n" +
                                "§7Tank: §e" + presseBE.getDieselLevel() + "/" + presseBE.getMaxDiesel() + " mB"
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        // 2. Mohnkapseln hinzufügen
        if (handStack.getItem() instanceof PoppyPodItem) {
            if (presseBE.isFull()) {
                player.displayClientMessage(Component.literal("§c✗ Presse ist voll!"), true);
                return InteractionResult.FAIL;
            }

            if (presseBE.addPod(handStack)) {
                handStack.shrink(1);
                player.displayClientMessage(Component.literal(
                        "§6✓ Mohnkapsel hinzugefügt!\n" +
                                "§7Inhalt: §e" + presseBE.getInputCount() + "/" + presseBE.getCapacity()
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.ITEM_FRAME_ADD_ITEM, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        // 3. Rohopium entnehmen
        if (handStack.isEmpty() && player.isShiftKeyDown()) {
            if (presseBE.hasOutput()) {
                ItemStack opium = presseBE.extractAllOpium();
                if (!opium.isEmpty()) {
                    player.getInventory().add(opium);
                    player.displayClientMessage(Component.literal(
                            "§6✓ Rohopium entnommen!\n" +
                                    "§7Menge: §e" + opium.getCount()
                    ), true);
                    player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        // 4. Status anzeigen
        if (handStack.isEmpty() && !player.isShiftKeyDown()) {
            float progress = presseBE.getAverageProgress() * 100;
            player.displayClientMessage(Component.literal(
                    "§4═══ Opium-Presse ═══\n" +
                            "§7Diesel: §e" + presseBE.getDieselLevel() + "/" + presseBE.getMaxDiesel() + " mB\n" +
                            "§7Kapseln: §e" + presseBE.getInputCount() + "/" + presseBE.getCapacity() + "\n" +
                            "§7Rohopium fertig: §e" + presseBE.getOutputCount() + "\n" +
                            "§7Fortschritt: §e" + String.format("%.1f", progress) + "%"
            ), false);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
