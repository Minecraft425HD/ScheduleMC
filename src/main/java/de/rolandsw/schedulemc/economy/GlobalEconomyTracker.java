package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.GsonHelper;
import de.rolandsw.schedulemc.util.IncrementalSaveManager;
import de.rolandsw.schedulemc.util.PersistenceHelper;
import org.slf4j.Logger;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verfolgt globale Wirtschaftsdaten über alle Spieler hinweg.
 *
 * Tracking:
 * - Gesamte Geldmenge im Umlauf
 * - Inflation/Deflation Rate
 * - Durchschnittliches Spieler-Vermögen
 * - Gesamte Transaktionsvolumen
 * - Verkaufte Mengen pro ItemCategory
 *
 * Diese Daten werden vom EconomyController genutzt um:
 * - Wirtschaftszyklen zu steuern
 * - Inflation zu erkennen und gegenzusteuern
 * - Preise global anzupassen
 */
public class GlobalEconomyTracker implements IncrementalSaveManager.ISaveable {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Singleton
    private static volatile GlobalEconomyTracker instance;

    // Persistenz
    private static volatile File file = new File("config/schedulemc_economy_tracker.json");
    private static final Gson gson = GsonHelper.get();
    private static volatile boolean needsSave = false;

    // ═══════════════════════════════════════════════════════════
    // TRACKING DATA
    // ═══════════════════════════════════════════════════════════

    /** Gesamte Geldmenge aller Spieler */
    private volatile double totalMoneySupply = 0.0;

    /** Geldmenge beim letzten Check (für Inflationsberechnung) */
    private volatile double previousMoneySupply = 0.0;

    /** Gesamtes Transaktionsvolumen seit Server-Start */
    private volatile double totalTransactionVolume = 0.0;

    /** Tägliches Transaktionsvolumen (wird täglich zurückgesetzt) */
    private volatile double dailyTransactionVolume = 0.0;

    /** Anzahl aktiver Spieler (mit Konten) */
    private volatile int activePlayerCount = 0;

    /** Verkaufte Mengen pro Kategorie (für S&D Tracking) */
    private final ConcurrentHashMap<ItemCategory, Long> categorySalesVolume = new ConcurrentHashMap<>();

    /** Gekaufte Mengen pro Kategorie */
    private final ConcurrentHashMap<ItemCategory, Long> categoryPurchaseVolume = new ConcurrentHashMap<>();

    /** Geld-Zufluss pro Kategorie (Einnahmen) */
    private final ConcurrentHashMap<ItemCategory, Double> categoryRevenue = new ConcurrentHashMap<>();

    /** Tägliche Einnahmen pro Spieler (für Anti-Exploit) */
    private final ConcurrentHashMap<UUID, Double> dailyPlayerEarnings = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════
    // INFLATIONS-TRACKING
    // ═══════════════════════════════════════════════════════════

    /** Aktuelle Inflationsrate (0.0 = stabil, >0 = Inflation, <0 = Deflation) */
    private volatile double inflationRate = 0.0;

    /** Inflations-Zielkorridor */
    private static final double TARGET_INFLATION_MIN = -0.02; // -2% Deflation
    private static final double TARGET_INFLATION_MAX = 0.05;  // 5% Inflation

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private GlobalEconomyTracker() {}

