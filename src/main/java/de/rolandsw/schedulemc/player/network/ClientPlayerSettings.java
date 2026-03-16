package de.rolandsw.schedulemc.player.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side Cache für Spieler-Einstellungen
 * Wird vom Server synchronisiert
 */
@OnlyIn(Dist.CLIENT)
public class ClientPlayerSettings {

    // volatile für korrekte Memory Visibility zwischen Packet-Thread und Render-Thread
    public static volatile boolean utilityWarningsEnabled = true;  // NOPMD
    public static volatile double electricityThreshold = 100.0;  // NOPMD
    public static volatile double waterThreshold = 500.0;  // NOPMD

    /**
     * Aktualisiert alle Settings atomar (verhindert teilweise Updates durch Race Conditions)
     */
    public static void update(boolean warnings, double electricity, double water) {
        synchronized (ClientPlayerSettings.class) {
            utilityWarningsEnabled = warnings;
            electricityThreshold = electricity;
            waterThreshold = water;
        }
    }

    /**
     * Setzt alle Settings auf Standardwerte zurück
     */
    public static void reset() {
        update(true, 100.0, 500.0);
    }
}
