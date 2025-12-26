package de.rolandsw.schedulemc.lightmap.util;

import de.rolandsw.schedulemc.lightmap.LightMapConstants;

public final class MessageUtils {
    private static final boolean debug = false;

    private MessageUtils() {}

    public static void chatInfo(String s) { LightMapConstants.getLightMapInstance().sendPlayerMessageOnMainThread(s); }

    public static void printDebug(String line) { if (debug) {
        LightMapConstants.getLogger().warn(line);
    } }
}