package de.rolandsw.schedulemc.mapview.core.event;

import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Event fired when a chunk needs to be processed for map rendering.
 * Replaces direct MapChangeListener.processChunk() calls.
 *
 * Part of Phase 2C refactoring - Event Bus architecture.
 */
public class ChunkProcessEvent extends MapEvent {

    private final LevelChunk chunk;
    private final ProcessReason reason;

    public ChunkProcessEvent(LevelChunk chunk, ProcessReason reason) {
        super();
        this.chunk = chunk;
        this.reason = reason;
    }

    public LevelChunk getChunk() {
        return chunk;
    }

    public ProcessReason getReason() {
        return reason;
    }

    /**
     * Reasons why a chunk needs processing.
     */
    public enum ProcessReason {
        CHUNK_LOADED,        // Chunk was newly loaded
        CHUNK_MODIFIED,      // Chunk data changed
        SURROUNDING_LOADED,  // Surrounding chunks became loaded
        FORCED_REFRESH       // Manual refresh requested
    }
}
