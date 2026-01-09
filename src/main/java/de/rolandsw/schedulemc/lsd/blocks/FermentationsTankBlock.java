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
                player.displayClientMessage(Component.translatable(
                        "block.lsd.fermentation_input", tank.getMutterkornCount()
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
                player.displayClientMessage(Component.translatable(
                        "block.lsd.fermentation_output", output.getCount()
                ), true);
                return InteractionResult.SUCCESS;
            }

            // Status
            player.displayClientMessage(Component.literal("§5⚗ Fermentations-Tank\n")
                    .append(Component.translatable("block.lsd.fermentation_count", tank.getMutterkornCount()))
                    .append(Component.literal("\n"))
                    .append(tank.isActive() ? Component.literal("§7Fortschritt: §e").append(Component.literal((int)(tank.getProgress() * 100) + "%")) : tank.hasOutput() ? Component.literal("§a").append(Component.literal(tank.getOutputCount() + "x Ergot-Kultur fertig!")) : Component.literal(""))
            , true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
