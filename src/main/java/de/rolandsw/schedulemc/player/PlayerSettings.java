package de.rolandsw.schedulemc.player;

import java.util.UUID;

/**
 * Speichert Spieler-spezifische Einstellungen
 */
public class PlayerSettings {

    private String playerUUID;

    // Utility-Warnungen
    private boolean utilityWarningsEnabled = true;
    private double electricityWarningThreshold = 100.0; // kWh
    private double waterWarningThreshold = 500.0; // L

    // Standardkonstruktor für Gson
    public PlayerSettings() {
    }

    public PlayerSettings(UUID playerUUID) {
        this.playerUUID = playerUUID.toString();
    }

    // Getter & Setter
    public String getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public boolean isUtilityWarningsEnabled() {
        return utilityWarningsEnabled;
    }

    public void setUtilityWarningsEnabled(boolean enabled) {
        this.utilityWarningsEnabled = enabled;
    }

    public double getElectricityWarningThreshold() {
        return electricityWarningThreshold;
    }

    public void setElectricityWarningThreshold(double threshold) {
        this.electricityWarningThreshold = threshold;
    }

    public double getWaterWarningThreshold() {
        return waterWarningThreshold;
    }

    public void setWaterWarningThreshold(double threshold) {
        this.waterWarningThreshold = threshold;
    }
}
