package de.rolandsw.schedulemc.mdma.blocks;

import de.rolandsw.schedulemc.mdma.blockentity.PillPressBlockEntity;
import de.rolandsw.schedulemc.mdma.items.BindingAgentItem;
import de.rolandsw.schedulemc.mdma.items.PillDyeItem;
import de.rolandsw.schedulemc.mdma.items.MDMACrystalItem;
import de.rolandsw.schedulemc.mdma.menu.PillPressMenu;
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

public class PillPressBlock extends Block implements EntityBlock {

    public PillPressBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PillPressBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof PillPressBlockEntity press) press.tick();
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PillPressBlockEntity press)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // MDMA-Kristalle hinzufügen
        if (heldItem.getItem() instanceof MDMACrystalItem) {
            if (press.addMDMACrystal(heldItem)) {
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(Component.translatable(
                        "block.mdma.press_crystal", press.getCrystalCount()
                ), true);
                return InteractionResult.SUCCESS;
            }
        }

        // Bindemittel hinzufügen
        if (heldItem.getItem() instanceof BindingAgentItem) {
            if (press.addBindingAgent(heldItem)) {
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(Component.translatable(
                        "block.mdma.press_binder", press.getBindingAgentCount()
                ), true);
                return InteractionResult.SUCCESS;
            }
        }

        // Farbstoff hinzufügen
        if (heldItem.getItem() instanceof PillDyeItem) {
            if (press.addPillDye(heldItem)) {
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(Component.translatable(
                        "block.mdma.press_color", press.getSelectedColor().getColoredName()
                ), true);
                return InteractionResult.SUCCESS;
            }
        }

        // Leere Hand
        if (heldItem.isEmpty()) {
            // Output entnehmen
            if (press.hasOutput()) {
                ItemStack output = press.extractOutput();
                if (!player.getInventory().add(output)) player.drop(output, false);
                player.displayClientMessage(Component.translatable(
                        "block.mdma.press_output", output.getCount()
                ), true);
                return InteractionResult.SUCCESS;
            }

            // Schleichen = Design/Farbe wechseln
            if (player.isShiftKeyDown() && !press.isMinigameActive()) {
                press.cycleDesign();
                player.displayClientMessage(Component.translatable(
                        "block.mdma.press_design", press.getSelectedDesign().getColoredName(),
                        press.getSelectedDesign().getSymbol()
                ), true);
                return InteractionResult.SUCCESS;
            }

            // GUI öffnen wenn bereit
            if (press.canStart()) {
                openGui(player, press, pos);
                return InteractionResult.SUCCESS;
            }

            // Status
            player.displayClientMessage(Component.translatable("gui.pill_press.status_header").append(Component.literal("\n"))
                    .append(Component.translatable("gui.pill_press.status_design", press.getSelectedDesign().getColoredName(), press.getSelectedDesign().getSymbol())).append(Component.literal("\n"))
                    .append(Component.translatable("gui.pill_press.status_color", press.getSelectedColor().getColoredName())).append(Component.literal("\n"))
                    .append(Component.translatable("block.mdma.press_crystal_count", press.getCrystalCount()))
                    .append(Component.literal("\n"))
                    .append(Component.translatable("block.mdma.press_binder_count", press.getBindingAgentCount()))
                    .append(Component.literal("\n"))
                    .append(press.canStart() ? Component.translatable("block.mdma.press_ready") : Component.literal(""))
            , true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void openGui(Player player, PillPressBlockEntity press, BlockPos pos) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // Starte das Minigame
        press.startMinigame(player.getUUID());

        NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("block.schedulemc.pill_press");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new PillPressMenu(containerId, playerInventory, press);
            }
        }, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PillPressBlockEntity press) {
                press.cancelMinigame();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
