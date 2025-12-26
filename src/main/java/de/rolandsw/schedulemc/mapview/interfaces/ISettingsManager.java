package de.rolandsw.schedulemc.mapview.interfaces;

import de.rolandsw.schedulemc.mapview.gui.overridden.EnumOptionsMapView;

public interface ISettingsManager {
    String getKeyText(EnumOptionsMapView options);

    void setOptionFloatValue(EnumOptionsMapView options, float value);

    float getOptionFloatValue(EnumOptionsMapView options);
}
