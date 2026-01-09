package de.rolandsw.schedulemc.npc.data;

import net.minecraft.network.chat.Component;

/**
 * Enum für verschiedene Verkäufer-Kategorien
 * Nur relevant wenn NPCType == VERKAEUFER
 */
public enum MerchantCategory {
    BAUMARKT(Component.translatable("enum.merchant_category.baumarkt").getString()),
    WAFFENHAENDLER(Component.translatable("enum.merchant_category.waffenhaendler").getString()),
    TANKSTELLE(Component.translatable("enum.merchant_category.tankstelle").getString()),
    LEBENSMITTEL(Component.translatable("enum.merchant_category.lebensmittel").getString()),
    PERSONALMANAGEMENT(Component.translatable("enum.merchant_category.personalmanagement").getString()),
    ILLEGALER_HAENDLER(Component.translatable("enum.merchant_category.illegaler_haendler").getString()),
    AUTOHAENDLER(Component.translatable("enum.merchant_category.autohaendler").getString());

    private final String displayName;

    MerchantCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MerchantCategory fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return BAUMARKT; // Default
    }
}
