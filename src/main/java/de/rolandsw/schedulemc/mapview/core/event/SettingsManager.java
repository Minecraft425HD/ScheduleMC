package de.rolandsw.schedulemc.mapview.core.event;

import de.rolandsw.schedulemc.mapview.gui.overridden.EnumOptionsMapView;

public interface SettingsManager {
    String getKeyText(EnumOptionsMapView options);

    void setOptionFloatValue(EnumOptionsMapView options, float value);

    float getOptionFloatValue(EnumOptionsMapView options);
}
