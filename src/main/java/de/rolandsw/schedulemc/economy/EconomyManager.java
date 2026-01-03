package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.GsonHelper;
import de.rolandsw.schedulemc.util.BackupManager;
import de.rolandsw.schedulemc.util.IncrementalSaveManager;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zentrales Economy-System für ScheduleMC
 * Verwaltet Spieler-Guthaben mit Thread-Safety und Batch-Saving
 */
public class EconomyManager implements IncrementalSaveManager.ISaveable {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static EconomyManager instance;
    private static final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    private static File file = new File("config/plotmod_economy.json");
    private static final Gson gson = GsonHelper.get();
    private static boolean needsSave = false;
    private static boolean isHealthy = true;
    private static String lastError = null;

    /**
     * Set the file location for economy data. Package-private for testing.
     * @param newFile the new file location
     */
    static void setFile(File newFile) {
        file = newFile;
    }

    /**
     * Get the current file location. Package-private for testing.
     * @return the current file location
     */
    static File getFile() {
        return file;
    }

    @Nullable
    private MinecraftServer server;

    private EconomyManager() {}

    /**
     * Initialisiert den EconomyManager mit dem Server
     */
    public static void initialize(MinecraftServer server) {
        if (instance == null) {
            instance = new EconomyManager();
        }
        instance.server = server;
    }

    /**
     * Gibt die Singleton-Instanz zurück
     */
    public static EconomyManager getInstance() {
        if (instance == null) {
            instance = new EconomyManager();
        }
        return instance;
    }

    /**
     * Lädt alle Konten aus der JSON-Datei mit Backup-Wiederherstellung
     */
    public static void loadAccounts() {
        if (!file.exists()) {
            LOGGER.info("Keine Economy-Datei gefunden, starte mit leerer Datenbank");
            isHealthy = true;
            return;
        }

        try {
            loadAccountsFromFile(file);
            isHealthy = true;
            lastError = null;
            LOGGER.info("Economy-Daten erfolgreich geladen: {} Konten", balances.size());
        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden der Economy-Daten", e);
            lastError = "Failed to load: " + e.getMessage();

            // Versuch Backup wiederherzustellen
            if (BackupManager.restoreFromBackup(file)) {
                LOGGER.warn("Economy-Datei korrupt, versuche Backup wiederherzustellen...");
                try {
                    loadAccountsFromFile(file);
                    LOGGER.info("Economy-Daten erfolgreich von Backup wiederhergestellt: {} Konten", balances.size());
                    isHealthy = true;
                    lastError = "Recovered from backup";
                } catch (Exception backupError) {
                    LOGGER.error("KRITISCH: Backup-Wiederherstellung fehlgeschlagen!", backupError);
                    handleCriticalLoadFailure();
                }
            } else {
                LOGGER.error("KRITISCH: Kein Backup verfügbar für Wiederherstellung!");
                handleCriticalLoadFailure();
            }
        }
    }

