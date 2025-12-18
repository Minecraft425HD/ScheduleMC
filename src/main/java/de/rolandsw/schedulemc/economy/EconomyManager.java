package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.GsonHelper;
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
public class EconomyManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static EconomyManager instance;
    private static final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    private static final File file = new File("config/plotmod_economy.json");
    private static final Gson gson = GsonHelper.get();
    private static boolean needsSave = false;

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
     * Lädt alle Konten aus der JSON-Datei
     */
    public static void loadAccounts() {
        if (!file.exists()) {
            LOGGER.info("Keine Economy-Datei gefunden, starte mit leerer Datenbank");
            return;
        }
        
        try (FileReader reader = new FileReader(file)) {
            Map<String, Double> loaded = gson.fromJson(reader, new TypeToken<Map<String, Double>>(){}.getType());
            if (loaded != null) {
                loaded.forEach((k, v) -> {
                    try {
                        balances.put(UUID.fromString(k), v);
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Ungültige UUID in Economy-Datei: {}", k, e);
                    }
                });
                LOGGER.info("Economy-Daten geladen: {} Konten", balances.size());
            }
        } catch (IOException e) {
            LOGGER.error("Fehler beim Laden der Economy-Daten", e);
        }
    }

    /**
     * Speichert alle Konten in die JSON-Datei
     */
    public static void saveAccounts() {
        try {
            file.getParentFile().mkdirs(); // Erstelle config-Ordner falls nicht vorhanden
            
            try (FileWriter writer = new FileWriter(file)) {
                Map<String, Double> saveMap = new HashMap<>();
                balances.forEach((k, v) -> saveMap.put(k.toString(), v));
                gson.toJson(saveMap, writer);
                needsSave = false;
                LOGGER.debug("Economy-Daten gespeichert: {} Konten", saveMap.size());
            }
        } catch (IOException e) {
            LOGGER.error("Fehler beim Speichern der Economy-Daten", e);
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
        return balances.containsKey(uuid);
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
     * @return true wenn erfolgreich, false wenn nicht genug Guthaben
     */
    public static boolean withdraw(UUID uuid, double amount, TransactionType type, @Nullable String description) {
        if (amount < 0) {
            LOGGER.warn("Versuch, negativen Betrag abzuheben: {}", amount);
            return false;
        }

        double currentBalance = balances.getOrDefault(uuid, 0.0);
        if (currentBalance >= amount) {
            double newBalance = currentBalance - amount;
            balances.put(uuid, newBalance);
            markDirty();
            LOGGER.debug("Abbuchung: {} € von {} ({})", amount, uuid, type);

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
}
