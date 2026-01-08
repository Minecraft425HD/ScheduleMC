package de.rolandsw.schedulemc.mdma.blocks;

import de.rolandsw.schedulemc.mdma.blockentity.PillenPresseBlockEntity;
import de.rolandsw.schedulemc.mdma.items.BindemittelItem;
import de.rolandsw.schedulemc.mdma.items.FarbstoffItem;
import de.rolandsw.schedulemc.mdma.items.MDMAKristallItem;
import de.rolandsw.schedulemc.mdma.menu.PillenPresseMenu;
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

public class PillenPresseBlock extends Block implements EntityBlock {

    public PillenPresseBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PillenPresseBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof PillenPresseBlockEntity presse) presse.tick();
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PillenPresseBlockEntity presse)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // MDMA-Kristalle hinzufügen
        if (heldItem.getItem() instanceof MDMAKristallItem) {
            if (presse.addMDMAKristall(heldItem)) {
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(Component.literal(
                        "§a✓ MDMA-Kristalle hinzugefügt! (" + presse.getKristallCount() + "/16)"
                ), true);
                return InteractionResult.SUCCESS;
            }
        }

        // Bindemittel hinzufügen
        if (heldItem.getItem() instanceof BindemittelItem) {
            if (presse.addBindemittel(heldItem)) {
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(Component.literal(
                        "§a✓ Bindemittel hinzugefügt! (" + presse.getBindemittelCount() + "/16)"
                ), true);
                return InteractionResult.SUCCESS;
            }
        }

        // Farbstoff hinzufügen
        if (heldItem.getItem() instanceof FarbstoffItem) {
            if (presse.addFarbstoff(heldItem)) {
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(Component.literal(
                        "§a✓ Farbe: " + presse.getSelectedColor().getColoredName()
                ), true);
                return InteractionResult.SUCCESS;
            }
        }

        // Leere Hand
        if (heldItem.isEmpty()) {
            // Output entnehmen
            if (presse.hasOutput()) {
                ItemStack output = presse.extractOutput();
                if (!player.getInventory().add(output)) player.drop(output, false);
                player.displayClientMessage(Component.literal(
                        "§a✓ " + output.getCount() + "x Ecstasy-Pillen entnommen!"
                ), true);
                return InteractionResult.SUCCESS;
            }

            // Schleichen = Design/Farbe wechseln
            if (player.isShiftKeyDown() && !presse.isMinigameActive()) {
                presse.cycleDesign();
                player.displayClientMessage(Component.literal(
                        "§7Design: " + presse.getSelectedDesign().getColoredName() +
                        " " + presse.getSelectedDesign().getSymbol()
                ), true);
                return InteractionResult.SUCCESS;
            }

            // GUI öffnen wenn bereit
            if (presse.canStart()) {
                openGui(player, presse, pos);
                return InteractionResult.SUCCESS;
            }

            // Status
            StringBuilder status = new StringBuilder();
            status.append("§d⚗ Pillen-Presse\n");
            status.append("§7Design: ").append(presse.getSelectedDesign().getColoredName())
                  .append(" ").append(presse.getSelectedDesign().getSymbol()).append("\n");
            status.append("§7Farbe: ").append(presse.getSelectedColor().getColoredName()).append("\n");
            status.append("§7Kristalle: §f").append(presse.getKristallCount()).append("/16\n");
            status.append("§7Bindemittel: §f").append(presse.getBindemittelCount()).append("/16\n");
            if (presse.canStart()) {
                status.append("§a▶ Bereit! Klicken zum Starten");
            }
            player.displayClientMessage(Component.literal(status.toString()), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void openGui(Player player, PillenPresseBlockEntity presse, BlockPos pos) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // Starte das Minigame
        presse.startMinigame(player.getUUID());

        NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Pillen-Presse");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new PillenPresseMenu(containerId, playerInventory, presse);
            }
        }, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PillenPresseBlockEntity presse) {
                presse.cancelMinigame();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
