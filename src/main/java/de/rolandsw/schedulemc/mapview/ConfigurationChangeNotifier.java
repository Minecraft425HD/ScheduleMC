package de.rolandsw.schedulemc.mapview;

import de.rolandsw.schedulemc.mapview.data.cache.RegionCache;
import java.util.concurrent.CopyOnWriteArraySet;

public class ConfigurationChangeNotifier {
    private final CopyOnWriteArraySet<RegionCache> listeners = new CopyOnWriteArraySet<>();

    public final void addObserver(RegionCache listener) {
        listeners.add(listener);
    }

    public final void removeObserver(RegionCache listener) {
        listeners.remove(listener);
    }

    public void notifyOfChanges() {
        for (RegionCache listener : listeners) {
            listener.notifyOfActionableChange(this);
        }

    }
}
