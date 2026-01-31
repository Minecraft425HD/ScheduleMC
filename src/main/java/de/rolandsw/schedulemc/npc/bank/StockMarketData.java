package de.rolandsw.schedulemc.npc.bank;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Verwaltet Börsenpreise für handelbare Items
 * Thread-Safe mit ConcurrentHashMap
 */
public class StockMarketData {
    private static final Logger LOGGER = LogUtils.getLogger();
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile StockMarketData instance;

    private final Map<Item, StockPrice> prices = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File saveFile;

    private long currentDay = 0;
    private long lastPriceUpdate = 0;

    private StockMarketData(MinecraftServer server) {
        this.saveFile = new File(server.getServerDirectory(), "config/plotmod_stock_market.json");
        initializeDefaults();
        load();
    }

    /**
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static StockMarketData getInstance(MinecraftServer server) {
        StockMarketData localRef = instance;
        if (localRef == null) {
            synchronized (StockMarketData.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new StockMarketData(server);
                }
            }
        }
        return localRef;
    }

    /**
     * Initialisiert Standard-Preise
     */
    private void initializeDefaults() {
        prices.put(Items.GOLD_INGOT, new StockPrice(
            ModConfigHandler.COMMON.STOCK_GOLD_BASE_PRICE.get(),
            ModConfigHandler.COMMON.STOCK_GOLD_BASE_PRICE.get()
        ));

        prices.put(Items.DIAMOND, new StockPrice(
            ModConfigHandler.COMMON.STOCK_DIAMOND_BASE_PRICE.get(),
            ModConfigHandler.COMMON.STOCK_DIAMOND_BASE_PRICE.get()
        ));

        prices.put(Items.EMERALD, new StockPrice(
            ModConfigHandler.COMMON.STOCK_EMERALD_BASE_PRICE.get(),
            ModConfigHandler.COMMON.STOCK_EMERALD_BASE_PRICE.get()
        ));
    }

    /**
     * Gibt aktuellen Preis für ein Item zurück
     */
    public double getCurrentPrice(Item item) {
        StockPrice price = prices.get(item);
        return price != null ? price.currentPrice : 0.0;
    }

    /**
     * Gibt vorherigen Preis zurück (für Trend-Anzeige)
     */
    public double getPreviousPrice(Item item) {
        StockPrice price = prices.get(item);
        return price != null ? price.previousPrice : 0.0;
    }

    /**
     * Berechnet Preisänderung in Prozent
     */
    public double getPriceChangePercent(Item item) {
        StockPrice price = prices.get(item);
        if (price == null || price.previousPrice == 0) {
            return 0.0;
        }

        return ((price.currentPrice - price.previousPrice) / price.previousPrice) * 100.0;
    }

    /**
     * Gibt Trend zurück: 1 = steigend, 0 = gleichbleibend, -1 = fallend
     */
    public int getTrend(Item item) {
        double change = getPriceChangePercent(item);

        if (change > 0.5) return 1;  // Steigend
        if (change < -0.5) return -1; // Fallend
        return 0; // Gleichbleibend
    }

    /**
     * Gibt Preis-Historie für ein Item zurück (letzte 7 Tage)
     */
    public List<Double> getPriceHistory(Item item) {
        StockPrice price = prices.get(item);
        return price != null ? price.getHistory(7) : new ArrayList<>();
    }

    /**
     * Gibt Höchstpreis der letzten 7 Tage zurück
     */
    public double getHighPrice(Item item) {
        StockPrice price = prices.get(item);
        return price != null ? price.getHighPrice() : 0.0;
    }

    /**
     * Gibt Tiefstpreis der letzten 7 Tage zurück
     */
    public double getLowPrice(Item item) {
        StockPrice price = prices.get(item);
        return price != null ? price.getLowPrice() : 0.0;
    }

    /**
     * Gibt Durchschnittspreis der letzten 7 Tage zurück
     */
    public double getAveragePrice(Item item) {
        StockPrice price = prices.get(item);
        return price != null ? price.getAveragePrice() : 0.0;
    }

    /**
     * Tick-Methode für tägliche Preisänderungen
     */
    public void tick(long dayTime) {
        long day = dayTime / 24000L;

        if (day != currentDay) {
            currentDay = day;
            lastPriceUpdate = day;
            updatePrices();
            save();
        }
    }

    /**
     * Aktualisiert alle Preise mit zufälligen Schwankungen
     */
    private void updatePrices() {
        double maxChange = ModConfigHandler.COMMON.STOCK_MAX_PRICE_CHANGE_PERCENT.get();

        for (Map.Entry<Item, StockPrice> entry : prices.entrySet()) {
            StockPrice price = entry.getValue();

            // Speichere alten Preis
            price.previousPrice = price.currentPrice;

            // Berechne zufällige Änderung (-maxChange bis +maxChange)
            double changePercent = (ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0) * maxChange;
            double changeAmount = price.currentPrice * changePercent;

            // Neuer Preis
            price.currentPrice = Math.max(10.0, price.currentPrice + changeAmount);

            // Füge zur Historie hinzu
            price.addToHistory(price.currentPrice);

            LOGGER.debug("Stock price updated: {} = {}€ ({}{}%)",
                entry.getKey().toString(),
                String.format("%.2f", price.currentPrice),
                changePercent > 0 ? "+" : "",
                String.format("%.2f", changePercent * 100)
            );
        }

        LOGGER.info("Stock market prices updated for day {}", currentDay);
    }

