package de.rolandsw.schedulemc.mdma;

import net.minecraft.network.chat.Component;

/**
 * Pillen-Farben für Ecstasy
 */
public enum PillColor {
    PINK("§d", 0xFFAACC),
    BLAU("§9", 0x5555FF),
    GRUEN("§a", 0x55FF55),
    ORANGE("§6", 0xFFAA00),
    GELB("§e", 0xFFFF55),
    WEISS("§f", 0xFFFFFF),
    ROT("§c", 0xFF5555),
    LILA("§5", 0xAA55AA);

    private final String colorCode;
    private final int hexColor;

    PillColor(String colorCode, int hexColor) {
        this.colorCode = colorCode;
        this.hexColor = hexColor;
    }

    public Component getDisplayName() {
        return Component.translatable("enum.pill_color." + this.name().toLowerCase());
    }

    public String getColorCode() { return colorCode; }
    public int getHexColor() { return hexColor; }

    public Component getColoredName() {
        return Component.literal(colorCode).append(getDisplayName());
    }

    public static PillColor fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return PINK;
    }
}
