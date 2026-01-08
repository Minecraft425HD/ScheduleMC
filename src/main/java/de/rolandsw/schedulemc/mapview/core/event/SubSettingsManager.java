package de.rolandsw.schedulemc.mapview.core.event;

import java.io.File;
import java.io.PrintWriter;

public interface SubSettingsManager extends SettingsManager {
    void loadSettings(File settingsFile);

    void saveAll(PrintWriter out);
}
