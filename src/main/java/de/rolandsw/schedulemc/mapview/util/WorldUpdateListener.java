package de.rolandsw.schedulemc.lightmap.util;

import de.rolandsw.schedulemc.lightmap.LightMapConstants;
import de.rolandsw.schedulemc.lightmap.interfaces.IChangeObserver;
import java.util.ArrayList;
import java.util.List;

public class WorldUpdateListener {
    private final List<IChangeObserver> chunkProcessors = new ArrayList<>();

    public void addListener(IChangeObserver chunkProcessor) {
        chunkProcessors.add(chunkProcessor);
    }

    public void notifyObservers(int chunkX, int chunkZ) {
        try {
            for (IChangeObserver chunkProcessor : this.chunkProcessors) {
                chunkProcessor.handleChangeInWorld(chunkX, chunkZ);
            }
        } catch (RuntimeException exception) {
            LightMapConstants.getLogger().error("Exception", exception);
        }
    }
}