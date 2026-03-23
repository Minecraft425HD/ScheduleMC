package de.rolandsw.schedulemc.mdma;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Pillen-Designs für Ecstasy
 */
public enum PillDesign {
    TESLA("§c", "T"),
    SUPERMAN("§9", "S"),
    TOTENKOPF("§8", "☠"),
    HERZ("§d", "♥"),
    SCHMETTERLING("§e", "🦋"),
    STERN("§6", "★"),
    PEACE("§a", "☮"),
    DIAMANT("§b", "◆");

    private final String colorCode;
    private final String symbol;

    PillDesign(String colorCode, String symbol) {
        this.colorCode = colorCode;
        this.symbol = symbol;
    }

    public Component getDisplayName() {
        return Component.translatable("enum.pill_design." + this.name().toLowerCase(Locale.ROOT));
    }

    public String getColorCode() { return colorCode; }
    public String getSymbol() { return symbol; }

    public Component getColoredName() {
        return Component.literal(colorCode).append(getDisplayName());
    }

    public static PillDesign fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return TESLA;
    }
}
