package de.rolandsw.schedulemc.lsd.blocks;

import de.rolandsw.schedulemc.lsd.blockentity.PerforationsPresseBlockEntity;
import de.rolandsw.schedulemc.lsd.items.BlotterPapierItem;
import de.rolandsw.schedulemc.lsd.items.LSDLoesungItem;
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

public class PerforationsPresseBlock extends Block implements EntityBlock {

    public PerforationsPresseBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PerforationsPresseBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof PerforationsPresseBlockEntity presse) {
                presse.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PerforationsPresseBlockEntity presse)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // LSD-Lösung hinzufügen
        if (heldItem.getItem() instanceof LSDLoesungItem) {
            if (presse.addLSDLoesung(heldItem)) {
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(Component.translatable(
                        "block.lsd.perforation_solution"
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.BOTTLE_EMPTY, 0.5f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        // Blotter-Papier hinzufügen
        if (heldItem.getItem() instanceof BlotterPapierItem) {
            if (presse.addBlotterPapier(heldItem)) {
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(Component.translatable(
                        "block.lsd.perforation_paper", presse.getBlotterPapierCount()
                ), true);
                return InteractionResult.SUCCESS;
            }
        }

        // Leere Hand
        if (heldItem.isEmpty()) {
            // Output entnehmen
            if (presse.hasOutput()) {
                ItemStack output = presse.extractOutput();
                if (!player.getInventory().add(output)) {
                    player.drop(output, false);
                }
                player.displayClientMessage(Component.translatable(
                        "block.lsd.perforation_output", output.getCount()
                ), true);
                return InteractionResult.SUCCESS;
            }

            // Schleichen = Design wechseln
            if (player.isShiftKeyDown() && !presse.isPressing()) {
                presse.cycleDesign();
                player.displayClientMessage(Component.translatable(
                        "block.lsd.perforation_design", presse.getSelectedDesign().getColoredName(), presse.getSelectedDesign().getSymbol()
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
                return InteractionResult.SUCCESS;
            }

            // Starten wenn bereit
            if (presse.hasLoesung() && presse.hasPapier() && !presse.isPressing()) {
                if (presse.startPressing()) {
                    player.displayClientMessage(Component.translatable(
                            "block.lsd.perforation_started", presse.getExpectedTabs()
                    ), true);
                    player.playSound(net.minecraft.sounds.SoundEvents.PISTON_EXTEND, 0.5f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }

            // Status
            player.displayClientMessage(Component.literal("§d⚗ Perforations-Presse\n")
                    .append(Component.literal("§7Design: " + presse.getSelectedDesign().getColoredName() + " " + presse.getSelectedDesign().getSymbol() + "\n"))
                    .append(Component.translatable("block.lsd.perforation_paper_count", presse.getBlotterPapierCount()))
                    .append(Component.literal("\n§7Lösung: §f" + (presse.hasLoesung() ? "✓" : "✗") + "\n"))
                    .append(presse.isPressing() ? Component.literal("§7Fortschritt: §e" + (int)(presse.getProgress() * 100) + "%") : presse.hasLoesung() && presse.hasPapier() ? Component.translatable("block.lsd.perforation_ready") : Component.literal(""))
            , true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
