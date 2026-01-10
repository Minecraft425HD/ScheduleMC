package de.rolandsw.schedulemc.npc.data;

import net.minecraft.network.chat.Component;

/**
 * Enum für verschiedene Verkäufer-Kategorien
 * Nur relevant wenn NPCType == VERKAEUFER
 */
public enum MerchantCategory {
    BAUMARKT,
    WAFFENHAENDLER,
    TANKSTELLE,
    LEBENSMITTEL,
    PERSONALMANAGEMENT,
    ILLEGALER_HAENDLER,
    AUTOHAENDLER;

    public String getDisplayName() {
        return Component.translatable("enum.merchant_category." + this.name().toLowerCase()).getString();
    }

    public static MerchantCategory fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return BAUMARKT; // Default
    }
}
