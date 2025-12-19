package de.rolandsw.schedulemc.production.core;

/**
 * Interface für alle Qualitätsstufen
 * Implementiert von TobaccoQuality, CannabisQuality, MDMAQuality, etc.
 *
 * Definiert gemeinsame Eigenschaften:
 * - 4 Stufen: Schlecht, Standard, Gut, Premium
 * - Preis-Multiplikator
 * - Upgrade/Downgrade-Logik
 */
public interface ProductionQuality {

    /**
     * @return Anzeigename der Qualität (z.B. "Premium", "Gut")
     */
    String getDisplayName();

    /**
     * @return Farbcode für Minecraft-Formatierung
     */
    String getColorCode();

    /**
     * @return Farbiger Name kombiniert
     */
    default String getColoredName() {
        return getColorCode() + getDisplayName();
    }

    /**
     * @return Qualitätsstufe (0 = Schlecht, 1 = Standard, 2 = Gut, 3 = Premium)
     */
    int getLevel();

    /**
     * @return Preis-Multiplikator für diese Qualität (0.5 - 4.0)
     */
    double getPriceMultiplier();

    /**
     * @return Beschreibung der Qualität
     */
    String getDescription();

    /**
     * Verbessert die Qualität um eine Stufe
     * @return Verbesserte Qualität (oder gleich wenn bereits Premium)
     */
    ProductionQuality upgrade();

    /**
     * Verschlechtert die Qualität um eine Stufe
     * @return Verschlechterte Qualität (oder gleich wenn bereits Schlecht)
     */
    ProductionQuality downgrade();
}
