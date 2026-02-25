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
    public static volatile boolean utilityWarningsEnabled = true;
    public static volatile double electricityThreshold = 100.0;
    public static volatile double waterThreshold = 500.0;

    /**
     * Aktualisiert alle Settings atomar (verhindert teilweise Updates durch Race Conditions)
     */
    public static synchronized void update(boolean warnings, double electricity, double water) {
        utilityWarningsEnabled = warnings;
        electricityThreshold = electricity;
        waterThreshold = water;
    }

    /**
     * Setzt alle Settings auf Standardwerte zurück
     */
    public static void reset() {
        update(true, 100.0, 500.0);
    }
}
