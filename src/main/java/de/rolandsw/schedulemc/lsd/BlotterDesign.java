package de.rolandsw.schedulemc.lsd;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Blotter-Design Varianten für LSD-Tabs
 */
public enum BlotterDesign {
    TOTENKOPF("§8", "☠"),
    SONNE("§e", "☀"),
    AUGE("§5", "◉"),
    PILZ("§c", "🍄"),
    FAHRRAD("§b", "⚙"),  // Bicycle Day Reference
    MANDALA("§d", "✿"),
    BLITZ("§6", "⚡"),
    STERN("§f", "★");

    private final String colorCode;
    private final String symbol;

    BlotterDesign(String colorCode, String symbol) {
        this.colorCode = colorCode;
        this.symbol = symbol;
    }

    public Component getDisplayName() {
        return Component.translatable("enum.blotter_design." + this.name().toLowerCase(Locale.ROOT));
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getSymbol() {
        return symbol;
    }

    public Component getColoredName() {
        return Component.literal(colorCode).append(getDisplayName());
    }

    public static BlotterDesign fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return TOTENKOPF;
    }
}
