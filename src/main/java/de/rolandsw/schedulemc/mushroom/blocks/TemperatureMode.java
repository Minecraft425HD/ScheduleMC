package de.rolandsw.schedulemc.mushroom.blocks;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * Temperatur-Modi für ClimateLamp
 */
public enum TemperatureMode implements StringRepresentable {
    OFF("off", "⚫"),
    COLD("cold", "❄"),
    WARM("warm", "🔥");

    private final String name;
    private final String icon;

    TemperatureMode(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public Component getDisplayName() {
        return Component.translatable("enum.temperature_mode." + this.name().toLowerCase(Locale.ROOT));
    }

    public Component getColoredName() {
        return getDisplayName().copy().append(Component.literal(" " + icon));
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
