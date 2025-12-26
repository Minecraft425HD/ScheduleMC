package de.rolandsw.schedulemc.mapview.service.coordination;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;

/**
 * Service responsible for tracking and managing world state.
 * Handles world changes, world naming, and seed management.
 *
 * Part of Phase 2 refactoring to reduce structural similarity.
 */
public class WorldStateService {

    private ClientLevel currentWorld;
    private String worldSeed = "";

    /**
     * Checks if the world has changed since last tick.
     * @param newWorld the current world from Minecraft
     * @return true if world changed, false otherwise
     */
    public boolean hasWorldChanged(ClientLevel newWorld) {
        if (this.currentWorld != newWorld) {
            this.currentWorld = newWorld;
            return true;
        }
        return false;
    }

    /**
     * Gets the current world instance.
     */
    public ClientLevel getCurrentWorld() {
        return this.currentWorld;
    }

    /**
     * Gets the current world name for display and caching purposes.
     * Returns singleplayer world name or multiplayer server name.
     */
    public String getCurrentWorldName() {
        if (MapViewConstants.isSinglePlayer()) {
            return MapViewConstants.getIntegratedServer()
                    .map(server -> server.getWorldData().getLevelName())
                    .filter(name -> name != null && !name.isBlank())
                    .orElse("Singleplayer World");
        } else {
            ServerData info = MapViewConstants.getMinecraft().getCurrentServer();
            if (info != null && info.name != null && !info.name.isBlank()) {
                return info.name;
            }
            if (MapViewConstants.isRealmServer()) {
                return "Realms";
            }
            return "Multiplayer Server";
        }
    }

    /**
     * Gets the world seed used for slime chunk calculation.
     * For singleplayer, automatically retrieves from integrated server.
     */
    public String getWorldSeed() {
        if (MapViewConstants.isSinglePlayer()) {
            return MapViewConstants.getIntegratedServer()
                    .map(server -> String.valueOf(server.getWorldData().worldGenOptions().seed()))
                    .orElse("");
        }
        return this.worldSeed;
    }

    /**
     * Sets the world seed for multiplayer slime chunk calculation.
     * Only used in multiplayer where seed isn't automatically available.
     */
    public void setWorldSeed(String seed) {
        this.worldSeed = seed != null ? seed : "";
    }

    /**
     * Resets world state (called on disconnect).
     */
    public void reset() {
        this.currentWorld = null;
        this.worldSeed = "";
    }
}
