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
        return marketManager.getBasePrice(item);
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
        marketManager.recordPurchase(item, amount);
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
        marketManager.recordSale(item, amount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPriceMultiplier(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        return marketManager.getPriceMultiplier(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDemandLevel(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        return marketManager.getDemandLevel(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSupplyLevel(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        return marketManager.getSupplyLevel(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Item, Double> getAllPrices() {
        return marketManager.getAllPrices();
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
        marketManager.setBasePrice(item, basePrice);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetMarketData(@Nullable Item item) {
        if (item == null) {
            marketManager.resetAllMarketData();
        } else {
            marketManager.resetMarketData(item);
        }
    }
}
