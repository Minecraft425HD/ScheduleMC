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
    private static final Map<UUID, TowingYardParkingSpot> parkingSpots = new ConcurrentHashMap<>();
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
     */
    public static UUID addParkingSpot(BlockPos location, String towingYardPlotId) {
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
        double baseFee = ModConfigHandler.SERVER.towingBaseFee.get();
        double distanceFee = ModConfigHandler.SERVER.towingDistanceFeePerBlock.get();
        return baseFee + (distance * distanceFee);
    }

    @Nullable
    public static String getLastError() {
        return persistence.getLastError();
    }

    public static boolean isHealthy() {
        return persistence.isHealthy();
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

            data.forEach((spotIdStr, saveData) -> {
                try {
                    UUID spotId = UUID.fromString(spotIdStr);
                    UUID towingYardPlotId = UUID.fromString(saveData.towingYardPlotId);

                    BlockPos location = new BlockPos(
                        Integer.parseInt(saveData.locationX),
                        Integer.parseInt(saveData.locationY),
                        Integer.parseInt(saveData.locationZ)
                    );

                    TowingYardParkingSpot spot = new TowingYardParkingSpot(spotId, location, towingYardPlotId);

                    if (saveData.occupied && saveData.vehicleEntityId != null && saveData.ownerPlayerId != null) {
                        spot.parkVehicle(
                            UUID.fromString(saveData.vehicleEntityId),
                            UUID.fromString(saveData.ownerPlayerId),
                            saveData.owedAmount,
                            saveData.originalDamage,
                            saveData.engineWasRunning
                        );
                    }

                    parkingSpots.put(spotId, spot);
                } catch (IllegalArgumentException | NullPointerException e) {
                    LOGGER.error("Invalid parking spot data: {}", spotIdStr, e);
                }
            });
        }

        @Override
        protected Map<String, ParkingSpotSaveData> getCurrentData() {
            Map<String, ParkingSpotSaveData> saveMap = new HashMap<>();

            parkingSpots.forEach((spotId, spot) -> {
                ParkingSpotSaveData saveData = new ParkingSpotSaveData();
                saveData.locationX = String.valueOf(spot.getLocation().getX());
                saveData.locationY = String.valueOf(spot.getLocation().getY());
                saveData.locationZ = String.valueOf(spot.getLocation().getZ());
                saveData.towingYardPlotId = spot.getTowingYardPlotId().toString();
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
