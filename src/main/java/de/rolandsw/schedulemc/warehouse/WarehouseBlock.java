package de.rolandsw.schedulemc.warehouse;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Nur Admins können Warehouse öffnen
        if (player instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.hasPermissions(2)) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§cNur Admins können Warehouses verwalten!"),
                    true
                );
                return InteractionResult.FAIL;
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WarehouseBlockEntity warehouse) {
                // TODO: Öffne GUI
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§eWarehouse GUI (TODO)"),
                    false
                );

                // Zeige Info
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                        "§e§l=== Warehouse Info ===\n" +
                        "§7Slots belegt: §e" + warehouse.getUsedSlots() + " §7/ §e" + warehouse.getSlots().length + "\n" +
                        "§7Total Items: §e" + warehouse.getTotalItems() + "\n" +
                        "§7Shop-ID: §e" + (warehouse.getShopId() != null ? warehouse.getShopId() : "Nicht verknüpft") + "\n" +
                        "§7Verkäufer: §e" + warehouse.getLinkedSellers().size()
                    )
                );
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
                // TODO: Drop items wenn gewünscht
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
