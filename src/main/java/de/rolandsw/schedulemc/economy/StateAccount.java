package de.rolandsw.schedulemc.economy;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.ThreadPoolManager;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static final AtomicInteger balance = new AtomicInteger(100000); // Start: 100,000€
    private static final File SAVE_FILE = new File("config/state_account.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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
        return balance.get();
    }

    /**
     * Zieht Geld aus der Staatskasse ab (CAS-Loop für Thread-Safety)
     */
    public static boolean withdraw(int amount, String reason) {
        int current;
        do {
            current = balance.get();
            if (current < amount) {
                LOGGER.warn("State treasury: Not enough money for {} ({}€)", reason, amount);
                return false;
            }
        } while (!balance.compareAndSet(current, current - amount));
        LOGGER.info("State treasury: -{}€ ({}), remaining: {}€", amount, reason, balance.get());
        save();
        return true;
    }

    /**
     * Zahlt Geld in die Staatskasse ein
     */
    public static void deposit(int amount, String reason) {
        int newBalance = balance.addAndGet(amount);
        LOGGER.info("State treasury: +{}€ ({}), total: {}€", amount, reason, newBalance);
        save();
    }

    /**
     * Zahlt Geld in die Staatskasse ein (double Überladung für FeeManager)
     */
    public void deposit(double amount, String reason) {
        deposit((int) Math.round(amount), reason);
    }

    /**
     * Speichert Staatskassen-Saldo (Async + Atomic Write)
     */
    public static void save() {
        int currentBalance = balance.get();
        ThreadPoolManager.getIOPool().submit(() -> {
            try {
                SAVE_FILE.getParentFile().mkdirs();
                File tempFile = new File(SAVE_FILE.getParent(), SAVE_FILE.getName() + ".tmp");
                try (FileWriter writer = new FileWriter(tempFile)) {
                    JsonObject json = new JsonObject();
                    json.addProperty("balance", currentBalance);
                    json.addProperty("lastUpdated", System.currentTimeMillis());
                    GSON.toJson(json, writer);
                    writer.flush();
                }
                Files.move(tempFile.toPath(), SAVE_FILE.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
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
            LOGGER.info("State treasury: New treasury created with {}€", balance.get());
            save();
            return;
        }

        try (FileReader reader = new FileReader(SAVE_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            balance.set(json.get("balance").getAsInt());
            LOGGER.info("State treasury: Loaded with {}€", balance.get());
        } catch (IOException e) {
            LOGGER.error("Error loading state treasury", e);
        }
    }

    /**
     * Setzt Staatskassen-Saldo (Admin)
     */
    public static void setBalance(int newBalance) {
        int oldBalance = balance.getAndSet(newBalance);
        LOGGER.info("State treasury: Balance changed from {}€ to {}€", oldBalance, newBalance);
        save();
    }
}
