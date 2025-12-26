package de.rolandsw.schedulemc.mapview.integration;

public interface ModApiBridge {
    default boolean isModEnabled(String modID) {
        return false;
    }
}