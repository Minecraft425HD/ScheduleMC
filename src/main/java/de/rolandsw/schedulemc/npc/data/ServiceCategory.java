package de.rolandsw.schedulemc.npc.data;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Enum für verschiedene Service-Kategorien
 * Nur relevant wenn NPCType == TOW_TRUCK_DRIVER
 */
public enum ServiceCategory {
    TOWING_SERVICE,
    PANNENHILFE,
    TAXI,
    NOTDIENST;

    public String getDisplayName() {
        return Component.translatable("enum.service_category." + this.name().toLowerCase(Locale.ROOT)).getString();
    }

    public static ServiceCategory fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return TOWING_SERVICE; // Default
    }
}
