package de.rolandsw.schedulemc.mushroom.blocks;

import net.minecraft.util.StringRepresentable;

/**
 * Temperatur-Modi für Klimalampe
 */
public enum TemperatureMode implements StringRepresentable {
    OFF("off", "§7Aus", "⚫"),
    COLD("cold", "§b Kalt", "❄"),
    WARM("warm", "§c Warm", "🔥");

    private final String name;
    private final String displayName;
    private final String icon;

    TemperatureMode(String name, String displayName, String icon) {
        this.name = name;
        this.displayName = displayName;
        this.icon = icon;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColoredName() {
        return displayName + " " + icon;
    }

    public String getIcon() {
        return icon;
    }

    /**
     * Nächster Modus im Zyklus
     */
    public TemperatureMode next() {
        return switch (this) {
            case OFF -> COLD;
            case COLD -> WARM;
            case WARM -> OFF;
        };
    }
}
