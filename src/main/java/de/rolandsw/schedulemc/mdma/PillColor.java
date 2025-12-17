package de.rolandsw.schedulemc.mdma;

/**
 * Pillen-Farben für Ecstasy
 */
public enum PillColor {
    PINK("Pink", "§d", 0xFFAACC),
    BLAU("Blau", "§9", 0x5555FF),
    GRUEN("Grün", "§a", 0x55FF55),
    ORANGE("Orange", "§6", 0xFFAA00),
    GELB("Gelb", "§e", 0xFFFF55),
    WEISS("Weiß", "§f", 0xFFFFFF),
    ROT("Rot", "§c", 0xFF5555),
    LILA("Lila", "§5", 0xAA55AA);

    private final String displayName;
    private final String colorCode;
    private final int hexColor;

    PillColor(String displayName, String colorCode, int hexColor) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.hexColor = hexColor;
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public int getHexColor() { return hexColor; }
    public String getColoredName() { return colorCode + displayName; }

    public static PillColor fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return PINK;
    }
}
