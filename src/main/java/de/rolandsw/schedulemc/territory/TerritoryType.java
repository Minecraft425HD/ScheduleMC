package de.rolandsw.schedulemc.territory;

/**
 * Territoriums-Typen
 */
public enum TerritoryType {
    GANG_TERRITORY("Gang-Gebiet", "👥", 0xFF4444),
    SAFE_ZONE("Schutzzone", "🛡️", 0x44FF44),
    PVP_ZONE("PVP-Zone", "⚔️", 0xFF4444),
    NEUTRAL("Neutral", "⚪", 0xAAAAAA),
    MARKET("Marktplatz", "💰", 0xFFAA00),
    FACTORY("Industriegebiet", "🏭", 0x888888),
    FARM("Farmgebiet", "🌾", 0x88FF44),
    RED_ZONE("Gefahrenzone", "☠️", 0xAA0000),
    EVENT_ZONE("Event-Zone", "🎉", 0xFF00FF),
    POLICE_STATION("Polizeirevier", "🚔", 0x0044FF);

    private final String displayName;
    private final String emoji;
    private final int color;

    TerritoryType(String displayName, String emoji, int color) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getColor() {
        return color;
    }

    public String getFormattedName() {
        return emoji + " §f" + displayName;
    }

    /**
     * Gibt Farb-Code für Chat zurück
     */
    public String getColorCode() {
        return switch (this) {
            case GANG_TERRITORY, PVP_ZONE, RED_ZONE -> "§c";
            case SAFE_ZONE, FARM -> "§a";
            case MARKET -> "§6";
            case FACTORY -> "§7";
            case EVENT_ZONE -> "§d";
            case POLICE_STATION -> "§9";
            default -> "§f";
        };
    }
}
