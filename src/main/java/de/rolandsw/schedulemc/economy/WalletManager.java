package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Verwaltet Geldbörsen-Guthaben per UUID
 * Überlebt Serverneustarts und Spielertod!
 */
public class WalletManager {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File WALLET_FILE = new File("plotmod_wallets.json");
    
    // UUID -> Geldbörsen-Guthaben
    private static Map<UUID, Double> wallets = new HashMap<>();
    private static boolean isDirty = false;
    
    /**
     * Lädt Geldbörsen vom Disk
     */
    public static void load() {
        if (!WALLET_FILE.exists()) {
            LOGGER.info("Keine Wallet-Daten gefunden, starte mit leerer Datenbank");
            return;
        }
        
        try (FileReader reader = new FileReader(WALLET_FILE)) {
            Map<String, Double> loaded = GSON.fromJson(reader, 
                new TypeToken<Map<String, Double>>(){}.getType());
            
            if (loaded != null) {
                wallets.clear();
                for (Map.Entry<String, Double> entry : loaded.entrySet()) {
                    wallets.put(UUID.fromString(entry.getKey()), entry.getValue());
                }
                LOGGER.info("Geldbörsen geladen: {} Spieler", wallets.size());
            }
        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden der Geldbörsen!", e);
        }
    }
    
    /**
     * Speichert Geldbörsen auf Disk
     */
    public static void save() {
        try (FileWriter writer = new FileWriter(WALLET_FILE)) {
            Map<String, Double> toSave = new HashMap<>();
            for (Map.Entry<UUID, Double> entry : wallets.entrySet()) {
                toSave.put(entry.getKey().toString(), entry.getValue());
            }
            GSON.toJson(toSave, writer);
            isDirty = false;
            LOGGER.info("Geldbörsen gespeichert: {} Spieler", wallets.size());
        } catch (Exception e) {
            LOGGER.error("Fehler beim Speichern der Geldbörsen!", e);
        }
    }
    
    /**
     * Speichert nur wenn Änderungen vorhanden
     */
    public static void saveIfNeeded() {
        if (isDirty) {
            save();
        }
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
        isDirty = true;
    }
    
    /**
     * Fügt Geld hinzu
     */
    public static void addMoney(UUID playerUUID, double amount) {
        double current = getBalance(playerUUID);
        setBalance(playerUUID, current + amount);
    }
    
    /**
     * Entfernt Geld (gibt false zurück wenn nicht genug)
     */
    public static boolean removeMoney(UUID playerUUID, double amount) {
        double current = getBalance(playerUUID);
        if (current >= amount) {
            setBalance(playerUUID, current - amount);
            return true;
        }
        return false;
    }
}
