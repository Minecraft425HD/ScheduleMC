package de.rolandsw.schedulemc.production.core;

/**
 * Interface für alle Produktions-Typen (Sorten/Varianten)
 * Implementiert von TobaccoType, CannabisStrain, MushroomType, etc.
 *
 * Definiert gemeinsame Eigenschaften aller Drogen/Produkte:
 * - Anzeigename und Farbe
 * - Preis (Seeds, Spores, Chemicals)
 * - Wachstums-/Produktionszeit
 * - Ertrag
 */
public interface ProductionType {

    /**
     * @return Anzeigename der Sorte (z.B. "Virginia", "Indica", "Cubensis")
     */
    String getDisplayName();

    /**
     * @return Farbcode für Minecraft-Formatierung (z.B. "§e", "§5")
     */
    String getColorCode();

    /**
     * @return Farbiger Name kombiniert (z.B. "§eVirginia")
     */
    default String getColoredName() {
        return getColorCode() + getDisplayName();
    }

    /**
     * @return Preis für Saatgut/Sporen/Chemikalien
     */
    double getBasePrice();

    /**
     * @return Wachstums-/Produktionszeit in Ticks
     */
    int getGrowthTicks();

    /**
     * @return Basis-Ertrag beim Ernten (Gramm, Stück, etc.)
     */
    int getBaseYield();

    /**
     * @return Registry-Name für Minecraft (lowercase)
     */
    default String getRegistryName() {
        return toString().toLowerCase();
    }

    /**
     * Berechnet den Verkaufspreis basierend auf Typ und Qualität
     *
     * @param quality Qualitätsstufe des Produkts
     * @param amount Menge
     * @return Berechneter Preis
     */
    double calculatePrice(ProductionQuality quality, int amount);
}
