package de.rolandsw.schedulemc.mapview.util;

import de.rolandsw.schedulemc.mapview.MapViewConstants;

public final class MessageUtils {
    private static final boolean debug = false;

    private MessageUtils() {}

    public static void chatInfo(String s) { MapViewConstants.getLightMapInstance().sendPlayerMessageOnMainThread(s); }

    public static void printDebug(String line) { if (debug) {
        MapViewConstants.getLogger().warn(line);
    } }
}