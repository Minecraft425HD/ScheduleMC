package de.rolandsw.schedulemc.config;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Import/Export der gesamten ScheduleMC-Config als eine einzelne ZIP-Datei.
 *
 * Seit der Umstellung auf Pro-Welt-Config liegen die Gameplay-Configs
 * ({@code schedulemc-server.toml}, {@code schedulemc-weapons.toml}) im
 * Welt-Ordner unter {@code serverconfig/}; die Client-Config
 * ({@code schedulemc-client.toml}) bleibt global im {@code config/}-Ordner.
 *
 * Export sammelt die jeweils vorhandenen Dateien aus beiden Orten; Import
 * schreibt sie an den korrekten Ort zurück. Forges Datei-Watcher lädt die
 * Werte danach automatisch neu. Server-Configs sind nur bei geladener Welt
 * verfügbar (sonst werden nur die globalen Dateien berücksichtigt).
 */
public final class ConfigTransfer {

    /** Globale Client-Config (immer im config/-Ordner). */
    private static final String CLIENT_FILE = "schedulemc-client.toml";
    /** Pro-Welt-Configs (im serverconfig/-Ordner der aktiven Welt). */
    private static final String[] SERVER_FILES = {
        "schedulemc-server.toml",
        "schedulemc-weapons.toml"
    };

    private ConfigTransfer() {
    }

    /** Vorgeschlagener Standardpfad für den Export-Dialog. */
    public static String suggestedExportPath() {
        return FMLPaths.GAMEDIR.get().resolve("schedulemc-config.zip").toString();
    }

    /** serverconfig/-Verzeichnis der aktiven Welt oder null (kein Server/keine Welt). */
    @Nullable
    private static Path serverConfigDir() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        // Forge speichert SERVER-Configs unter <welt>/serverconfig/.
        return server.getWorldPath(LevelResource.ROOT).resolve("serverconfig");
    }

    /**
     * Schreibt alle vorhandenen Config-Dateien (global + pro Welt) in eine ZIP-Datei.
     * @return Anzahl exportierter Dateien
     */
    public static int exportZip(Path zipTarget) throws IOException {
        Path parent = zipTarget.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path globalDir = FMLPaths.CONFIGDIR.get();
        Path serverDir = serverConfigDir();
        int count = 0;
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipTarget))) {
            count += addToZip(zos, globalDir.resolve(CLIENT_FILE), CLIENT_FILE);
            if (serverDir != null) {
                for (String file : SERVER_FILES) {
                    count += addToZip(zos, serverDir.resolve(file), file);
                }
            }
        }
        return count;
    }

    private static int addToZip(ZipOutputStream zos, Path src, String entryName) throws IOException {
        if (!Files.exists(src)) {
            return 0;
        }
        zos.putNextEntry(new ZipEntry(entryName));
        Files.copy(src, zos);
        zos.closeEntry();
        return 1;
    }

    /**
     * Lädt alle passenden ScheduleMC-Config-Dateien aus einer ZIP-Datei und schreibt sie
     * an den korrekten Ort (Client global, Server/Weapons in den serverconfig/-Ordner der
     * aktiven Welt). Forge lädt die Werte über den Datei-Watcher neu.
     * @return Anzahl importierter Dateien
     */
    public static int importZip(Path zipSource) throws IOException {
        Path globalDir = FMLPaths.CONFIGDIR.get();
        Path serverDir = serverConfigDir();
        int count = 0;
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipSource))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String name = Paths.get(entry.getName()).getFileName().toString();
                    Path target = targetFor(name, globalDir, serverDir);
                    if (target != null) {
                        Files.createDirectories(target.getParent());
                        Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
                        count++;
                    }
                }
                zis.closeEntry();
            }
        }
        return count;
    }

    @Nullable
    private static Path targetFor(String name, Path globalDir, @Nullable Path serverDir) {
        if (CLIENT_FILE.equals(name)) {
            return globalDir.resolve(name);
        }
        for (String file : SERVER_FILES) {
            if (file.equals(name)) {
                return serverDir != null ? serverDir.resolve(name) : null;
            }
        }
        return null;
    }

    /**
     * Speichert die Config des aktiven Spielstands als Vorlage für NEUE Welten.
     * Forge initialisiert neue Welten aus {@code defaultconfigs/<datei>}, sofern vorhanden.
     *
     * @return Anzahl kopierter Dateien, oder -1 wenn keine Welt geladen ist
     */
    public static int saveAsNewWorldDefaults() throws IOException {
        Path serverDir = serverConfigDir();
        if (serverDir == null) {
            return -1;
        }
        Path defaults = FMLPaths.GAMEDIR.get().resolve("defaultconfigs");
        Files.createDirectories(defaults);
        int count = 0;
        for (String file : SERVER_FILES) {
            Path src = serverDir.resolve(file);
            if (Files.exists(src)) {
                Files.copy(src, defaults.resolve(file), StandardCopyOption.REPLACE_EXISTING);
                count++;
            }
        }
        return count;
    }
}