    /**
     * Manuell Preise neu generieren (Admin-Befehl)
     */
    public void forceUpdate() {
        updatePrices();
        save();
    }

    /**
     * Reset auf Standardpreise
     */
    public void resetToDefaults() {
        initializeDefaults();
        save();
        LOGGER.info("Stock market prices reset to defaults");
    }

    // ========== Persistence ==========

    private void load() {
        if (!saveFile.exists()) {
            LOGGER.info("No stock market data found, using defaults");
            save(); // Speichere Defaults
            return;
        }

        try (FileReader reader = new FileReader(saveFile)) {
            Type type = new TypeToken<SaveData>(){}.getType();
            SaveData data = gson.fromJson(reader, type);

            if (data != null) {
                // Lade Preise aus SaveData
                if (data.goldPrice != null) {
                    // Initialisiere Historie falls nicht vorhanden (alte Saves)
                    if (data.goldPrice.priceHistory == null) {
                        data.goldPrice.priceHistory = new LinkedList<>();
                        data.goldPrice.priceHistory.add(data.goldPrice.currentPrice);
                    }
                    prices.put(Items.GOLD_INGOT, data.goldPrice);
                }
                if (data.diamondPrice != null) {
                    if (data.diamondPrice.priceHistory == null) {
                        data.diamondPrice.priceHistory = new LinkedList<>();
                        data.diamondPrice.priceHistory.add(data.diamondPrice.currentPrice);
                    }
                    prices.put(Items.DIAMOND, data.diamondPrice);
                }
                if (data.emeraldPrice != null) {
                    if (data.emeraldPrice.priceHistory == null) {
                        data.emeraldPrice.priceHistory = new LinkedList<>();
                        data.emeraldPrice.priceHistory.add(data.emeraldPrice.currentPrice);
                    }
                    prices.put(Items.EMERALD, data.emeraldPrice);
                }

                currentDay = data.lastUpdateDay;
                LOGGER.info("Loaded stock market data (day {})", currentDay);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load stock market data, using defaults", e);
            initializeDefaults();
        }
    }

    private void save() {
        try {
            saveFile.getParentFile().mkdirs();

            SaveData data = new SaveData();
            data.goldPrice = prices.get(Items.GOLD_INGOT);
            data.diamondPrice = prices.get(Items.DIAMOND);
            data.emeraldPrice = prices.get(Items.EMERALD);
            data.lastUpdateDay = currentDay;

            try (FileWriter writer = new FileWriter(saveFile)) {
                gson.toJson(data, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save stock market data", e);
        }
    }

    // ========== Data Classes ==========

    /**
     * Speichert Preis-Daten für ein Item
     */
    public static class StockPrice {
        public double currentPrice;
        public double previousPrice;
        public LinkedList<Double> priceHistory; // Letzte 7 Tage

        public StockPrice(double currentPrice, double previousPrice) {
            this.currentPrice = currentPrice;
            this.previousPrice = previousPrice;
            this.priceHistory = new LinkedList<>();
            this.priceHistory.add(currentPrice); // Initial price
        }

        /**
         * Fügt einen neuen Preis zur Historie hinzu (maximal 7 Einträge)
         */
        public void addToHistory(double price) {
            priceHistory.add(price);
            if (priceHistory.size() > 7) {
                priceHistory.removeFirst(); // Ältesten Eintrag entfernen
            }
        }

        /**
         * Gibt die letzten N Preise zurück (maximal 7)
         */
        public List<Double> getHistory(int days) {
            int size = Math.min(days, priceHistory.size());
            return new ArrayList<>(priceHistory.subList(
                Math.max(0, priceHistory.size() - size),
                priceHistory.size()
            ));
        }

        /**
         * Berechnet Höchstpreis der letzten 7 Tage
         */
        public double getHighPrice() {
            return priceHistory.stream().mapToDouble(Double::doubleValue).max().orElse(currentPrice);
        }

        /**
         * Berechnet Tiefstpreis der letzten 7 Tage
         */
        public double getLowPrice() {
            return priceHistory.stream().mapToDouble(Double::doubleValue).min().orElse(currentPrice);
        }

        /**
         * Berechnet Durchschnittspreis der letzten 7 Tage
         */
        public double getAveragePrice() {
            return priceHistory.stream().mapToDouble(Double::doubleValue).average().orElse(currentPrice);
        }
    }

    /**
     * Speicher-Struktur für JSON
     */
    private static class SaveData {
        StockPrice goldPrice;
        StockPrice diamondPrice;
        StockPrice emeraldPrice;
        long lastUpdateDay;
    }
}
