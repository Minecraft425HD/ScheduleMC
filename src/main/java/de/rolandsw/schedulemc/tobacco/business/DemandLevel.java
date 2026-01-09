package de.rolandsw.schedulemc.tobacco.business;

import net.minecraft.network.chat.Component;

/**
 * Nachfrage-Level für Tabak-Käufe
 */
public enum DemandLevel {
    LOW(0.7, "§c🔻 "),
    MEDIUM(1.0, "§e➡️ "),
    HIGH(1.3, "§a📈 ");

    private final double priceMultiplier;
    private final String prefix;

    DemandLevel(double priceMultiplier, String prefix) {
        this.priceMultiplier = priceMultiplier;
        this.prefix = prefix;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public Component getDisplayName() {
        return Component.literal(prefix).append(Component.translatable("enum.demand_level." + this.name().toLowerCase()));
    }
}
