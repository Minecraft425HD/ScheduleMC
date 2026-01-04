package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.GsonHelper;
import de.rolandsw.schedulemc.util.BackupManager;
import de.rolandsw.schedulemc.util.IncrementalSaveManager;
import de.rolandsw.schedulemc.util.PersistenceHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zentrales Economy-System für ScheduleMC
 * Verwaltet Spieler-Guthaben mit Thread-Safety und Batch-Saving
 */
public class EconomyManager implements IncrementalSaveManager.ISaveable {

    private static final Logger LOGGER = LogUtils.getLogger();
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile EconomyManager instance;
    private static final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    // SICHERHEIT: volatile für Memory Visibility zwischen Threads (IncrementalSaveManager)
    private static volatile File file = new File("config/plotmod_economy.json");
    private static final Gson gson = GsonHelper.get();
    private static volatile boolean needsSave = false;
    private static volatile boolean isHealthy = true;
    private static volatile String lastError = null;

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
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static EconomyManager getInstance() {
        EconomyManager localRef = instance;
        if (localRef == null) {
            synchronized (EconomyManager.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new EconomyManager();
                }
            }
        }
        return localRef;
    }

    // Typ für Gson-Deserialisierung
    private static final Type BALANCE_MAP_TYPE = new TypeToken<Map<String, Double>>(){}.getType();

    /**
     * Lädt alle Konten aus der JSON-Datei mit Backup-Wiederherstellung
     * OPTIMIERT: Nutzt PersistenceHelper für reduzierte Code-Duplikation
     */
    public static void loadAccounts() {
        PersistenceHelper.LoadResult<Map<String, Double>> result =
            PersistenceHelper.load(file, gson, BALANCE_MAP_TYPE, "EconomyManager");

        if (!result.isSuccess()) {
            // Kritischer Fehler - starte mit leeren Daten
            balances.clear();
            isHealthy = false;
            lastError = result.getError();
            LOGGER.error("KRITISCH: Economy-System startet mit leeren Daten!");
            return;
        }

        if (!result.hasData()) {
            // Keine Datei gefunden - normaler Start
            isHealthy = true;
            lastError = null;
            return;
        }

        // Daten verarbeiten
        Map<String, Double> loaded = result.getData();
        processLoadedData(loaded);

        isHealthy = true;
        lastError = result.isRecoveredFromBackup() ? "Recovered from backup" : null;
        LOGGER.info("Economy-Daten geladen: {} Konten", balances.size());
    }

    /**
     * Verarbeitet geladene Daten mit Validierung
     */
    private static void processLoadedData(Map<String, Double> loaded) {
        LOGGER.info("=== LOADING ECONOMY DATA ===");
        LOGGER.info("Loading {} accounts from file", loaded.size());

        balances.clear();
        int invalidUUIDs = 0;
        int zeroBalanceAccounts = 0;

        for (Map.Entry<String, Double> entry : loaded.entrySet()) {
            try {
                UUID uuid = UUID.fromString(entry.getKey());
                Double balance = entry.getValue();

                if (balance == null) {
                    LOGGER.warn("Null-Balance für UUID {}, überspringe", entry.getKey());
                    invalidUUIDs++;
                    continue;
                }

                if (balance == 0.0) {
                    zeroBalanceAccounts++;
                }

                balances.put(uuid, balance);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Ungültige UUID in Economy-Datei: {}", entry.getKey());
                invalidUUIDs++;
            }
        }

        if (invalidUUIDs > 0) {
            LOGGER.warn("{} ungültige Einträge beim Laden übersprungen", invalidUUIDs);
        }
        if (zeroBalanceAccounts > 0) {
            LOGGER.warn("⚠ {} accounts with 0€ balance found!", zeroBalanceAccounts);
        }
        LOGGER.info("=== ECONOMY DATA LOADED: {} accounts ===", balances.size());
    }

    /**
     * Speichert alle Konten in die JSON-Datei mit Backup
     * OPTIMIERT: Nutzt PersistenceHelper für reduzierte Code-Duplikation
     */
    public static void saveAccounts() {
        // UUID -> String Transformation für JSON
        Map<String, Double> saveMap = new HashMap<>();
        balances.forEach((k, v) -> saveMap.put(k.toString(), v));

        PersistenceHelper.SaveResult result =
            PersistenceHelper.save(file, gson, saveMap, "EconomyManager");

        if (result.isSuccess()) {
            needsSave = false;
            isHealthy = true;
            lastError = null;
            LOGGER.debug("Economy-Daten gespeichert: {} Konten", balances.size());
        } else {
            isHealthy = false;
            lastError = result.getError();
            needsSave = true; // Für nächsten Versuch markieren
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
     * SICHERHEIT: Atomare Operation für Thread-Sicherheit
     */
    public static void deposit(UUID uuid, double amount, TransactionType type, @Nullable String description) {
        if (amount < 0) {
            LOGGER.warn("Versuch, negativen Betrag einzuzahlen: {}", amount);
            return;
        }

        // SICHERHEIT: Atomare Operation mit compute()
        final double[] newBalance = {0.0};
        balances.compute(uuid, (key, currentBalance) -> {
            if (currentBalance == null) currentBalance = 0.0;
            newBalance[0] = currentBalance + amount;
            return newBalance[0];
        });

        markDirty();
        LOGGER.debug("Einzahlung: {} € für {} ({})", amount, uuid, type);

        // Transaction History
        logTransaction(uuid, type, null, uuid, amount, description, newBalance[0]);
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
     * SICHERHEIT: Atomare Operation verhindert Race Conditions bei gleichzeitigen Transaktionen
     * @return true wenn erfolgreich, false wenn nicht genug Guthaben (oder Dispo-Limit erreicht)
     */
    public static boolean withdraw(UUID uuid, double amount, TransactionType type, @Nullable String description) {
        if (amount < 0) {
            LOGGER.warn("Versuch, negativen Betrag abzuheben: {}", amount);
            return false;
        }

        // SICHERHEIT: Atomare read-modify-write Operation mit compute()
        final double[] resultBalance = {0.0};
        final boolean[] success = {false};

        balances.compute(uuid, (key, currentBalance) -> {
            if (currentBalance == null) currentBalance = 0.0;
            double newBalance = currentBalance - amount;

            // Prüfe ob genug Guthaben ODER Dispo-Limit nicht überschritten
            if (currentBalance >= amount || de.rolandsw.schedulemc.economy.OverdraftManager.canOverdraft(newBalance)) {
                resultBalance[0] = newBalance;
                success[0] = true;
                return newBalance;
            }
            resultBalance[0] = currentBalance;
            return currentBalance; // Keine Änderung
        });

        if (success[0]) {
            markDirty();
            LOGGER.debug("Abbuchung: {} € von {} ({}) - Neuer Stand: {}", amount, uuid, type, resultBalance[0]);

            // Transaction History
            logTransaction(uuid, type, uuid, null, -amount, description, resultBalance[0]);
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
     * OPTIMIERT: Nutzt PersistenceHelper
     */
    public static String getHealthInfo() {
        return PersistenceHelper.getHealthInfo(file, isHealthy, lastError,
            balances.size() + " Konten");
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
