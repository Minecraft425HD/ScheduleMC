package de.rolandsw.schedulemc.vehicle.fuel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Verwaltet alle Tankrechnungen
 */
public class FuelBillManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File BILLS_FILE = new File("config/fuel_bills.json");

    // PlayerUUID → List<UnpaidBill>
    private static Map<UUID, List<UnpaidBill>> playerBills = new HashMap<>();
    private static boolean isDirty = false;

    /**
     * Lädt Rechnungen vom Disk
     */
    public static void load() {
        if (!BILLS_FILE.exists()) {
            LOGGER.info("Keine Tankrechnungen gefunden, starte mit leerer Datenbank");
            return;
        }

        try (FileReader reader = new FileReader(BILLS_FILE)) {
            Map<String, List<UnpaidBill>> loaded = GSON.fromJson(reader,
                new TypeToken<Map<String, List<UnpaidBill>>>(){}.getType());

            if (loaded != null) {
                playerBills.clear();
                for (Map.Entry<String, List<UnpaidBill>> entry : loaded.entrySet()) {
                    playerBills.put(UUID.fromString(entry.getKey()), entry.getValue());
                }
                LOGGER.info("Tankrechnungen geladen: {} Spieler", playerBills.size());
            }
        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden der Tankrechnungen!", e);
        }
    }

    /**
     * Speichert Rechnungen auf Disk
     */
    public static void save() {
        BILLS_FILE.getParentFile().mkdirs(); // Erstelle config-Ordner falls nicht vorhanden
        try (FileWriter writer = new FileWriter(BILLS_FILE)) {
            Map<String, List<UnpaidBill>> toSave = new HashMap<>();
            for (Map.Entry<UUID, List<UnpaidBill>> entry : playerBills.entrySet()) {
                toSave.put(entry.getKey().toString(), entry.getValue());
            }
            GSON.toJson(toSave, writer);
            isDirty = false;
            LOGGER.info("Tankrechnungen gespeichert");
        } catch (Exception e) {
            LOGGER.error("Fehler beim Speichern der Tankrechnungen!", e);
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
     * Erstellt eine neue Rechnung
     */
    public static void createBill(UUID playerUUID, UUID fuelStationId, int amountFueled, double totalCost) {
        UnpaidBill bill = new UnpaidBill(fuelStationId, playerUUID, amountFueled, totalCost, System.currentTimeMillis());

        List<UnpaidBill> bills = playerBills.computeIfAbsent(playerUUID, k -> new ArrayList<>());
        bills.add(bill);
        isDirty = true;

        LOGGER.info("Rechnung erstellt: Player={}, Station={}, Amount={} mB, Cost={}€",
            playerUUID, fuelStationId, amountFueled, totalCost);
    }

    /**
     * Gibt alle unbezahlten Rechnungen eines Spielers zurück
     */
    public static List<UnpaidBill> getUnpaidBills(UUID playerUUID) {
        return playerBills.getOrDefault(playerUUID, new ArrayList<>())
            .stream()
            .filter(b -> !b.paid)
            .collect(Collectors.toList());
    }

    /**
     * Gibt alle unbezahlten Rechnungen für eine bestimmte Zapfsäule zurück
     */
    public static List<UnpaidBill> getUnpaidBills(UUID playerUUID, UUID fuelStationId) {
        return getUnpaidBills(playerUUID)
            .stream()
            .filter(b -> b.fuelStationId.equals(fuelStationId))
            .collect(Collectors.toList());
    }

    /**
     * Berechnet die Gesamtsumme aller unbezahlten Rechnungen
     */
    public static double getTotalUnpaidAmount(UUID playerUUID) {
        return getUnpaidBills(playerUUID)
            .stream()
            .mapToDouble(b -> b.totalCost)
            .sum();
    }

    /**
     * Berechnet die Gesamtsumme für eine bestimmte Zapfsäule
     */
    public static double getTotalUnpaidAmount(UUID playerUUID, UUID fuelStationId) {
        return getUnpaidBills(playerUUID, fuelStationId)
            .stream()
            .mapToDouble(b -> b.totalCost)
            .sum();
    }

    /**
     * Markiert alle Rechnungen eines Spielers für eine Zapfsäule als bezahlt
     */
    public static void payBills(UUID playerUUID, UUID fuelStationId) {
        List<UnpaidBill> bills = playerBills.get(playerUUID);
        if (bills != null) {
            bills.stream()
                .filter(b -> b.fuelStationId.equals(fuelStationId) && !b.paid)
                .forEach(b -> b.paid = true);
            isDirty = true;
        }
    }

    /**
     * Löscht alte bezahlte Rechnungen (älter als 7 Tage)
     */
    public static void cleanupOldBills() {
        long weekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);

        for (List<UnpaidBill> bills : playerBills.values()) {
            bills.removeIf(b -> b.paid && b.timestamp < weekAgo);
        }
        isDirty = true;
    }

    /**
     * Repräsentiert eine Tankrechnung
     */
    public static class UnpaidBill {
        public UUID fuelStationId;
        public UUID playerUUID;
        public int amountFueled;  // mB
        public double totalCost;
        public long timestamp;
        public boolean paid;

        public UnpaidBill(UUID fuelStationId, UUID playerUUID, int amountFueled, double totalCost, long timestamp) {
            this.fuelStationId = fuelStationId;
            this.playerUUID = playerUUID;
            this.amountFueled = amountFueled;
            this.totalCost = totalCost;
            this.timestamp = timestamp;
            this.paid = false;
        }

        // No-arg constructor für GSON
        public UnpaidBill() {
        }
    }
}
