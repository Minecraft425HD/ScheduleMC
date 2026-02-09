package de.rolandsw.schedulemc.production;

/**
 * Enum für verschiedene Produktionsgrößen
 * Reduziert Code-Duplikation in Block Entities
 */
public enum ProductionSize {
    SMALL("Small", 6, 500, 1.0),
    MEDIUM("Medium", 12, 1000, 1.5),
    BIG("Big", 24, 2000, 2.0);

    private final String displayName;
    private final int capacity;
    private final int maxFuel;
    private final double speedMultiplier;

    ProductionSize(String displayName, int capacity, int maxFuel, double speedMultiplier) {
        this.displayName = displayName;
        this.capacity = capacity;
        this.maxFuel = maxFuel;
        this.speedMultiplier = speedMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Kapazität (Anzahl Items gleichzeitig)
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Maximaler Brennstoff-Level
     */
    public int getMaxFuel() {
        return maxFuel;
    }

    /**
     * Geschwindigkeits-Multiplikator (für zukünftige Verwendung)
     */
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    /**
     * Berechne Verarbeitungsgeschwindigkeit basierend auf Basiszeit
     * @param baseTime Basis-Verarbeitungszeit in Ticks
     * @return Angepasste Verarbeitungszeit
     */
    public int getProcessingTime(int baseTime) {
        if (speedMultiplier <= 0) return baseTime;
        return (int) (baseTime / speedMultiplier);
    }
}
