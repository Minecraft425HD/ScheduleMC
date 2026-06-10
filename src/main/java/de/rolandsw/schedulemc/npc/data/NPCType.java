package de.rolandsw.schedulemc.npc.data;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Enum für verschiedene NPC-Typen
 */
public enum NPCType {
    CITIZEN,
    MERCHANT,
    POLICE,
    BANK,
    TOW_TRUCK_DRIVER,
    BANKER,
    DRUG_DEALER;

    public Component getDisplayName() {
        return Component.translatable("enum.npc_type." + this.name().toLowerCase(Locale.ROOT));
    }

    public static NPCType fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return CITIZEN; // Default
    }
}
