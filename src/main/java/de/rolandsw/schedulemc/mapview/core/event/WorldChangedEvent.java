package de.rolandsw.schedulemc.mapview.core.event;

import net.minecraft.client.multiplayer.ClientLevel;

/**
 * Event fired when the world changes (dimension switch, server join, etc).
 * Replaces direct world change notifications in Observer pattern.
 *
 * Part of Phase 2C refactoring - Event Bus architecture.
 */
public class WorldChangedEvent extends MapEvent {

    private final ClientLevel oldWorld;
    private final ClientLevel newWorld;

    public WorldChangedEvent(ClientLevel oldWorld, ClientLevel newWorld) {
        super();
        this.oldWorld = oldWorld;
        this.newWorld = newWorld;
    }

    public ClientLevel getOldWorld() {
        return oldWorld;
    }

    public ClientLevel getNewWorld() {
        return newWorld;
    }

    public boolean isWorldNull() {
        return newWorld == null;
    }
}
