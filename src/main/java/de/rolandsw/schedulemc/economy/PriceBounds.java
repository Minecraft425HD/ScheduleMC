package de.rolandsw.schedulemc.economy;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * Definiert und erzwingt Preis-Grenzen für jede ItemCategory.
 *
 * Verhindert:
 * - Preise die zu niedrig fallen (Floor)
 * - Preise die zu hoch steigen (Ceiling)
 * - Negative Preise
 *
 * Die Grenzen werden aus der ItemCategory gelesen und können
 * durch Wirtschafts-Events temporär erweitert werden.
 */
public class PriceBounds {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Minimum-Preis der niemals unterschritten werden darf (1 Cent)
     */
    public static final double ABSOLUTE_MINIMUM = 0.01;

    /**
     * Maximum-Preis der niemals überschritten werden darf
     */
    public static final double ABSOLUTE_MAXIMUM = 1_000_000.0;

    private PriceBounds() {
        // Utility class
    }

    /**
     * Begrenzt einen berechneten Preis auf die erlaubten Grenzen der Kategorie.
     *
     * @param calculatedPrice der berechnete Rohpreis
     * @param basePrice       der Basis-Referenzpreis
     * @param category        die Item-Kategorie
     * @return begrenzter Preis innerhalb der erlaubten Grenzen
     */
    public static double clamp(double calculatedPrice, double basePrice, ItemCategory category) {
        double minPrice = basePrice * category.getMinPriceMultiplier();
        double maxPrice = basePrice * category.getMaxPriceMultiplier();

        // Absolute Grenzen sicherstellen
        minPrice = Math.max(ABSOLUTE_MINIMUM, minPrice);
        maxPrice = Math.min(ABSOLUTE_MAXIMUM, maxPrice);

        double clampedPrice = Math.max(minPrice, Math.min(maxPrice, calculatedPrice));

        if (clampedPrice != calculatedPrice) {
            LOGGER.debug("Price clamped for category {}: {:.2f} → {:.2f} (bounds: {:.2f}-{:.2f})",
                    category.name(), calculatedPrice, clampedPrice, minPrice, maxPrice);
        }

        return clampedPrice;
    }

    /**
     * Begrenzt mit temporärer Event-Erweiterung der Grenzen.
     *
     * @param calculatedPrice der berechnete Rohpreis
     * @param basePrice       der Basis-Referenzpreis
     * @param category        die Item-Kategorie
     * @param eventExpansion  um wie viel die Grenzen erweitert werden (z.B. 0.2 = 20% mehr Spielraum)
     * @return begrenzter Preis
     */
    public static double clampWithEvent(double calculatedPrice, double basePrice,
                                         ItemCategory category, double eventExpansion) {
        double minMult = category.getMinPriceMultiplier() * (1.0 - eventExpansion);
        double maxMult = category.getMaxPriceMultiplier() * (1.0 + eventExpansion);

        double minPrice = Math.max(ABSOLUTE_MINIMUM, basePrice * minMult);
        double maxPrice = Math.min(ABSOLUTE_MAXIMUM, basePrice * maxMult);

        return Math.max(minPrice, Math.min(maxPrice, calculatedPrice));
    }

    /**
     * Berechnet den aktuell erlaubten Preisbereich.
     *
     * @param basePrice der Basis-Referenzpreis
     * @param category  die Item-Kategorie
     * @return Array [minPrice, maxPrice]
     */
    public static double[] getRange(double basePrice, ItemCategory category) {
        double minPrice = Math.max(ABSOLUTE_MINIMUM, basePrice * category.getMinPriceMultiplier());
        double maxPrice = Math.min(ABSOLUTE_MAXIMUM, basePrice * category.getMaxPriceMultiplier());
        return new double[]{minPrice, maxPrice};
    }

    /**
     * Prüft ob ein Preis innerhalb der Grenzen liegt.
     *
     * @param price    zu prüfender Preis
     * @param basePrice Basis-Referenzpreis
     * @param category Item-Kategorie
     * @return true wenn Preis innerhalb der Grenzen
     */
    public static boolean isInBounds(double price, double basePrice, ItemCategory category) {
        double[] range = getRange(basePrice, category);
        return price >= range[0] && price <= range[1];
    }

    /**
     * Berechnet wie weit der aktuelle Preis von der Obergrenze entfernt ist (0.0-1.0).
     * Nützlich für UI-Anzeigen (Preis-Barometer).
     *
     * @param currentPrice aktueller Preis
     * @param basePrice    Basis-Referenzpreis
     * @param category     Item-Kategorie
     * @return 0.0 = am Minimum, 1.0 = am Maximum
     */
    public static double getPricePosition(double currentPrice, double basePrice, ItemCategory category) {
        double[] range = getRange(basePrice, category);
        if (range[1] - range[0] <= 0) return 0.5;
        return Math.max(0.0, Math.min(1.0, (currentPrice - range[0]) / (range[1] - range[0])));
    }
}
