package de.rolandsw.schedulemc.lightmap;

public interface ModApiBridge {
    default boolean isModEnabled(String modID) {
        return false;
    }
}