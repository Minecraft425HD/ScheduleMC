package de.rolandsw.schedulemc.npc.data;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Enum für verschiedene Bank-Kategorien
 * Nur relevant wenn NPCType == BANK
 */
public enum BankCategory {
    BANKER,
    BOERSE,
    KREDITBERATER;

    public Component getDisplayName() {
        return Component.translatable("enum.bank_category." + this.name().toLowerCase(Locale.ROOT));
    }

    public static BankCategory fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return BANKER; // Default
    }
}
