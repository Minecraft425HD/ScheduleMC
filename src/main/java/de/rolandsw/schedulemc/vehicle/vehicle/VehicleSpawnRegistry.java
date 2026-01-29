package de.rolandsw.schedulemc.vehicle.vehicle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Verwaltet Fahrzeug-Spawn-Punkte für Autohändler
 * SICHERHEIT: Thread-safe Collections für concurrent access
 */
public class VehicleSpawnRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File SPAWN_FILE = new File("config/vehicle_spawns.json");

    // SICHERHEIT: ConcurrentHashMap mit CopyOnWriteArrayList für Thread-safe Zugriff
    // UUID (Dealer NPC ID) → List<VehicleSpawnPoint>
    private static Map<UUID, List<VehicleSpawnPoint>> dealerSpawnPoints = new ConcurrentHashMap<>();
    // SICHERHEIT: volatile für Memory Visibility
    private static volatile boolean isDirty = false;

    /**
     * Lädt Spawn-Punkte vom Disk
     */
    public static void load() {
        if (!SPAWN_FILE.exists()) {
            LOGGER.info("Keine Fahrzeug-Spawn-Daten gefunden, starte mit leerer Datenbank");
            return;
        }

        try (FileReader reader = new FileReader(SPAWN_FILE)) {
            Map<String, List<VehicleSpawnPoint>> loaded = GSON.fromJson(reader,
                new TypeToken<Map<String, List<VehicleSpawnPoint>>>(){}.getType());

            if (loaded != null) {
                dealerSpawnPoints.clear();
                for (Map.Entry<String, List<VehicleSpawnPoint>> entry : loaded.entrySet()) {
                    dealerSpawnPoints.put(UUID.fromString(entry.getKey()), new CopyOnWriteArrayList<>(entry.getValue()));
                }
                LOGGER.info("Fahrzeug-Spawn-Punkte geladen: {} Händler", dealerSpawnPoints.size());
            }
        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden der Fahrzeug-Spawn-Punkte!", e);
        }
    }

    /**
     * Speichert Spawn-Punkte auf Disk
     */
    public static void save() {
        SPAWN_FILE.getParentFile().mkdirs(); // Erstelle config-Ordner falls nicht vorhanden
        try (FileWriter writer = new FileWriter(SPAWN_FILE)) {
            Map<String, List<VehicleSpawnPoint>> toSave = new HashMap<>();
            for (Map.Entry<UUID, List<VehicleSpawnPoint>> entry : dealerSpawnPoints.entrySet()) {
                toSave.put(entry.getKey().toString(), entry.getValue());
            }
            GSON.toJson(toSave, writer);
            isDirty = false;
            LOGGER.info("Fahrzeug-Spawn-Punkte gespeichert");
        } catch (Exception e) {
            LOGGER.error("Fehler beim Speichern der Fahrzeug-Spawn-Punkte!", e);
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
     * Fügt einen Spawn-Punkt für einen Händler hinzu
     */
    public static void addSpawnPoint(UUID dealerId, BlockPos position, float yaw) {
        List<VehicleSpawnPoint> points = dealerSpawnPoints.computeIfAbsent(dealerId, k -> new CopyOnWriteArrayList<>());
        points.add(new VehicleSpawnPoint(position, yaw));
        isDirty = true;
    }

    /**
     * Entfernt einen Spawn-Punkt
     */
    public static void removeSpawnPoint(UUID dealerId, BlockPos position) {
        List<VehicleSpawnPoint> points = dealerSpawnPoints.get(dealerId);
        if (points != null) {
            points.removeIf(p -> p.position.equals(position));
            isDirty = true;
        }
    }

    /**
     * Gibt alle Spawn-Punkte eines Händlers zurück
     */
    public static List<VehicleSpawnPoint> getSpawnPoints(UUID dealerId) {
        return dealerSpawnPoints.getOrDefault(dealerId, new ArrayList<>());
    }

    /**
     * Prüft ob ein Spawn-Punkt frei ist
     */
    public static boolean isSpawnPointFree(BlockPos position) {
        for (List<VehicleSpawnPoint> points : dealerSpawnPoints.values()) {
            for (VehicleSpawnPoint point : points) {
                if (point.position.equals(position)) {
                    return !point.occupied;
                }
            }
        }
        return false;
    }

    /**
     * Findet einen freien Spawn-Punkt für einen Händler
     */
    public static VehicleSpawnPoint findFreeSpawnPoint(UUID dealerId) {
        List<VehicleSpawnPoint> points = getSpawnPoints(dealerId);
        for (VehicleSpawnPoint point : points) {
            if (!point.occupied) {
                return point;
            }
        }
        return null;
    }

    /**
     * Belegt einen Spawn-Punkt
     */
    public static void occupySpawnPoint(BlockPos position, UUID vehicleId) {
        for (List<VehicleSpawnPoint> points : dealerSpawnPoints.values()) {
            for (VehicleSpawnPoint point : points) {
                if (point.position.equals(position)) {
                    point.occupied = true;
                    point.occupyingVehicleId = vehicleId;
                    isDirty = true;
                    return;
                }
            }
        }
    }

    /**
     * Gibt einen Spawn-Punkt frei
     */
    public static void releaseSpawnPoint(UUID vehicleId) {
        for (List<VehicleSpawnPoint> points : dealerSpawnPoints.values()) {
            for (VehicleSpawnPoint point : points) {
                if (vehicleId.equals(point.occupyingVehicleId)) {
                    point.occupied = false;
                    point.occupyingVehicleId = null;
                    isDirty = true;
                    return;
                }
            }
        }
    }

    /**
     * Repräsentiert einen Fahrzeug-Spawn-Punkt
     */
    public static class VehicleSpawnPoint {
        private BlockPos position;
        private float yaw;  // Rotation
        private boolean occupied;
        private UUID occupyingVehicleId; // null wenn frei

        public VehicleSpawnPoint(BlockPos position, float yaw) {
            this.position = position;
            this.yaw = yaw;
            this.occupied = false;
            this.occupyingVehicleId = null;
        }

        public BlockPos getPosition() {
            return position;
        }

        public float getYaw() {
            return yaw;
        }

        public boolean isOccupied() {
            return occupied;
        }

        public UUID getOccupyingVehicleId() {
            return occupyingVehicleId;
        }
    }
}
