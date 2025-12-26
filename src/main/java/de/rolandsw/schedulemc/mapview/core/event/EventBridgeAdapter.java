package de.rolandsw.schedulemc.mapview.core.event;

/**
 * Adapter that bridges between EventBus and legacy MapChangeListener interface.
 * Allows gradual migration from Observer pattern to Event Bus.
 *
 * Part of Phase 2C refactoring - provides backward compatibility during migration.
 */
public class EventBridgeAdapter {

    /**
     * Subscribes a MapChangeListener to the EventBus.
     * Translates ChunkProcessEvent to processChunk() calls.
     *
     * @param listener The legacy listener to subscribe
     */
    public static void subscribeListener(MapChangeListener listener) {
        MapEventBus.getInstance().subscribe(ChunkProcessEvent.class, event -> {
            if (event.getChunk() != null) {
                listener.processChunk(event.getChunk());
            }
        });

        MapEventBus.getInstance().subscribe(WorldChangedEvent.class, event -> {
            if (event.getNewWorld() != null) {
                listener.newWorld(event.getNewWorld());
            }
        });
    }

    /**
     * Posts a chunk process event to the EventBus.
     * Convenience method for migration.
     */
    public static void postChunkProcess(net.minecraft.world.level.chunk.LevelChunk chunk,
                                        ChunkProcessEvent.ProcessReason reason) {
        MapEventBus.getInstance().post(new ChunkProcessEvent(chunk, reason));
    }

    /**
     * Posts a world changed event to the EventBus.
     * Convenience method for migration.
     */
    public static void postWorldChanged(net.minecraft.client.multiplayer.ClientLevel oldWorld,
                                        net.minecraft.client.multiplayer.ClientLevel newWorld) {
        MapEventBus.getInstance().post(new WorldChangedEvent(oldWorld, newWorld));
    }
}
