package de.rolandsw.schedulemc.lsd.blocks;

import de.rolandsw.schedulemc.lsd.blockentity.MikroDosiererBlockEntity;
import de.rolandsw.schedulemc.lsd.items.LysergsaeureItem;
import de.rolandsw.schedulemc.lsd.menu.MikroDosiererMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class MikroDosiererBlock extends Block implements EntityBlock {

    public MikroDosiererBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MikroDosiererBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof MikroDosiererBlockEntity dosierer) {
                dosierer.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MikroDosiererBlockEntity dosierer)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Lysergsäure hinzufügen
        if (heldItem.getItem() instanceof LysergsaeureItem) {
            if (dosierer.addLysergsaeure(heldItem)) {
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(Component.translatable(
                        "block.lsd.mikro_input", dosierer.getLysergsaeureCount()
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.BOTTLE_FILL, 0.5f, 1.2f);
                return InteractionResult.SUCCESS;
            }
        }

        // Leere Hand - GUI öffnen oder Output entnehmen
        if (heldItem.isEmpty()) {
            if (dosierer.hasOutput()) {
                ItemStack output = dosierer.extractOutput();
                if (!player.getInventory().add(output)) {
                    player.drop(output, false);
                }
                player.displayClientMessage(Component.translatable(
                        "block.lsd.mikro_output"
                ), true);
                return InteractionResult.SUCCESS;
            }

            // GUI öffnen wenn Input vorhanden
            if (dosierer.hasInput() && !dosierer.isProcessing()) {
                openGui(player, dosierer, pos);
                return InteractionResult.SUCCESS;
            }

            // Status
            Component message = Component.translatable("block.mikro_dosierer.title")
                    .append(Component.literal("\n"))
                    .append(Component.translatable("block.lsd.mikro_count", dosierer.getLysergsaeureCount()))
                    .append(Component.literal("\n"))
                    .append(Component.translatable("block.mikro_dosierer.dosage", dosierer.getCurrentMicrograms()))
                    .append(Component.literal("\n"));

            if (dosierer.isProcessing()) {
                message = message.append(Component.translatable("block.mikro_dosierer.progress", (int)(dosierer.getProgress() * 100)));
            }

            player.displayClientMessage(message, true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void openGui(Player player, MikroDosiererBlockEntity dosierer, BlockPos pos) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("block.mikro_dosierer.display_name");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new MikroDosiererMenu(containerId, playerInventory, dosierer);
            }
        }, pos);
    }
}
