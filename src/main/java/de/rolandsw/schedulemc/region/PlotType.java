package de.rolandsw.schedulemc.region;

import net.minecraft.network.chat.Component;

/**
 * Plot-Typen für verschiedene Gebäude-Arten
 *
 * SHOP-Plots sind nicht kaufbar und gehören der Staatskasse
 */
public enum PlotType {
    RESIDENTIAL(true, true),
    COMMERCIAL(true, true),
    SHOP(false, false),           // Nicht kaufbar, Staatseigentum
    PUBLIC(false, false),
    GOVERNMENT(false, false),
    PRISON(false, false),         // Gefängnis-Plot
    TOWING_YARD(true, true);      // Abschlepphof

    private final boolean canBePurchased;
    private final boolean canBeRented;

    PlotType(boolean canBePurchased, boolean canBeRented) {
        this.canBePurchased = canBePurchased;
        this.canBeRented = canBeRented;
    }

    public boolean canBePurchased() {
        return canBePurchased;
    }

    public boolean canBeRented() {
        return canBeRented;
    }

    public Component getDisplayName() {
        return Component.translatable("enum.plot_type." + this.name().toLowerCase());
    }

    public boolean isShop() {
        return this == SHOP;
    }

    public boolean isPrison() {
        return this == PRISON;
    }

    public boolean isTowingYard() {
        return this == TOWING_YARD;
    }
}
