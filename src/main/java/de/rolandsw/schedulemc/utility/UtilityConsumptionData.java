package de.rolandsw.schedulemc.utility;

/**
 * Verbrauchsdaten für einen Block-Typ
 *
 * @param electricityActive Stromverbrauch in kWh pro Tag wenn aktiv (100%)
 * @param electricityIdle   Stromverbrauch in kWh pro Tag wenn idle (50%)
 * @param waterActive       Wasserverbrauch in Liter pro Tag wenn aktiv (100%)
 * @param waterIdle         Wasserverbrauch in Liter pro Tag wenn idle (50%)
 * @param category          Kategorie für Gruppierung (LIGHTING, CLIMATE, PROCESSING, etc.)
 */
public record UtilityConsumptionData(
        double electricityActive,
        double electricityIdle,
        double waterActive,
        double waterIdle,
        UtilityCategory category
) {

    /**
     * Erstellt Verbrauchsdaten mit automatischer 50% idle-Berechnung
     */
    public static UtilityConsumptionData of(double electricity, double water, UtilityCategory category) {
        return new UtilityConsumptionData(
                electricity,
                electricity * 0.5,
                water,
                water * 0.5,
                category
        );
    }

    /**
     * Erstellt Verbrauchsdaten für Blöcke die immer gleich viel verbrauchen (z.B. Töpfe)
     */
    public static UtilityConsumptionData constant(double electricity, double water, UtilityCategory category) {
        return new UtilityConsumptionData(
                electricity,
                electricity,
                water,
                water,
                category
        );
    }

    /**
     * Erstellt Verbrauchsdaten nur für Strom
     */
    public static UtilityConsumptionData electricityOnly(double active, UtilityCategory category) {
        return new UtilityConsumptionData(active, active * 0.5, 0, 0, category);
    }

    /**
     * Erstellt Verbrauchsdaten nur für Wasser
     */
    public static UtilityConsumptionData waterOnly(double active, UtilityCategory category) {
        return new UtilityConsumptionData(0, 0, active, active * 0.5, category);
    }

    /**
     * Berechnet den aktuellen Verbrauch basierend auf Aktivitätsstatus
     */
    public double getCurrentElectricity(boolean isActive) {
        return isActive ? electricityActive : electricityIdle;
    }

    public double getCurrentWater(boolean isActive) {
        return isActive ? waterActive : waterIdle;
    }
}
