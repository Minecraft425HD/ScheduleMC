package de.rolandsw.schedulemc.npc.data;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Enum für verschiedene Verkäufer-Kategorien
 * Nur relevant wenn NPCType == MERCHANT
 */
public enum MerchantCategory {
    HARDWARE_STORE,
    WEAPONS_DEALER,
    GAS_STATION,
    GROCERY,
    STAFF_MANAGEMENT,
    ILLEGAL_DEALER,
    CAR_DEALER;

    public String getDisplayName() {
        return Component.translatable("enum.merchant_category." + this.name().toLowerCase(Locale.ROOT)).getString();
    }

    public static MerchantCategory fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return HARDWARE_STORE; // Default
    }
}
