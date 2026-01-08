package de.rolandsw.schedulemc.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.Reader;
import java.util.function.Function;

/**
 * Utility für versionierte Datenpersistenz
 *
 * ARCHITEKTUR: Ermöglicht Datenmigration bei Format-Änderungen.
 * Jede gespeicherte Datei enthält eine Versionsnummer, die bei
 * Inkompatibilitäten automatische Migration ermöglicht.
 *
 * Verwendung:
 * <pre>
 * // Beim Speichern
 * JsonObject wrapper = VersionedData.wrap(dataJson, 2);
 * 
 * // Beim Laden
 * VersionedData.Result result = VersionedData.unwrap(reader, 2, migrator);
 * if (result.isSuccess()) {
 *     MyData data = gson.fromJson(result.getData(), MyData.class);
 * }
 * </pre>
 */
public class VersionedData {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String VERSION_KEY = "dataVersion";
    private static final String DATA_KEY = "data";

    /**
     * Ergebnis des Unwrap-Vorgangs
     */
    public static class Result {
        private final JsonElement data;
        private final int version;
        private final boolean migrated;
        private final String error;

        private Result(JsonElement data, int version, boolean migrated, @Nullable String error) {
            this.data = data;
            this.version = version;
            this.migrated = migrated;
            this.error = error;
        }

        public static Result success(JsonElement data, int version, boolean migrated) {
            return new Result(data, version, migrated, null);
        }

        public static Result failure(String error) {
            return new Result(null, -1, false, error);
        }

        public boolean isSuccess() { return error == null && data != null; }
        public JsonElement getData() { return data; }
        public int getVersion() { return version; }
        public boolean wasMigrated() { return migrated; }
        @Nullable public String getError() { return error; }
    }

    /**
     * Wraps data with version information
     *
     * @param data Die zu speichernden Daten als JsonElement
     * @param version Die aktuelle Datenversion
     * @return JsonObject mit Version und Daten
     */
    public static JsonObject wrap(JsonElement data, int version) {
        JsonObject wrapper = new JsonObject();
        wrapper.addProperty(VERSION_KEY, version);
        wrapper.add(DATA_KEY, data);
        return wrapper;
    }

    /**
     * Unwraps data and performs migration if needed
     *
     * @param reader Reader für die JSON-Daten
     * @param currentVersion Die erwartete aktuelle Version
     * @param migrator Funktion die alte Daten auf neue Version migriert (kann null sein)
     * @return Result mit Daten oder Fehler
     */
    public static Result unwrap(Reader reader, int currentVersion, 
                                @Nullable Function<MigrationContext, JsonElement> migrator) {
        try {
            JsonElement root = JsonParser.parseReader(reader);

            if (!root.isJsonObject()) {
                // Legacy-Format ohne Versionierung - als Version 0 behandeln
                LOGGER.warn("Legacy data format detected (no version wrapper)");
                if (migrator != null) {
                    JsonElement migrated = migrator.apply(new MigrationContext(root, 0, currentVersion));
                    return Result.success(migrated, currentVersion, true);
                }
                return Result.success(root, 0, false);
            }

            JsonObject wrapper = root.getAsJsonObject();

            // Prüfe ob es ein versioniertes Format ist
            if (!wrapper.has(VERSION_KEY)) {
                // Legacy-Format ohne Versionierung
                LOGGER.warn("Legacy data format detected (no version field)");
                if (migrator != null) {
                    JsonElement migrated = migrator.apply(new MigrationContext(wrapper, 0, currentVersion));
                    return Result.success(migrated, currentVersion, true);
                }
                return Result.success(wrapper, 0, false);
            }

            int dataVersion = wrapper.get(VERSION_KEY).getAsInt();
            JsonElement data = wrapper.get(DATA_KEY);

            if (dataVersion == currentVersion) {
                // Aktuelle Version - keine Migration nötig
                return Result.success(data, dataVersion, false);
            }

            if (dataVersion > currentVersion) {
                // Neuere Version als erwartet - Downgrade nicht unterstützt
                return Result.failure("Data version " + dataVersion + " is newer than current " + currentVersion);
            }

            // Migration nötig
            if (migrator == null) {
                LOGGER.warn("No migrator provided, using data as-is (version {} -> {})", dataVersion, currentVersion);
                return Result.success(data, dataVersion, false);
            }

            LOGGER.info("Migrating data from version {} to {}", dataVersion, currentVersion);
            JsonElement migrated = migrator.apply(new MigrationContext(data, dataVersion, currentVersion));
            return Result.success(migrated, currentVersion, true);

        } catch (Exception e) {
            LOGGER.error("Failed to parse versioned data", e);
            return Result.failure("Parse error: " + e.getMessage());
        }
    }

    /**
     * Kontext für Datenmigration
     */
    public static class MigrationContext {
        private final JsonElement data;
        private final int fromVersion;
        private final int toVersion;

        public MigrationContext(JsonElement data, int fromVersion, int toVersion) {
            this.data = data;
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
        }

        public JsonElement getData() { return data; }
        public int getFromVersion() { return fromVersion; }
        public int getToVersion() { return toVersion; }
    }

    /**
     * Prüft ob Daten versioniert sind
     */
    public static boolean isVersioned(JsonElement element) {
        if (!element.isJsonObject()) return false;
        JsonObject obj = element.getAsJsonObject();
        return obj.has(VERSION_KEY) && obj.has(DATA_KEY);
    }

    private VersionedData() {
        throw new UnsupportedOperationException("Utility class");
    }
}
