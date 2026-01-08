package de.rolandsw.schedulemc.mapview.core.event;

import de.rolandsw.schedulemc.mapview.config.MapOption;

public interface SettingsManager {
    String getKeyText(MapOption options);

    void setOptionFloatValue(MapOption options, float value);

    float getOptionFloatValue(MapOption options);
}
