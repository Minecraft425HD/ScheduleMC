package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.warehouse.IWarehouseAPI;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseManager;
import de.rolandsw.schedulemc.warehouse.WarehouseSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of IWarehouseAPI
 *
 * Wrapper für WarehouseBlockEntity mit vollständiger Thread-Safety.
 *
 * @author ScheduleMC Team
 * @version 3.2.0
 * @since 3.0.0
 */
public class WarehouseAPIImpl implements IWarehouseAPI {

    /**
     * Finds the WarehouseBlockEntity at the given position across all loaded levels.
     */
    @Nullable
    private WarehouseBlockEntity getWarehouse(BlockPos position) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        for (ServerLevel level : server.getAllLevels()) {
            if (level.isLoaded(position)) {
                BlockEntity be = level.getBlockEntity(position);
                if (be instanceof WarehouseBlockEntity) {
                    return (WarehouseBlockEntity) be;
                }
            }
        }
        return null;
    }

    /**
     * Finds the ServerLevel containing the given loaded position.
     */
    @Nullable
    private ServerLevel getLevelFor(BlockPos position) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        for (ServerLevel level : server.getAllLevels()) {
            if (level.isLoaded(position)) {
                return level;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasWarehouse(BlockPos position) {
        if (position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        return getWarehouse(position) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addItemToWarehouse(BlockPos position, Item item, int amount) {
        if (position == null || item == null) {
            throw new IllegalArgumentException("position and item cannot be null");
        }
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be at least 1, got: " + amount);
        }
        WarehouseBlockEntity warehouse = getWarehouse(position);
        return warehouse != null && warehouse.addItem(item, amount) > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeItemFromWarehouse(BlockPos position, Item item, int amount) {
        if (position == null || item == null) {
            throw new IllegalArgumentException("position and item cannot be null");
        }
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be at least 1, got: " + amount);
        }
        WarehouseBlockEntity warehouse = getWarehouse(position);
        return warehouse != null && warehouse.removeItem(item, amount) > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemStock(BlockPos position, Item item) {
        if (position == null || item == null) {
            throw new IllegalArgumentException("position and item cannot be null");
        }
        WarehouseBlockEntity warehouse = getWarehouse(position);
        if (warehouse == null) return 0;
        return warehouse.getStock(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCapacity(BlockPos position, Item item) {
        if (position == null || item == null) {
            throw new IllegalArgumentException("position and item cannot be null");
        }
        WarehouseBlockEntity warehouse = getWarehouse(position);
        if (warehouse == null) return 0;
        for (WarehouseSlot slot : warehouse.getSlots()) {
            if (slot.getAllowedItem() == item) {
                return slot.getMaxCapacity();
            }
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WarehouseSlot> getAllSlots(BlockPos position) {
        if (position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        WarehouseBlockEntity warehouse = getWarehouse(position);
        if (warehouse == null) return Collections.emptyList();
        return warehouse.getAllSlots();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSeller(BlockPos position, UUID sellerUUID) {
        if (position == null || sellerUUID == null) {
            throw new IllegalArgumentException("position and sellerUUID cannot be null");
        }
        WarehouseBlockEntity warehouse = getWarehouse(position);
        if (warehouse != null) {
            warehouse.addSeller(sellerUUID);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeSeller(BlockPos position, UUID sellerUUID) {
        if (position == null || sellerUUID == null) {
            throw new IllegalArgumentException("position and sellerUUID cannot be null");
        }
        WarehouseBlockEntity warehouse = getWarehouse(position);
        if (warehouse != null) {
            warehouse.removeSeller(sellerUUID);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSeller(BlockPos position, UUID sellerUUID) {
        if (position == null || sellerUUID == null) {
            throw new IllegalArgumentException("position and sellerUUID cannot be null");
        }
        WarehouseBlockEntity warehouse = getWarehouse(position);
        return warehouse != null && warehouse.getLinkedSellers().contains(sellerUUID);
    }

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<BlockPos> getAllWarehousePositions() {
        Set<BlockPos> allPositions = new HashSet<>();
        for (Map.Entry<String, Set<BlockPos>> entry : WarehouseManager.getAllWarehouses().entrySet()) {
            allPositions.addAll(entry.getValue());
        }
        return Collections.unmodifiableSet(allPositions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalItemCount(BlockPos position) {
        if (position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        WarehouseBlockEntity warehouse = getWarehouse(position);
        if (warehouse == null) return 0;
        return warehouse.getTotalItems();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getUsagePercentage(BlockPos position) {
        if (position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        WarehouseBlockEntity warehouse = getWarehouse(position);
        if (warehouse == null) return 0.0;
        return warehouse.getFillRate() * 100.0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<UUID> getAllSellers(BlockPos position) {
        if (position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        WarehouseBlockEntity warehouse = getWarehouse(position);
        if (warehouse == null) return Collections.emptySet();
        return new HashSet<>(warehouse.getLinkedSellers());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean linkToShop(BlockPos position, String shopPlotId) {
        if (position == null || shopPlotId == null) {
            throw new IllegalArgumentException("position and shopPlotId cannot be null");
        }
        WarehouseBlockEntity warehouse = getWarehouse(position);
        if (warehouse == null) return false;
        warehouse.setShopId(shopPlotId);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean triggerDelivery(BlockPos position) {
        if (position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        WarehouseBlockEntity warehouse = getWarehouse(position);
        if (warehouse == null) return false;
        ServerLevel level = getLevelFor(position);
        if (level == null) return false;
        warehouse.performManualDelivery(level);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean clearWarehouse(BlockPos position) {
        if (position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        WarehouseBlockEntity warehouse = getWarehouse(position);
        if (warehouse == null) return false;
        warehouse.clearAll();
        return true;
    }
}
