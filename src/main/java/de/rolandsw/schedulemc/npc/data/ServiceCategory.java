package de.rolandsw.schedulemc.npc.data;

import net.minecraft.network.chat.Component;

/**
 * Enum für verschiedene Service-Kategorien
 * Nur relevant wenn NPCType == ABSCHLEPPER
 */
public enum ServiceCategory {
    ABSCHLEPPDIENST,
    PANNENHILFE,
    TAXI,
    NOTDIENST;

    public String getDisplayName() {
        return Component.translatable("enum.service_category." + this.name().toLowerCase()).getString();
    }

    public static ServiceCategory fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return ABSCHLEPPDIENST; // Default
    }
}
