package de.rolandsw.schedulemc.economy;

import net.minecraft.network.chat.Component;

/**
 * Intervalle für Daueraufträge
 */
public enum RecurringPaymentInterval {
    DAILY("interval.daily", 1),
    WEEKLY("interval.weekly", 7),
    MONTHLY("interval.monthly", 30);

    private final String translationKey;
    private final int daysInterval;

    RecurringPaymentInterval(String translationKey, int daysInterval) {
        this.translationKey = translationKey;
        this.daysInterval = daysInterval;
    }

    public String getDisplayName() {
        return Component.translatable(translationKey).getString();
    }

    public int getDaysInterval() {
        return daysInterval;
    }

    /**
     * Gibt das nächste Intervall zurück (für Cycling-Buttons)
     */
    public RecurringPaymentInterval next() {
        return values()[(ordinal() + 1) % values().length];
    }

    /**
     * Findet Intervall anhand Display-Name
     */
    public static RecurringPaymentInterval fromDisplayName(String displayName) {
        for (RecurringPaymentInterval interval : values()) {
            if (interval.getDisplayName().equals(displayName)) {
                return interval;
            }
        }
        return MONTHLY; // Default
    }

    /**
     * Findet Intervall anhand der Anzahl Tage
     */
    public static RecurringPaymentInterval fromDays(int days) {
        for (RecurringPaymentInterval interval : values()) {
            if (interval.daysInterval == days) {
                return interval;
            }
        }
        return MONTHLY; // Default
    }
}
