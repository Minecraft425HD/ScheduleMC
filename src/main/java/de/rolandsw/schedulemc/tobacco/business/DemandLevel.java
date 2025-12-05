package de.rolandsw.schedulemc.tobacco.business;

/**
 * Nachfrage-Level für Tabak-Käufe
 */
public enum DemandLevel {
    LOW(0.7, "§c🔻 NIEDRIG"),
    MEDIUM(1.0, "§e➡️ MITTEL"),
    HIGH(1.3, "§a📈 HOCH");

    private final double priceMultiplier;
    private final String displayName;

    DemandLevel(double priceMultiplier, String displayName) {
        this.priceMultiplier = priceMultiplier;
        this.displayName = displayName;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }
}
