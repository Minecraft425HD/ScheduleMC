package de.rolandsw.schedulemc.vehicle.fuel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Verwaltet alle Tankrechnungen
 *
 * Nutzt AbstractPersistenceManager für robuste Datenpersistenz
 * SICHERHEIT: Thread-safe Collections für concurrent access
 */
public class FuelBillManager {

    private static final Gson GSON = GsonHelper.get();
    private static final File BILLS_FILE = new File("config/fuel_bills.json");

    // SICHERHEIT: ConcurrentHashMap für Thread-safe Zugriff
    // PlayerUUID → List<UnpaidBill>
    private static Map<UUID, List<UnpaidBill>> playerBills = new ConcurrentHashMap<>();

    // Persistence-Manager (eliminiert ~80 Zeilen Duplikation)
    private static final FuelBillPersistenceManager persistence =
        new FuelBillPersistenceManager(BILLS_FILE, GSON);

    /**
     * Lädt Rechnungen vom Disk
     */
    public static void load() {
        persistence.load();
    }

    /**
     * Speichert Rechnungen auf Disk
     */
    public static void save() {
        persistence.save();
    }

    /**
     * Speichert nur wenn Änderungen vorhanden
     */
    public static void saveIfNeeded() {
        persistence.saveIfNeeded();
    }

    /**
     * Markiert als geändert
     */
    private static void markDirty() {
        persistence.markDirty();
    }

    /**
     * Erstellt eine neue Rechnung
     */
    public static void createBill(UUID playerUUID, UUID fuelStationId, int amountFueled, double totalCost) {
        UnpaidBill bill = new UnpaidBill(fuelStationId, playerUUID, amountFueled, totalCost, System.currentTimeMillis());

        List<UnpaidBill> bills = playerBills.computeIfAbsent(playerUUID, k -> new ArrayList<>());
        bills.add(bill);
        markDirty();
    }

    /**
     * Gibt alle unbezahlten Rechnungen eines Spielers zurück
     */
    public static List<UnpaidBill> getUnpaidBills(UUID playerUUID) {
        List<UnpaidBill> bills = playerBills.get(playerUUID);
        if (bills == null || bills.isEmpty()) {
            return Collections.emptyList();
        }
        return bills.stream()
            .filter(b -> !b.paid)
            .collect(Collectors.toList());
    }

    /**
     * Gibt alle unbezahlten Rechnungen für eine bestimmte Zapfsäule zurück
     * OPTIMIERT: Single-Pass statt Double-Stream
     */
    public static List<UnpaidBill> getUnpaidBills(UUID playerUUID, UUID fuelStationId) {
        List<UnpaidBill> bills = playerBills.get(playerUUID);
        if (bills == null || bills.isEmpty()) {
            return Collections.emptyList();
        }
        // OPTIMIERT: Ein Stream mit beiden Filtern statt zwei separate Streams
        return bills.stream()
            .filter(b -> !b.paid && b.fuelStationId.equals(fuelStationId))
            .collect(Collectors.toList());
    }

    /**
     * Berechnet die Gesamtsumme aller unbezahlten Rechnungen
     * OPTIMIERT: Direkter Zugriff ohne Zwischenliste
     */
    public static double getTotalUnpaidAmount(UUID playerUUID) {
        List<UnpaidBill> bills = playerBills.get(playerUUID);
        if (bills == null || bills.isEmpty()) {
            return 0.0;
        }
        // OPTIMIERT: Direkte Berechnung ohne Zwischenliste
        return bills.stream()
            .filter(b -> !b.paid)
            .mapToDouble(b -> b.totalCost)
            .sum();
    }

    /**
     * Berechnet die Gesamtsumme für eine bestimmte Zapfsäule
     * OPTIMIERT: Single-Pass statt Triple-Stream
     */
    public static double getTotalUnpaidAmount(UUID playerUUID, UUID fuelStationId) {
        List<UnpaidBill> bills = playerBills.get(playerUUID);
        if (bills == null || bills.isEmpty()) {
            return 0.0;
        }
        // OPTIMIERT: Ein Stream mit allen Filtern statt drei separate Streams
        return bills.stream()
            .filter(b -> !b.paid && b.fuelStationId.equals(fuelStationId))
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
            markDirty();
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
        markDirty();
    }

    // ========== HEALTH MONITORING ==========

    /**
     * Gibt Health-Status zurück
     */
    public static boolean isHealthy() {
        return persistence.isHealthy();
    }

    /**
     * Gibt letzte Fehlermeldung zurück
     */
    @Nullable
    public static String getLastError() {
        return persistence.getLastError();
    }

    /**
     * Gibt Health-Info zurück
     */
    public static String getHealthInfo() {
        return persistence.getHealthInfo();
    }

    /**
     * Innere Persistence-Manager-Klasse
     */
    private static class FuelBillPersistenceManager extends AbstractPersistenceManager<Map<String, List<UnpaidBill>>> {

        public FuelBillPersistenceManager(File dataFile, Gson gson) {
            super(dataFile, gson);
        }

        @Override
        protected Type getDataType() {
            return new TypeToken<Map<String, List<UnpaidBill>>>(){}.getType();
        }

        @Override
        protected void onDataLoaded(Map<String, List<UnpaidBill>> data) {
            playerBills.clear();
            for (Map.Entry<String, List<UnpaidBill>> entry : data.entrySet()) {
                playerBills.put(UUID.fromString(entry.getKey()), entry.getValue());
            }
        }

        @Override
        protected Map<String, List<UnpaidBill>> getCurrentData() {
            Map<String, List<UnpaidBill>> toSave = new HashMap<>();
            for (Map.Entry<UUID, List<UnpaidBill>> entry : playerBills.entrySet()) {
                toSave.put(entry.getKey().toString(), entry.getValue());
            }
            return toSave;
        }

        @Override
        protected String getComponentName() {
            return "Fuel Bill System";
        }

        @Override
        protected String getHealthDetails() {
            int totalBills = playerBills.values().stream().mapToInt(List::size).sum();
            return String.format("%d Players, %d Bills", playerBills.size(), totalBills);
        }

        @Override
        protected void onCriticalLoadFailure() {
            playerBills.clear();
        }
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
