package de.rolandsw.schedulemc.vehicle.blocks.tileentity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry für alle Zapfsäulen auf dem Server
 * Verwaltet Zapfsäulen-IDs und deren Positionen
 *
 * OPTIMIERT: Verwendet Long-basierte BlockPos-Keys statt String-Konvertierung
 */
public class FuelStationRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = GsonHelper.get();
    private static final File REGISTRY_FILE = new File("fuel_station_registry.json");

    // UUID (Gas Station ID) → BlockPos
    private static final Map<UUID, BlockPos> fuelStations = new ConcurrentHashMap<>();
    // Long (packed BlockPos) → UUID (für schnellen Lookup ohne String-Allokation)
    private static final Map<Long, UUID> positionToId = new ConcurrentHashMap<>();
    private static volatile boolean isDirty = false;

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
                    positionToId.put(pos.asLong(), id);  // OPTIMIERT: Long statt String
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
        try {
            REGISTRY_FILE.getParentFile().mkdirs();
            Map<String, String> toSave = new HashMap<>();
            for (Map.Entry<UUID, BlockPos> entry : fuelStations.entrySet()) {
                toSave.put(entry.getKey().toString(), posToString(entry.getValue()));
            }
            File tempFile = new File(REGISTRY_FILE.getParent(), REGISTRY_FILE.getName() + ".tmp");
            try (FileWriter writer = new FileWriter(tempFile)) {
                GSON.toJson(toSave, writer);
                writer.flush();
            }
            Files.move(tempFile.toPath(), REGISTRY_FILE.toPath(),
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
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
     * OPTIMIERT: Atomare Operation mit computeIfAbsent verhindert Race Condition
     */
    public static UUID registerFuelStation(BlockPos pos) {
        long posKey = pos.asLong();  // OPTIMIERT: Long statt String

        // OPTIMIERT: Atomare Operation - Thread-safe und effizienter
        UUID existingId = positionToId.get(posKey);
        if (existingId != null) {
            return existingId;
        }

        // Erstelle neue ID - Atomare Registrierung
        UUID newId = UUID.randomUUID();
        UUID previousId = positionToId.putIfAbsent(posKey, newId);

        if (previousId != null) {
            // Andere Thread war schneller
            return previousId;
        }

        fuelStations.put(newId, pos);
        isDirty = true;

        LOGGER.info("Zapfsäule registriert: ID={}, Pos={}", newId, pos);
        return newId;
    }

    /**
     * Entfernt eine Zapfsäule aus der Registry
     */
    public static void unregisterFuelStation(UUID id) {
        BlockPos pos = fuelStations.remove(id);
        if (pos != null) {
            positionToId.remove(pos.asLong());  // OPTIMIERT: Long statt String
            isDirty = true;
            LOGGER.info("Zapfsäule entfernt: ID={}", id);
        }
    }

    /**
     * Gibt die ID für eine Position zurück
     * OPTIMIERT: O(1) Lookup mit Long-Key statt String-Allokation
     */
    public static UUID getIdByPosition(BlockPos pos) {
        return positionToId.get(pos.asLong());
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
            return net.minecraft.network.chat.Component.translatable("fuel_station.unknown").getString();
        }
        return net.minecraft.network.chat.Component.translatable("fuel_station.display_name",
            pos.toShortString()
        ).getString();
    }

    // Hilfsmethoden für Position-Konvertierung
    private static String posToString(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static BlockPos parseBlockPos(String str) {
        String[] parts = str.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid BlockPos format: " + str);
        }
        return new BlockPos(
            Integer.parseInt(parts[0].trim()),
            Integer.parseInt(parts[1].trim()),
            Integer.parseInt(parts[2].trim())
        );
    }
}
