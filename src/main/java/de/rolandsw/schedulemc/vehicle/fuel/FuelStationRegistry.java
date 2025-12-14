package de.rolandsw.schedulemc.vehicle.fuel;

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

/**
 * Verwaltet Registrierung und IDs aller Zapfsäulen
 */
public class FuelStationRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File REGISTRY_FILE = new File("config/fuel_station_registry.json");

    // UUID → FuelStationEntry
    private static Map<UUID, FuelStationEntry> fuelStations = new HashMap<>();
    // BlockPos → UUID (für schnelle Lookup)
    private static Map<BlockPos, UUID> positionToId = new HashMap<>();
    private static boolean isDirty = false;

    /**
     * Lädt Registry vom Disk
     */
    public static void load() {
        if (!REGISTRY_FILE.exists()) {
            LOGGER.info("Keine Zapfsäulen-Registry gefunden, starte mit leerer Datenbank");
            return;
        }

        try (FileReader reader = new FileReader(REGISTRY_FILE)) {
            List<FuelStationEntry> loaded = GSON.fromJson(reader,
                new TypeToken<List<FuelStationEntry>>(){}.getType());

            if (loaded != null) {
                fuelStations.clear();
                positionToId.clear();
                for (FuelStationEntry entry : loaded) {
                    fuelStations.put(entry.id, entry);
                    positionToId.put(entry.position, entry.id);
                }
                LOGGER.info("Zapfsäulen-Registry geladen: {} Zapfsäulen", fuelStations.size());
            }
        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden der Zapfsäulen-Registry!", e);
        }
    }

    /**
     * Speichert Registry auf Disk
     */
    public static void save() {
        REGISTRY_FILE.getParentFile().mkdirs(); // Erstelle config-Ordner falls nicht vorhanden
        try (FileWriter writer = new FileWriter(REGISTRY_FILE)) {
            List<FuelStationEntry> toSave = new ArrayList<>(fuelStations.values());
            GSON.toJson(toSave, writer);
            isDirty = false;
            LOGGER.info("Zapfsäulen-Registry gespeichert");
        } catch (Exception e) {
            LOGGER.error("Fehler beim Speichern der Zapfsäulen-Registry!", e);
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
     * Registriert eine neue Zapfsäule oder gibt existierende ID zurück
     */
    public static UUID registerFuelStation(BlockPos position) {
        // Prüfe ob bereits registriert
        UUID existing = positionToId.get(position);
        if (existing != null) {
            return existing;
        }

        // Erstelle neue Zapfsäule
        UUID id = UUID.randomUUID();
        String displayName = "Zapfsäule #" + (fuelStations.size() + 1);

        FuelStationEntry entry = new FuelStationEntry(id, position, displayName);
        fuelStations.put(id, entry);
        positionToId.put(position, id);
        isDirty = true;

        LOGGER.info("Neue Zapfsäule registriert: {} at {}", displayName, position);
        return id;
    }

    /**
     * Gibt die ID einer Zapfsäule an einer Position zurück
     */
    public static UUID getIdByPosition(BlockPos position) {
        return positionToId.get(position);
    }

    /**
     * Gibt den Anzeigenamen einer Zapfsäule zurück
     */
    public static String getDisplayName(UUID id) {
        FuelStationEntry entry = fuelStations.get(id);
        return entry != null ? entry.displayName : "Unbekannte Zapfsäule";
    }

    /**
     * Gibt alle Zapfsäulen-IDs zurück
     */
    public static Set<UUID> getAllFuelStationIds() {
        return new HashSet<>(fuelStations.keySet());
    }

    /**
     * Gibt alle Zapfsäulen zurück
     */
    public static Collection<FuelStationEntry> getAllFuelStations() {
        return new ArrayList<>(fuelStations.values());
    }

    /**
     * Setzt einen benutzerdefinierten Namen für eine Zapfsäule
     */
    public static void setDisplayName(UUID id, String displayName) {
        FuelStationEntry entry = fuelStations.get(id);
        if (entry != null) {
            entry.displayName = displayName;
            isDirty = true;
        }
    }

    /**
     * Entfernt eine Zapfsäule aus der Registry
     */
    public static void unregisterFuelStation(BlockPos position) {
        UUID id = positionToId.remove(position);
        if (id != null) {
            fuelStations.remove(id);
            isDirty = true;
            LOGGER.info("Zapfsäule entfernt: {}", position);
        }
    }

    /**
     * Repräsentiert eine registrierte Zapfsäule
     */
    public static class FuelStationEntry {
        public UUID id;
        public BlockPos position;
        public String displayName;

        public FuelStationEntry(UUID id, BlockPos position, String displayName) {
            this.id = id;
            this.position = position;
            this.displayName = displayName;
        }

        // No-arg constructor für GSON
        public FuelStationEntry() {
        }
    }
}
