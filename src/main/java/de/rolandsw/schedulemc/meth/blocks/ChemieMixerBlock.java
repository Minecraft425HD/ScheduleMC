package de.rolandsw.schedulemc.meth.blocks;

import de.rolandsw.schedulemc.meth.blockentity.ChemieMixerBlockEntity;
import de.rolandsw.schedulemc.meth.blockentity.MethBlockEntities;
import de.rolandsw.schedulemc.meth.items.EphedrinItem;
import de.rolandsw.schedulemc.meth.items.JodItem;
import de.rolandsw.schedulemc.meth.items.PseudoephedrinItem;
import de.rolandsw.schedulemc.meth.items.RoterPhosphorItem;
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

/**
 * Chemie-Mixer Block - Erster Schritt der Meth-Herstellung
 * Mischt Ephedrin + Roter Phosphor + Jod zu Meth-Paste
 */
public class ChemieMixerBlock extends Block implements EntityBlock {

    public ChemieMixerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChemieMixerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof ChemieMixerBlockEntity mixer) {
                mixer.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ChemieMixerBlockEntity mixer)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Ephedrin/Pseudoephedrin hinzufügen
        if (heldItem.getItem() instanceof EphedrinItem || heldItem.getItem() instanceof PseudoephedrinItem) {
            if (mixer.addEphedrin(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
                String type = heldItem.getItem() instanceof PseudoephedrinItem ? "Pseudoephedrin" : "Ephedrin";
                player.displayClientMessage(Component.literal(
                        "§a✓ " + type + " hinzugefügt!\n" +
                        "§7Status: §f" + mixer.getIngredientStatus()
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.BREWING_STAND_BREW, 0.5f, 1.2f);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.literal(
                        "§c✗ Mixer ist voll oder kein Slot verfügbar!"
                ), true);
                return InteractionResult.FAIL;
            }
        }

        // Roter Phosphor hinzufügen
        if (heldItem.getItem() instanceof RoterPhosphorItem) {
            if (mixer.addPhosphor(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
                player.displayClientMessage(Component.literal(
                        "§a✓ Roter Phosphor hinzugefügt!\n" +
                        "§7Status: §f" + mixer.getIngredientStatus()
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.BREWING_STAND_BREW, 0.5f, 1.0f);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.literal(
                        "§c✗ Kein Slot mit Ephedrin verfügbar!"
                ), true);
                return InteractionResult.FAIL;
            }
        }

        // Jod hinzufügen
        if (heldItem.getItem() instanceof JodItem) {
            if (mixer.addJod(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
                player.displayClientMessage(Component.literal(
                        "§a✓ Jod hinzugefügt - Mischvorgang gestartet!\n" +
                        "§7Status: §f" + mixer.getIngredientStatus()
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.BREWING_STAND_BREW, 0.5f, 0.8f);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.literal(
                        "§c✗ Kein Slot mit Phosphor verfügbar!"
                ), true);
                return InteractionResult.FAIL;
            }
        }

        // Leere Hand - Produkt entnehmen oder Status anzeigen
        if (heldItem.isEmpty()) {
            if (mixer.hasOutput()) {
                ItemStack output = mixer.extractAllOutput();
                if (!output.isEmpty()) {
                    if (!player.getInventory().add(output)) {
                        player.drop(output, false);
                    }
                    player.displayClientMessage(Component.literal(
                            "§a✓ " + output.getCount() + "x Meth-Paste entnommen!"
                    ), true);
                    player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }

            // Status anzeigen
            StringBuilder status = new StringBuilder();
            status.append("§9⚗ Chemie-Mixer\n");
            status.append("§7Zutaten: §f").append(mixer.getIngredientStatus()).append("\n");

            if (mixer.isActive()) {
                int progress = (int)(mixer.getAverageProgress() * 100);
                status.append("§7Fortschritt: §e").append(progress).append("%\n");
            }

            if (mixer.hasOutput()) {
                status.append("§a").append(mixer.getOutputCount()).append("x Meth-Paste fertig!");
            } else if (!mixer.hasIngredients()) {
                status.append("§8Füge Ephedrin, Phosphor und Jod hinzu");
            }

            player.displayClientMessage(Component.literal(status.toString()), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
