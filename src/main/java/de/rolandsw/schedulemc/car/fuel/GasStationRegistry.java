package de.rolandsw.schedulemc.car.fuel;

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
public class GasStationRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File REGISTRY_FILE = new File("config/gas_station_registry.json");

    // UUID → GasStationEntry
    private static Map<UUID, GasStationEntry> gasStations = new HashMap<>();
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
            List<GasStationEntry> loaded = GSON.fromJson(reader,
                new TypeToken<List<GasStationEntry>>(){}.getType());

            if (loaded != null) {
                gasStations.clear();
                positionToId.clear();
                for (GasStationEntry entry : loaded) {
                    gasStations.put(entry.id, entry);
                    positionToId.put(entry.position, entry.id);
                }
                LOGGER.info("Zapfsäulen-Registry geladen: {} Zapfsäulen", gasStations.size());
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
            List<GasStationEntry> toSave = new ArrayList<>(gasStations.values());
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
    public static UUID registerGasStation(BlockPos position) {
        // Prüfe ob bereits registriert
        UUID existing = positionToId.get(position);
        if (existing != null) {
            return existing;
        }

        // Erstelle neue Zapfsäule
        UUID id = UUID.randomUUID();
        String displayName = "Zapfsäule #" + (gasStations.size() + 1);

        GasStationEntry entry = new GasStationEntry(id, position, displayName);
        gasStations.put(id, entry);
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
        GasStationEntry entry = gasStations.get(id);
        return entry != null ? entry.displayName : "Unbekannte Zapfsäule";
    }

    /**
     * Gibt alle Zapfsäulen-IDs zurück
     */
    public static Set<UUID> getAllGasStationIds() {
        return new HashSet<>(gasStations.keySet());
    }

    /**
     * Gibt alle Zapfsäulen zurück
     */
    public static Collection<GasStationEntry> getAllGasStations() {
        return new ArrayList<>(gasStations.values());
    }

    /**
     * Setzt einen benutzerdefinierten Namen für eine Zapfsäule
     */
    public static void setDisplayName(UUID id, String displayName) {
        GasStationEntry entry = gasStations.get(id);
        if (entry != null) {
            entry.displayName = displayName;
            isDirty = true;
        }
    }

    /**
     * Entfernt eine Zapfsäule aus der Registry
     */
    public static void unregisterGasStation(BlockPos position) {
        UUID id = positionToId.remove(position);
        if (id != null) {
            gasStations.remove(id);
            isDirty = true;
            LOGGER.info("Zapfsäule entfernt: {}", position);
        }
    }

    /**
     * Repräsentiert eine registrierte Zapfsäule
     */
    public static class GasStationEntry {
        public UUID id;
        public BlockPos position;
        public String displayName;

        public GasStationEntry(UUID id, BlockPos position, String displayName) {
            this.id = id;
            this.position = position;
            this.displayName = displayName;
        }

        // No-arg constructor für GSON
        public GasStationEntry() {
        }
    }
}
