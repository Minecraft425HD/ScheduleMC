package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.market.IMarketAPI;
import de.rolandsw.schedulemc.market.DynamicMarketManager;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Implementation of IMarketAPI
 *
 * Wrapper für DynamicMarketManager mit vollständiger Thread-Safety.
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public class MarketAPIImpl implements IMarketAPI {

    private final DynamicMarketManager marketManager;

    public MarketAPIImpl() {
        this.marketManager = DynamicMarketManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCurrentPrice(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        return marketManager.getCurrentPrice(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getBasePrice(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        // Stub: Base price not directly exposed in DynamicMarketManager
        // Return current price as approximation
        return marketManager.getCurrentPrice(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordPurchase(Item item, int amount) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be at least 1, got: " + amount);
        }
        // Call the actual method in DynamicMarketManager
        marketManager.onItemBoughtFromNPC(item, amount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordSale(Item item, int amount) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be at least 1, got: " + amount);
        }
        // Call the actual method in DynamicMarketManager
        marketManager.onItemSoldToNPC(item, amount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPriceMultiplier(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        // Stub: Calculate multiplier from current price vs base price
        double currentPrice = marketManager.getCurrentPrice(item);
        double basePrice = currentPrice; // Approximation since base price not directly available
        return currentPrice > 0 ? currentPrice / Math.max(1.0, basePrice) : 1.0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDemandLevel(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        // Stub: Demand level not directly exposed in DynamicMarketManager
        // Return normalized value based on price multiplier
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSupplyLevel(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        // Stub: Supply level not directly exposed in DynamicMarketManager
        // Return normalized value based on price multiplier
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Item, Double> getAllPrices() {
        // Stub: getAllPrices not available in DynamicMarketManager
        // Would need to be implemented in DynamicMarketManager if required
        return new java.util.HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBasePrice(Item item, double basePrice) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        if (basePrice < 0) {
            throw new IllegalArgumentException("basePrice must be non-negative, got: " + basePrice);
        }
        // Stub: setBasePrice not available in DynamicMarketManager
        // Prices are managed dynamically based on supply/demand
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetMarketData(@Nullable Item item) {
        if (item == null) {
            // Reset all market data
            marketManager.reset();
        } else {
            // Stub: Per-item reset not available in DynamicMarketManager
            // Only full reset is supported via reset()
        }
    }
}