    public static GlobalEconomyTracker getInstance() {
        GlobalEconomyTracker localRef = instance;
        if (localRef == null) {
            synchronized (GlobalEconomyTracker.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new GlobalEconomyTracker();
                }
            }
        }
        return localRef;
    }

    // ═══════════════════════════════════════════════════════════
    // TRACKING METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert einen Verkauf (Spieler verkauft an NPC/Markt).
     *
     * @param playerUUID Spieler-UUID
     * @param category   Item-Kategorie
     * @param amount     Menge
     * @param revenue    Einnahmen
     */
    public void onSale(UUID playerUUID, ItemCategory category, int amount, double revenue) {
        categorySalesVolume.merge(category, (long) amount, Long::sum);
        categoryRevenue.merge(category, revenue, Double::sum);
        dailyPlayerEarnings.merge(playerUUID, revenue, Double::sum);
        dailyTransactionVolume += revenue;
        totalTransactionVolume += revenue;
        needsSave = true;

        LOGGER.debug("Sale tracked: player={}, cat={}, amt={}, rev={:.2f}",
                playerUUID, category.name(), amount, revenue);
    }

    /**
     * Registriert einen Kauf (Spieler kauft von NPC/Markt).
     *
     * @param category Item-Kategorie
     * @param amount   Menge
     * @param cost     Kosten
     */
    public void onPurchase(ItemCategory category, int amount, double cost) {
        categoryPurchaseVolume.merge(category, (long) amount, Long::sum);
        dailyTransactionVolume += cost;
        totalTransactionVolume += cost;
        needsSave = true;
    }

    /**
     * Aktualisiert die Geldmengen-Statistiken.
     * Sollte periodisch (alle 5 Minuten) aufgerufen werden.
     */
    public void updateMoneySupplyStats() {
        Map<UUID, Double> allAccounts = EconomyManager.getAllAccounts();

        previousMoneySupply = totalMoneySupply;
        totalMoneySupply = 0.0;
        activePlayerCount = allAccounts.size();

        for (double balance : allAccounts.values()) {
            totalMoneySupply += balance;
        }

        // Inflationsrate berechnen
        if (previousMoneySupply > 0) {
            inflationRate = (totalMoneySupply - previousMoneySupply) / previousMoneySupply;
        }

        needsSave = true;
        LOGGER.debug("Money supply updated: {:.2f}€ ({} players), inflation: {:.4f}",
                totalMoneySupply, activePlayerCount, inflationRate);
    }

    /**
     * Wird täglich aufgerufen um tägliche Zähler zurückzusetzen.
     */
    public void onNewDay() {
        dailyTransactionVolume = 0.0;
        dailyPlayerEarnings.clear();
        needsSave = true;
        LOGGER.info("Daily economy counters reset");
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public double getTotalMoneySupply() {
        return totalMoneySupply;
    }

    public double getInflationRate() {
        return inflationRate;
    }

    public double getAveragePlayerWealth() {
        return activePlayerCount > 0 ? totalMoneySupply / activePlayerCount : 0.0;
    }

    public double getDailyTransactionVolume() {
        return dailyTransactionVolume;
    }

    public double getTotalTransactionVolume() {
        return totalTransactionVolume;
    }

    public int getActivePlayerCount() {
        return activePlayerCount;
    }

    public long getCategorySalesVolume(ItemCategory category) {
        return categorySalesVolume.getOrDefault(category, 0L);
    }

    public long getCategoryPurchaseVolume(ItemCategory category) {
        return categoryPurchaseVolume.getOrDefault(category, 0L);
    }

    public double getCategoryRevenue(ItemCategory category) {
        return categoryRevenue.getOrDefault(category, 0.0);
    }

    public double getDailyPlayerEarnings(UUID playerUUID) {
        return dailyPlayerEarnings.getOrDefault(playerUUID, 0.0);
    }

    /**
     * @return true wenn Inflation über dem Zielkorridor liegt
     */
    public boolean isInflationHigh() {
        return inflationRate > TARGET_INFLATION_MAX;
    }

    /**
     * @return true wenn Deflation unter dem Zielkorridor liegt
     */
    public boolean isDeflationHigh() {
        return inflationRate < TARGET_INFLATION_MIN;
    }

    /**
     * Berechnet einen Inflations-Anpassungsmultiplikator.
     * Bei hoher Inflation werden Verkaufspreise leicht gesenkt.
     * Bei hoher Deflation werden sie leicht erhöht.
     *
     * @return Multiplikator (0.9-1.1)
     */
    public double getInflationAdjustment() {
        if (inflationRate > TARGET_INFLATION_MAX) {
            // Inflation: Preise leicht senken
            double excess = inflationRate - TARGET_INFLATION_MAX;
            return Math.max(0.9, 1.0 - excess * 0.5);
        } else if (inflationRate < TARGET_INFLATION_MIN) {
            // Deflation: Preise leicht erhöhen
            double deficit = TARGET_INFLATION_MIN - inflationRate;
            return Math.min(1.1, 1.0 + deficit * 0.5);
        }
        return 1.0;
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENZ
    // ═══════════════════════════════════════════════════════════

    private static final Type TRACKER_DATA_TYPE = new TypeToken<TrackerData>(){}.getType();

    public void loadData() {
        PersistenceHelper.LoadResult<TrackerData> result =
                PersistenceHelper.load(file, gson, TRACKER_DATA_TYPE, "GlobalEconomyTracker");

        if (result.isSuccess() && result.hasData()) {
            TrackerData data = result.getData();
            this.totalMoneySupply = data.totalMoneySupply;
            this.previousMoneySupply = data.previousMoneySupply;
            this.totalTransactionVolume = data.totalTransactionVolume;
            this.inflationRate = data.inflationRate;
            this.activePlayerCount = data.activePlayerCount;

            if (data.categorySales != null) {
                this.categorySalesVolume.clear();
                data.categorySales.forEach((k, v) -> {
                    try {
                        this.categorySalesVolume.put(ItemCategory.valueOf(k), v);
                    } catch (IllegalArgumentException ignored) {}
                });
            }
            if (data.categoryPurchases != null) {
                this.categoryPurchaseVolume.clear();
                data.categoryPurchases.forEach((k, v) -> {
                    try {
                        this.categoryPurchaseVolume.put(ItemCategory.valueOf(k), v);
                    } catch (IllegalArgumentException ignored) {}
                });
            }
            if (data.categoryRevenue != null) {
                this.categoryRevenue.clear();
                data.categoryRevenue.forEach((k, v) -> {
                    try {
                        this.categoryRevenue.put(ItemCategory.valueOf(k), v);
                    } catch (IllegalArgumentException ignored) {}
                });
            }

            LOGGER.info("GlobalEconomyTracker loaded: money={:.2f}, inflation={:.4f}", totalMoneySupply, inflationRate);
        }
    }

    public void saveData() {
        TrackerData data = new TrackerData();
        data.totalMoneySupply = this.totalMoneySupply;
        data.previousMoneySupply = this.previousMoneySupply;
        data.totalTransactionVolume = this.totalTransactionVolume;
        data.inflationRate = this.inflationRate;
        data.activePlayerCount = this.activePlayerCount;

        data.categorySales = new ConcurrentHashMap<>();
        this.categorySalesVolume.forEach((k, v) -> data.categorySales.put(k.name(), v));
        data.categoryPurchases = new ConcurrentHashMap<>();
        this.categoryPurchaseVolume.forEach((k, v) -> data.categoryPurchases.put(k.name(), v));
        data.categoryRevenue = new ConcurrentHashMap<>();
        this.categoryRevenue.forEach((k, v) -> data.categoryRevenue.put(k.name(), v));

        PersistenceHelper.SaveResult result = PersistenceHelper.save(file, gson, data, "GlobalEconomyTracker");
        if (result.isSuccess()) {
            needsSave = false;
        }
    }

    /**
     * Interne Datenstruktur für JSON-Persistenz
     */
    private static class TrackerData {
        double totalMoneySupply;
        double previousMoneySupply;
        double totalTransactionVolume;
        double inflationRate;
        int activePlayerCount;
        Map<String, Long> categorySales;
        Map<String, Long> categoryPurchases;
        Map<String, Double> categoryRevenue;
    }

    // ═══════════════════════════════════════════════════════════
    // INCREMENTAL SAVE MANAGER
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean isDirty() {
        return needsSave;
    }

    @Override
    public void save() {
        saveData();
    }

    @Override
    public String getName() {
        return "GlobalEconomyTracker";
    }

    @Override
    public int getPriority() {
        return 5; // Weniger kritisch als EconomyManager
    }
}
