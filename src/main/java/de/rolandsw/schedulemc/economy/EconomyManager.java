package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.GsonHelper;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zentrales Economy-System für ScheduleMC
 * Verwaltet Spieler-Guthaben mit Thread-Safety und Batch-Saving
 */
public class EconomyManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    private static final File file = new File("config/plotmod_economy.json");
    private static final Gson gson = GsonHelper.get();
    private static boolean needsSave = false;

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
        if (amount < 0) {
            LOGGER.warn("Versuch, negativen Betrag einzuzahlen: {}", amount);
            return;
        }

        balances.put(uuid, getBalance(uuid) + amount);
        markDirty();
        LOGGER.debug("Einzahlung: {} € für {}", amount, uuid);
    }

    /**
     * Hebt Geld von einem Konto ab
     * @return true wenn erfolgreich, false wenn nicht genug Guthaben
     */
    public static boolean withdraw(UUID uuid, double amount) {
        if (amount < 0) {
            LOGGER.warn("Versuch, negativen Betrag abzuheben: {}", amount);
            return false;
        }

        if (getBalance(uuid) >= amount) {
            balances.put(uuid, getBalance(uuid) - amount);
            markDirty();
            LOGGER.debug("Abbuchung: {} € von {}", amount, uuid);
            return true;
        }
        return false;
    }

    /**
     * Setzt das Guthaben eines Spielers (Admin-Funktion)
     */
    public static void setBalance(UUID uuid, double amount) {
        if (amount < 0) {
            amount = 0;
        }
        balances.put(uuid, amount);
        markDirty();
        LOGGER.info("Guthaben gesetzt: {} auf {} €", uuid, amount);
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
}
