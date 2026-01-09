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
                player.displayClientMessage(Component.translatable(
                        "block.mdma.reaction_input", kessel.getSafrolCount()
                ), true);
                return InteractionResult.SUCCESS;
            }
        }

        if (heldItem.isEmpty()) {
            if (kessel.hasOutput()) {
                ItemStack output = kessel.extractOutput();
                if (!player.getInventory().add(output)) player.drop(output, false);
                player.displayClientMessage(Component.translatable(
                        "block.mdma.reaction_output", output.getCount()
                ), true);
                return InteractionResult.SUCCESS;
            }

            player.displayClientMessage(Component.literal("§6⚗ Reaktions-Kessel\n")
                    .append(Component.translatable("block.mdma.reaction_count", kessel.getSafrolCount()))
                    .append(Component.literal("\n"))
                    .append(kessel.isActive() ? Component.literal("§7Fortschritt: §e" + (int)(kessel.getProgress() * 100) + "%") : Component.literal(""))
            , true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
