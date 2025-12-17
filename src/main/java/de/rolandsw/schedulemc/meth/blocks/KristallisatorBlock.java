package de.rolandsw.schedulemc.meth.blocks;

import de.rolandsw.schedulemc.meth.blockentity.KristallisatorBlockEntity;
import de.rolandsw.schedulemc.meth.blockentity.MethBlockEntities;
import de.rolandsw.schedulemc.meth.items.RohMethItem;
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
 * Kristallisator Block - Dritter Schritt der Meth-Herstellung
 * Wandelt Roh-Meth in Kristall-Meth (feucht) um
 */
public class KristallisatorBlock extends Block implements EntityBlock {

    public KristallisatorBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KristallisatorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof KristallisatorBlockEntity kristall) {
                kristall.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof KristallisatorBlockEntity kristall)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Roh-Meth hinzufügen
        if (heldItem.getItem() instanceof RohMethItem) {
            if (kristall.addRohMeth(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
                player.displayClientMessage(Component.literal(
                        "§a✓ Roh-Meth eingefüllt!\n" +
                        "§7Kristallisation läuft... (" + kristall.getActiveSlots() + "/4)"
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.GLASS_PLACE, 0.5f, 1.2f);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.literal(
                        "§c✗ Kristallisator ist voll!"
                ), true);
                return InteractionResult.FAIL;
            }
        }

        // Leere Hand
        if (heldItem.isEmpty()) {
            // Produkt entnehmen wenn fertig
            if (kristall.hasOutput()) {
                ItemStack output = kristall.extractAllOutput();
                if (!output.isEmpty()) {
                    if (!player.getInventory().add(output)) {
                        player.drop(output, false);
                    }
                    player.displayClientMessage(Component.literal(
                            "§a✓ " + output.getCount() + "x Kristall-Meth entnommen!"
                    ), true);
                    player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }

            // Status anzeigen
            StringBuilder status = new StringBuilder();
            status.append("§b❄ Kristallisator\n");

            if (kristall.isActive()) {
                int progress = (int) (kristall.getAverageProgress() * 100);
                status.append("§7Aktive Prozesse: §f").append(kristall.getActiveSlots()).append("/4\n");
                status.append("§7Fortschritt: §e").append(progress).append("%\n");
            }

            if (kristall.hasOutput()) {
                status.append("§a").append(kristall.getOutputCount()).append("x Kristall-Meth fertig!");
            } else if (!kristall.hasInput()) {
                status.append("§8Füge Roh-Meth hinzu um zu starten");
            }

            player.displayClientMessage(Component.literal(status.toString()), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
