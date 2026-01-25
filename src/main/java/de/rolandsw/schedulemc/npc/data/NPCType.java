package de.rolandsw.schedulemc.npc.data;

import net.minecraft.network.chat.Component;

/**
 * Enum für verschiedene NPC-Typen
 */
public enum NPCType {
    BEWOHNER,
    VERKAEUFER,
    POLIZEI,
    BANK,
    ABSCHLEPPER,
    // Aliase für englische Namen (API Kompatibilität)
    CITIZEN,
    MERCHANT,
    POLICE,
    BANKER,
    DRUG_DEALER;

    public Component getDisplayName() {
        return Component.translatable("enum.npc_type." + this.name().toLowerCase());
    }

    public static NPCType fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return BEWOHNER; // Default
    }
}
