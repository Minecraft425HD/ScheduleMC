package de.rolandsw.schedulemc.meth.blocks;

import de.rolandsw.schedulemc.meth.blockentity.MethBlockEntities;
import de.rolandsw.schedulemc.meth.blockentity.ReduktionskesselBlockEntity;
import de.rolandsw.schedulemc.meth.items.MethPasteItem;
import de.rolandsw.schedulemc.meth.menu.ReduktionskesselMenu;
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

/**
 * Reduktionskessel Block - Zweiter Schritt der Meth-Herstellung
 * ACHTUNG: Temperaturkontrolle mit Explosionsgefahr!
 */
public class ReduktionskesselBlock extends Block implements EntityBlock {

    public ReduktionskesselBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReduktionskesselBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof ReduktionskesselBlockEntity kessel) {
                kessel.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ReduktionskesselBlockEntity kessel)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Meth-Paste hinzufügen
        if (heldItem.getItem() instanceof MethPasteItem) {
            if (kessel.addMethPaste(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
                player.displayClientMessage(Component.literal(
                        "§a✓ Meth-Paste eingefüllt!\n" +
                        "§c⚠ ACHTUNG: Temperaturkontrolle erforderlich!\n" +
                        "§7Leere Hand → GUI öffnen"
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.BUCKET_FILL_LAVA, 0.5f, 1.0f);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.literal(
                        "§c✗ Kessel bereits gefüllt oder Produkt noch nicht entnommen!"
                ), true);
                return InteractionResult.FAIL;
            }
        }

        // Leere Hand
        if (heldItem.isEmpty()) {
            // Produkt entnehmen wenn fertig
            if (kessel.hasOutput()) {
                ItemStack output = kessel.extractOutput();
                if (!output.isEmpty()) {
                    if (!player.getInventory().add(output)) {
                        player.drop(output, false);
                    }
                    player.displayClientMessage(Component.literal(
                            "§a✓ Roh-Meth entnommen!"
                    ), true);
                    player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }

            // GUI öffnen wenn Input vorhanden
            if (kessel.hasInput()) {
                openGui(player, kessel, pos);
                return InteractionResult.SUCCESS;
            }

            // Status anzeigen
            player.displayClientMessage(Component.literal(
                    "§6⚗ Reduktionskessel\n" +
                    "§7Temperatur: §f" + kessel.getTemperatureInt() + "°C " + kessel.getTemperatureZone() + "\n" +
                    "§8Füge Meth-Paste hinzu um zu starten"
            ), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void openGui(Player player, ReduktionskesselBlockEntity kessel, BlockPos pos) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        kessel.setActivePlayer(player.getUUID());

        NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Reduktionskessel");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new ReduktionskesselMenu(containerId, playerInventory, kessel);
            }
        }, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ReduktionskesselBlockEntity kessel) {
                kessel.clearActivePlayer();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
