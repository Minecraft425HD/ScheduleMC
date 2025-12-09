package de.rolandsw.schedulemc.vehicle.fuel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.core.BlockPos;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Registry for gas stations in the new ECS-based vehicle system.
 * Tracks gas station locations, names, and owners.
 */
public class GasStationRegistry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "gas_stations.json";
    private static File saveFile;
    private static boolean isDirty = false;

    // Map: GasStationUUID -> GasStationData
    private static final Map<UUID, GasStationData> gasStations = new HashMap<>();

    /**
     * Represents a gas station
     */
    public static class GasStationData {
        public UUID id;
        public String displayName;
        public BlockPos position;
        public String dimension;
        public UUID ownerUUID;
        public double pricePerMb; // Price per millibucket of fuel
        public long createdTime;

        public GasStationData(UUID id, String displayName, BlockPos position, String dimension, UUID ownerUUID, double pricePerMb) {
            this.id = id;
            this.displayName = displayName;
            this.position = position;
            this.dimension = dimension;
            this.ownerUUID = ownerUUID;
            this.pricePerMb = pricePerMb;
            this.createdTime = System.currentTimeMillis();
        }

        // Default constructor for GSON
        public GasStationData() {
        }
    }

    /**
     * Initialize the save file location
     */
    public static void init(File worldSaveFolder) {
        saveFile = new File(worldSaveFolder, FILE_NAME);
    }

    /**
     * Load gas stations from disk
     */
    public static void load() {
        if (saveFile == null) {
            ScheduleMC.LOGGER.error("GasStationRegistry not initialized! Call init() first.");
            return;
        }

        gasStations.clear();

        if (!saveFile.exists()) {
            ScheduleMC.LOGGER.info("No gas stations file found, starting fresh.");
            return;
        }

        try (FileReader reader = new FileReader(saveFile)) {
            Type type = new TypeToken<Map<UUID, GasStationData>>() {}.getType();
            Map<UUID, GasStationData> loaded = GSON.fromJson(reader, type);

            if (loaded != null) {
                gasStations.putAll(loaded);
                ScheduleMC.LOGGER.info("Loaded {} gas stations.", gasStations.size());
            }
        } catch (Exception e) {
            ScheduleMC.LOGGER.error("Failed to load gas stations!", e);
        }

        isDirty = false;
    }

    /**
     * Save gas stations to disk
     */
    public static void save() {
        if (saveFile == null) {
            ScheduleMC.LOGGER.error("GasStationRegistry not initialized! Call init() first.");
            return;
        }

        try {
            saveFile.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(saveFile)) {
                GSON.toJson(gasStations, writer);
                ScheduleMC.LOGGER.info("Saved {} gas stations.", gasStations.size());
            }
        } catch (Exception e) {
            ScheduleMC.LOGGER.error("Failed to save gas stations!", e);
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
     * Register a new gas station
     */
    public static UUID registerGasStation(String displayName, BlockPos position, String dimension, UUID ownerUUID, double pricePerMb) {
        UUID id = UUID.randomUUID();
        GasStationData data = new GasStationData(id, displayName, position, dimension, ownerUUID, pricePerMb);
        gasStations.put(id, data);
        isDirty = true;

        ScheduleMC.LOGGER.info("Registered gas station '{}' at {} in {}", displayName, position, dimension);
        return id;
    }

    /**
     * Unregister a gas station
     */
    public static boolean unregisterGasStation(UUID id) {
        GasStationData removed = gasStations.remove(id);
        if (removed != null) {
            isDirty = true;
            ScheduleMC.LOGGER.info("Unregistered gas station '{}'", removed.displayName);
            return true;
        }
        return false;
    }

    /**
     * Get gas station data by ID
     */
    public static GasStationData getGasStation(UUID id) {
        return gasStations.get(id);
    }

    /**
     * Get display name of a gas station
     */
    public static String getDisplayName(UUID id) {
        GasStationData data = gasStations.get(id);
        return data != null ? data.displayName : "Unknown Station";
    }

    /**
     * Get all gas station IDs
     */
    public static Set<UUID> getAllGasStationIds() {
        return new HashSet<>(gasStations.keySet());
    }

    /**
     * Get all gas stations
     */
    public static Collection<GasStationData> getAllGasStations() {
        return new ArrayList<>(gasStations.values());
    }

    /**
     * Get gas stations owned by a player
     */
    public static List<GasStationData> getGasStationsByOwner(UUID ownerUUID) {
        return gasStations.values().stream()
            .filter(data -> data.ownerUUID.equals(ownerUUID))
            .toList();
    }

    /**
     * Update gas station display name
     */
    public static void setDisplayName(UUID id, String newName) {
        GasStationData data = gasStations.get(id);
        if (data != null) {
            data.displayName = newName;
            isDirty = true;
        }
    }

    /**
     * Update gas station price
     */
    public static void setPricePerMb(UUID id, double pricePerMb) {
        GasStationData data = gasStations.get(id);
        if (data != null) {
            data.pricePerMb = pricePerMb;
            isDirty = true;
        }
    }

    /**
     * Check if a gas station exists
     */
    public static boolean exists(UUID id) {
        return gasStations.containsKey(id);
    }
}
