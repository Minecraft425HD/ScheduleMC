package de.rolandsw.schedulemc.mapview.util;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.core.event.MapChangeListener;
import java.util.ArrayList;
import java.util.List;

public class WorldUpdateListener {
    private final List<MapChangeListener> chunkProcessors = new ArrayList<>();

    public void addListener(MapChangeListener chunkProcessor) {
        chunkProcessors.add(chunkProcessor);
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