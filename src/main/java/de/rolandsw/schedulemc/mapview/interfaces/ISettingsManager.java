package de.rolandsw.schedulemc.lightmap.interfaces;

import de.rolandsw.schedulemc.lightmap.gui.overridden.EnumOptionsMinimap;

public interface ISettingsManager {
    String getKeyText(EnumOptionsMinimap options);

    void setOptionFloatValue(EnumOptionsMinimap options, float value);

    float getOptionFloatValue(EnumOptionsMinimap options);
}