    /**
     * Lädt Konten aus einer spezifischen Datei
     */
    private static void loadAccountsFromFile(File sourceFile) throws IOException {
        try (FileReader reader = new FileReader(sourceFile)) {
            Map<String, Double> loaded = gson.fromJson(reader, new TypeToken<Map<String, Double>>(){}.getType());

            if (loaded == null) {
                throw new IOException("Geladene Daten sind null");
            }

            LOGGER.info("=== LOADING ECONOMY DATA FROM {} ===", sourceFile.getName());
            LOGGER.info("Loading {} accounts from file", loaded.size());

            balances.clear();
            int invalidUUIDs = 0;
            int zeroBalanceAccounts = 0;

            for (Map.Entry<String, Double> entry : loaded.entrySet()) {
                try {
                    UUID uuid = UUID.fromString(entry.getKey());
                    Double balance = entry.getValue();

                    // Validierung
                    if (balance == null) {
                        LOGGER.warn("Null-Balance für UUID {}, überspringe", entry.getKey());
                        invalidUUIDs++;
                        continue;
                    }

                    if (balance == 0.0) {
                        LOGGER.warn("⚠ FOUND ACCOUNT WITH 0€ BALANCE: UUID={}, Balance={}", uuid, balance);
                        zeroBalanceAccounts++;
                    }

                    balances.put(uuid, balance);
                    LOGGER.debug("Loaded account: UUID={}, Balance={}", uuid, balance);
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Ungültige UUID in Economy-Datei: {}", entry.getKey());
                    invalidUUIDs++;
                }
            }

            if (invalidUUIDs > 0) {
                LOGGER.warn("{} ungültige Einträge beim Laden übersprungen", invalidUUIDs);
            }
            if (zeroBalanceAccounts > 0) {
                LOGGER.warn("⚠ {} accounts with 0€ balance found in saved data!", zeroBalanceAccounts);
            }
            LOGGER.info("=== ECONOMY DATA LOADED: {} accounts ===", balances.size());
        }
    }

    /**
     * Behandelt kritischen Ladefehler mit Graceful Degradation
     */
    private static void handleCriticalLoadFailure() {
        LOGGER.error("KRITISCH: Economy-System konnte nicht geladen werden!");
        LOGGER.error("Starte mit leerem Economy-System als Fallback");

        balances.clear();
        isHealthy = false;
        lastError = "Critical load failure - running with empty data";

        // Erstelle Notfall-Backup der korrupten Datei für forensische Analyse
        if (file.exists()) {
            File corruptBackup = new File(file.getParent(), file.getName() + ".CORRUPT_" + System.currentTimeMillis());
            try {
                java.nio.file.Files.copy(file.toPath(), corruptBackup.toPath());
                LOGGER.info("Korrupte Datei gesichert nach: {}", corruptBackup.getName());
            } catch (IOException e) {
                LOGGER.error("Konnte korrupte Datei nicht sichern", e);
            }
        }
    }

