package de.rolandsw.schedulemc.production.core;

import de.rolandsw.schedulemc.economy.EconomyController;
import de.rolandsw.schedulemc.economy.ItemCategory;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Interface für alle Produktions-Typen (Sorten/Varianten)
 * Implementiert von TobaccoType, CannabisStrain, MushroomType, etc.
 *
 * Definiert gemeinsame Eigenschaften aller Drogen/Produkte:
 * - Anzeigename und Farbe
 * - Preis (Seeds, Spores, Chemicals)
 * - Wachstums-/Produktionszeit
 * - Ertrag
 *
 * UDPS Integration:
 * - getProductId() liefert den Identifier für den EconomyController
 * - getItemCategory() liefert die Kategorie für Preis-Grenzen und Risiko
 * - calculateDynamicPrice() delegiert an den EconomyController
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
     * @return Produkt-Identifier für den EconomyController (z.B. "CANNABIS_INDICA")
     */
    default String getProductId() {
        return getClass().getSimpleName().toUpperCase().replace("TYPE", "").replace("STRAIN", "")
                + "_" + toString();
    }

    /**
     * @return ItemCategory für Preis-Grenzen und Risiko-Aufschläge
     */
    default ItemCategory getItemCategory() {
        return ItemCategory.OTHER;
    }

    /**
     * Berechnet den Verkaufspreis basierend auf Typ und Qualität.
     *
     * Alte Formel - wird weiterhin als Fallback genutzt.
     * Neue Systeme sollten calculateDynamicPrice() verwenden.
     *
     * @param quality Qualitätsstufe des Produkts
     * @param amount Menge
     * @return Berechneter Preis
     */
    double calculatePrice(ProductionQuality quality, int amount);

    /**
     * Berechnet den Verkaufspreis über den EconomyController (UDPS).
     *
     * @param quality    Qualitätsstufe
     * @param amount     Menge
     * @param playerUUID Spieler-UUID für Tracking (nullable)
     * @return Dynamischer Verkaufspreis
     */
    default double calculateDynamicPrice(ProductionQuality quality, int amount,
                                          @Nullable UUID playerUUID) {
        try {
            return EconomyController.getInstance().getSellPrice(
                    getProductId(), quality, amount, playerUUID);
        } catch (Exception e) {
            // Fallback auf alte Formel wenn EconomyController nicht bereit
            return calculatePrice(quality, amount);
        }
    }
}
