package de.rolandsw.schedulemc.mdma.blocks;

import de.rolandsw.schedulemc.mdma.blockentity.ReaktionsKesselBlockEntity;
import de.rolandsw.schedulemc.mdma.items.SafrolItem;
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

public class ReaktionsKesselBlock extends Block implements EntityBlock {

    public ReaktionsKesselBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReaktionsKesselBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof ReaktionsKesselBlockEntity kessel) kessel.tick();
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ReaktionsKesselBlockEntity kessel)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.getItem() instanceof SafrolItem) {
            if (kessel.addSafrol(heldItem)) {
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(Component.literal(
                        "§a✓ Safrol hinzugefügt! (" + kessel.getSafrolCount() + "/8)"
                ), true);
                return InteractionResult.SUCCESS;
            }
        }

        if (heldItem.isEmpty()) {
            if (kessel.hasOutput()) {
                ItemStack output = kessel.extractOutput();
                if (!player.getInventory().add(output)) player.drop(output, false);
                player.displayClientMessage(Component.literal(
                        "§a✓ " + output.getCount() + "x MDMA-Base entnommen!"
                ), true);
                return InteractionResult.SUCCESS;
            }

            StringBuilder status = new StringBuilder();
            status.append("§6⚗ Reaktions-Kessel\n");
            status.append("§7Safrol: §f").append(kessel.getSafrolCount()).append("/8\n");
            if (kessel.isActive()) {
                status.append("§7Fortschritt: §e").append((int)(kessel.getProgress() * 100)).append("%");
            }
            player.displayClientMessage(Component.literal(status.toString()), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
