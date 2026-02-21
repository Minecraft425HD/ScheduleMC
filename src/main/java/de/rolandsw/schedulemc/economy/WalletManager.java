package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Geldbörsen-Guthaben per UUID
 * Überlebt Serverneustarts und Spielertod!
 *
 * SICHERHEIT: Verwendet ConcurrentHashMap für Thread-Sicherheit
 * Nutzt AbstractPersistenceManager für robuste Datenpersistenz
 */
public class WalletManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File WALLET_FILE = new File("config/plotmod_wallets.json");

    // SICHERHEIT: ConcurrentHashMap statt HashMap für Thread-Sicherheit
    // UUID -> Geldbörsen-Guthaben
    private static final Map<UUID, Double> wallets = new ConcurrentHashMap<>();

    // Persistence-Manager (eliminiert ~150 Zeilen Duplikation)
    private static final WalletPersistenceManager persistence =
        new WalletPersistenceManager(WALLET_FILE, GSON);

    /**
     * Lädt Geldbörsen vom Disk
     */
    public static void load() {
        persistence.load();
    }

    /**
     * Speichert Geldbörsen auf Disk
     */
    public static void save() {
        persistence.save();
    }

    /**
     * Speichert nur wenn Änderungen vorhanden
     */
    public static void saveIfNeeded() {
        persistence.saveIfNeeded();
    }

    /**
     * Gibt Geldbörsen-Guthaben zurück
     */
    public static double getBalance(UUID playerUUID) {
        return wallets.getOrDefault(playerUUID, 0.0);
    }

    /**
     * Setzt Geldbörsen-Guthaben
     */
    public static void setBalance(UUID playerUUID, double amount) {
        wallets.put(playerUUID, Math.max(0, amount));
        persistence.markDirty();
    }

    /**
     * Fügt Geld hinzu
     * SICHERHEIT: Atomare Operation für Thread-Sicherheit
     */
    public static void addMoney(UUID playerUUID, double amount) {
        if (!Double.isFinite(amount) || amount < 0) {
            return;
        }
        wallets.compute(playerUUID, (key, current) -> {
            if (current == null) current = 0.0;
            return current + amount;
        });
        persistence.markDirty();
    }

    /**
     * Entfernt Geld (wenn genug vorhanden)
     * SICHERHEIT: Atomare Operation für Thread-Sicherheit
     */
    public static boolean removeMoney(UUID playerUUID, double amount) {
        if (!Double.isFinite(amount) || amount < 0) {
            return false;
        }
        final boolean[] success = {false};
        wallets.compute(playerUUID, (key, current) -> {
            if (current == null) current = 0.0;
            if (current >= amount) {
                success[0] = true;
                return current - amount;
            }
            return current; // Keine Änderung
        });
        if (success[0]) {
            persistence.markDirty();
        }
        return success[0];
    }

    /**
     * Gibt Health-Status zurück
     */
    public static boolean isHealthy() {
        return persistence.isHealthy();
    }

    /**
     * Gibt letzte Fehlermeldung zurück
     */
    @Nullable
    public static String getLastError() {
        return persistence.getLastError();
    }

    /**
     * Gibt Health-Info zurück
     */
    public static String getHealthInfo() {
        return persistence.getHealthInfo();
    }

    /**
     * Innere Persistence-Manager-Klasse
     * Implementiert die abstrakten Methoden für Wallet-spezifische Logik
     */
    private static class WalletPersistenceManager extends AbstractPersistenceManager<Map<String, Double>> {

        public WalletPersistenceManager(File dataFile, Gson gson) {
            super(dataFile, gson);
        }

        @Override
        protected Type getDataType() {
            return new TypeToken<Map<String, Double>>(){}.getType();
        }

        @Override
        protected void onDataLoaded(Map<String, Double> data) {
            // SICHERHEIT: Thread-safe clear und fill
            wallets.clear();

            int invalidCount = 0;
            int correctedCount = 0;

            // NULL CHECK
            if (data == null) {
                LOGGER.warn("Null data loaded for wallets");
                invalidCount++;
                return;
            }

            // Check collection size
            if (data.size() > 10000) {
                LOGGER.warn("Wallets map size ({}) exceeds limit, potential corruption",
                    data.size());
                correctedCount++;
            }

            data.forEach((key, value) -> {
                try {
                    // VALIDATE UUID STRING
                    if (key == null || key.isEmpty()) {
                        LOGGER.warn("Null/empty UUID string in wallets, skipping");
                        return;
                    }

                    UUID playerUUID;
                    try {
                        playerUUID = UUID.fromString(key);
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Invalid UUID in wallets: {}", key, e);
                        return;
                    }

                    // NULL CHECK
                    if (value == null) {
                        LOGGER.warn("Null wallet value for player {}, setting to 0", playerUUID);
                        wallets.put(playerUUID, 0.0);
                        return;
                    }

                    // VALIDATE BALANCE (>= 0)
                    if (value < 0) {
                        LOGGER.warn("Player {} has negative wallet balance {}, resetting to 0",
                            playerUUID, value);
                        wallets.put(playerUUID, 0.0);
                    } else {
                        wallets.put(playerUUID, value);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error loading wallet for {}", key, e);
                }
            });

            // SUMMARY
            if (invalidCount > 0 || correctedCount > 0) {
                LOGGER.warn("Data validation: {} invalid entries, {} corrected entries",
                    invalidCount, correctedCount);
                if (correctedCount > 0) {
                    markDirty(); // Re-save corrected data
                }
            }
        }

        @Override
        protected Map<String, Double> getCurrentData() {
            Map<String, Double> toSave = new HashMap<>();
            for (Map.Entry<UUID, Double> entry : wallets.entrySet()) {
                toSave.put(entry.getKey().toString(), entry.getValue());
            }
            return toSave;
        }

        @Override
        protected String getComponentName() {
            return "Wallet System";
        }

        @Override
        protected String getHealthDetails() {
            return String.format("%d Wallets", wallets.size());
        }

        @Override
        protected void onCriticalLoadFailure() {
            wallets.clear();
        }
    }
}
