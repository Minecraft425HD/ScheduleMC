package de.rolandsw.schedulemc.region;

/**
 * Plot-Typen für verschiedene Gebäude-Arten
 *
 * SHOP-Plots sind nicht kaufbar und gehören der Staatskasse
 */
public enum PlotType {
    RESIDENTIAL("Wohngebäude", true, true),
    COMMERCIAL("Gewerbefläche", true, true),
    SHOP("Laden", false, false),           // Nicht kaufbar, Staatseigentum
    PUBLIC("Öffentlich", false, false),
    GOVERNMENT("Regierung", false, false),
    PRISON("Gefängnis", false, false);     // Gefängnis-Plot

    private final String displayName;
    private final boolean canBePurchased;
    private final boolean canBeRented;

    PlotType(String displayName, boolean canBePurchased, boolean canBeRented) {
        this.displayName = displayName;
        this.canBePurchased = canBePurchased;
        this.canBeRented = canBeRented;
    }

    public boolean canBePurchased() {
        return canBePurchased;
    }

    public boolean canBeRented() {
        return canBeRented;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isShop() {
        return this == SHOP;
    }

    public boolean isPrison() {
        return this == PRISON;
    }
}
