package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.GsonHelper;
import de.rolandsw.schedulemc.util.BackupManager;
import de.rolandsw.schedulemc.util.IncrementalSaveManager;
import de.rolandsw.schedulemc.util.PersistenceHelper;
import de.rolandsw.schedulemc.util.RateLimiter;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zentrales Economy-System für ScheduleMC
 * Verwaltet Spieler-Guthaben mit Thread-Safety und Batch-Saving
 */
public class EconomyManager implements IncrementalSaveManager.ISaveable {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Rate Limiting Constants
    private static final int TRANSFER_MAX_OPS_PER_WINDOW = 10;
    private static final int WITHDRAW_MAX_OPS_PER_WINDOW = 20;
    private static final int DEPOSIT_MAX_OPS_PER_WINDOW = 20;
    private static final long RATE_LIMIT_WINDOW_MS = 1000L;

    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile EconomyManager instance;
    private static final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    // SICHERHEIT: volatile für Memory Visibility zwischen Threads (IncrementalSaveManager)
    private static volatile File file = new File("config/plotmod_economy.json");
    private static final Gson gson = GsonHelper.get();
    private static volatile boolean needsSave = false;
    private static volatile boolean isHealthy = true;
    private static volatile String lastError = null;

    // SICHERHEIT: Rate Limiting für DoS-Protection
    private static final RateLimiter transferLimiter = new RateLimiter("money_transfer", TRANSFER_MAX_OPS_PER_WINDOW, RATE_LIMIT_WINDOW_MS);
    private static final RateLimiter withdrawLimiter = new RateLimiter("money_withdraw", WITHDRAW_MAX_OPS_PER_WINDOW, RATE_LIMIT_WINDOW_MS);
    private static final RateLimiter depositLimiter = new RateLimiter("money_deposit", DEPOSIT_MAX_OPS_PER_WINDOW, RATE_LIMIT_WINDOW_MS);

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
     * Creates a new economy account for the specified player with the configured starting balance.
     *
     * The account is created with the balance defined in the server configuration and is
     * immediately marked as dirty for persistence. This method should be called when a
     * player joins the server for the first time.
     *
     * @param uuid The unique identifier of the player
     */
    public static void createAccount(UUID uuid) {
        double startBalance = getStartBalance();
        balances.put(uuid, startBalance);
        markDirty();
        LOGGER.info("Neues Konto erstellt für {} mit {} €", uuid, startBalance);
    }

    /**
     * Checks if an economy account exists for the specified player.
     *
     * This method performs a thread-safe lookup in the concurrent balance map.
     * Debug logging is enabled to trace account existence checks.
     *
     * @param uuid The unique identifier of the player
     * @return true if an account exists for this player, false otherwise
     */
    public static boolean hasAccount(UUID uuid) {
        boolean exists = balances.containsKey(uuid);
        LOGGER.debug("hasAccount({}) = {} (current balance: {})", uuid, exists,
            exists ? balances.get(uuid) : "N/A");
        return exists;
    }

