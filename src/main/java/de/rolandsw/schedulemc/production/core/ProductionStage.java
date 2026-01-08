package de.rolandsw.schedulemc.production.core;

/**
 * Repräsentiert eine Produktionsphase in der Herstellungskette
 *
 * Beispiele:
 * - Tobacco: GROWING → DRYING → FERMENTING → PACKAGING
 * - Cannabis: GROWING → FLOWERING → TRIMMING → CURING → PACKAGING
 * - Mushroom: INOCULATION → INCUBATION → FRUITING → DRYING
 * - Coca: GROWING → EXTRACTION → REFINEMENT → COOKING
 */
public interface ProductionStage {

    /**
     * @return Name der Phase (z.B. "DRYING", "FERMENTING")
     */
    String getStageName();

    /**
     * @return Dauer dieser Phase in Ticks
     */
    int getDurationTicks();

    /**
     * @return Nächste Phase in der Kette (oder null wenn letzte Phase)
     */
    ProductionStage getNextStage();

    /**
     * @return Ob diese Phase Qualität verbessern kann
     */
    default boolean canImproveQuality() {
        return false;
    }

    /**
     * @return Ob diese Phase Wasser benötigt
     */
    default boolean requiresWater() {
        return false;
    }

    /**
     * @return Ob diese Phase Licht benötigt
     */
    default boolean requiresLight() {
        return false;
    }

    /**
     * @return Ob diese Phase Temperatur-kontrolliert sein muss
     */
    default boolean requiresTemperatureControl() {
        return false;
    }
}
