package de.rolandsw.schedulemc.coffee;

import net.minecraft.network.chat.Component;

/**
 * Mahlgrade für Kaffee
 */
public enum CoffeeGrindSize {
    COARSE("coffee.grind.coarse.name", "§e", "coffee.grind.coarse.best_for"),
    MEDIUM("coffee.grind.medium.name", "§6", "coffee.grind.medium.best_for"),
    FINE("coffee.grind.fine.name", "§c", "coffee.grind.fine.best_for"),
    EXTRA_FINE("coffee.grind.extra_fine.name", "§4", "coffee.grind.extra_fine.best_for");

    private final String displayName;
    private final String colorCode;
    private final String bestFor;

    CoffeeGrindSize(String displayName, String colorCode, String bestFor) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.bestFor = bestFor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getBestFor() {
        return bestFor;
    }

    public String getColoredName() {
        return colorCode + Component.translatable(displayName).getString();
    }
}
