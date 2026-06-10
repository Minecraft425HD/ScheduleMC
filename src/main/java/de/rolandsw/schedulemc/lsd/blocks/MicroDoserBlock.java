package de.rolandsw.schedulemc.lsd.blocks;

import de.rolandsw.schedulemc.lsd.blockentity.MicroDoserBlockEntity;
import de.rolandsw.schedulemc.lsd.items.LysergicAcidItem;
import de.rolandsw.schedulemc.lsd.menu.MicroDoserMenu;
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

public class MicroDoserBlock extends Block implements EntityBlock {

    public MicroDoserBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MicroDoserBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof MicroDoserBlockEntity doser) {
                doser.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MicroDoserBlockEntity doser)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Lysergsäure hinzufügen
        if (heldItem.getItem() instanceof LysergicAcidItem) {
            if (doser.addLysergicAcid(heldItem)) {
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(Component.translatable(
                        "block.lsd.mikro_input", doser.getLysergicAcidCount()
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.BOTTLE_FILL, 0.5f, 1.2f);
                return InteractionResult.SUCCESS;
            }
        }

        // Leere Hand - GUI öffnen oder Output entnehmen
        if (heldItem.isEmpty()) {
            if (doser.hasOutput()) {
                ItemStack output = doser.extractOutput();
                if (!player.getInventory().add(output)) {
                    player.drop(output, false);
                }
                player.displayClientMessage(Component.translatable(
                        "block.lsd.mikro_output"
                ), true);
                return InteractionResult.SUCCESS;
            }

            // GUI öffnen wenn Input vorhanden
            if (doser.hasInput() && !doser.isProcessing()) {
                openGui(player, doser, pos);
                return InteractionResult.SUCCESS;
            }

            // Status
            net.minecraft.network.chat.MutableComponent message = Component.translatable("block.micro_doser.title")
                    .append(Component.literal("\n"))
                    .append(Component.translatable("block.lsd.mikro_count", doser.getLysergicAcidCount()))
                    .append(Component.literal("\n"))
                    .append(Component.translatable("block.micro_doser.dosage", doser.getCurrentMicrograms()))
                    .append(Component.literal("\n"));

            if (doser.isProcessing()) {
                message = message.append(Component.translatable("block.micro_doser.progress", (int)(doser.getProgress() * 100)));
            }

            player.displayClientMessage(message, true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void openGui(Player player, MicroDoserBlockEntity doser, BlockPos pos) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("block.micro_doser.display_name");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new MicroDoserMenu(containerId, playerInventory, doser);
            }
        }, pos);
    }
}
