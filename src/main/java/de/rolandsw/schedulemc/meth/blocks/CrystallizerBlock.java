package de.rolandsw.schedulemc.meth.blocks;

import de.rolandsw.schedulemc.meth.blockentity.CrystallizerBlockEntity;
import de.rolandsw.schedulemc.meth.blockentity.MethBlockEntities;
import de.rolandsw.schedulemc.meth.items.RawMethItem;
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
 * Crystallizer Block - Dritter Schritt der Meth-Herstellung
 * Wandelt Roh-Meth in Kristall-Meth (feucht) um
 */
public class CrystallizerBlock extends Block implements EntityBlock {

    public CrystallizerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystallizerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof CrystallizerBlockEntity crystal) {
                crystal.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CrystallizerBlockEntity crystal)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Roh-Meth hinzufügen
        if (heldItem.getItem() instanceof RawMethItem) {
            if (crystal.addRawMeth(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
                player.displayClientMessage(Component.translatable(
                        "block.meth.crystallizer_input", crystal.getActiveSlots()
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.GLASS_PLACE, 0.5f, 1.2f);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.translatable(
                        "block.meth.crystallizer_full"
                ), true);
                return InteractionResult.FAIL;
            }
        }

        // Leere Hand
        if (heldItem.isEmpty()) {
            // Produkt entnehmen wenn fertig
            if (crystal.hasOutput()) {
                ItemStack output = crystal.extractAllOutput();
                if (!output.isEmpty()) {
                    if (!player.getInventory().add(output)) {
                        player.drop(output, false);
                    }
                    player.displayClientMessage(Component.translatable(
                            "block.meth.crystallizer_output", output.getCount()
                    ), true);
                    player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }

            // Status anzeigen
            StringBuilder status = new StringBuilder();
            status.append(Component.translatable("block.meth.crystallizer_title").getString()).append('\n');

            if (crystal.isActive()) {
                int progress = (int) (crystal.getAverageProgress() * 100);
                status.append(Component.translatable("block.meth.crystallizer_active", crystal.getActiveSlots()).getString()).append('\n');
                status.append(Component.translatable("block.meth.crystallizer_progress", progress).getString()).append('\n');
            }

            if (crystal.hasOutput()) {
                status.append(Component.translatable("block.meth.crystallizer_ready", crystal.getOutputCount()).getString());
            } else if (!crystal.hasInput()) {
                status.append(Component.translatable("block.meth.crystallizer_hint").getString());
            }

            player.displayClientMessage(Component.literal(status.toString()), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
