package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.market.IMarketAPI;
import de.rolandsw.schedulemc.market.DynamicMarketManager;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        de.rolandsw.schedulemc.market.MarketData data = marketManager.getMarketData(item);
        return data != null ? data.getBasePrice() : 0.0;
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
        de.rolandsw.schedulemc.market.MarketData data = marketManager.getMarketData(item);
        return data != null ? data.getPriceMultiplier() : 1.0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDemandLevel(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        de.rolandsw.schedulemc.market.MarketData data = marketManager.getMarketData(item);
        return data != null ? data.getDemand() : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSupplyLevel(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        de.rolandsw.schedulemc.market.MarketData data = marketManager.getMarketData(item);
        return data != null ? data.getSupply() : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Item, Double> getAllPrices() {
        Map<Item, Double> prices = new java.util.concurrent.ConcurrentHashMap<>();
        for (de.rolandsw.schedulemc.market.MarketData data : marketManager.getAllMarketData()) {
            prices.put(data.getItem(), data.getCurrentPrice());
        }
        return prices;
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
            // Reset all market data
            marketManager.reset();
        } else {
            de.rolandsw.schedulemc.market.MarketData data = marketManager.getMarketData(item);
            if (data != null) {
                double base = data.getBasePrice();
                marketManager.unregisterItem(item);
                marketManager.registerItem(item, base);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map.Entry<Item, Double>> getTopPricedItems(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be at least 1, got: " + limit);
        }
        return getAllPrices().entrySet().stream()
            .sorted(Map.Entry.<Item, Double>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map.Entry<Item, Integer>> getTopDemandItems(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be at least 1, got: " + limit);
        }
        List<Map.Entry<Item, Integer>> demandEntries = new ArrayList<>();
        for (de.rolandsw.schedulemc.market.MarketData data : marketManager.getAllMarketData()) {
            demandEntries.add(new java.util.AbstractMap.SimpleEntry<>(data.getItem(), data.getDemand()));
        }
        demandEntries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return demandEntries.subList(0, Math.min(limit, demandEntries.size()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMarketData(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        double price = marketManager.getCurrentPrice(item);
        return price > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTrackedItemCount() {
        return getAllPrices().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetAllMarketData() {
        resetMarketData(null);
    }
}
