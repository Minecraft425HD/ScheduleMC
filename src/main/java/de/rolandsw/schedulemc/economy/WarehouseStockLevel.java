package de.rolandsw.schedulemc.economy;

/**
 * Füllstands-Kategorien für Warehouses.
 *
 * Der Füllstand eines Warehouses beeinflusst die Preise:
 * - LEER: Preise steigen (Knappheit)
 * - NIEDRIG: Preise leicht erhöht
 * - NORMAL: Normalpreise
 * - VOLL: Preise sinken (Überangebot)
 * - UEBERFUELLT: Preise stark reduziert
 */
public enum WarehouseStockLevel {

    LEER("Leer", "§4", 0.0, 0.2, 1.40),
    NIEDRIG("Niedrig", "§c", 0.2, 0.4, 1.15),
    NORMAL("Normal", "§a", 0.4, 0.7, 1.00),
    VOLL("Voll", "§e", 0.7, 0.9, 0.90),
    UEBERFUELLT("Überfüllt", "§6", 0.9, 1.0, 0.75);

    private final String displayName;
    private final String colorCode;
    private final double minFillPercent;
    private final double maxFillPercent;
    private final double priceMultiplier;

    WarehouseStockLevel(String displayName, String colorCode,
                        double minFillPercent, double maxFillPercent,
                        double priceMultiplier) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.minFillPercent = minFillPercent;
        this.maxFillPercent = maxFillPercent;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    /**
     * @return Preis-Multiplikator für Einkaufspreise bei diesem Füllstand
     */
    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    /**
     * Bestimmt das Stock-Level basierend auf der Füllrate.
     *
     * @param fillPercent Füllrate (0.0 = leer, 1.0 = voll)
     * @return Passendes Stock-Level
     */
    public static WarehouseStockLevel fromFillPercent(double fillPercent) {
        fillPercent = Math.max(0.0, Math.min(1.0, fillPercent));

        for (WarehouseStockLevel level : values()) {
            if (fillPercent >= level.minFillPercent && fillPercent < level.maxFillPercent) {
                return level;
            }
        }
        return UEBERFUELLT; // >= 0.9
    }

    public String getFormattedName() {
        return colorCode + displayName + "§r";
    }
}
