package de.rolandsw.schedulemc.economy;

/**
 * Intervalle für Daueraufträge
 */
public enum RecurringPaymentInterval {
    DAILY("Täglich", 1),
    WEEKLY("Wöchentlich", 7),
    MONTHLY("Monatlich", 30);

    private final String displayName;
    private final int daysInterval;

    RecurringPaymentInterval(String displayName, int daysInterval) {
        this.displayName = displayName;
        this.daysInterval = daysInterval;
    }

    public String getDisplayName() {
        return displayName;
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
            if (interval.displayName.equals(displayName)) {
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
