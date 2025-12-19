package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.BackupManager;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    private static final File WALLET_FILE = new File("config/plotmod_wallets.json");

    // UUID -> Geldbörsen-Guthaben
    private static Map<UUID, Double> wallets = new HashMap<>();
    private static boolean isDirty = false;
    private static boolean isHealthy = true;
    private static String lastError = null;

    /**
     * Lädt Geldbörsen vom Disk mit Backup-Wiederherstellung
     */
    public static void load() {
        if (!WALLET_FILE.exists()) {
            LOGGER.info("Keine Wallet-Daten gefunden, starte mit leerer Datenbank");
            isHealthy = true;
            return;
        }

        try {
            loadFromFile(WALLET_FILE);
            isHealthy = true;
            lastError = null;
            LOGGER.info("Geldbörsen erfolgreich geladen: {} Spieler", wallets.size());
        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden der Geldbörsen!", e);
            lastError = "Failed to load: " + e.getMessage();

            // Backup-Wiederherstellung
            if (BackupManager.restoreFromBackup(WALLET_FILE)) {
                LOGGER.warn("Wallet-Datei korrupt, versuche Backup wiederherzustellen...");
                try {
                    loadFromFile(WALLET_FILE);
                    LOGGER.info("Wallets erfolgreich von Backup wiederhergestellt: {} Spieler", wallets.size());
                    isHealthy = true;
                    lastError = "Recovered from backup";
                } catch (Exception backupError) {
                    LOGGER.error("KRITISCH: Backup-Wiederherstellung fehlgeschlagen!", backupError);
                    handleCriticalLoadFailure();
                }
            } else {
                LOGGER.error("KRITISCH: Kein Backup verfügbar!");
                handleCriticalLoadFailure();
            }
        }
    }

    private static void loadFromFile(File file) throws Exception {
        try (FileReader reader = new FileReader(file)) {
            Map<String, Double> loaded = GSON.fromJson(reader,
                new TypeToken<Map<String, Double>>(){}.getType());

            if (loaded == null) {
                throw new IOException("Geladene Wallet-Daten sind null");
            }

            wallets.clear();
            for (Map.Entry<String, Double> entry : loaded.entrySet()) {
                wallets.put(UUID.fromString(entry.getKey()), entry.getValue());
            }
        }
    }

    private static void handleCriticalLoadFailure() {
        LOGGER.error("KRITISCH: Wallet-System konnte nicht geladen werden!");
        LOGGER.error("Starte mit leerem Wallet-System als Fallback");
        wallets.clear();
        isHealthy = false;
        lastError = "Critical load failure - running with empty data";

        if (WALLET_FILE.exists()) {
            File corruptBackup = new File(WALLET_FILE.getParent(),
                WALLET_FILE.getName() + ".CORRUPT_" + System.currentTimeMillis());
            try {
                java.nio.file.Files.copy(WALLET_FILE.toPath(), corruptBackup.toPath());
                LOGGER.info("Korrupte Datei gesichert nach: {}", corruptBackup.getName());
            } catch (IOException e) {
                LOGGER.error("Konnte korrupte Datei nicht sichern", e);
            }
        }
    }

    /**
     * Speichert Geldbörsen auf Disk mit Backup
     */
    public static void save() {
        try {
            WALLET_FILE.getParentFile().mkdirs();

            // Backup erstellen
            if (WALLET_FILE.exists() && WALLET_FILE.length() > 0) {
                BackupManager.createBackup(WALLET_FILE);
            }

            // Temporäre Datei für atomares Schreiben
            File tempFile = new File(WALLET_FILE.getParent(), WALLET_FILE.getName() + ".tmp");

            try (FileWriter writer = new FileWriter(tempFile)) {
                Map<String, Double> toSave = new HashMap<>();
                for (Map.Entry<UUID, Double> entry : wallets.entrySet()) {
                    toSave.put(entry.getKey().toString(), entry.getValue());
                }
                GSON.toJson(toSave, writer);
                writer.flush();
            }

            // Atomares Ersetzen
            java.nio.file.Files.move(tempFile.toPath(), WALLET_FILE.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE);

            isDirty = false;
            isHealthy = true;
            lastError = null;
            LOGGER.info("Geldbörsen gespeichert: {} Spieler", wallets.size());

        } catch (Exception e) {
            LOGGER.error("KRITISCH: Fehler beim Speichern der Geldbörsen!", e);
            isHealthy = false;
            lastError = "Save failed: " + e.getMessage();
            isDirty = true;
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
        return isHealthy;
    }

    /**
     * Gibt letzte Fehlermeldung zurück
     */
    @Nullable
    public static String getLastError() {
        return lastError;
    }

    /**
     * Gibt Health-Info zurück
     */
    public static String getHealthInfo() {
        if (isHealthy) {
            return String.format("§aGESUND§r - %d Wallets, %d Backups verfügbar",
                wallets.size(), BackupManager.getBackupCount(WALLET_FILE));
        } else {
            return String.format("§cUNGESUND§r - Letzter Fehler: %s, %d Wallets geladen",
                lastError != null ? lastError : "Unknown", wallets.size());
        }
    }
}
