package de.rolandsw.schedulemc.config;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Import/Export der gesamten ScheduleMC-Config (alle TOML-Dateien).
 *
 * Export kopiert die Config-Dateien in einen Unterordner {@code schedulemc-export}
 * des Config-Verzeichnisses; Import kopiert sie von dort zurück. Forge's
 * Datei-Watcher lädt die Specs nach dem Überschreiben automatisch neu.
 */
public final class ConfigTransfer {

    /** Alle zu ScheduleMC gehörenden Config-Dateien. */
    private static final String[] CONFIG_FILES = {
        "schedulemc-common.toml",
        "schedulemc-client.toml",
        "schedulemc-weapons.toml"
    };

    private static final String EXPORT_FOLDER = "schedulemc-export";

    private ConfigTransfer() {
    }

    /** Verzeichnis, in/aus dem exportiert bzw. importiert wird. */
    public static Path exportDir() {
        return FMLPaths.CONFIGDIR.get().resolve(EXPORT_FOLDER);
    }

    /**
     * Kopiert alle vorhandenen Config-Dateien ins Export-Verzeichnis.
     * @return Anzahl exportierter Dateien
     */
    public static int exportAll() throws IOException {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path out = exportDir();
        Files.createDirectories(out);
        int count = 0;
        for (String file : CONFIG_FILES) {
            Path src = configDir.resolve(file);
            if (Files.exists(src)) {
                Files.copy(src, out.resolve(file), StandardCopyOption.REPLACE_EXISTING);
                count++;
            }
        }
        return count;
    }

    /**
     * Kopiert alle im Export-Verzeichnis vorhandenen Config-Dateien zurück ins
     * Config-Verzeichnis. Forge lädt die Werte über den Datei-Watcher neu.
     * @return Anzahl importierter Dateien
     */
    public static int importAll() throws IOException {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path in = exportDir();
        if (!Files.isDirectory(in)) {
            return 0;
        }
        int count = 0;
        for (String file : CONFIG_FILES) {
            Path src = in.resolve(file);
            if (Files.exists(src)) {
                Files.copy(src, configDir.resolve(file), StandardCopyOption.REPLACE_EXISTING);
                count++;
            }
        }
        return count;
    }
}
