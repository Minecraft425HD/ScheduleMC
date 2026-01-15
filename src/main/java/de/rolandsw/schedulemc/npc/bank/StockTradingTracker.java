package de.rolandsw.schedulemc.npc.bank;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.achievement.AchievementManager;
import de.rolandsw.schedulemc.achievement.AchievementTracker;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trackt Stock Trading für Achievements
 * Speichert Käufe mit Preisen und berechnet Gewinn/Verlust bei Verkäufen
 */
public class StockTradingTracker extends AbstractPersistenceManager<Map<UUID, StockTradingTracker.PlayerTradingData>> {
    private static volatile StockTradingTracker instance;

    private final Map<UUID, PlayerTradingData> playerData = new ConcurrentHashMap<>();
    private MinecraftServer server;

    private StockTradingTracker(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("plotmod_stock_trading.json").toFile(),
            GsonHelper.get()
        );
        this.server = server;
        load();
    }

    /**
     * Thread-safe Singleton
     */
    public static StockTradingTracker getInstance(MinecraftServer server) {
        StockTradingTracker localRef = instance;
        if (localRef == null) {
            synchronized (StockTradingTracker.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new StockTradingTracker(server);
                }
            }
        }
        localRef.server = server;
        return localRef;
    }

    @Nullable
    public static StockTradingTracker getInstance() {
        return instance;
    }

    /**
     * Registriert einen Kauf
     */
    public void recordPurchase(UUID playerUUID, Item item, int quantity, double pricePerUnit) {
        PlayerTradingData data = playerData.computeIfAbsent(playerUUID, PlayerTradingData::new);
        data.addPurchase(item, quantity, pricePerUnit);

        // Achievement: Erster Trade
        AchievementManager achievementManager = AchievementManager.getInstance(server);
        if (achievementManager != null) {
            achievementManager.addProgress(playerUUID, "FIRST_TRADE", 1.0);
            achievementManager.addProgress(playerUUID, "ACTIVE_TRADER", 1.0);
            achievementManager.addProgress(playerUUID, "DAY_TRADER", 1.0);
            achievementManager.addProgress(playerUUID, "WOLF_OF_MINECRAFT", 1.0);
        }

        save();
    }

    /**
     * Registriert einen Verkauf und berechnet Gewinn/Verlust
     *
     * WICHTIG - Achievement Integration:
     * - Trading-Gewinne triggern BIG_SPENDER (Gesamt-Verdienst)
     * - FIRST_EURO, RICH, WEALTHY, MILLIONAIRE werden automatisch getriggert,
     *   da der Gewinn via EconomyManager.deposit() ins Geld eingezahlt wird
     *   und AchievementTracker alle 60s den Balance-Check macht
     *
     * @return Gewinn (positiv) oder Verlust (negativ)
     */
    public double recordSale(UUID playerUUID, Item item, int quantity, double pricePerUnit) {
        PlayerTradingData data = playerData.computeIfAbsent(playerUUID, PlayerTradingData::new);

        // Berechne durchschnittlichen Kaufpreis
        double avgBuyPrice = data.getAveragePurchasePrice(item);

        // Berechne Gewinn/Verlust
        double profitPerUnit = pricePerUnit - avgBuyPrice;
        double totalProfit = profitPerUnit * quantity;

        // Entferne verkaufte Items aus Holdings (FIFO)
        data.removeSold(item, quantity);

        // Tracke Gewinn/Verlust
        if (totalProfit > 0) {
            data.totalProfit += totalProfit;
            data.profitableTrades++;
        } else if (totalProfit < 0) {
            data.totalLoss += Math.abs(totalProfit);
            data.losingTrades++;
        }

        data.totalTrades++;

        // Achievement Triggers
        AchievementManager achievementManager = AchievementManager.getInstance(server);
        if (achievementManager != null) {
            // Trade Achievements
            achievementManager.addProgress(playerUUID, "FIRST_TRADE", 1.0);
            achievementManager.addProgress(playerUUID, "ACTIVE_TRADER", 1.0);
            achievementManager.addProgress(playerUUID, "DAY_TRADER", 1.0);
            achievementManager.addProgress(playerUUID, "WOLF_OF_MINECRAFT", 1.0);

            // Gewinn Achievements
            if (totalProfit > 0) {
                achievementManager.addProgress(playerUUID, "FIRST_PROFIT", 1.0);
                achievementManager.setProgress(playerUUID, "PROFIT_100", data.totalProfit);
                achievementManager.setProgress(playerUUID, "PROFIT_1K", data.totalProfit);
                achievementManager.setProgress(playerUUID, "PROFIT_10K", data.totalProfit);
                achievementManager.setProgress(playerUUID, "PROFIT_MASTER", data.totalProfit);

                // WICHTIG: Trading-Gewinne zählen auch als "Geld verdient"
                // Dies triggert BIG_SPENDER Achievement (Gesamt-Verdienst)
                AchievementTracker.trackMoneyEarned(playerUUID, totalProfit);
            }

            // Verlust Achievement (als Fun Achievement)
            if (totalProfit < 0) {
                achievementManager.addProgress(playerUUID, "FIRST_LOSS", 1.0);
                achievementManager.setProgress(playerUUID, "BIG_LOSER", data.totalLoss);
            }

            // Erfolgsrate Achievements
            if (data.totalTrades >= 10) {
                double successRate = (double) data.profitableTrades / data.totalTrades * 100.0;
                if (successRate >= 80.0) {
                    achievementManager.addProgress(playerUUID, "TRADING_GENIUS", 1.0);
                }
            }
        }

        save();
        return totalProfit;
    }

    /**
     * Gibt Trading-Statistiken für einen Spieler zurück
     */
    public PlayerTradingData getPlayerData(UUID playerUUID) {
        return playerData.computeIfAbsent(playerUUID, PlayerTradingData::new);
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<UUID, PlayerTradingData>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<UUID, PlayerTradingData> data) {
        playerData.clear();
        playerData.putAll(data);
    }

    @Override
    protected Map<UUID, PlayerTradingData> getCurrentData() {
        return new HashMap<>(playerData);
    }

    @Override
    protected String getComponentName() {
        return "StockTradingTracker";
    }

    @Override
    protected String getHealthDetails() {
        return playerData.size() + " traders";
    }

    @Override
    protected void onCriticalLoadFailure() {
        playerData.clear();
    }

    // ═══════════════════════════════════════════════════════════
    // DATA CLASSES
    // ═══════════════════════════════════════════════════════════

    /**
     * Speichert Trading-Daten eines Spielers
     */
    public static class PlayerTradingData {
        @SerializedName("playerUUID")
        private final UUID playerUUID;

        @SerializedName("holdings")
        private final Map<String, List<Purchase>> holdings = new HashMap<>();

        @SerializedName("totalProfit")
        private double totalProfit = 0.0;

        @SerializedName("totalLoss")
        private double totalLoss = 0.0;

        @SerializedName("totalTrades")
        private int totalTrades = 0;

        @SerializedName("profitableTrades")
        private int profitableTrades = 0;

        @SerializedName("losingTrades")
        private int losingTrades = 0;

        public PlayerTradingData(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }

        /**
         * Fügt einen Kauf hinzu
         */
        public void addPurchase(Item item, int quantity, double pricePerUnit) {
            String itemKey = getItemKey(item);
            List<Purchase> purchases = holdings.computeIfAbsent(itemKey, k -> new ArrayList<>());
            purchases.add(new Purchase(quantity, pricePerUnit, System.currentTimeMillis()));
        }

        /**
         * Entfernt verkaufte Items (FIFO - First In, First Out)
         */
        public void removeSold(Item item, int quantity) {
            String itemKey = getItemKey(item);
            List<Purchase> purchases = holdings.get(itemKey);
            if (purchases == null) return;

            int remaining = quantity;
            Iterator<Purchase> iterator = purchases.iterator();
            while (iterator.hasNext() && remaining > 0) {
                Purchase purchase = iterator.next();
                if (purchase.quantity <= remaining) {
                    remaining -= purchase.quantity;
                    iterator.remove();
                } else {
                    purchase.quantity -= remaining;
                    remaining = 0;
                }
            }
        }

        /**
         * Berechnet durchschnittlichen Kaufpreis für ein Item
         */
        public double getAveragePurchasePrice(Item item) {
            String itemKey = getItemKey(item);
            List<Purchase> purchases = holdings.get(itemKey);
            if (purchases == null || purchases.isEmpty()) {
                return 0.0; // Keine Käufe vorhanden (sollte nicht passieren)
            }

            double totalCost = 0.0;
            int totalQuantity = 0;

            for (Purchase purchase : purchases) {
                totalCost += purchase.quantity * purchase.pricePerUnit;
                totalQuantity += purchase.quantity;
            }

            return totalQuantity > 0 ? totalCost / totalQuantity : 0.0;
        }

        private String getItemKey(Item item) {
            if (item == Items.GOLD_INGOT) return "gold";
            if (item == Items.DIAMOND) return "diamond";
            if (item == Items.EMERALD) return "emerald";
            return "unknown";
        }

        // Getters
        public double getTotalProfit() { return totalProfit; }
        public double getTotalLoss() { return totalLoss; }
        public int getTotalTrades() { return totalTrades; }
        public int getProfitableTrades() { return profitableTrades; }
        public int getLosingTrades() { return losingTrades; }

        public double getSuccessRate() {
            return totalTrades > 0 ? (double) profitableTrades / totalTrades * 100.0 : 0.0;
        }

        public double getNetProfit() {
            return totalProfit - totalLoss;
        }
    }

    /**
     * Repräsentiert einen einzelnen Kauf
     */
    public static class Purchase {
        @SerializedName("quantity")
        private int quantity;

        @SerializedName("pricePerUnit")
        private final double pricePerUnit;

        @SerializedName("timestamp")
        private final long timestamp;

        public Purchase(int quantity, double pricePerUnit, long timestamp) {
            this.quantity = quantity;
            this.pricePerUnit = pricePerUnit;
            this.timestamp = timestamp;
        }
    }
}
