package de.rolandsw.schedulemc.api.production;

import de.rolandsw.schedulemc.production.config.ProductionConfig;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Public Production API für ScheduleMC
 *
 * Ermöglicht externen Mods Zugriff auf das Produktions-System.
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>8 Produktionsketten (Tobacco, Cannabis, Coca, Poppy, Mushroom, MDMA, LSD, Meth)</li>
 *   <li>Dynamische Produktionsregistrierung zur Laufzeit</li>
 *   <li>Qualitätssystem (Bronze, Silver, Gold, Diamond, Platinum)</li>
 *   <li>Mehrstufige Verarbeitung (Trocknung, Fermentation, Raffination, etc.)</li>
 *   <li>Kategorisierung nach Produktionstyp</li>
 * </ul>
 *
 * <h2>Thread-Safety:</h2>
 * Alle Methoden sind Thread-Safe durch ConcurrentHashMap-basierte Registry.
 *
 * <h2>Performance:</h2>
 * Produktions-Lookups sind O(1) dank HashMap-basierter Registry.
 *
 * <h2>Produktionskategorien:</h2>
 * <ul>
 *   <li>PLANT - Pflanzen-basierte Produktion (Tobacco, Cannabis, Coca, Poppy)</li>
 *   <li>MUSHROOM - Pilz-Zucht (Psilocybe Cubensis, Golden Teacher)</li>
 *   <li>CHEMICAL - Chemische Synthese (MDMA, LSD, Meth)</li>
 * </ul>
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * IProductionAPI productionAPI = ScheduleMCAPI.getProductionAPI();
 *
 * // Alle Produktionen abrufen
 * Collection<ProductionConfig> allProductions = productionAPI.getAllProductions();
 *
 * // Produktionen nach Kategorie filtern
 * List<ProductionConfig> plants = productionAPI.getProductionsByCategory(ProductionCategory.PLANT);
 *
 * // Spezifische Produktion abrufen
 * ProductionConfig cannabis = productionAPI.getProduction("cannabis_indica");
 *
 * // Neue Produktion registrieren
 * ProductionConfig custom = new ProductionConfig.Builder("custom_plant", "Custom Plant")
 *     .basePrice(20.0)
 *     .growthTicks(3600)
 *     .category(ProductionCategory.PLANT)
 *     .build();
 * productionAPI.registerProduction(custom);
 * }</pre>
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public interface IProductionAPI {

    /**
     * Gibt eine Produktionskonfiguration anhand ihrer ID zurück.
     *
     * @param productionId Die Produktions-ID (z.B. "cannabis_indica")
     * @return Die ProductionConfig oder null wenn nicht gefunden
     * @throws IllegalArgumentException wenn productionId null ist
     */
    @Nullable
    ProductionConfig getProduction(String productionId);

    /**
     * Prüft ob eine Produktion mit dieser ID existiert.
     *
     * @param productionId Die Produktions-ID
     * @return true wenn Produktion existiert
     * @throws IllegalArgumentException wenn productionId null ist
     */
    boolean hasProduction(String productionId);

    /**
     * Gibt alle registrierten Produktionen zurück.
     *
     * @return Collection aller ProductionConfigs (unveränderbare Kopie)
     */
    Collection<ProductionConfig> getAllProductions();

    /**
     * Gibt alle Produktionen einer bestimmten Kategorie zurück.
     *
     * @param category Die Produktionskategorie (PLANT, MUSHROOM, CHEMICAL)
     * @return Liste aller Produktionen dieser Kategorie (kann leer sein)
     * @throws IllegalArgumentException wenn category null ist
     */
    List<ProductionConfig> getProductionsByCategory(ProductionConfig.ProductionCategory category);

    /**
     * Registriert eine neue Produktionskonfiguration.
     * <p>
     * WARNUNG: Überschreibt existierende Produktionen mit gleicher ID.
     *
     * @param config Die zu registrierende ProductionConfig
     * @throws IllegalArgumentException wenn config null ist
     */
    void registerProduction(ProductionConfig config);

    /**
     * Entfernt eine Produktionskonfiguration.
     *
     * @param productionId Die ID der zu entfernenden Produktion
     * @return true wenn erfolgreich entfernt, false wenn nicht existiert
     * @throws IllegalArgumentException wenn productionId null ist
     */
    boolean unregisterProduction(String productionId);

    /**
     * Gibt die Anzahl registrierter Produktionen zurück.
     *
     * @return Anzahl der Produktionen
     */
    int getProductionCount();

    /**
     * Startet Produktionsprozess an einer Position.
     * <p>
     * Nur verfügbar wenn an der Position ein ProcessingBlockEntity existiert.
     *
     * @param position Die BlockPos des Processing-Blocks
     * @param productionId Die ID der zu startenden Produktion
     * @return true wenn erfolgreich gestartet
     * @throws IllegalArgumentException wenn Parameter null sind
     */
    boolean startProduction(BlockPos position, String productionId);

    /**
     * Stoppt Produktionsprozess an einer Position.
     *
     * @param position Die BlockPos des Processing-Blocks
     * @return true wenn erfolgreich gestoppt
     * @throws IllegalArgumentException wenn position null ist
     */
    boolean stopProduction(BlockPos position);

    /**
     * Gibt den Produktionsfortschritt an einer Position zurück.
     *
     * @param position Die BlockPos des Processing-Blocks
     * @return Fortschritt in % (0.0 - 100.0), -1 wenn keine Produktion aktiv
     * @throws IllegalArgumentException wenn position null ist
     */
    double getProductionProgress(BlockPos position);
}
