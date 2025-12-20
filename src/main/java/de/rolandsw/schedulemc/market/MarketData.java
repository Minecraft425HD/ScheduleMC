package de.rolandsw.schedulemc.market;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;

/**
 * Market Data - Angebot & Nachfrage Daten für ein Item
 *
 * Features:
 * - Supply (Angebot): Wie viel wird verkauft
 * - Demand (Nachfrage): Wie viel wird gekauft
 * - Dynamic Pricing basierend auf S&D Ratio
 * - Trend Tracking (steigend/fallend)
 */
public class MarketData {

    private static final Logger LOGGER = LogUtils.getLogger();

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final Item item;
    private final double basePrice;

    // Supply & Demand
    private int supply;          // Wie viel wird angeboten
    private int demand;          // Wie viel wird nachgefragt
    private double currentPrice; // Aktueller Marktpreis

    // Historical Data (für Trends)
    private double previousPrice;
    private int previousSupply;
    private int previousDemand;

    // Config
    private final double supplyDemandFactor;  // Wie stark S&D Preis beeinflusst (0-1)
    private final double minPriceMultiplier;  // Min Preis (z.B. 0.5 = 50% des BasePrice)
    private final double maxPriceMultiplier;  // Max Preis (z.B. 3.0 = 300% des BasePrice)

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public MarketData(Item item, double basePrice, double supplyDemandFactor,
                     double minPriceMultiplier, double maxPriceMultiplier) {
        this.item = item;
        this.basePrice = basePrice;
        this.supplyDemandFactor = supplyDemandFactor;
        this.minPriceMultiplier = minPriceMultiplier;
        this.maxPriceMultiplier = maxPriceMultiplier;

        // Start mit ausgeglichenem Markt
        this.supply = 100;
        this.demand = 100;
        this.currentPrice = basePrice;
        this.previousPrice = basePrice;
        this.previousSupply = 100;
        this.previousDemand = 100;
    }

    public MarketData(Item item, double basePrice) {
        this(item, basePrice, 0.3, 0.5, 3.0);  // Default values
    }

    // ═══════════════════════════════════════════════════════════
    // SUPPLY & DEMAND UPDATES
    // ═══════════════════════════════════════════════════════════

    /**
     * Item wurde verkauft (erhöht Supply)
     */
    public void onItemSold(int amount) {
        supply += amount;
        updatePrice();
        LOGGER.debug("Item {} sold: {} units, new supply: {}", getItemName(), amount, supply);
    }

    /**
     * Item wurde gekauft (erhöht Demand)
     */
    public void onItemBought(int amount) {
        demand += amount;
        updatePrice();
        LOGGER.debug("Item {} bought: {} units, new demand: {}", getItemName(), amount, demand);
    }

    /**
     * Reduziert Supply (natürlicher Decay)
     */
    public void decaySupply(double rate) {
        int oldSupply = supply;
        supply = Math.max(1, (int) (supply * (1.0 - rate)));

        if (oldSupply != supply) {
            updatePrice();
            LOGGER.debug("Item {} supply decayed: {} → {}", getItemName(), oldSupply, supply);
        }
    }

    /**
     * Reduziert Demand (natürlicher Decay)
     */
    public void decayDemand(double rate) {
        int oldDemand = demand;
        demand = Math.max(1, (int) (demand * (1.0 - rate)));

        if (oldDemand != demand) {
            updatePrice();
            LOGGER.debug("Item {} demand decayed: {} → {}", getItemName(), oldDemand, demand);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PRICE CALCULATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Aktualisiert Preis basierend auf S&D Ratio
     *
     * Formel:
     * ratio = demand / supply
     * multiplier = ratio ^ supplyDemandFactor
     * price = basePrice × multiplier
     *
     * Beispiele (mit factor=0.3):
     * - Hohe Demand, niedrige Supply (10:1): ratio=10, multiplier=2.0x
     * - Ausgeglichen (1:1): ratio=1, multiplier=1.0x
     * - Hohe Supply, niedrige Demand (1:10): ratio=0.1, multiplier=0.5x
     */
    private void updatePrice() {
        // Speichere vorherigen Preis für Trend
        previousPrice = currentPrice;
        previousSupply = supply;
        previousDemand = demand;

        // Berechne S&D Ratio
        double ratio = (double) demand / Math.max(1, supply);

        // Berechne Preis-Multiplier
        double multiplier = Math.pow(ratio, supplyDemandFactor);

        // Clamp auf Min/Max
        multiplier = Math.max(minPriceMultiplier, Math.min(maxPriceMultiplier, multiplier));

        // Berechne finalen Preis
        currentPrice = basePrice * multiplier;

        LOGGER.debug("Price updated for {}: S/D={}/{} (ratio={:.2f}), price={:.2f} (×{:.2f})",
            getItemName(), supply, demand, ratio, currentPrice, multiplier);
    }

    // ═══════════════════════════════════════════════════════════
    // TREND ANALYSIS
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt Preis-Trend zurück
     */
    public PriceTrend getPriceTrend() {
        double diff = currentPrice - previousPrice;
        double threshold = basePrice * 0.01;  // 1% threshold

        if (diff > threshold) {
            return PriceTrend.RISING;
        } else if (diff < -threshold) {
            return PriceTrend.FALLING;
        } else {
            return PriceTrend.STABLE;
        }
    }

    /**
     * Price Trend
     */
    public enum PriceTrend {
        RISING("Steigend", "§a", "↗"),
        FALLING("Fallend", "§c", "↘"),
        STABLE("Stabil", "§7", "→");

        private final String displayName;
        private final String colorCode;
        private final String symbol;

        PriceTrend(String displayName, String colorCode, String symbol) {
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.symbol = symbol;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColorCode() {
            return colorCode;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getFormatted() {
            return colorCode + symbol + " " + displayName + "§r";
        }
    }

    /**
     * Gibt Preis-Änderung in Prozent zurück
     */
    public double getPriceChangePercent() {
        if (previousPrice == 0) return 0;
        return ((currentPrice - previousPrice) / previousPrice) * 100.0;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public Item getItem() {
        return item;
    }

    public String getItemName() {
        return item.toString();  // TODO: Better item name
    }

    public double getBasePrice() {
        return basePrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public int getSupply() {
        return supply;
    }

    public int getDemand() {
        return demand;
    }

    public double getSupplyDemandRatio() {
        return (double) demand / Math.max(1, supply);
    }

    public double getPriceMultiplier() {
        return currentPrice / basePrice;
    }

    // ═══════════════════════════════════════════════════════════
    // DISPLAY
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("MarketData{item=%s, price=%.2f (×%.2f), S=%d, D=%d, trend=%s}",
            getItemName(), currentPrice, getPriceMultiplier(), supply, demand, getPriceTrend());
    }

    /**
     * Formatierte Market-Info für UI
     */
    public String getMarketInfo() {
        PriceTrend trend = getPriceTrend();
        double changePercent = getPriceChangePercent();

        return String.format(
            "%s\n" +
            "§6Aktueller Preis: §f%.2f€ §7(Base: %.2f€)\n" +
            "§aAngebot: §f%d §7| §cNachfrage: §f%d\n" +
            "§7Trend: %s §7(%.1f%%)",
            getItemName(),
            currentPrice,
            basePrice,
            supply,
            demand,
            trend.getFormatted(),
            changePercent
        );
    }

    /**
     * Kompakte Anzeige für Liste
     */
    public String getCompactInfo() {
        PriceTrend trend = getPriceTrend();
        return String.format("%s - §6%.2f€ %s",
            getItemName(), currentPrice, trend.getSymbol());
    }
}
