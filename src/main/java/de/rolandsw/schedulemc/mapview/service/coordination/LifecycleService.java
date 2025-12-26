package de.rolandsw.schedulemc.mapview.service.coordination;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.config.MapViewConfiguration;
import de.rolandsw.schedulemc.mapview.data.persistence.AsyncPersistenceManager;

/**
 * Service responsible for managing application lifecycle events.
 * Handles initialization, server join/disconnect, and shutdown operations.
 *
 * Part of Phase 2 refactoring to reduce structural similarity.
 */
public class LifecycleService {

    private final MapViewConfiguration configuration;

    public LifecycleService(MapViewConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Called when play mode is initialized (registries ready, no world yet).
     */
    public void onPlayInit() {
        // Registries are ready, but no world loaded yet
        // Reserved for future initialization needs
    }

    /**
     * Called when joining a server.
     */
    public void onJoinServer() {
        // No-op after radar feature removal
        // Reserved for future server join logic
    }

    /**
     * Called when disconnecting from server.
     * Clears server-specific settings.
     */
    public void onDisconnect() {
        clearServerSettings();
    }

    /**
     * Called during configuration phase initialization.
     * Ensures clean server settings state.
     */
    public void onConfigurationInit() {
        clearServerSettings();
    }

    /**
     * Called when client is stopping.
     * Performs cleanup and flushes pending save operations.
     */
    public void onClientStopping() {
        MapViewConstants.onShutDown();
        AsyncPersistenceManager.flushSaveQueue();
    }

    /**
     * Clears server-specific permission settings.
     * Resets to default (all features allowed).
     */
    public void clearServerSettings() {
        if (configuration != null) {
            configuration.serverTeleportCommand = null;
            configuration.worldmapAllowed = true;
            configuration.minimapAllowed = true;
        }
    }

    /**
     * Sets permissions for features (legacy from cave mode).
     * Currently no-op as cave mode was removed.
     */
    public void setPermissions(boolean hasCavemodePermission) {
        // Cave mode removed - no permissions to set
        // Method kept for backward compatibility
    }
}
