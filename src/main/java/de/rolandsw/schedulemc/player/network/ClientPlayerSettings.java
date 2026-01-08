package de.rolandsw.schedulemc.player.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side Cache für Spieler-Einstellungen
 * Wird vom Server synchronisiert
 */
@OnlyIn(Dist.CLIENT)
public class ClientPlayerSettings {

    public static boolean utilityWarningsEnabled = true;
    public static double electricityThreshold = 100.0;
    public static double waterThreshold = 500.0;

    /**
     * Setzt alle Settings auf Standardwerte zurück
     */
    public static void reset() {
        utilityWarningsEnabled = true;
        electricityThreshold = 100.0;
        waterThreshold = 500.0;
    }
}
