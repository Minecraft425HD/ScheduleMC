package de.rolandsw.schedulemc.vehicle.fuel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.ScheduleMC;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages fuel bills for the new ECS-based vehicle system.
 * Tracks unpaid fuel bills per player per gas station.
 */
public class FuelBillManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "fuel_bills.json";
    private static File saveFile;
    private static boolean isDirty = false;

    // Map: PlayerUUID -> List of UnpaidBills
    private static final Map<UUID, List<UnpaidBill>> playerBills = new HashMap<>();

    /**
     * Represents an unpaid fuel bill
     */
    public static class UnpaidBill {
        public UUID gasStationId;
        public UUID playerUUID;
        public int amountFueled; // in mB (millibuckets)
        public double totalCost;  // in currency
        public long timestamp;    // when the bill was created
        public boolean paid = false;

        public UnpaidBill(UUID gasStationId, UUID playerUUID, int amountFueled, double totalCost) {
            this.gasStationId = gasStationId;
            this.playerUUID = playerUUID;
            this.amountFueled = amountFueled;
            this.totalCost = totalCost;
            this.timestamp = System.currentTimeMillis();
        }

        // Default constructor for GSON
        public UnpaidBill() {
        }
    }

    /**
     * Initialize the save file location
     */
    public static void init(File worldSaveFolder) {
        saveFile = new File(worldSaveFolder, FILE_NAME);
    }

    /**
     * Load fuel bills from disk
     */
    public static void load() {
        if (saveFile == null) {
            ScheduleMC.LOGGER.error("FuelBillManager not initialized! Call init() first.");
            return;
        }

        playerBills.clear();

        if (!saveFile.exists()) {
            ScheduleMC.LOGGER.info("No fuel bills file found, starting fresh.");
            return;
        }

        try (FileReader reader = new FileReader(saveFile)) {
            Type type = new TypeToken<Map<UUID, List<UnpaidBill>>>() {}.getType();
            Map<UUID, List<UnpaidBill>> loaded = GSON.fromJson(reader, type);

            if (loaded != null) {
                playerBills.putAll(loaded);
                ScheduleMC.LOGGER.info("Loaded {} player fuel bill records.", playerBills.size());
            }
        } catch (Exception e) {
            ScheduleMC.LOGGER.error("Failed to load fuel bills!", e);
        }

        isDirty = false;
    }

    /**
     * Save fuel bills to disk
     */
    public static void save() {
        if (saveFile == null) {
            ScheduleMC.LOGGER.error("FuelBillManager not initialized! Call init() first.");
            return;
        }

        try {
            saveFile.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(saveFile)) {
                GSON.toJson(playerBills, writer);
                ScheduleMC.LOGGER.info("Saved {} player fuel bill records.", playerBills.size());
            }
        } catch (Exception e) {
            ScheduleMC.LOGGER.error("Failed to save fuel bills!", e);
        }

        isDirty = false;
    }

    /**
     * Save only if there are unsaved changes
     */
    public static void saveIfNeeded() {
        if (isDirty) {
            save();
        }
    }

    /**
     * Add a new unpaid bill for a player
     */
    public static void addBill(UUID playerUUID, UUID gasStationId, int amountFueled, double cost) {
        UnpaidBill bill = new UnpaidBill(gasStationId, playerUUID, amountFueled, cost);

        playerBills.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(bill);
        isDirty = true;

        ScheduleMC.LOGGER.debug("Added fuel bill for player {} at station {}: {}mB for {}€",
            playerUUID, gasStationId, amountFueled, cost);
    }

    /**
     * Get all unpaid bills for a player
     */
    public static List<UnpaidBill> getUnpaidBills(UUID playerUUID) {
        return playerBills.getOrDefault(playerUUID, Collections.emptyList())
            .stream()
            .filter(bill -> !bill.paid)
            .collect(Collectors.toList());
    }

    /**
     * Get unpaid bills for a player at a specific gas station
     */
    public static List<UnpaidBill> getUnpaidBills(UUID playerUUID, UUID gasStationId) {
        return playerBills.getOrDefault(playerUUID, Collections.emptyList())
            .stream()
            .filter(bill -> !bill.paid && bill.gasStationId.equals(gasStationId))
            .collect(Collectors.toList());
    }

    /**
     * Get total unpaid amount for a player
     */
    public static double getTotalUnpaidAmount(UUID playerUUID) {
        return getUnpaidBills(playerUUID).stream()
            .mapToDouble(bill -> bill.totalCost)
            .sum();
    }

    /**
     * Get total unpaid amount for a player at a specific gas station
     */
    public static double getTotalUnpaidAmount(UUID playerUUID, UUID gasStationId) {
        return getUnpaidBills(playerUUID, gasStationId).stream()
            .mapToDouble(bill -> bill.totalCost)
            .sum();
    }

    /**
     * Mark all bills for a player at a gas station as paid
     */
    public static void payBills(UUID playerUUID, UUID gasStationId) {
        List<UnpaidBill> bills = playerBills.get(playerUUID);
        if (bills != null) {
            bills.stream()
                .filter(bill -> bill.gasStationId.equals(gasStationId) && !bill.paid)
                .forEach(bill -> bill.paid = true);
            isDirty = true;
        }
    }

    /**
     * Mark all bills for a player as paid
     */
    public static void payAllBills(UUID playerUUID) {
        List<UnpaidBill> bills = playerBills.get(playerUUID);
        if (bills != null) {
            bills.stream()
                .filter(bill -> !bill.paid)
                .forEach(bill -> bill.paid = true);
            isDirty = true;
        }
    }

    /**
     * Remove old paid bills (cleanup)
     */
    public static void cleanupOldBills(long maxAgeMillis) {
        long cutoffTime = System.currentTimeMillis() - maxAgeMillis;

        playerBills.values().forEach(bills ->
            bills.removeIf(bill -> bill.paid && bill.timestamp < cutoffTime)
        );

        // Remove empty player entries
        playerBills.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        isDirty = true;
    }
}
