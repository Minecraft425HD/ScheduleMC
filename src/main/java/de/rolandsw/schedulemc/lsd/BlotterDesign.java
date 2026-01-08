package de.rolandsw.schedulemc.lsd;

/**
 * Blotter-Design Varianten für LSD-Tabs
 */
public enum BlotterDesign {
    TOTENKOPF("Totenkopf", "§8", "☠"),
    SONNE("Sonne", "§e", "☀"),
    AUGE("Auge", "§5", "◉"),
    PILZ("Pilz", "§c", "🍄"),
    FAHRRAD("Fahrrad", "§b", "⚙"),  // Bicycle Day Reference
    MANDALA("Mandala", "§d", "✿"),
    BLITZ("Blitz", "§6", "⚡"),
    STERN("Stern", "§f", "★");

    private final String displayName;
    private final String colorCode;
    private final String symbol;

    BlotterDesign(String displayName, String colorCode, String symbol) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }

    public static BlotterDesign fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return TOTENKOPF;
    }
}
