package de.rolandsw.schedulemc.market;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamic Market Manager - Zentrale Verwaltung des dynamischen Marktes
 *
 * Features:
 * - Supply & Demand Tracking für alle Items
 * - Automatische Preis-Updates
 * - Periodischer Decay
 * - Statistiken & Trends
 */
public class DynamicMarketManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile DynamicMarketManager instance;

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    /**
     * Market Data: Item → MarketData
     */
    private final Map<Item, MarketData> marketData = new ConcurrentHashMap<>();

    /**
     * Config
     */
    private boolean enabled = false;
    private double supplyDemandFactor = 0.3;   // Wie stark S&D Preise beeinflusst
    private double minPriceMultiplier = 0.5;   // Min 50% des Base Price
    private double maxPriceMultiplier = 3.0;   // Max 300% des Base Price
    private double supplyDecayRate = 0.1;      // 10% Decay pro Update
    private double demandDecayRate = 0.1;      // 10% Decay pro Update
    private int updateInterval = 6000;         // Ticks (5 Minuten)

    private static final File MARKET_FILE = new File("config/plotmod_market.json");
    private static final Gson GSON = GsonHelper.get();
    private boolean dirty = false;

    // Statistics
    private long lastUpdateTime = 0;
    private long totalUpdates = 0;
    private int tickCounter = 0;

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private DynamicMarketManager() {
        LOGGER.info("DynamicMarketManager initialized");
    }

    public static DynamicMarketManager getInstance() {
        if (instance == null) {
            synchronized (DynamicMarketManager.class) {
                if (instance == null) {
                    instance = new DynamicMarketManager();
                }
            }
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // REGISTRATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert Item im dynamischen Markt
     */
    public void registerItem(Item item, double basePrice) {
        if (!marketData.containsKey(item)) {
            MarketData data = new MarketData(
                item,
                basePrice,
                supplyDemandFactor,
                minPriceMultiplier,
                maxPriceMultiplier
            );
            marketData.put(item, data);
            dirty = true;
            LOGGER.info("Registered item in market: {} (base price: {}€)", item, basePrice);
        }
    }

    /**
     * Entfernt Item aus Markt
     */
    public void unregisterItem(Item item) {
        if (marketData.remove(item) != null) {
            dirty = true;
            LOGGER.info("Unregistered item from market: {}", item);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MARKET ACTIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Item wurde an NPC verkauft (erhöht Supply)
     * Benachrichtigt auch den EconomyController für UDPS-Integration.
     */
    public void onItemSoldToNPC(Item item, int amount) {
        if (!enabled) return;

        MarketData data = marketData.get(item);
        if (data != null) {
            data.onItemSold(amount);
            dirty = true;
        }

        // UDPS Bridge: Auch EconomyController benachrichtigen
        try {
            de.rolandsw.schedulemc.economy.EconomyController ec =
                    de.rolandsw.schedulemc.economy.EconomyController.getInstance();
            // Versuche productId aus Registry-Name abzuleiten
            net.minecraft.resources.ResourceLocation itemId =
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
            if (itemId != null) {
                String productId = itemId.getPath().toUpperCase().replace("schedulemc:", "");
                MarketData ecMarketData = ec.getMarketData(productId);
                if (ecMarketData != null) {
                    ecMarketData.onItemSold(amount);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Could not bridge sale to EconomyController: {}", e.getMessage());
        }
    }

    /**
     * Item wurde von NPC gekauft (erhöht Demand)
     * Benachrichtigt auch den EconomyController für UDPS-Integration.
     */
    public void onItemBoughtFromNPC(Item item, int amount) {
        if (!enabled) return;

        MarketData data = marketData.get(item);
        if (data != null) {
            data.onItemBought(amount);
            dirty = true;
        }

        // UDPS Bridge: Auch EconomyController benachrichtigen
        try {
            de.rolandsw.schedulemc.economy.EconomyController ec =
                    de.rolandsw.schedulemc.economy.EconomyController.getInstance();
            net.minecraft.resources.ResourceLocation itemId =
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
            if (itemId != null) {
                String productId = itemId.getPath().toUpperCase().replace("schedulemc:", "");
                MarketData ecMarketData = ec.getMarketData(productId);
                if (ecMarketData != null) {
                    ecMarketData.onItemBought(amount);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Could not bridge purchase to EconomyController: {}", e.getMessage());
        }
    }

    /**
     * Holt aktuellen Marktpreis
     */
    public double getCurrentPrice(Item item) {
        MarketData data = marketData.get(item);
        return data != null ? data.getCurrentPrice() : 0.0;
    }

    /**
     * Holt MarketData für Item
     */
    @Nullable
    public MarketData getMarketData(Item item) {
        return marketData.get(item);
    }

    // ═══════════════════════════════════════════════════════════
    // UPDATE SYSTEM
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird jeden Tick aufgerufen (vom Server)
     */
    public void tick() {
        if (!enabled) return;

        tickCounter++;

        if (tickCounter >= updateInterval) {
            tickCounter = 0;
            performMarketUpdate();
        }
    }

    /**
     * Führt Market Update aus (Decay + Price Recalculation)
     */
    private void performMarketUpdate() {
        LOGGER.debug("Performing market update...");

        int itemsUpdated = 0;

        for (MarketData data : marketData.values()) {
            // Decay Supply & Demand
            data.decaySupply(supplyDecayRate);
            data.decayDemand(demandDecayRate);

            itemsUpdated++;
        }

        lastUpdateTime = System.currentTimeMillis();
        totalUpdates++;
        dirty = true;

        LOGGER.info("Market update completed: {} items updated", itemsUpdated);
    }

    /**
     * Manueller Update (Admin)
     */
    public void forceUpdate() {
        LOGGER.warn("Forcing market update...");
        performMarketUpdate();
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTICS & QUERIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt alle Items zurück
     */
    public Collection<MarketData> getAllMarketData() {
        return new ArrayList<>(marketData.values());
    }

    /**
     * Gibt Top N Items nach Preis zurück
     */
    public List<MarketData> getTopPricedItems(int limit) {
        List<MarketData> all = new ArrayList<>(marketData.values());
        all.sort((a, b) -> Double.compare(b.getCurrentPrice(), a.getCurrentPrice()));
        return all.subList(0, Math.min(limit, all.size()));
    }

    /**
     * Gibt Top N Items mit steigendem Trend zurück
     */
    public List<MarketData> getTrendingUpItems(int limit) {
        List<MarketData> rising = new ArrayList<>();
        for (MarketData data : marketData.values()) {
            if (data.getPriceTrend() == MarketData.PriceTrend.RISING) {
                rising.add(data);
            }
        }
        rising.sort((a, b) -> Double.compare(b.getPriceChangePercent(), a.getPriceChangePercent()));
        return rising.subList(0, Math.min(limit, rising.size()));
    }

    /**
     * Gibt Top N Items mit fallendem Trend zurück
     */
    public List<MarketData> getTrendingDownItems(int limit) {
        List<MarketData> falling = new ArrayList<>();
        for (MarketData data : marketData.values()) {
            if (data.getPriceTrend() == MarketData.PriceTrend.FALLING) {
                falling.add(data);
            }
        }
        falling.sort((a, b) -> Double.compare(a.getPriceChangePercent(), b.getPriceChangePercent()));
        return falling.subList(0, Math.min(limit, falling.size()));
    }

    /**
     * Market Statistics
     */
    public MarketStatistics getStatistics() {
        int totalItems = marketData.size();
        int risingCount = 0;
        int fallingCount = 0;
        int stableCount = 0;

        double avgPrice = 0;
        double avgMultiplier = 0;

        for (MarketData data : marketData.values()) {
            switch (data.getPriceTrend()) {
                case RISING -> risingCount++;
                case FALLING -> fallingCount++;
                case STABLE -> stableCount++;
            }

            avgPrice += data.getCurrentPrice();
            avgMultiplier += data.getPriceMultiplier();
        }

        if (totalItems > 0) {
            avgPrice /= totalItems;
            avgMultiplier /= totalItems;
        }

        return new MarketStatistics(
            totalItems,
            risingCount,
            fallingCount,
            stableCount,
            avgPrice,
            avgMultiplier,
            totalUpdates,
            lastUpdateTime
        );
    }

    /**
     * Market Statistics Record
     */
    public record MarketStatistics(
        int totalItems,
        int risingCount,
        int fallingCount,
        int stableCount,
        double averagePrice,
        double averageMultiplier,
        long totalUpdates,
        long lastUpdateTime
    ) {
        @Override
        public String toString() {
            return String.format(
                "MarketStats{items=%d, rising=%d, falling=%d, stable=%d, avgPrice=%.2f, avgMult=%.2fx, updates=%d}",
                totalItems, risingCount, fallingCount, stableCount, averagePrice, averageMultiplier, totalUpdates
            );
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CONFIGURATION
    // ═══════════════════════════════════════════════════════════

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        LOGGER.info("Dynamic market {}", enabled ? "enabled" : "disabled");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setSupplyDemandFactor(double factor) {
        this.supplyDemandFactor = Math.max(0.0, Math.min(1.0, factor));
        LOGGER.info("Supply/Demand factor set to {}", this.supplyDemandFactor);
    }

    public void setMinPriceMultiplier(double multiplier) {
        this.minPriceMultiplier = Math.max(0.1, multiplier);
        LOGGER.info("Min price multiplier set to {}", this.minPriceMultiplier);
    }

    public void setMaxPriceMultiplier(double multiplier) {
        this.maxPriceMultiplier = Math.max(1.0, multiplier);
        LOGGER.info("Max price multiplier set to {}", this.maxPriceMultiplier);
    }

    public void setDecayRates(double supplyRate, double demandRate) {
        this.supplyDecayRate = Math.max(0.0, Math.min(1.0, supplyRate));
        this.demandDecayRate = Math.max(0.0, Math.min(1.0, demandRate));
        LOGGER.info("Decay rates set to: supply={}, demand={}", this.supplyDecayRate, this.demandDecayRate);
    }

    public void setUpdateInterval(int ticks) {
        this.updateInterval = Math.max(100, ticks);
        LOGGER.info("Update interval set to {} ticks", this.updateInterval);
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════

    public void load() {
        if (!MARKET_FILE.exists()) {
            LOGGER.info("No market file found, starting fresh");
            return;
        }

        try (FileReader reader = new FileReader(MARKET_FILE)) {
            List<SerializedMarketData> loaded = GSON.fromJson(
                reader,
                new TypeToken<List<SerializedMarketData>>(){}.getType()
            );

            if (loaded == null) {
                LOGGER.warn("Loaded market data is null");
                return;
            }

            // Deserialize: Convert SerializedMarketData → MarketData
            int successCount = 0;
            int failCount = 0;

            for (SerializedMarketData serialized : loaded) {
                try {
                    // Convert String → Item using registry lookup
                    ResourceLocation itemId = ResourceLocation.parse(serialized.itemId);
                    Item item = BuiltInRegistries.ITEM.get(itemId);

                    // Verify item exists
                    if (item == null || item == net.minecraft.world.item.Items.AIR) {
                        LOGGER.warn("Could not deserialize item {} - not found in registry", serialized.itemId);
                        failCount++;
                        continue;
                    }

                    // Restore MarketData with saved state
                    MarketData data = new MarketData(
                        item,
                        serialized.basePrice,
                        supplyDemandFactor,        // Use current config
                        minPriceMultiplier,        // Use current config
                        maxPriceMultiplier,        // Use current config
                        serialized.supply,
                        serialized.demand,
                        serialized.currentPrice,
                        serialized.previousPrice,
                        serialized.previousSupply,
                        serialized.previousDemand
                    );

                    marketData.put(item, data);
                    successCount++;

                } catch (Exception e) {
                    LOGGER.error("Error deserializing market data for {}: {}", serialized.itemId, e.getMessage());
                    failCount++;
                }
            }

            LOGGER.info("Market data loaded: {} items restored, {} failed", successCount, failCount);

        } catch (IOException e) {
            LOGGER.error("Error loading market data", e);
        }
    }

    public void save() {
        if (!dirty) return;

        try {
            MARKET_FILE.getParentFile().mkdirs();

            // Serialize: Convert MarketData → SerializedMarketData
            List<SerializedMarketData> toSave = new ArrayList<>();

            for (Map.Entry<Item, MarketData> entry : marketData.entrySet()) {
                Item item = entry.getKey();
                MarketData data = entry.getValue();

                // Convert Item to ResourceLocation String (e.g. "minecraft:diamond")
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                if (itemId == null) {
                    LOGGER.warn("Could not serialize item {} - no registry ID found", item);
                    continue;
                }

                toSave.add(new SerializedMarketData(
                    itemId.toString(),
                    data.getBasePrice(),
                    data.getSupply(),
                    data.getDemand(),
                    data.getCurrentPrice(),
                    data.getPreviousPrice(),
                    data.getPreviousSupply(),
                    data.getPreviousDemand()
                ));
            }

            try (FileWriter writer = new FileWriter(MARKET_FILE)) {
                GSON.toJson(toSave, writer);
                writer.flush();
            }

            dirty = false;
            LOGGER.info("Market data saved: {} items", toSave.size());

        } catch (IOException e) {
            LOGGER.error("Error saving market data", e);
        }
    }

    public void saveIfNeeded() {
        if (dirty) {
            save();
        }
    }

    /**
     * Serializable Market Data (Item wird als ResourceLocation String gespeichert)
     */
    private static class SerializedMarketData {
        String itemId;              // z.B. "minecraft:diamond"
        double basePrice;
        int supply;
        int demand;
        double currentPrice;
        double previousPrice;
        int previousSupply;
        int previousDemand;

        // Default constructor für Gson
        SerializedMarketData() {}

        // Constructor mit allen Feldern
        SerializedMarketData(String itemId, double basePrice, int supply, int demand,
                           double currentPrice, double previousPrice, int previousSupply, int previousDemand) {
            this.itemId = itemId;
            this.basePrice = basePrice;
            this.supply = supply;
            this.demand = demand;
            this.currentPrice = currentPrice;
            this.previousPrice = previousPrice;
            this.previousSupply = previousSupply;
            this.previousDemand = previousDemand;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    public void reset() {
        marketData.clear();
        tickCounter = 0;
        totalUpdates = 0;
        dirty = true;
        LOGGER.warn("Market reset!");
    }

    public void printStatus() {
        LOGGER.info("═══ DynamicMarketManager Status ═══");
        LOGGER.info("Enabled: {}", enabled);
        LOGGER.info("Registered Items: {}", marketData.size());
        LOGGER.info("Statistics: {}", getStatistics());
        LOGGER.info("Next update in: {} ticks", updateInterval - tickCounter);
        LOGGER.info("═══════════════════════════════════════");
    }
}
