package de.rolandsw.schedulemc.mdma;

/**
 * Pillen-Designs für Ecstasy
 */
public enum PillDesign {
    TESLA("Tesla", "§c", "T"),
    SUPERMAN("Superman", "§9", "S"),
    TOTENKOPF("Totenkopf", "§8", "☠"),
    HERZ("Herz", "§d", "♥"),
    SCHMETTERLING("Schmetterling", "§e", "🦋"),
    STERN("Stern", "§6", "★"),
    PEACE("Peace", "§a", "☮"),
    DIAMANT("Diamant", "§b", "◆");

    private final String displayName;
    private final String colorCode;
    private final String symbol;

    PillDesign(String displayName, String colorCode, String symbol) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.symbol = symbol;
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public String getSymbol() { return symbol; }
    public String getColoredName() { return colorCode + displayName; }

    public static PillDesign fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return TESLA;
    }
}
