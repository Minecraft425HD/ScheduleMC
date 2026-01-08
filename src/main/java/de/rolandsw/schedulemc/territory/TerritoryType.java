package de.rolandsw.schedulemc.territory;

/**
 * Territoriums-Typen (nur Farben)
 * Namen werden ausschließlich vom Spieler gesetzt
 */
public enum TerritoryType {
    COLOR_RED(0xFF4444),
    COLOR_GREEN(0x44FF44),
    COLOR_ORANGE(0xFFAA00),
    COLOR_BLUE(0x4444FF),
    COLOR_YELLOW(0xFFFF44),
    COLOR_PURPLE(0xFF44FF),
    COLOR_CYAN(0x44FFFF),
    COLOR_GRAY(0xAAAAAA),
    COLOR_DARK_RED(0xAA0000),
    COLOR_LIME(0x88FF44);

    private final int color;

    TerritoryType(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    /**
     * Gibt Farb-Code für Chat zurück
     */
    public String getColorCode() {
        return switch (this) {
            case COLOR_RED, COLOR_DARK_RED -> "§c";
            case COLOR_GREEN, COLOR_LIME -> "§a";
            case COLOR_ORANGE -> "§6";
            case COLOR_YELLOW -> "§e";
            case COLOR_PURPLE -> "§d";
            case COLOR_CYAN -> "§b";
            case COLOR_BLUE -> "§9";
            case COLOR_GRAY -> "§7";
        };
    }

    /**
     * Gibt Anzeige-Name zurück (Farbe #1, Farbe #2, ...)
     */
    public String getDisplayName() {
        return switch (this) {
            case COLOR_RED -> "Farbe #1 (Rot)";
            case COLOR_GREEN -> "Farbe #2 (Grün)";
            case COLOR_ORANGE -> "Farbe #3 (Orange)";
            case COLOR_BLUE -> "Farbe #4 (Blau)";
            case COLOR_YELLOW -> "Farbe #5 (Gelb)";
            case COLOR_PURPLE -> "Farbe #6 (Lila)";
            case COLOR_CYAN -> "Farbe #7 (Cyan)";
            case COLOR_GRAY -> "Farbe #8 (Grau)";
            case COLOR_DARK_RED -> "Farbe #9 (Dunkelrot)";
            case COLOR_LIME -> "Farbe #10 (Hellgrün)";
        };
    }
}
