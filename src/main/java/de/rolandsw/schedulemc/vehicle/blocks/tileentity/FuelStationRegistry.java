package de.rolandsw.schedulemc.vehicle.blocks.tileentity;

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
 * Registry für alle Zapfsäulen auf dem Server
 * Verwaltet Zapfsäulen-IDs und deren Positionen
 */
public class FuelStationRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File REGISTRY_FILE = new File("fuel_station_registry.json");

    // UUID (Gas Station ID) → BlockPos
    private static Map<UUID, BlockPos> fuelStations = new HashMap<>();
    // BlockPos → UUID (for reverse lookup)
    private static Map<String, UUID> positionToId = new HashMap<>();
    private static boolean isDirty = false;

    /**
     * Lädt Registry vom Disk
     */
    public static void load() {
        if (!REGISTRY_FILE.exists()) {
            LOGGER.info("Keine Fuel Station Registry gefunden, starte mit leerer Datenbank");
            return;
        }

        try (FileReader reader = new FileReader(REGISTRY_FILE)) {
            Map<String, String> loaded = GSON.fromJson(reader,
                new TypeToken<Map<String, String>>(){}.getType());

            if (loaded != null) {
                fuelStations.clear();
                positionToId.clear();

                for (Map.Entry<String, String> entry : loaded.entrySet()) {
                    UUID id = UUID.fromString(entry.getKey());
                    BlockPos pos = parseBlockPos(entry.getValue());
                    fuelStations.put(id, pos);
                    positionToId.put(posToString(pos), id);
                }

                LOGGER.info("Fuel Station Registry geladen: {} Zapfsäulen", fuelStations.size());
            }
        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden der Fuel Station Registry!", e);
        }
    }

    /**
     * Speichert Registry auf Disk
     */
    public static void save() {
        try (FileWriter writer = new FileWriter(REGISTRY_FILE)) {
            Map<String, String> toSave = new HashMap<>();
            for (Map.Entry<UUID, BlockPos> entry : fuelStations.entrySet()) {
                toSave.put(entry.getKey().toString(), posToString(entry.getValue()));
            }
            GSON.toJson(toSave, writer);
            isDirty = false;
            LOGGER.info("Fuel Station Registry gespeichert");
        } catch (Exception e) {
            LOGGER.error("Fehler beim Speichern der Fuel Station Registry!", e);
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
     * Registriert eine neue Zapfsäule
     */
    public static UUID registerFuelStation(BlockPos pos) {
        String posKey = posToString(pos);

        // Prüfe ob schon registriert
        if (positionToId.containsKey(posKey)) {
            return positionToId.get(posKey);
        }

        // Erstelle neue ID
        UUID id = UUID.randomUUID();
        fuelStations.put(id, pos);
        positionToId.put(posKey, id);
        isDirty = true;

        LOGGER.info("Zapfsäule registriert: ID={}, Pos={}", id, pos);
        return id;
    }

    /**
     * Entfernt eine Zapfsäule aus der Registry
     */
    public static void unregisterFuelStation(UUID id) {
        BlockPos pos = fuelStations.remove(id);
        if (pos != null) {
            positionToId.remove(posToString(pos));
            isDirty = true;
            LOGGER.info("Zapfsäule entfernt: ID={}", id);
        }
    }

    /**
     * Gibt die ID für eine Position zurück
     */
    public static UUID getIdByPosition(BlockPos pos) {
        return positionToId.get(posToString(pos));
    }

    /**
     * Gibt die Position für eine ID zurück
     */
    public static BlockPos getPositionById(UUID id) {
        return fuelStations.get(id);
    }

    /**
     * Gibt alle registrierten Zapfsäulen zurück
     */
    public static Set<UUID> getAllFuelStationIds() {
        return new HashSet<>(fuelStations.keySet());
    }

    /**
     * Prüft ob eine ID existiert
     */
    public static boolean isRegistered(UUID id) {
        return fuelStations.containsKey(id);
    }

    /**
     * Gibt einen lesbaren Namen für eine Zapfsäule zurück
     */
    public static String getDisplayName(UUID id) {
        BlockPos pos = fuelStations.get(id);
        if (pos == null) {
            return "Unbekannte Zapfsäule";
        }
        return "Zapfsäule " + pos.toShortString();
    }

    // Hilfsmethoden für Position-Konvertierung
    private static String posToString(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static BlockPos parseBlockPos(String str) {
        String[] parts = str.split(",");
        return new BlockPos(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2])
        );
    }
}
