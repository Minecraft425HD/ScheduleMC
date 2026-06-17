package de.rolandsw.schedulemc.config;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Import/Export der gesamten ScheduleMC-Config als eine einzelne ZIP-Datei.
 *
 * Die ZIP enthält alle ScheduleMC-TOMLs (common/client/weapons). Beim Import
 * werden die enthaltenen Dateien in das Config-Verzeichnis zurückgeschrieben;
 * Forges Datei-Watcher lädt die Werte danach automatisch neu.
 */
public final class ConfigTransfer {

    /** Alle zu ScheduleMC gehörenden Config-Dateien. */
    private static final String[] CONFIG_FILES = {
        "schedulemc-common.toml",
        "schedulemc-client.toml",
        "schedulemc-weapons.toml"
    };

    private ConfigTransfer() {
    }

    /** Vorgeschlagener Standardpfad für den Export-Dialog. */
    public static String suggestedExportPath() {
        return FMLPaths.GAMEDIR.get().resolve("schedulemc-config.zip").toString();
    }

    /**
     * Schreibt alle vorhandenen Config-Dateien in eine ZIP-Datei.
     * @return Anzahl exportierter Dateien
     */
    public static int exportZip(Path zipTarget) throws IOException {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path parent = zipTarget.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        int count = 0;
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipTarget))) {
            for (String file : CONFIG_FILES) {
                Path src = configDir.resolve(file);
                if (Files.exists(src)) {
                    zos.putNextEntry(new ZipEntry(file));
                    Files.copy(src, zos);
                    zos.closeEntry();
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Lädt alle passenden ScheduleMC-Config-Dateien aus einer ZIP-Datei und schreibt
     * sie ins Config-Verzeichnis zurück. Forge lädt die Werte über den Datei-Watcher neu.
     * @return Anzahl importierter Dateien
     */
    public static int importZip(Path zipSource) throws IOException {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Set<String> known = Set.of(CONFIG_FILES);
        int count = 0;
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipSource))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = Paths.get(entry.getName()).getFileName().toString();
                if (!entry.isDirectory() && known.contains(name)) {
                    Files.copy(zis, configDir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
                    count++;
                }
                zis.closeEntry();
            }
        }
        return count;
    }
}
