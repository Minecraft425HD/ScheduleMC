package de.rolandsw.schedulemc.level;

/**
 * Kategorien von freischaltbaren Inhalten im Level-System.
 */
public enum UnlockCategory {

    PRODUCTION_CHAIN("Production Chain", "§a", "Produce new products"),
    MACHINE("Machine", "§b", "Use better machines"),
    POT("Pot/Container", "§6", "Increase grow capacity"),
    QUALITY("Quality Tier", "§d", "Reach higher quality"),
    STRAIN("Strain/Variant", "§e", "Grow new strains"),
    PROCESSING("Processing Method", "§5", "Unlock new processing methods"),
    VEHICLE("Vehicle", "§7", "Buy better vehicles"),
    SHOP_FEATURE("Shop Feature", "§f", "Unlock advanced shop features"),
    ECONOMY_FEATURE("Economy Feature", "§6", "Unlock advanced economy features"),
    GANG("Gang", "§c", "Gang system features");

    private final String displayName;
    private final String colorCode;
    private final String description;

    UnlockCategory(String displayName, String colorCode, String description) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedName() {
        return colorCode + displayName + "§r";
    }
}
