package de.rolandsw.schedulemc.mapview;

public interface ModApiBridge {
    default boolean isModEnabled(String modID) {
        return false;
    }
}