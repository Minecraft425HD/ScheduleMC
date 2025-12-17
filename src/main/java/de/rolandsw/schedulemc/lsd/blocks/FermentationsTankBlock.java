package de.rolandsw.schedulemc.lsd.blocks;

import de.rolandsw.schedulemc.lsd.blockentity.FermentationsTankBlockEntity;
import de.rolandsw.schedulemc.lsd.items.MutterkornItem;
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

public class FermentationsTankBlock extends Block implements EntityBlock {

    public FermentationsTankBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FermentationsTankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof FermentationsTankBlockEntity tank) {
                tank.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FermentationsTankBlockEntity tank)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Mutterkorn hinzufügen
        if (heldItem.getItem() instanceof MutterkornItem) {
            if (tank.addMutterkorn(heldItem)) {
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(Component.literal(
                        "§a✓ Mutterkorn hinzugefügt! (" + tank.getMutterkornCount() + "/8)"
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.BREWING_STAND_BREW, 0.5f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        // Leere Hand
        if (heldItem.isEmpty()) {
            if (tank.hasOutput()) {
                ItemStack output = tank.extractOutput();
                if (!player.getInventory().add(output)) {
                    player.drop(output, false);
                }
                player.displayClientMessage(Component.literal(
                        "§a✓ " + output.getCount() + "x Ergot-Kultur entnommen!"
                ), true);
                return InteractionResult.SUCCESS;
            }

            // Status
            StringBuilder status = new StringBuilder();
            status.append("§5⚗ Fermentations-Tank\n");
            status.append("§7Mutterkorn: §f").append(tank.getMutterkornCount()).append("/8\n");
            if (tank.isActive()) {
                status.append("§7Fortschritt: §e").append((int)(tank.getProgress() * 100)).append("%");
            } else if (tank.hasOutput()) {
                status.append("§a").append(tank.getOutputCount()).append("x Ergot-Kultur fertig!");
            }
            player.displayClientMessage(Component.literal(status.toString()), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
