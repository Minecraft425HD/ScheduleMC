package de.rolandsw.schedulemc.warehouse;

import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.warehouse.items.WarehouseTool;
import de.rolandsw.schedulemc.warehouse.menu.WarehouseMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
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
 * Warehouse Block - Physischer Block für Warehouse-System
 *
 * Nur Admins können interagieren
 */
public class WarehouseBlock extends Block implements EntityBlock {

    public WarehouseBlock(Properties properties) {
        super(properties);
    }

    // === BLOCK ENTITY ===

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WarehouseBlockEntity(pos, state);
    }

    /**
     * Wird aufgerufen, wenn der Block platziert wird
     * Setzt automatisch die Shop-ID, wenn Warehouse in einem Shop-Plot platziert wird
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WarehouseBlockEntity warehouse) {
                // Initialize warehouse with current game time
                warehouse.initializeOnPlace(level);

                // Register with WarehouseManager for global delivery system
                WarehouseManager.registerWarehouse(serverLevel, pos);

                // Prüfe ob Warehouse in einem Plot liegt
                PlotRegion plot = PlotManager.getPlotAt(pos);
                if (plot != null && plot.getType().isShop()) {
                    // Setze Shop-ID automatisch auf Plot-ID
                    warehouse.setShopId(plot.getPlotId());

                    if (placer instanceof Player player) {
                        player.displayClientMessage(
                            Component.literal("§a✓ Warehouse mit Shop-Plot verknüpft: §e" + plot.getPlotId()),
                            true
                        );
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof WarehouseBlockEntity warehouse) {
                WarehouseBlockEntity.tick(level1, pos, state1, warehouse);
            }
        };
    }

    // === INTERACTION ===

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // Wenn Spieler Warehouse-Tool hält, lasse das Tool die Interaktion handhaben
        if (player.getItemInHand(hand).getItem() instanceof WarehouseTool) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Nur Admins können Warehouse öffnen
        if (player instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.hasPermissions(2)) {
                player.displayClientMessage(
                    Component.literal("§cNur Admins können Warehouses verwalten!"),
                    true
                );
                return InteractionResult.FAIL;
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WarehouseBlockEntity warehouse) {
                // Öffne GUI
                NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.literal("Warehouse Management");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
                        return new WarehouseMenu(id, playerInventory, warehouse, pos);
                    }
                }, pos);
            }
        }

        return InteractionResult.SUCCESS;
    }

    // === BLOCK REMOVAL ===

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WarehouseBlockEntity) {
                // Unregister from WarehouseManager
                if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                    WarehouseManager.unregisterWarehouse(serverLevel, pos);
                }
                // TODO: Drop items wenn gewünscht
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
