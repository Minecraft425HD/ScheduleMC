package de.rolandsw.schedulemc.economy;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.ThreadPoolManager;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.io.*;

/**
 * Staatskasse - Verwaltet Staatsgelder
 *
 * Zahlt für:
 * - Warehouse-Lieferungen
 * - Öffentliche Infrastruktur
 *
 * Einnahmen:
 * - Admin-Einzahlungen
 * - Steuern (zukünftig)
 * - ATM-Gebühren
 * - Transfer-Gebühren
 */
public class StateAccount {
    private static final Logger LOGGER = LogUtils.getLogger();
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile StateAccount instance;
    // SICHERHEIT: volatile für Memory Visibility zwischen Threads
    private static volatile int balance = 100000; // Start: 100,000€
    private static final File SAVE_FILE = new File("config/state_account.json");

    /**
     * Singleton-Instanz für Kompatibilität mit neuen Managern
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static StateAccount getInstance(MinecraftServer server) {
        StateAccount localRef = instance;
        if (localRef == null) {
            synchronized (StateAccount.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new StateAccount();
                }
            }
        }
        return localRef;
    }

    public static int getBalance() {
        return balance;
    }

    /**
     * Zieht Geld aus der Staatskasse ab
     */
    public static boolean withdraw(int amount, String reason) {
        if (balance >= amount) {
            balance -= amount;
            LOGGER.info("State treasury: -{}€ ({}), remaining: {}€", amount, reason, balance);
            save();
            return true;
        }
        LOGGER.warn("State treasury: Not enough money for {} ({}€)", reason, amount);
        return false;
    }

    /**
     * Zahlt Geld in die Staatskasse ein
     */
    public static void deposit(int amount, String reason) {
        balance += amount;
        LOGGER.info("State treasury: +{}€ ({}), total: {}€", amount, reason, balance);
        save();
    }

    /**
     * Zahlt Geld in die Staatskasse ein (double Überladung für FeeManager)
     */
    public void deposit(double amount, String reason) {
        deposit((int) Math.round(amount), reason);
    }

    /**
     * Speichert Staatskassen-Saldo (Async - blockiert Game Thread nicht)
     */
    public static void save() {
        // PERFORMANCE: File I/O auf IO Thread Pool statt Game Thread
        int currentBalance = balance; // Capture current balance
        ThreadPoolManager.getIOPool().submit(() -> {
            try (FileWriter writer = new FileWriter(SAVE_FILE)) {
                JsonObject json = new JsonObject();
                json.addProperty("balance", currentBalance);
                json.addProperty("lastUpdated", System.currentTimeMillis());
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            } catch (IOException e) {
                LOGGER.error("Error saving state treasury", e);
            }
        });
    }

    /**
     * Lädt Staatskassen-Saldo
     */
    public static void load() {
        if (!SAVE_FILE.exists()) {
            LOGGER.info("State treasury: New treasury created with {}€", balance);
            save();
            return;
        }

        try (FileReader reader = new FileReader(SAVE_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            balance = json.get("balance").getAsInt();
            LOGGER.info("State treasury: Loaded with {}€", balance);
        } catch (IOException e) {
            LOGGER.error("Error loading state treasury", e);
        }
    }

    /**
     * Setzt Staatskassen-Saldo (Admin)
     */
    public static void setBalance(int newBalance) {
        int oldBalance = balance;
        balance = newBalance;
        LOGGER.info("State treasury: Balance changed from {}€ to {}€", oldBalance, balance);
        save();
    }
}
