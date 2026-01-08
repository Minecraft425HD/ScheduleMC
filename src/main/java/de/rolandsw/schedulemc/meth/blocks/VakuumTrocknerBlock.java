package de.rolandsw.schedulemc.meth.blocks;

import de.rolandsw.schedulemc.meth.MethQuality;
import de.rolandsw.schedulemc.meth.blockentity.MethBlockEntities;
import de.rolandsw.schedulemc.meth.blockentity.VakuumTrocknerBlockEntity;
import de.rolandsw.schedulemc.meth.items.KristallMethItem;
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
 * Vakuum-Trockner Block - Vierter und letzter Schritt der Meth-Herstellung
 * Trocknet Kristall-Meth (feucht) zu fertigem Meth
 */
public class VakuumTrocknerBlock extends Block implements EntityBlock {

    public VakuumTrocknerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VakuumTrocknerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof VakuumTrocknerBlockEntity trockner) {
                trockner.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof VakuumTrocknerBlockEntity trockner)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Kristall-Meth hinzufügen
        if (heldItem.getItem() instanceof KristallMethItem) {
            if (trockner.addKristallMeth(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
                player.displayClientMessage(Component.literal(
                        "§a✓ Kristall-Meth eingefüllt!\n" +
                        "§7Trocknung läuft... (" + trockner.getActiveSlots() + "/6)"
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.IRON_DOOR_CLOSE, 0.5f, 1.5f);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.literal(
                        "§c✗ Vakuum-Trockner ist voll!"
                ), true);
                return InteractionResult.FAIL;
            }
        }

        // Leere Hand
        if (heldItem.isEmpty()) {
            // Produkt entnehmen wenn fertig
            if (trockner.hasOutput()) {
                ItemStack output = trockner.extractAllOutput();
                if (!output.isEmpty()) {
                    if (!player.getInventory().add(output)) {
                        player.drop(output, false);
                    }

                    // Spezielle Nachricht für Blue Sky
                    MethQuality quality = de.rolandsw.schedulemc.meth.items.MethItem.getQuality(output);
                    String message = quality == MethQuality.BLUE_SKY ?
                            "§b✓ " + output.getCount() + "x Blue Sky entnommen!\n§7\"I am the one who knocks.\"" :
                            "§a✓ " + output.getCount() + "x Crystal Meth entnommen!";

                    player.displayClientMessage(Component.literal(message), true);
                    player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }

            // Status anzeigen
            StringBuilder status = new StringBuilder();
            status.append("§7⬛ Vakuum-Trockner\n");

            if (trockner.isActive()) {
                int progress = (int) (trockner.getAverageProgress() * 100);
                status.append("§7Aktive Prozesse: §f").append(trockner.getActiveSlots()).append("/6\n");
                status.append("§7Fortschritt: §e").append(progress).append("%\n");

                MethQuality best = trockner.getBestQuality();
                String qualityInfo = switch (best) {
                    case STANDARD -> "§fStandard";
                    case GUT -> "§ePremium";
                    case BLUE_SKY -> "§b§lBlue Sky";
                };
                status.append("§7Beste Qualität: ").append(qualityInfo);
            }

            if (trockner.hasOutput()) {
                status.append("§a").append(trockner.getOutputCount()).append("x Crystal Meth fertig!");
            } else if (!trockner.hasInput()) {
                status.append("§8Füge Kristall-Meth hinzu um zu starten");
            }

            player.displayClientMessage(Component.literal(status.toString()), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
