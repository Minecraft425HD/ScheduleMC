package de.rolandsw.schedulemc.mapview.core.model;

import de.rolandsw.schedulemc.mapview.integration.DebugRenderState;
import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.core.event.MapChangeListener;
import de.rolandsw.schedulemc.mapview.core.event.ChunkProcessEvent;
import de.rolandsw.schedulemc.mapview.core.event.EventBridgeAdapter;
import net.minecraft.world.level.chunk.LevelChunk;

public class MapChunk {
    private final int x;
    private final int z;
    private LevelChunk chunk;
    private boolean isChanged;
    private boolean isLoaded;
    private boolean isSurroundedByLoaded;

    public MapChunk(int x, int z) {
        this.x = x;
        this.z = z;
        this.chunk = MapViewConstants.getPlayer().level().getChunk(x, z);
        this.isLoaded = this.chunk != null && !this.chunk.isEmpty() && MapViewConstants.getPlayer().level().hasChunk(x, z);
        this.isSurroundedByLoaded = false;
        this.isChanged = true;
    }

    public void checkIfChunkChanged(MapChangeListener changeObserver) {
        if (this.hasChunkLoadedOrUnloaded() || this.isChanged) {
            DebugRenderState.checkChunkX = x;
            DebugRenderState.checkChunkZ = z;
            DebugRenderState.chunksChanged++;

            // Phase 2C: Dual dispatch - both legacy observer and new event bus
            changeObserver.processChunk(this.chunk);

            // Post event to EventBus for new event-based listeners
            ChunkProcessEvent.ProcessReason reason = this.isChanged ?
                    ChunkProcessEvent.ProcessReason.CHUNK_MODIFIED :
                    ChunkProcessEvent.ProcessReason.CHUNK_LOADED;
            EventBridgeAdapter.postChunkProcess(this.chunk, reason);

            this.isChanged = false;
        }

    }

    private boolean hasChunkLoadedOrUnloaded() {
        boolean hasChanged = false;
        if (!this.isLoaded) {
            this.chunk = MapViewConstants.getPlayer().level().getChunk(this.x, this.z);
            if (this.chunk != null && !this.chunk.isEmpty() && MapViewConstants.getPlayer().level().hasChunk(this.x, this.z)) {
                this.isLoaded = true;
                hasChanged = true;
            }
        } else if (this.chunk == null || this.chunk.isEmpty() || !MapViewConstants.getPlayer().level().hasChunk(this.x, this.z)) {
            this.isLoaded = false;
            hasChanged = true;
        }

        return hasChanged;
    }

    public void checkIfChunkBecameSurroundedByLoaded(MapChangeListener changeObserver) {
        this.chunk = MapViewConstants.getPlayer().level().getChunk(this.x, this.z);
        this.isLoaded = this.chunk != null && !this.chunk.isEmpty() && MapViewConstants.getPlayer().level().hasChunk(this.x, this.z);
        if (this.isLoaded) {
            boolean formerSurroundedByLoaded = this.isSurroundedByLoaded;
            this.isSurroundedByLoaded = this.isSurroundedByLoaded();
            if (!formerSurroundedByLoaded && this.isSurroundedByLoaded) {
                // Phase 2C: Dual dispatch - both legacy observer and new event bus
                changeObserver.processChunk(this.chunk);

                // Post event to EventBus
                EventBridgeAdapter.postChunkProcess(this.chunk,
                        ChunkProcessEvent.ProcessReason.SURROUNDING_LOADED);
            }
        } else {
            this.isSurroundedByLoaded = false;
        }

    }

    public boolean isSurroundedByLoaded() {
        this.chunk = MapViewConstants.getPlayer().level().getChunk(this.x, this.z);
        this.isLoaded = this.chunk != null && !this.chunk.isEmpty() && MapViewConstants.getPlayer().level().hasChunk(this.x, this.z);
        boolean neighborsLoaded = this.isLoaded;

        for (int t = this.x - 1; t <= this.x + 1 && neighborsLoaded; ++t) {
            for (int s = this.z - 1; s <= this.z + 1 && neighborsLoaded; ++s) {
                LevelChunk neighborChunk = MapViewConstants.getPlayer().level().getChunk(t, s);
                neighborsLoaded = neighborChunk != null && !neighborChunk.isEmpty() && MapViewConstants.getPlayer().level().hasChunk(t, s);
            }
        }

        return neighborsLoaded;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public void setModified(boolean isModified) {
        this.isChanged = isModified;
    }
}
