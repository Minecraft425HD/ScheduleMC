package de.rolandsw.schedulemc.util;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Hot-Reload-faehige Konfiguration.
 *
 * Ueberwacht Config-Dateien auf Aenderungen und laedt sie automatisch neu.
 * Kein Server-Neustart noetig fuer Konfigurationsaenderungen.
 *
 * Features:
 * - Automatische Datei-Ueberwachung via WatchService
 * - Listener-System fuer Reload-Benachrichtigungen
 * - Validierung vor dem Anwenden
 * - Debouncing (verhindert mehrfaches Laden bei schnellen Aenderungen)
 * - Thread-safe
 *
 * Verwendung:
 *   HotReloadableConfig<MyConfig> config = new HotReloadableConfig<>(
 *       new File("config/myconfig.json"), MyConfig.class);
 *   config.addReloadListener(newConfig -> applyConfig(newConfig));
 *   config.startWatching();
 */
public class HotReloadableConfig<T> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = GsonHelper.get();

    // Globale WatchService-Instanz (geteilt)
    private static volatile WatchService watchService;
    private static final AtomicBoolean watcherRunning = new AtomicBoolean(false);
    private static final Map<Path, HotReloadableConfig<?>> watchedConfigs = new ConcurrentHashMap<>();
    private static Thread watchThread;

    private final File configFile;
    private final Class<T> configClass;
    private volatile T currentConfig;
    private long lastModified = 0;
    private long lastReloadTime = 0;
    private static final long DEBOUNCE_MS = 500; // Minimum 500ms zwischen Reloads

    private final List<Consumer<T>> reloadListeners = new CopyOnWriteArrayList<>();

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTION
    // ═══════════════════════════════════════════════════════════

    public HotReloadableConfig(File configFile, Class<T> configClass) {
        this.configFile = configFile;
        this.configClass = configClass;
        this.currentConfig = loadFromFile();
    }

    public HotReloadableConfig(File configFile, Class<T> configClass, T defaultConfig) {
        this.configFile = configFile;
        this.configClass = configClass;

        if (configFile.exists()) {
            this.currentConfig = loadFromFile();
        } else {
            this.currentConfig = defaultConfig;
            saveToFile(defaultConfig);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CONFIG ACCESS
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt die aktuelle Konfiguration zurueck.
     */
    public T get() {
        return currentConfig;
    }

    /**
     * Setzt die Konfiguration und speichert sie.
     */
    public void set(T config) {
        this.currentConfig = config;
        saveToFile(config);
    }

    // ═══════════════════════════════════════════════════════════
    // LISTENERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert einen Listener der bei Config-Reload aufgerufen wird.
     */
    public void addReloadListener(Consumer<T> listener) {
        reloadListeners.add(listener);
    }

    /**
     * Entfernt einen Listener.
     */
    public void removeReloadListener(Consumer<T> listener) {
        reloadListeners.remove(listener);
    }

    // ═══════════════════════════════════════════════════════════
    // FILE WATCHING
    // ═══════════════════════════════════════════════════════════

    /**
     * Startet die Datei-Ueberwachung fuer diese Config.
     */
    public void startWatching() {
        Path dir = configFile.getParentFile().toPath();
        watchedConfigs.put(configFile.toPath().toAbsolutePath(), this);

        ensureWatchServiceRunning(dir);
        LOGGER.info("Config Hot-Reload aktiv fuer: {}", configFile.getName());
    }

    /**
     * Stoppt die Datei-Ueberwachung fuer diese Config.
     */
    public void stopWatching() {
        watchedConfigs.remove(configFile.toPath().toAbsolutePath());
    }

    private static synchronized void ensureWatchServiceRunning(Path directory) {
        if (watcherRunning.get()) return;

        try {
            watchService = FileSystems.getDefault().newWatchService();
            directory.register(watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE);

            watchThread = new Thread(() -> {
                LOGGER.info("Config-WatchService gestartet");
                while (watcherRunning.get()) {
                    try {
                        WatchKey key = watchService.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                        if (key == null) continue;

                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue;

                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                            Path changed = ((Path) key.watchable()).resolve(pathEvent.context()).toAbsolutePath();

                            HotReloadableConfig<?> config = watchedConfigs.get(changed);
                            if (config != null) {
                                config.onFileChanged();
                            }
                        }

                        key.reset();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (ClosedWatchServiceException e) {
                        break;
                    }
                }
                LOGGER.info("Config-WatchService gestoppt");
            }, "ScheduleMC-ConfigWatcher");

            watchThread.setDaemon(true);
            watcherRunning.set(true);
            watchThread.start();

        } catch (IOException e) {
            LOGGER.error("Konnte WatchService nicht starten", e);
        }
    }

    /**
     * Stoppt den globalen WatchService.
     */
    public static void shutdownWatcher() {
        watcherRunning.set(false);
        watchedConfigs.clear();

        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                LOGGER.error("Fehler beim Schliessen des WatchService", e);
            }
        }

        if (watchThread != null) {
            watchThread.interrupt();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // RELOAD LOGIC
    // ═══════════════════════════════════════════════════════════

    private void onFileChanged() {
        long now = System.currentTimeMillis();

        // Debouncing
        if (now - lastReloadTime < DEBOUNCE_MS) return;

        // Pruefe ob sich Datei wirklich geaendert hat
        long currentModified = configFile.lastModified();
        if (currentModified == lastModified) return;

        lastReloadTime = now;
        lastModified = currentModified;

        LOGGER.info("Config-Aenderung erkannt: {}", configFile.getName());
        reload();
    }

    /**
     * Laedt die Konfiguration manuell neu.
     */
    public boolean reload() {
        try {
            T newConfig = loadFromFile();
            if (newConfig == null) {
                LOGGER.warn("Config-Reload fehlgeschlagen (null): {}", configFile.getName());
                return false;
            }

            T oldConfig = currentConfig;
            currentConfig = newConfig;

            // Listener benachrichtigen
            for (Consumer<T> listener : reloadListeners) {
                try {
                    listener.accept(newConfig);
                } catch (Exception e) {
                    LOGGER.error("Fehler in Reload-Listener fuer {}", configFile.getName(), e);
                }
            }

            LOGGER.info("Config erfolgreich neu geladen: {}", configFile.getName());
            return true;

        } catch (Exception e) {
            LOGGER.error("Config-Reload fehlgeschlagen: {}", configFile.getName(), e);
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FILE I/O
    // ═══════════════════════════════════════════════════════════

    private T loadFromFile() {
        if (!configFile.exists()) {
            LOGGER.warn("Config-Datei nicht gefunden: {}", configFile.getAbsolutePath());
            return null;
        }

        try (FileReader reader = new FileReader(configFile)) {
            T config = GSON.fromJson(reader, configClass);
            lastModified = configFile.lastModified();
            return config;
        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden der Config: {}", configFile.getName(), e);
            return null;
        }
    }

    private void saveToFile(T config) {
        try {
            configFile.getParentFile().mkdirs();
            // Atomic write: temp file + move
            File tempFile = new File(configFile.getAbsolutePath() + ".tmp");
            try (FileWriter writer = new FileWriter(tempFile)) {
                GSON.toJson(config, writer);
                writer.flush();
            }
            Files.move(tempFile.toPath(), configFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            lastModified = configFile.lastModified();
            LOGGER.debug("Config gespeichert: {}", configFile.getName());
        } catch (IOException e) {
            LOGGER.error("Fehler beim Speichern der Config: {}", configFile.getName(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DIAGNOSTICS
    // ═══════════════════════════════════════════════════════════

    public String getConfigFileName() {
        return configFile.getName();
    }

    public long getLastModified() {
        return lastModified;
    }

    public int getListenerCount() {
        return reloadListeners.size();
    }

    /**
     * Globaler Status aller ueberwachten Configs.
     */
    public static String getWatcherStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Config Hot-Reload Status ===\n");
        sb.append(String.format("WatchService: %s | Configs: %d\n",
            watcherRunning.get() ? "aktiv" : "inaktiv", watchedConfigs.size()));

        for (Map.Entry<Path, HotReloadableConfig<?>> entry : watchedConfigs.entrySet()) {
            HotReloadableConfig<?> config = entry.getValue();
            sb.append(String.format("  %s [%d Listener] (last: %s)\n",
                config.getConfigFileName(),
                config.getListenerCount(),
                config.lastModified > 0 ? new Date(config.lastModified).toString() : "never"));
        }

        return sb.toString();
    }
}