    /**
     * Speichert alle Konten in die JSON-Datei mit Backup
     */
    public static void saveAccounts() {
        try {
            file.getParentFile().mkdirs(); // Erstelle config-Ordner falls nicht vorhanden

            // Erstelle Backup vor dem Speichern (falls Datei existiert)
            if (file.exists() && file.length() > 0) {
                BackupManager.createBackup(file);
            }

            // Temporäre Datei für atomares Schreiben
            File tempFile = new File(file.getParent(), file.getName() + ".tmp");

            try (FileWriter writer = new FileWriter(tempFile)) {
                Map<String, Double> saveMap = new HashMap<>();
                balances.forEach((k, v) -> saveMap.put(k.toString(), v));
                gson.toJson(saveMap, writer);
                writer.flush();
            }

            // Atomares Ersetzen (verhindert Datenverlust bei Absturz während Schreibvorgang)
            java.nio.file.Files.move(tempFile.toPath(), file.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE);

            needsSave = false;
            isHealthy = true;
            lastError = null;
            LOGGER.debug("Economy-Daten gespeichert: {} Konten", balances.size());

        } catch (IOException e) {
            LOGGER.error("KRITISCH: Fehler beim Speichern der Economy-Daten", e);
            isHealthy = false;
            lastError = "Save failed: " + e.getMessage();

            // Versuche erneut nach kurzer Wartezeit
            try {
                Thread.sleep(100);
                retrySave();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Retry-Mechanismus für fehlgeschlagene Saves
     */
    private static void retrySave() {
        LOGGER.warn("Versuche erneut zu speichern...");
        try {
            File tempFile = new File(file.getParent(), file.getName() + ".tmp");

            try (FileWriter writer = new FileWriter(tempFile)) {
                Map<String, Double> saveMap = new HashMap<>();
                balances.forEach((k, v) -> saveMap.put(k.toString(), v));
                gson.toJson(saveMap, writer);
                writer.flush();
            }

            java.nio.file.Files.move(tempFile.toPath(), file.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE);

            LOGGER.info("Retry erfolgreich - Daten gespeichert");
            isHealthy = true;
            lastError = null;
            needsSave = false;

        } catch (IOException retryError) {
            LOGGER.error("KRITISCH: Retry fehlgeschlagen - Daten konnten nicht gespeichert werden!", retryError);
            // Markiere als needs save für nächsten Versuch
            needsSave = true;
        }
    }

    /**
     * Speichert nur wenn Änderungen vorliegen (für periodisches Speichern)
     */
    public static void saveIfNeeded() {
        if (needsSave) {
            saveAccounts();
        }
        // Speichere auch Transaction History
        if (instance != null && instance.server != null) {
            TransactionHistory history = TransactionHistory.getInstance(instance.server);
            if (history != null) {
                history.save();
            }
        }
    }

    /**
     * Markiert, dass Änderungen vorliegen
     */
    private static void markDirty() {
        needsSave = true;
    }

    /**
     * Erstellt ein neues Konto mit Startguthaben aus der Config
     */
    public static void createAccount(UUID uuid) {
        double startBalance = getStartBalance();
        balances.put(uuid, startBalance);
        markDirty();
        LOGGER.info("Neues Konto erstellt für {} mit {} €", uuid, startBalance);
    }

    /**
     * Prüft ob ein Konto existiert
     */
    public static boolean hasAccount(UUID uuid) {
        boolean exists = balances.containsKey(uuid);
        LOGGER.debug("hasAccount({}) = {} (current balance: {})", uuid, exists,
            exists ? balances.get(uuid) : "N/A");
        return exists;
    }

    /**
     * Gibt das Guthaben eines Spielers zurück
     */
    public static double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0.0);
    }

    /**
     * Zahlt Geld auf ein Konto ein
     */
    public static void deposit(UUID uuid, double amount) {
        deposit(uuid, amount, TransactionType.OTHER, null);
    }

    /**
     * Zahlt Geld auf ein Konto ein mit Transaktions-Logging
     */
    public static void deposit(UUID uuid, double amount, TransactionType type, @Nullable String description) {
        if (amount < 0) {
            LOGGER.warn("Versuch, negativen Betrag einzuzahlen: {}", amount);
            return;
        }

        double currentBalance = balances.getOrDefault(uuid, 0.0);
        double newBalance = currentBalance + amount;
        balances.put(uuid, newBalance);
        markDirty();
        LOGGER.debug("Einzahlung: {} € für {} ({})", amount, uuid, type);

        // Transaction History
        logTransaction(uuid, type, null, uuid, amount, description, newBalance);
    }

    /**
     * Hebt Geld von einem Konto ab
     * @return true wenn erfolgreich, false wenn nicht genug Guthaben
     */
    public static boolean withdraw(UUID uuid, double amount) {
        return withdraw(uuid, amount, TransactionType.OTHER, null);
    }

    /**
     * Hebt Geld von einem Konto ab mit Transaktions-Logging
     * @return true wenn erfolgreich, false wenn nicht genug Guthaben (oder Dispo-Limit erreicht)
     */
    public static boolean withdraw(UUID uuid, double amount, TransactionType type, @Nullable String description) {
        if (amount < 0) {
            LOGGER.warn("Versuch, negativen Betrag abzuheben: {}", amount);
            return false;
        }

        double currentBalance = balances.getOrDefault(uuid, 0.0);
        double newBalance = currentBalance - amount;

        // Prüfe ob genug Guthaben ODER Dispo-Limit nicht überschritten
        if (currentBalance >= amount || de.rolandsw.schedulemc.economy.OverdraftManager.canOverdraft(newBalance)) {
            balances.put(uuid, newBalance);
            markDirty();
            LOGGER.debug("Abbuchung: {} € von {} ({}) - Neuer Stand: {}", amount, uuid, type, newBalance);

            // Transaction History
            logTransaction(uuid, type, uuid, null, -amount, description, newBalance);
            return true;
        }
        return false;
    }

    /**
     * Setzt das Guthaben eines Spielers (Admin-Funktion)
     */
    public static void setBalance(UUID uuid, double amount) {
        setBalance(uuid, amount, TransactionType.ADMIN_SET, null);
    }

    /**
     * Setzt das Guthaben eines Spielers mit Transaktions-Logging
     */
    public static void setBalance(UUID uuid, double amount, TransactionType type, @Nullable String description) {
        if (amount < 0) {
            amount = 0;
        }
        double oldBalance = balances.getOrDefault(uuid, 0.0);
        double difference = amount - oldBalance;
        balances.put(uuid, amount);
        markDirty();
        LOGGER.info("Guthaben gesetzt: {} auf {} € ({})", uuid, amount, type);

        // Transaction History
        logTransaction(uuid, type, null, uuid, difference, description, amount);
    }

    /**
     * Gibt das Startguthaben aus der Config zurück
     */
    public static double getStartBalance() {
        return ModConfigHandler.COMMON.START_BALANCE.get();
    }

    /**
     * Gibt alle Konten zurück (für Admin-Befehle)
     */
    public static Map<UUID, Double> getAllAccounts() {
        return new HashMap<>(balances);
    }

    /**
     * Löscht ein Konto (Admin-Funktion)
     */
    public static void deleteAccount(UUID uuid) {
        balances.remove(uuid);
        markDirty();
        LOGGER.info("Konto gelöscht: {}", uuid);
    }

    /**
     * Loggt eine Transaktion in die History
     */
    private static void logTransaction(UUID playerUUID, TransactionType type,
                                      @Nullable UUID from, @Nullable UUID to,
                                      double amount, @Nullable String description,
                                      double balanceAfter) {
        if (instance != null && instance.server != null) {
            TransactionHistory history = TransactionHistory.getInstance(instance.server);
            Transaction transaction = new Transaction(type, from, to, amount, description, balanceAfter);
            history.addTransaction(playerUUID, transaction);
        }
    }

    /**
     * Transfer zwischen zwei Spielern
     */
    public static boolean transfer(UUID from, UUID to, double amount, @Nullable String description) {
        if (withdraw(from, amount)) {
            deposit(to, amount, TransactionType.TRANSFER, description);

            // Separate Logging für beide Seiten
            double fromBalance = getBalance(from);
            double toBalance = getBalance(to);

            logTransaction(from, TransactionType.TRANSFER, from, to, -amount,
                description != null ? "An " + to : null, fromBalance);
            logTransaction(to, TransactionType.TRANSFER, from, to, amount,
                description != null ? "Von " + from : null, toBalance);

            return true;
        }
        return false;
    }

    /**
     * Gibt den Health-Status des Economy-Systems zurück
     */
    public static boolean isHealthy() {
        return isHealthy;
    }

    /**
     * Gibt die letzte Fehlermeldung zurück (oder null wenn gesund)
     */
    @Nullable
    public static String getLastError() {
        return lastError;
    }

    /**
     * Gibt detaillierte Health-Informationen zurück
     */
    public static String getHealthInfo() {
        if (isHealthy) {
            return String.format("§aGESUND§r - %d Konten, %d Backups verfügbar",
                balances.size(), BackupManager.getBackupCount(file));
        } else {
            return String.format("§cUNGESUND§r - Letzter Fehler: %s, %d Konten geladen",
                lastError != null ? lastError : "Unknown", balances.size());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // INCREMENTAL SAVE MANAGER INTEGRATION
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean isDirty() {
        return needsSave;
    }

    @Override
    public void save() {
        saveAccounts();
    }

    @Override
    public String getName() {
        return "EconomyManager";
    }

    @Override
    public int getPriority() {
        return 0; // Höchste Priorität - Economy-Daten sind kritisch
    }
}
