package de.rolandsw.schedulemc.mdma.blocks;

import de.rolandsw.schedulemc.mdma.blockentity.TrocknungsOfenBlockEntity;
import de.rolandsw.schedulemc.mdma.items.MDMABaseItem;
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

public class TrocknungsOfenBlock extends Block implements EntityBlock {

    public TrocknungsOfenBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrocknungsOfenBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof TrocknungsOfenBlockEntity ofen) ofen.tick();
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TrocknungsOfenBlockEntity ofen)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.getItem() instanceof MDMABaseItem) {
            if (ofen.addMDMABase(heldItem)) {
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(Component.literal(
                        "§a✓ MDMA-Base hinzugefügt! (" + ofen.getInputCount() + "/8)"
                ), true);
                return InteractionResult.SUCCESS;
            }
        }

        if (heldItem.isEmpty()) {
            if (ofen.hasOutput()) {
                ItemStack output = ofen.extractOutput();
                if (!player.getInventory().add(output)) player.drop(output, false);
                player.displayClientMessage(Component.literal(
                        "§a✓ " + output.getCount() + "x MDMA-Kristalle entnommen!"
                ), true);
                return InteractionResult.SUCCESS;
            }

            StringBuilder status = new StringBuilder();
            status.append("§6⚗ Trocknungs-Ofen\n");
            status.append("§7MDMA-Base: §f").append(ofen.getInputCount()).append("/8\n");
            if (ofen.isActive()) {
                status.append("§7Fortschritt: §e").append((int)(ofen.getProgress() * 100)).append("%");
            }
            player.displayClientMessage(Component.literal(status.toString()), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
