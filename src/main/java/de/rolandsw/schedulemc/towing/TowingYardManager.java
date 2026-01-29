package de.rolandsw.schedulemc.towing;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages towing yard parking spots
 */
public class TowingYardManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    // MEMORY LEAK PREVENTION: Size limits for all collections
    /**
     * Maximum parking spots (50000 entries)
     * Prevents unbounded growth - reasonable limit for towing yard parking system
     */
    private static final int MAX_PARKING_SPOTS = 50000;

    /**
     * Maximum invoices per player (100 entries)
     * Prevents unbounded growth - reasonable limit for unpaid invoices
     */
    private static final int MAX_UNPAID_INVOICES = 10000;

    /**
     * Maximum transactions per towing yard (1000 entries)
     * Prevents unbounded growth - keeps only recent transactions
     */
    private static final int MAX_TRANSACTIONS_PER_YARD = 1000;

    private static final Map<UUID, TowingYardParkingSpot> parkingSpots = new ConcurrentHashMap<>();
    private static final Map<String, List<TowingTransaction>> towingTransactions = new ConcurrentHashMap<>();
    private static final Map<UUID, TowingInvoiceData> unpaidInvoices = new ConcurrentHashMap<>();
    private static final File file = new File("config/plotmod_towing_parking_spots.json");
    private static final Gson gson = GsonHelper.get();

    private static final ParkingSpotPersistenceManager persistence =
        new ParkingSpotPersistenceManager(file, gson);

    public static void load() {
        persistence.load();
    }

    public static void save() {
        persistence.save();
    }

    public static void saveIfNeeded() {
        persistence.saveIfNeeded();
    }

    private static void markDirty() {
        persistence.markDirty();
    }

    /**
     * Adds a new parking spot
     * MEMORY LEAK PREVENTION: Enforces size limit on parking spots
     */
    public static UUID addParkingSpot(BlockPos location, String towingYardPlotId) {
        // SIZE CHECK before adding
        if (parkingSpots.size() >= MAX_PARKING_SPOTS) {
            LOGGER.error("Parking spots limit reached ({}), cannot add new spot at {} for yard {}",
                MAX_PARKING_SPOTS, location, towingYardPlotId);
            throw new IllegalStateException("Maximum parking spots limit reached: " + MAX_PARKING_SPOTS);
        }

        UUID spotId = UUID.randomUUID();
        TowingYardParkingSpot spot = new TowingYardParkingSpot(spotId, location, towingYardPlotId);
        parkingSpots.put(spotId, spot);
        markDirty();
        LOGGER.info("Added parking spot {} at {} for towing yard {}", spotId, location, towingYardPlotId);
        return spotId;
    }

    /**
     * Removes a parking spot
     */
    public static void removeParkingSpot(UUID spotId) {
        parkingSpots.remove(spotId);
        markDirty();
        LOGGER.info("Removed parking spot {}", spotId);
    }

    /**
     * Finds a free parking spot at a specific towing yard
     */
    @Nullable
    public static TowingYardParkingSpot findFreeSpot(String towingYardPlotId) {
        return parkingSpots.values().stream()
            .filter(spot -> spot.getTowingYardPlotId().equals(towingYardPlotId))
            .filter(spot -> !spot.isOccupied())
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets all towing yards (distinct plot IDs with at least one spot)
     */
    public static List<String> getAllTowingYards() {
        return parkingSpots.values().stream()
            .map(TowingYardParkingSpot::getTowingYardPlotId)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * Gets parking spots at a towing yard
     */
    public static List<TowingYardParkingSpot> getSpotsAtYard(String towingYardPlotId) {
        return parkingSpots.values().stream()
            .filter(spot -> spot.getTowingYardPlotId().equals(towingYardPlotId))
            .collect(Collectors.toList());
    }

    /**
     * Counts free spots at a towing yard
     */
    public static int countFreeSpots(String towingYardPlotId) {
        return (int) parkingSpots.values().stream()
            .filter(spot -> spot.getTowingYardPlotId().equals(towingYardPlotId))
            .filter(spot -> !spot.isOccupied())
            .count();
    }

    /**
     * Counts total spots at a towing yard
     */
    public static int countTotalSpots(String towingYardPlotId) {
        return (int) parkingSpots.values().stream()
            .filter(spot -> spot.getTowingYardPlotId().equals(towingYardPlotId))
            .count();
    }

    /**
     * Gets vehicles owned by a player across all towing yards
     */
    public static List<TowingYardParkingSpot> getPlayerVehicles(UUID playerId) {
        return parkingSpots.values().stream()
            .filter(TowingYardParkingSpot::isOccupied)
            .filter(spot -> playerId.equals(spot.getOwnerPlayerId()))
            .collect(Collectors.toList());
    }

    /**
     * Gets a parking spot by ID
     */
    @Nullable
    public static TowingYardParkingSpot getSpot(UUID spotId) {
        return parkingSpots.get(spotId);
    }

    /**
     * Calculates towing cost based on distance
     */
    public static double calculateTowingCost(double distance) {
        double baseFee = ModConfigHandler.VEHICLE_SERVER.towingBaseFee.get();
        double distanceFee = ModConfigHandler.VEHICLE_SERVER.towingDistanceFeePerBlock.get();
        return baseFee + (distance * distanceFee);
    }

    @Nullable
    public static String getLastError() {
        return persistence.getLastError();
    }

    public static boolean isHealthy() {
        return persistence.isHealthy();
    }

    // ═══════════════════════════════════════════════════════════
    // TOWING TRANSACTION TRACKING (Warehouse Integration)
    // ═══════════════════════════════════════════════════════════

    /**
     * Records a towing transaction for revenue tracking
     * MEMORY LEAK PREVENTION: Enforces size limit per towing yard
     */
    public static void recordTransaction(long timestamp, UUID playerId, UUID vehicleId, String towingYardPlotId,
                                          double totalCost, double playerPaid, MembershipTier membershipTier) {
        // Calculate yard revenue (player paid amount goes to the yard)
        double yardRevenue = playerPaid;

        TowingTransaction transaction = new TowingTransaction(
            timestamp, playerId, vehicleId, towingYardPlotId,
            totalCost, playerPaid, yardRevenue, membershipTier
        );

        List<TowingTransaction> transactions = towingTransactions.computeIfAbsent(
            towingYardPlotId, k -> new ArrayList<>());

        // SIZE CHECK: Limit transactions per yard
        if (transactions.size() >= MAX_TRANSACTIONS_PER_YARD) {
            // Remove oldest transaction
            transactions.remove(0);
            LOGGER.warn("Transaction limit reached for yard {} ({}), removed oldest entry",
                towingYardPlotId, MAX_TRANSACTIONS_PER_YARD);
        }

        transactions.add(transaction);
        markDirty();
        LOGGER.info("Recorded towing transaction: {} → yard {} (revenue: {}€)", playerId, towingYardPlotId, yardRevenue);
    }

    /**
     * Gets all transactions for a specific towing yard
     */
    public static List<TowingTransaction> getTransactions(String towingYardPlotId) {
        return new ArrayList<>(towingTransactions.getOrDefault(towingYardPlotId, new ArrayList<>()));
    }

    /**
     * Gets total revenue for a towing yard in the last X days
     */
    public static double getTotalRevenue(String towingYardPlotId, long currentTime, int days) {
        List<TowingTransaction> transactions = towingTransactions.getOrDefault(towingYardPlotId, new ArrayList<>());
        return transactions.stream()
            .filter(t -> !t.isOlderThan(currentTime, days))
            .mapToDouble(TowingTransaction::getYardRevenue)
            .sum();
    }

    /**
     * Gets number of towing operations in the last X days
     */
    public static int getTowingCount(String towingYardPlotId, long currentTime, int days) {
        List<TowingTransaction> transactions = towingTransactions.getOrDefault(towingYardPlotId, new ArrayList<>());
        return (int) transactions.stream()
            .filter(t -> !t.isOlderThan(currentTime, days))
            .count();
    }

    /**
     * Gets average revenue per towing operation
     */
    public static double getAverageRevenue(String towingYardPlotId, long currentTime, int days) {
        List<TowingTransaction> transactions = towingTransactions.getOrDefault(towingYardPlotId, new ArrayList<>());
        List<TowingTransaction> recent = transactions.stream()
            .filter(t -> !t.isOlderThan(currentTime, days))
            .toList();

        if (recent.isEmpty()) {
            return 0.0;
        }

        double total = recent.stream().mapToDouble(TowingTransaction::getYardRevenue).sum();
        return total / recent.size();
    }

    /**
     * Cleans up old transactions (older than 30 days)
     */
    public static void cleanupOldTransactions(long currentTime) {
        for (List<TowingTransaction> transactions : towingTransactions.values()) {
            transactions.removeIf(t -> t.isOlderThan(currentTime, 30));
        }
        markDirty();
    }

    // ═══════════════════════════════════════════════════════════
    // TOWING INVOICE MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Creates a new invoice for a towed vehicle
     * MEMORY LEAK PREVENTION: Enforces size limit on unpaid invoices
     */
    public static TowingInvoiceData createInvoice(UUID playerId, UUID vehicleId, String towingYardPlotId,
                                                    double amount, long timestamp) {
        // SIZE CHECK before adding
        if (unpaidInvoices.size() >= MAX_UNPAID_INVOICES) {
            // Find and remove oldest invoice
            unpaidInvoices.entrySet().stream()
                .min(Comparator.comparingLong(e -> e.getValue().getTimestamp()))
                .ifPresent(oldest -> {
                    unpaidInvoices.remove(oldest.getKey());
                    LOGGER.warn("Unpaid invoices limit reached ({}), removed oldest invoice: {}",
                        MAX_UNPAID_INVOICES, oldest.getKey());
                });
        }

        TowingInvoiceData invoice = new TowingInvoiceData(playerId, vehicleId, towingYardPlotId, amount, timestamp);
        unpaidInvoices.put(invoice.getInvoiceId(), invoice);
        markDirty();
        LOGGER.info("Created towing invoice {} for player {} ({}€)", invoice.getInvoiceId(), playerId, amount);
        return invoice;
    }

    /**
     * Gets an unpaid invoice by vehicle ID
     */
    @Nullable
    public static TowingInvoiceData getUnpaidInvoice(UUID playerId, UUID vehicleId) {
        return unpaidInvoices.values().stream()
            .filter(i -> i.getPlayerId().equals(playerId))
            .filter(i -> i.getVehicleId().equals(vehicleId))
            .filter(i -> !i.isPaid())
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets an unpaid invoice by invoice ID
     */
    @Nullable
    public static TowingInvoiceData getInvoice(UUID invoiceId) {
        return unpaidInvoices.get(invoiceId);
    }

    /**
     * Gets all unpaid invoices for a player
     */
    public static List<TowingInvoiceData> getUnpaidInvoices(UUID playerId) {
        return unpaidInvoices.values().stream()
            .filter(i -> i.getPlayerId().equals(playerId))
            .filter(i -> !i.isPaid())
            .toList();
    }

    /**
     * Marks an invoice as paid
     */
    public static void payInvoice(UUID invoiceId) {
        TowingInvoiceData invoice = unpaidInvoices.get(invoiceId);
        if (invoice != null) {
            invoice.markAsPaid();
            unpaidInvoices.remove(invoiceId);
            markDirty();
            LOGGER.info("Invoice {} paid ({}€)", invoiceId, invoice.getAmount());
        }
    }

    /**
     * Clears ALL static data. Called on server shutdown.
     * MEMORY LEAK PREVENTION: Ensures no data persists across sessions.
     * Clears all 3 static collections (parking spots, transactions, invoices).
     */
    public static void clearAll() {
        int spotCount = parkingSpots.size();
        int transactionCount = towingTransactions.values().stream()
            .mapToInt(List::size)
            .sum();
        int invoiceCount = unpaidInvoices.size();

        parkingSpots.clear();
        towingTransactions.clear();
        unpaidInvoices.clear();

        if (spotCount > 0 || transactionCount > 0 || invoiceCount > 0) {
            LOGGER.info("[TowingYardManager] Cleared all static data: {} parking spots, {} transactions, {} invoices",
                spotCount, transactionCount, invoiceCount);
        }
    }

    /**
     * Data class for JSON serialization
     */
    private static class ParkingSpotSaveData {
        String locationX;
        String locationY;
        String locationZ;
        String towingYardPlotId;
        boolean occupied;
        String vehicleEntityId;
        String ownerPlayerId;
        double owedAmount;
        int originalDamage;
        long towedTimestamp;
        boolean engineWasRunning;
    }

    /**
     * Persistence manager
     */
    private static class ParkingSpotPersistenceManager extends AbstractPersistenceManager<Map<String, ParkingSpotSaveData>> {

        public ParkingSpotPersistenceManager(File dataFile, Gson gson) {
            super(dataFile, gson);
        }

        @Override
        protected Type getDataType() {
            return new TypeToken<Map<String, ParkingSpotSaveData>>(){}.getType();
        }

        @Override
        protected void onDataLoaded(Map<String, ParkingSpotSaveData> data) {
            parkingSpots.clear();

            int invalidCount = 0;
            int correctedCount = 0;

            // NULL CHECK
            if (data == null) {
                LOGGER.warn("Null data loaded for parking spots");
                invalidCount++;
                return;
            }

            // Check collection size
            if (data.size() > 10000) {
                LOGGER.warn("Parking spots map size ({}) exceeds limit, potential corruption",
                    data.size());
                correctedCount++;
            }

            data.forEach((spotIdStr, saveData) -> {
                try {
                    // VALIDATE UUID STRING
                    if (spotIdStr == null || spotIdStr.isEmpty()) {
                        LOGGER.warn("Null/empty spot ID string, skipping");
                        return;
                    }

                    // NULL CHECK
                    if (saveData == null) {
                        LOGGER.warn("Null parking spot data for ID {}, skipping", spotIdStr);
                        return;
                    }

                    UUID spotId;
                    try {
                        spotId = UUID.fromString(spotIdStr);
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Invalid spot UUID: {}", spotIdStr, e);
                        return;
                    }

                    // VALIDATE TOWING YARD PLOT ID
                    if (saveData.towingYardPlotId == null || saveData.towingYardPlotId.isEmpty()) {
                        LOGGER.warn("Spot {} has null/empty towing yard plot ID, skipping", spotId);
                        return;
                    }

                    // VALIDATE LOCATION COORDINATES
                    if (saveData.locationX == null || saveData.locationY == null || saveData.locationZ == null) {
                        LOGGER.warn("Spot {} has null location coordinates, skipping", spotId);
                        return;
                    }

                    BlockPos location;
                    try {
                        location = new BlockPos(
                            Integer.parseInt(saveData.locationX),
                            Integer.parseInt(saveData.locationY),
                            Integer.parseInt(saveData.locationZ)
                        );
                    } catch (NumberFormatException e) {
                        LOGGER.error("Invalid location coordinates for spot {}: {}, {}, {}",
                            spotId, saveData.locationX, saveData.locationY, saveData.locationZ);
                        return;
                    }

                    String towingYardPlotId = saveData.towingYardPlotId;

                    TowingYardParkingSpot spot = new TowingYardParkingSpot(spotId, location, towingYardPlotId);

                    if (saveData.occupied && saveData.vehicleEntityId != null && saveData.ownerPlayerId != null) {
                        try {
                            UUID vehicleId = UUID.fromString(saveData.vehicleEntityId);
                            UUID ownerId = UUID.fromString(saveData.ownerPlayerId);

                            // VALIDATE OWED AMOUNT (>= 0)
                            if (saveData.owedAmount < 0) {
                                LOGGER.warn("Spot {} has negative owed amount {}, resetting to 0",
                                    spotId, saveData.owedAmount);
                                saveData.owedAmount = 0;
                            }

                            // VALIDATE ORIGINAL DAMAGE (0-1)
                            if (saveData.originalDamage < 0 || saveData.originalDamage > 1) {
                                LOGGER.warn("Spot {} has invalid original damage {}, clamping to 0-1",
                                    spotId, saveData.originalDamage);
                                saveData.originalDamage = Math.max(0, Math.min(1, saveData.originalDamage));
                            }

                            // VALIDATE TOWED TIMESTAMP (>= 0)
                            if (saveData.towedTimestamp < 0) {
                                LOGGER.warn("Spot {} has negative towed timestamp {}, resetting to 0",
                                    spotId, saveData.towedTimestamp);
                                saveData.towedTimestamp = 0;
                            }

                            spot.parkVehicle(
                                vehicleId,
                                ownerId,
                                saveData.owedAmount,
                                saveData.originalDamage,
                                saveData.engineWasRunning
                            );
                        } catch (IllegalArgumentException e) {
                            LOGGER.error("Invalid vehicle/owner UUID for spot {}: vehicle={}, owner={}",
                                spotId, saveData.vehicleEntityId, saveData.ownerPlayerId, e);
                        }
                    }

                    parkingSpots.put(spotId, spot);
                } catch (IllegalArgumentException | NullPointerException e) {
                    LOGGER.error("Invalid parking spot data: {}", spotIdStr, e);
                }
            });

            // SUMMARY
            if (invalidCount > 0 || correctedCount > 0) {
                LOGGER.warn("Data validation: {} invalid entries, {} corrected entries",
                    invalidCount, correctedCount);
                if (correctedCount > 0) {
                    markDirty(); // Re-save corrected data
                }
            }
        }

        @Override
        protected Map<String, ParkingSpotSaveData> getCurrentData() {
            Map<String, ParkingSpotSaveData> saveMap = new HashMap<>();

            parkingSpots.forEach((spotId, spot) -> {
                ParkingSpotSaveData saveData = new ParkingSpotSaveData();
                saveData.locationX = String.valueOf(spot.getLocation().getX());
                saveData.locationY = String.valueOf(spot.getLocation().getY());
                saveData.locationZ = String.valueOf(spot.getLocation().getZ());
                saveData.towingYardPlotId = spot.getTowingYardPlotId();
                saveData.occupied = spot.isOccupied();

                if (spot.isOccupied()) {
                    saveData.vehicleEntityId = spot.getVehicleEntityId().toString();
                    saveData.ownerPlayerId = spot.getOwnerPlayerId().toString();
                    saveData.owedAmount = spot.getOwedAmount();
                    saveData.originalDamage = spot.getOriginalDamage();
                    saveData.towedTimestamp = spot.getTowedTimestamp();
                    saveData.engineWasRunning = spot.wasEngineRunning();
                }

                saveMap.put(spotId.toString(), saveData);
            });

            return saveMap;
        }

        @Override
        protected String getComponentName() {
            return "Towing Yard Parking System";
        }

        @Override
        protected String getHealthDetails() {
            long occupiedCount = parkingSpots.values().stream()
                .filter(TowingYardParkingSpot::isOccupied)
                .count();
            return String.format("%d spots total, %d occupied", parkingSpots.size(), occupiedCount);
        }

        @Override
        protected void onCriticalLoadFailure() {
            parkingSpots.clear();
        }
    }
}
