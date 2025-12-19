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

/**
 * Verwaltet Geldbörsen-Guthaben per UUID
 * Überlebt Serverneustarts und Spielertod!
 *
 * Nutzt AbstractPersistenceManager für robuste Datenpersistenz
 */
public class WalletManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File WALLET_FILE = new File("config/plotmod_wallets.json");

    // UUID -> Geldbörsen-Guthaben
    private static Map<UUID, Double> wallets = new HashMap<>();

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
     */
    public static void addMoney(UUID playerUUID, double amount) {
        double current = getBalance(playerUUID);
        setBalance(playerUUID, current + amount);
    }

    /**
     * Entfernt Geld (wenn genug vorhanden)
     */
    public static boolean removeMoney(UUID playerUUID, double amount) {
        double current = getBalance(playerUUID);
        if (current >= amount) {
            setBalance(playerUUID, current - amount);
            return true;
        }
        return false;
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
            wallets.clear();
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                wallets.put(UUID.fromString(entry.getKey()), entry.getValue());
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
