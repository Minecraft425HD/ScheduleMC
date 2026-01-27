package de.rolandsw.schedulemc.mapview.util;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.core.event.MapChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorldUpdateListener {
    // THREAD-SAFETY: CopyOnWriteArrayList for concurrent access
    private final List<MapChangeListener> chunkProcessors = new CopyOnWriteArrayList<>();

    public void addListener(MapChangeListener chunkProcessor) {
        chunkProcessors.add(chunkProcessor);
    }

    /**
     * Removes a listener from the list.
     * Call this when the listener is no longer needed to prevent memory leaks.
     */
    public void removeListener(MapChangeListener chunkProcessor) {
        chunkProcessors.remove(chunkProcessor);
    }

    /**
     * Removes all listeners.
     * Call this during cleanup/shutdown to prevent memory leaks.
     */
    public void clearListeners() {
        chunkProcessors.clear();
    }

    public void notifyObservers(int chunkX, int chunkZ) {
        try {
            for (MapChangeListener chunkProcessor : this.chunkProcessors) {
                chunkProcessor.handleChangeInWorld(chunkX, chunkZ);
            }
        } catch (RuntimeException exception) {
            MapViewConstants.getLogger().error("Exception", exception);
        }
    }
}