    /**
     * Retrieves the current balance of the specified player's economy account.
     *
     * If no account exists for the player, returns 0.0 as the default balance.
     * This is a thread-safe read operation.
     *
     * @param uuid The unique identifier of the player
     * @return The player's current balance in the economy system, or 0.0 if no account exists
     */
    public static double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0.0);
    }

    /**
     * Deposits money into the specified player's economy account.
     *
     * This is a convenience method that calls the full deposit method with
     * TransactionType.OTHER and no description.
     *
     * @param uuid The unique identifier of the player
     * @param amount The amount to deposit (must be non-negative)
     */
    public static void deposit(UUID uuid, double amount) {
        deposit(uuid, amount, TransactionType.OTHER, null);
    }

    /**
     * Deposits money into the specified player's economy account with transaction tracking.
     *
     * This method performs a thread-safe atomic deposit operation with rate limiting
     * protection against abuse. The transaction is logged for audit purposes and added
     * to the transaction history. Negative amounts are rejected.
     *
     * Rate limiting is applied for player-initiated deposits (OTHER and TRANSFER types)
     * but not for system operations. If an account doesn't exist, it will be created
     * with a 0.0 starting balance before the deposit.
     *
     * @param uuid The unique identifier of the player
     * @param amount The amount to deposit (negative values are rejected)
     * @param type The type of transaction for categorization
     * @param description Optional description of the transaction (can be null)
     */
    public static void deposit(UUID uuid, double amount, TransactionType type, @Nullable String description) {
        if (amount < 0) {
            LOGGER.warn("Versuch, negativen Betrag einzuzahlen: {}", amount);
            return;
        }

        // Rate Limiting (nur für Spieler-initiierte Deposits, nicht System)
        if (type == TransactionType.OTHER || type == TransactionType.TRANSFER) {
            if (!depositLimiter.allowOperation(uuid)) {
                LOGGER.warn("Rate limit exceeded for deposit by player {}", uuid);
                return;
            }
        }

        // SICHERHEIT: Atomare Operation mit compute()
        final double[] newBalance = {0.0};
        balances.compute(uuid, (key, currentBalance) -> {
            if (currentBalance == null) currentBalance = 0.0;
            newBalance[0] = currentBalance + amount;
            return newBalance[0];
        });

        markDirty();

        // AUDIT: Strukturiertes Transaction Logging für Forensics
        LOGGER.info("[ECONOMY] DEPOSIT | Player={} | Amount=+{}€ | Type={} | Balance={}€ | Description={}",
            uuid, String.format("%.2f", amount), type, String.format("%.2f", newBalance[0]),
            description != null ? description : "N/A");

        // Transaction History
        logTransaction(uuid, type, null, uuid, amount, description, newBalance[0]);
    }

    /**
     * Withdraws money from the specified player's economy account.
     *
     * This is a convenience method that calls the full withdraw method with
     * TransactionType.OTHER and no description. The withdrawal will fail if
     * the player has insufficient funds and overdraft is not allowed.
     *
     * @param uuid The unique identifier of the player
     * @param amount The amount to withdraw (must be non-negative)
     * @return true if the withdrawal was successful, false if insufficient funds or overdraft limit exceeded
     */
    public static boolean withdraw(UUID uuid, double amount) {
        return withdraw(uuid, amount, TransactionType.OTHER, null);
    }

    /**
     * Withdraws money from the specified player's economy account with transaction tracking.
     *
     * This method performs a thread-safe atomic withdrawal operation with rate limiting
     * protection. The withdrawal succeeds if either the player has sufficient funds OR
     * if the overdraft limit is not exceeded. The transaction is logged for audit purposes
     * and added to transaction history. Negative amounts are rejected.
     *
     * Rate limiting is applied for player-initiated withdrawals (OTHER and TRANSFER types)
     * but not for system operations. Failed withdrawals are logged with the reason.
     *
     * @param uuid The unique identifier of the player
     * @param amount The amount to withdraw (negative values are rejected)
     * @param type The type of transaction for categorization
     * @param description Optional description of the transaction (can be null)
     * @return true if the withdrawal was successful, false if insufficient funds or overdraft limit exceeded
     */
    public static boolean withdraw(UUID uuid, double amount, TransactionType type, @Nullable String description) {
        if (amount < 0) {
            LOGGER.warn("Versuch, negativen Betrag abzuheben: {}", amount);
            return false;
        }

        // Rate Limiting (nur für Spieler-initiierte Withdrawals)
        if (type == TransactionType.OTHER || type == TransactionType.TRANSFER) {
            if (!withdrawLimiter.allowOperation(uuid)) {
                LOGGER.warn("Rate limit exceeded for withdrawal by player {}", uuid);
                return false;
            }
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

            // AUDIT: Strukturiertes Transaction Logging für Forensics
            LOGGER.info("[ECONOMY] WITHDRAW | Player={} | Amount=-{}€ | Type={} | Balance={}€ | Description={}",
                uuid, String.format("%.2f", amount), type, String.format("%.2f", resultBalance[0]),
                description != null ? description : "N/A");

            // Transaction History
            logTransaction(uuid, type, uuid, null, -amount, description, resultBalance[0]);
            return true;
        } else {
            // AUDIT: Fehlgeschlagene Transaktion loggen
            LOGGER.warn("[ECONOMY] WITHDRAW_FAILED | Player={} | Amount=-{}€ | Type={} | Reason=InsufficientFunds | Balance={}€",
                uuid, String.format("%.2f", amount), type, String.format("%.2f", resultBalance[0]));
        }
        return false;
    }

    /**
     * Sets the balance of a player's account directly to the specified amount.
     *
     * This is an administrative function that bypasses normal deposit/withdrawal
     * mechanics. This is a convenience method that calls the full setBalance method
     * with TransactionType.ADMIN_SET and no description. Negative amounts are
     * automatically clamped to 0.0.
     *
     * @param uuid The unique identifier of the player
     * @param amount The new balance to set (negative values are converted to 0.0)
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

        // AUDIT: Strukturiertes Admin-Transaction Logging
        LOGGER.info("[ECONOMY] SET_BALANCE | Player={} | OldBalance={}€ | NewBalance={}€ | Difference={}€ | Type={} | Description={}",
            uuid, String.format("%.2f", oldBalance), String.format("%.2f", amount),
            String.format("%.2f", difference), type, description != null ? description : "N/A");

        // Transaction History
        logTransaction(uuid, type, null, uuid, difference, description, amount);
    }

    /**
     * Retrieves the starting balance for new accounts from the server configuration.
     *
     * This value is defined in the mod's configuration file and determines how much
     * money new players receive when their account is first created.
     *
     * @return The configured starting balance for new player accounts
     */
    public static double getStartBalance() {
        return ModConfigHandler.COMMON.START_BALANCE.get();
    }

    /**
     * Retrieves all economy accounts in the system.
     *
     * Returns a defensive copy of the accounts map to prevent external modification
     * of the internal state. This method is primarily used for administrative commands
     * and reporting purposes.
     *
     * @return A new HashMap containing all player UUIDs and their corresponding balances
     */
    public static Map<UUID, Double> getAllAccounts() {
        return new HashMap<>(balances);
    }

    /**
     * Deletes an economy account from the system.
     *
     * This is an administrative function that permanently removes a player's account
     * and all associated balance information. The deletion is immediately marked for
     * persistence. This action cannot be undone.
     *
     * @param uuid The unique identifier of the player whose account should be deleted
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
     * Transfers money from one player's account to another player's account.
     *
     * This method performs a secure transfer by first withdrawing from the sender's
     * account and then depositing to the receiver's account. The operation includes
     * rate limiting protection against abuse. Both sides of the transfer are logged
     * separately in the transaction history with appropriate descriptions.
     *
     * The transfer will fail if the sender has insufficient funds or if rate limits
     * are exceeded. If the withdrawal succeeds but something goes wrong, the money
     * may be lost, so this operation should be used carefully.
     *
     * @param from The UUID of the player sending money
     * @param to The UUID of the player receiving money
     * @param amount The amount to transfer (must be positive)
     * @param description Optional description of the transfer (can be null)
     * @return true if the transfer was successful, false if the sender had insufficient funds or rate limit was exceeded
     */
    public static boolean transfer(UUID from, UUID to, double amount, @Nullable String description) {
        // Rate Limiting für Transfers
        if (!transferLimiter.allowOperation(from)) {
            LOGGER.warn("Rate limit exceeded for transfer by player {}", from);
            return false;
        }

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
