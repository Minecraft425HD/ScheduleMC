package de.rolandsw.schedulemc.poppy.blocks;

import de.rolandsw.schedulemc.poppy.blockentity.RitzmaschineBlockEntity;
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
 * Ritzmaschine - ritzt Mohnkapseln automatisch zu Rohopium
 * Benötigt Redstone-Signal
 */
public class RitzmaschineBlock extends Block implements EntityBlock {

    public RitzmaschineBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RitzmaschineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof RitzmaschineBlockEntity machineB) {
                machineB.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RitzmaschineBlockEntity machineBE)) return InteractionResult.PASS;

        ItemStack handStack = player.getItemInHand(hand);

        // 1. Mohnkapseln hinzufügen
        if (handStack.getItem() instanceof PoppyPodItem) {
            if (machineBE.isFull()) {
                player.displayClientMessage(Component.translatable("message.ritzmaschine.machine_full"), true);
                return InteractionResult.FAIL;
            }

            if (machineBE.addPod(handStack)) {
                handStack.shrink(1);
                player.displayClientMessage(Component.translatable("message.ritzmaschine.pod_added", machineBE.getInputCount(), machineBE.getCapacity()), true);
                player.playSound(net.minecraft.sounds.SoundEvents.ITEM_FRAME_ADD_ITEM, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        // 2. Rohopium entnehmen
        if (handStack.isEmpty() && player.isShiftKeyDown()) {
            if (machineBE.hasOutput()) {
                ItemStack opium = machineBE.extractAllOpium();
                if (!opium.isEmpty()) {
                    player.getInventory().add(opium);
                    player.displayClientMessage(Component.translatable("message.ritzmaschine.opium_extracted", opium.getCount()), true);
                    player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        // 3. Status anzeigen
        if (handStack.isEmpty() && !player.isShiftKeyDown()) {
            boolean hasPower = level.hasNeighborSignal(pos);
            float progress = machineBE.getAverageProgress() * 100;
            player.displayClientMessage(Component.translatable("message.ritzmaschine.status",
                    hasPower ? Component.translatable("message.ritzmaschine.power_on") : Component.translatable("message.ritzmaschine.power_off"),
                    machineBE.getInputCount(), machineBE.getCapacity(),
                    machineBE.getOutputCount(),
                    String.format("%.1f", progress)), false);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
