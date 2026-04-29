package de.rolandsw.schedulemc.utility;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry für Verbrauchswerte aller Blöcke
 *
 * Werte sind in:
 * - Strom: kWh pro Minecraft-Tag (20 Minuten Echtzeit)
 * - Wasser: Liter pro Minecraft-Tag
 *
 * Aktiv = Block verarbeitet gerade etwas (100% Verbrauch)
 * Idle = Block steht still, ist aber eingeschaltet (50% Verbrauch)
 */
public class UtilityRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    // SICHERHEIT: ConcurrentHashMap für Thread-safe Zugriff
    private static final Map<Block, UtilityConsumptionData> CONSUMPTION_MAP = new ConcurrentHashMap<>();
    private static final Map<String, UtilityConsumptionData> CONSUMPTION_BY_ID = new ConcurrentHashMap<>();
    private static final Set<String> DEFERRED_RESOLVE_LOGGED = ConcurrentHashMap.newKeySet();

    // Keine Instanziierung
    private UtilityRegistry() {}

    /**
     * Registriert einen Block mit Verbrauchsdaten
     */
    public static void register(Block block, UtilityConsumptionData data) {
        CONSUMPTION_MAP.put(block, data);
    }

    /**
     * Registriert einen Block via Registry-ID (für späte Registrierung)
     */
    public static void registerById(String registryId, UtilityConsumptionData data) {
        CONSUMPTION_BY_ID.put(registryId, data);
    }

    /**
     * Holt Verbrauchsdaten für einen Block
     */
    public static Optional<UtilityConsumptionData> getConsumption(Block block) {
        return Optional.ofNullable(CONSUMPTION_MAP.get(block));
    }

    /**
     * Holt Verbrauchsdaten via Registry-ID
     */
    public static Optional<UtilityConsumptionData> getConsumptionById(String registryId) {
        return Optional.ofNullable(CONSUMPTION_BY_ID.get(registryId));
    }

    /**
     * Prüft ob ein Block Utilities verbraucht
     */
    public static boolean isConsumer(Block block) {
        return CONSUMPTION_MAP.containsKey(block);
    }

    /**
     * Gibt alle registrierten Blöcke zurück
     */
    public static Map<Block, UtilityConsumptionData> getAllConsumers() {
        return new HashMap<>(CONSUMPTION_MAP);
    }

    /**
     * Gibt alle bekannten Registry-IDs zurück (aus registerById — unabhängig vom Config-Filter).
     * Wird von UtilityBlockListConfigScreen für Autocomplete verwendet.
     */
    public static Set<String> getAllRegisteredIds() {
        return java.util.Collections.unmodifiableSet(CONSUMPTION_BY_ID.keySet());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STANDARD-VERBRAUCHSWERTE
    // Werden beim Mod-Start via registerDefaults() geladen
    // ═══════════════════════════════════════════════════════════════════════════

    public static void registerDefaults() {
        // ─────────────────────────────────────────────────────────────
        // GROW LIGHTS (Beleuchtung)
        // Hoher Stromverbrauch, kein Wasser
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:basic_grow_light_slab",
                UtilityConsumptionData.electricityOnly(50, UtilityCategory.LIGHTING));
        registerById("schedulemc:advanced_grow_light_slab",
                UtilityConsumptionData.electricityOnly(100, UtilityCategory.LIGHTING));
        registerById("schedulemc:premium_grow_light_slab",
                UtilityConsumptionData.electricityOnly(200, UtilityCategory.LIGHTING));

        // ─────────────────────────────────────────────────────────────
        // CLIMATE_LAMPN (Mushroom - Klimatisierung)
        // Strom + etwas Wasser für Luftfeuchtigkeit
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:klimalampe_small",
                UtilityConsumptionData.of(30, 5, UtilityCategory.CLIMATE));
        registerById("schedulemc:klimalampe_medium",
                UtilityConsumptionData.of(60, 10, UtilityCategory.CLIMATE));
        registerById("schedulemc:klimalampe_large",
                UtilityConsumptionData.of(120, 20, UtilityCategory.CLIMATE));

        // ─────────────────────────────────────────────────────────────
        // TÖPFE (Bewässerung)
        // Kein Strom, konstanter Wasserverbrauch
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:terracotta_pot",
                UtilityConsumptionData.constant(0, 10, UtilityCategory.IRRIGATION));
        registerById("schedulemc:ceramic_pot",
                UtilityConsumptionData.constant(0, 15, UtilityCategory.IRRIGATION));
        registerById("schedulemc:iron_pot",
                UtilityConsumptionData.constant(0, 20, UtilityCategory.IRRIGATION));
        registerById("schedulemc:golden_pot",
                UtilityConsumptionData.constant(0, 25, UtilityCategory.IRRIGATION));

        // WaterTank (Pilze)
        registerById("schedulemc:wassertank",
                UtilityConsumptionData.constant(5, 50, UtilityCategory.IRRIGATION));

        // ─────────────────────────────────────────────────────────────
        // VENTILATOREN
        // Konstanter Stromverbrauch (kein BlockEntity → immer aktiv)
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:fan_tier1",
                UtilityConsumptionData.constant(20, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:fan_tier2",
                UtilityConsumptionData.constant(50, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:fan_tier3",
                UtilityConsumptionData.constant(100, 0, UtilityCategory.MECHANICAL));

        // ─────────────────────────────────────────────────────────────
        // TROCKNUNGSGESTELLE (Tabak)
        // Niedriger Stromverbrauch für Ventilation
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:small_drying_rack",
                UtilityConsumptionData.of(10, 0, UtilityCategory.DRYING));
        registerById("schedulemc:medium_drying_rack",
                UtilityConsumptionData.of(20, 0, UtilityCategory.DRYING));
        registerById("schedulemc:big_drying_rack",
                UtilityConsumptionData.of(40, 0, UtilityCategory.DRYING));

        // ─────────────────────────────────────────────────────────────
        // FERMENTIERUNGSFÄSSER (Tabak)
        // Niedriger Strom für Temperaturkontrolle
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:small_fermentation_barrel",
                UtilityConsumptionData.of(15, 5, UtilityCategory.FERMENTATION));
        registerById("schedulemc:medium_fermentation_barrel",
                UtilityConsumptionData.of(30, 10, UtilityCategory.FERMENTATION));
        registerById("schedulemc:big_fermentation_barrel",
                UtilityConsumptionData.of(60, 20, UtilityCategory.FERMENTATION));

        // LSD Fermentationstank
        registerById("schedulemc:fermentations_tank",
                UtilityConsumptionData.of(80, 30, UtilityCategory.FERMENTATION));

        // ─────────────────────────────────────────────────────────────
        // VERPACKUNGSTISCHE (Tabak)
        // Sehr niedriger Verbrauch
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:small_packaging_table",
                UtilityConsumptionData.of(5, 0, UtilityCategory.PACKAGING));
        registerById("schedulemc:medium_packaging_table",
                UtilityConsumptionData.of(10, 0, UtilityCategory.PACKAGING));
        registerById("schedulemc:large_packaging_table",
                UtilityConsumptionData.of(20, 0, UtilityCategory.PACKAGING));

        // Sink (Waschbecken)
        registerById("schedulemc:sink",
                UtilityConsumptionData.of(5, 30, UtilityCategory.OTHER));

        // ─────────────────────────────────────────────────────────────
        // COCA VERARBEITUNG
        // Chemische Extraktion - mittlerer Verbrauch
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:small_extraction_vat",
                UtilityConsumptionData.of(40, 30, UtilityCategory.CHEMICAL));
        registerById("schedulemc:medium_extraction_vat",
                UtilityConsumptionData.of(80, 60, UtilityCategory.CHEMICAL));
        registerById("schedulemc:big_extraction_vat",
                UtilityConsumptionData.of(160, 120, UtilityCategory.CHEMICAL));

        registerById("schedulemc:small_refinery",
                UtilityConsumptionData.of(60, 20, UtilityCategory.CHEMICAL));
        registerById("schedulemc:medium_refinery",
                UtilityConsumptionData.of(120, 40, UtilityCategory.CHEMICAL));
        registerById("schedulemc:big_refinery",
                UtilityConsumptionData.of(240, 80, UtilityCategory.CHEMICAL));

        registerById("schedulemc:crack_kocher",
                UtilityConsumptionData.of(100, 10, UtilityCategory.CHEMICAL));

        // ─────────────────────────────────────────────────────────────
        // POPPY/HEROIN VERARBEITUNG
        // Mittlerer Verbrauch
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:ritzmaschine",
                UtilityConsumptionData.of(20, 5, UtilityCategory.MECHANICAL));
        registerById("schedulemc:opium_presse",
                UtilityConsumptionData.of(50, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:kochstation",
                UtilityConsumptionData.of(80, 40, UtilityCategory.CHEMICAL));
        registerById("schedulemc:heroin_raffinerie",
                UtilityConsumptionData.of(150, 50, UtilityCategory.CHEMICAL));

        // ─────────────────────────────────────────────────────────────
        // METH LAB
        // SEHR hoher Verbrauch - Breaking Bad style
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:chemie_mixer",
                UtilityConsumptionData.of(100, 30, UtilityCategory.CHEMICAL));
        registerById("schedulemc:reduktionskessel",
                UtilityConsumptionData.of(250, 50, UtilityCategory.CHEMICAL));
        registerById("schedulemc:kristallisator",
                UtilityConsumptionData.of(180, 100, UtilityCategory.CHEMICAL));
        registerById("schedulemc:vakuum_trockner",
                UtilityConsumptionData.of(120, 0, UtilityCategory.DRYING));

        // ─────────────────────────────────────────────────────────────
        // LSD LAB
        // Hoher Verbrauch - Präzisionslabor
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:destillations_apparat",
                UtilityConsumptionData.of(150, 60, UtilityCategory.CHEMICAL));
        registerById("schedulemc:mikro_dosierer",
                UtilityConsumptionData.of(80, 10, UtilityCategory.CHEMICAL));
        registerById("schedulemc:perforations_presse",
                UtilityConsumptionData.of(40, 0, UtilityCategory.MECHANICAL));

        // ─────────────────────────────────────────────────────────────
        // MDMA LAB
        // Hoher Verbrauch
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:reaktions_kessel",
                UtilityConsumptionData.of(200, 40, UtilityCategory.CHEMICAL));
        registerById("schedulemc:trocknungs_ofen",
                UtilityConsumptionData.of(150, 0, UtilityCategory.DRYING));
        registerById("schedulemc:pillen_presse",
                UtilityConsumptionData.of(60, 0, UtilityCategory.MECHANICAL));

        // ─────────────────────────────────────────────────────────────
        // CANNABIS VERARBEITUNG
        // Mittlerer Verbrauch
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:cannabis_trimm_station",
                UtilityConsumptionData.of(10, 5, UtilityCategory.MECHANICAL));
        registerById("schedulemc:cannabis_curing_glas",
                UtilityConsumptionData.of(5, 0, UtilityCategory.FERMENTATION));
        registerById("schedulemc:cannabis_hash_presse",
                UtilityConsumptionData.of(80, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:cannabis_oel_extraktor",
                UtilityConsumptionData.of(120, 20, UtilityCategory.CHEMICAL));

        // ─────────────────────────────────────────────────────────────
        // BIER-PRODUKTION
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:malting_station",
                UtilityConsumptionData.of(30, 20, UtilityCategory.MECHANICAL));
        registerById("schedulemc:mash_tun",
                UtilityConsumptionData.of(50, 60, UtilityCategory.MECHANICAL));
        registerById("schedulemc:small_brew_kettle",
                UtilityConsumptionData.of(80, 40, UtilityCategory.MECHANICAL));
        registerById("schedulemc:medium_brew_kettle",
                UtilityConsumptionData.of(120, 60, UtilityCategory.MECHANICAL));
        registerById("schedulemc:large_brew_kettle",
                UtilityConsumptionData.of(200, 100, UtilityCategory.MECHANICAL));
        registerById("schedulemc:small_beer_fermentation_tank",
                UtilityConsumptionData.of(20, 10, UtilityCategory.FERMENTATION));
        registerById("schedulemc:medium_beer_fermentation_tank",
                UtilityConsumptionData.of(40, 20, UtilityCategory.FERMENTATION));
        registerById("schedulemc:large_beer_fermentation_tank",
                UtilityConsumptionData.of(80, 40, UtilityCategory.FERMENTATION));
        registerById("schedulemc:small_conditioning_tank",
                UtilityConsumptionData.of(15, 5, UtilityCategory.FERMENTATION));
        registerById("schedulemc:medium_conditioning_tank",
                UtilityConsumptionData.of(30, 10, UtilityCategory.FERMENTATION));
        registerById("schedulemc:large_conditioning_tank",
                UtilityConsumptionData.of(60, 20, UtilityCategory.FERMENTATION));
        registerById("schedulemc:beer_bottling_station",
                UtilityConsumptionData.of(20, 5, UtilityCategory.PACKAGING));

        // ─────────────────────────────────────────────────────────────
        // WEIN-PRODUKTION
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:crushing_station",
                UtilityConsumptionData.of(30, 10, UtilityCategory.MECHANICAL));
        registerById("schedulemc:small_wine_press",
                UtilityConsumptionData.of(20, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:medium_wine_press",
                UtilityConsumptionData.of(40, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:large_wine_press",
                UtilityConsumptionData.of(80, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:small_fermentation_tank",
                UtilityConsumptionData.of(15, 5, UtilityCategory.FERMENTATION));
        registerById("schedulemc:medium_fermentation_tank",
                UtilityConsumptionData.of(30, 10, UtilityCategory.FERMENTATION));
        registerById("schedulemc:large_fermentation_tank",
                UtilityConsumptionData.of(60, 20, UtilityCategory.FERMENTATION));
        registerById("schedulemc:small_aging_barrel",
                UtilityConsumptionData.of(5, 0, UtilityCategory.FERMENTATION));
        registerById("schedulemc:medium_aging_barrel",
                UtilityConsumptionData.of(10, 0, UtilityCategory.FERMENTATION));
        registerById("schedulemc:large_aging_barrel",
                UtilityConsumptionData.of(20, 0, UtilityCategory.FERMENTATION));
        registerById("schedulemc:wine_bottling_station",
                UtilityConsumptionData.of(15, 5, UtilityCategory.PACKAGING));

        // ─────────────────────────────────────────────────────────────
        // HONIG-PRODUKTION
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:beehive",
                UtilityConsumptionData.constant(0, 5, UtilityCategory.IRRIGATION));
        registerById("schedulemc:advanced_beehive",
                UtilityConsumptionData.constant(5, 10, UtilityCategory.IRRIGATION));
        registerById("schedulemc:apiary",
                UtilityConsumptionData.constant(10, 20, UtilityCategory.IRRIGATION));
        registerById("schedulemc:honey_extractor",
                UtilityConsumptionData.of(40, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:centrifugal_extractor",
                UtilityConsumptionData.of(80, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:filtering_station",
                UtilityConsumptionData.of(20, 10, UtilityCategory.MECHANICAL));
        registerById("schedulemc:small_aging_chamber",
                UtilityConsumptionData.of(10, 0, UtilityCategory.FERMENTATION));
        registerById("schedulemc:medium_aging_chamber",
                UtilityConsumptionData.of(20, 0, UtilityCategory.FERMENTATION));
        registerById("schedulemc:large_aging_chamber",
                UtilityConsumptionData.of(40, 0, UtilityCategory.FERMENTATION));
        registerById("schedulemc:processing_station",
                UtilityConsumptionData.of(30, 10, UtilityCategory.MECHANICAL));
        registerById("schedulemc:creaming_station",
                UtilityConsumptionData.of(40, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:bottling_station",
                UtilityConsumptionData.of(15, 5, UtilityCategory.PACKAGING));

        // ─────────────────────────────────────────────────────────────
        // KÄSE-PRODUKTION
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:pasteurization_station",
                UtilityConsumptionData.of(80, 30, UtilityCategory.MECHANICAL));
        registerById("schedulemc:curdling_vat",
                UtilityConsumptionData.of(30, 40, UtilityCategory.MECHANICAL));
        registerById("schedulemc:small_cheese_press",
                UtilityConsumptionData.of(20, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:medium_cheese_press",
                UtilityConsumptionData.of(40, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:large_cheese_press",
                UtilityConsumptionData.of(80, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:small_aging_cave",
                UtilityConsumptionData.of(15, 5, UtilityCategory.FERMENTATION));
        registerById("schedulemc:medium_aging_cave",
                UtilityConsumptionData.of(30, 10, UtilityCategory.FERMENTATION));
        registerById("schedulemc:large_aging_cave",
                UtilityConsumptionData.of(60, 20, UtilityCategory.FERMENTATION));
        registerById("schedulemc:packaging_station",
                UtilityConsumptionData.of(20, 5, UtilityCategory.PACKAGING));

        // ─────────────────────────────────────────────────────────────
        // SCHOKOLADEN-PRODUKTION
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:roasting_station",
                UtilityConsumptionData.of(100, 0, UtilityCategory.DRYING));
        registerById("schedulemc:winnowing_machine",
                UtilityConsumptionData.of(40, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:grinding_mill",
                UtilityConsumptionData.of(60, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:pressing_station",
                UtilityConsumptionData.of(50, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:small_conching_machine",
                UtilityConsumptionData.of(80, 10, UtilityCategory.MECHANICAL));
        registerById("schedulemc:medium_conching_machine",
                UtilityConsumptionData.of(120, 15, UtilityCategory.MECHANICAL));
        registerById("schedulemc:large_conching_machine",
                UtilityConsumptionData.of(200, 25, UtilityCategory.MECHANICAL));
        registerById("schedulemc:tempering_station",
                UtilityConsumptionData.of(60, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:small_molding_station",
                UtilityConsumptionData.of(30, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:medium_molding_station",
                UtilityConsumptionData.of(50, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:large_molding_station",
                UtilityConsumptionData.of(80, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:enrobing_machine",
                UtilityConsumptionData.of(40, 20, UtilityCategory.MECHANICAL));
        registerById("schedulemc:cooling_tunnel",
                UtilityConsumptionData.of(120, 0, UtilityCategory.CLIMATE));
        registerById("schedulemc:wrapping_station",
                UtilityConsumptionData.of(20, 0, UtilityCategory.PACKAGING));

        // ─────────────────────────────────────────────────────────────
        // KAFFEE-PRODUKTION
        // ─────────────────────────────────────────────────────────────
        registerById("schedulemc:wet_processing_station",
                UtilityConsumptionData.of(30, 50, UtilityCategory.MECHANICAL));
        registerById("schedulemc:small_coffee_roaster",
                UtilityConsumptionData.of(80, 0, UtilityCategory.DRYING));
        registerById("schedulemc:medium_coffee_roaster",
                UtilityConsumptionData.of(120, 0, UtilityCategory.DRYING));
        registerById("schedulemc:large_coffee_roaster",
                UtilityConsumptionData.of(200, 0, UtilityCategory.DRYING));
        registerById("schedulemc:coffee_grinder",
                UtilityConsumptionData.of(40, 0, UtilityCategory.MECHANICAL));
        registerById("schedulemc:coffee_packaging_table",
                UtilityConsumptionData.of(15, 0, UtilityCategory.PACKAGING));
    }

    /**
     * Initialisiert die Registry mit Block-Referenzen
     * Muss nach der Block-Registrierung aufgerufen werden
     */
    public static void resolveBlockReferences() {
        // Fan Blocks
        tryResolve("schedulemc:fan_tier1",
                de.rolandsw.schedulemc.fan.blocks.FanBlocks.FAN_TIER_1);
        tryResolve("schedulemc:fan_tier2",
                de.rolandsw.schedulemc.fan.blocks.FanBlocks.FAN_TIER_2);
        tryResolve("schedulemc:fan_tier3",
                de.rolandsw.schedulemc.fan.blocks.FanBlocks.FAN_TIER_3);

        // Tobacco Blocks
        tryResolve("schedulemc:basic_grow_light_slab",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.BASIC_GROW_LIGHT_SLAB);
        tryResolve("schedulemc:advanced_grow_light_slab",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.ADVANCED_GROW_LIGHT_SLAB);
        tryResolve("schedulemc:premium_grow_light_slab",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.PREMIUM_GROW_LIGHT_SLAB);
        tryResolve("schedulemc:terracotta_pot",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.TERRACOTTA_POT);
        tryResolve("schedulemc:ceramic_pot",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.CERAMIC_POT);
        tryResolve("schedulemc:iron_pot",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.IRON_POT);
        tryResolve("schedulemc:golden_pot",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.GOLDEN_POT);
        tryResolve("schedulemc:small_drying_rack",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.SMALL_DRYING_RACK);
        tryResolve("schedulemc:medium_drying_rack",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.MEDIUM_DRYING_RACK);
        tryResolve("schedulemc:big_drying_rack",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.BIG_DRYING_RACK);
        tryResolve("schedulemc:small_fermentation_barrel",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.SMALL_FERMENTATION_BARREL);
        tryResolve("schedulemc:medium_fermentation_barrel",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.MEDIUM_FERMENTATION_BARREL);
        tryResolve("schedulemc:big_fermentation_barrel",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.BIG_FERMENTATION_BARREL);
        tryResolve("schedulemc:small_packaging_table",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.SMALL_PACKAGING_TABLE);
        tryResolve("schedulemc:medium_packaging_table",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.MEDIUM_PACKAGING_TABLE);
        tryResolve("schedulemc:large_packaging_table",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.LARGE_PACKAGING_TABLE);
        tryResolve("schedulemc:sink",
                de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks.SINK);

        // Coca Blocks
        tryResolve("schedulemc:small_extraction_vat",
                de.rolandsw.schedulemc.coca.blocks.CocaBlocks.SMALL_EXTRACTION_VAT);
        tryResolve("schedulemc:medium_extraction_vat",
                de.rolandsw.schedulemc.coca.blocks.CocaBlocks.MEDIUM_EXTRACTION_VAT);
        tryResolve("schedulemc:big_extraction_vat",
                de.rolandsw.schedulemc.coca.blocks.CocaBlocks.BIG_EXTRACTION_VAT);
        tryResolve("schedulemc:small_refinery",
                de.rolandsw.schedulemc.coca.blocks.CocaBlocks.SMALL_REFINERY);
        tryResolve("schedulemc:medium_refinery",
                de.rolandsw.schedulemc.coca.blocks.CocaBlocks.MEDIUM_REFINERY);
        tryResolve("schedulemc:big_refinery",
                de.rolandsw.schedulemc.coca.blocks.CocaBlocks.BIG_REFINERY);
        tryResolve("schedulemc:crack_kocher",
                de.rolandsw.schedulemc.coca.blocks.CocaBlocks.CRACK_KOCHER);

        // Poppy Blocks
        tryResolve("schedulemc:ritzmaschine",
                de.rolandsw.schedulemc.poppy.blocks.PoppyBlocks.SCORING_MACHINE);
        tryResolve("schedulemc:opium_presse",
                de.rolandsw.schedulemc.poppy.blocks.PoppyBlocks.OPIUM_PRESS);
        tryResolve("schedulemc:kochstation",
                de.rolandsw.schedulemc.poppy.blocks.PoppyBlocks.COOKING_STATION);
        tryResolve("schedulemc:heroin_raffinerie",
                de.rolandsw.schedulemc.poppy.blocks.PoppyBlocks.HEROIN_REFINERY);

        // Mushroom Blocks
        tryResolve("schedulemc:klimalampe_small",
                de.rolandsw.schedulemc.mushroom.blocks.MushroomBlocks.CLIMATE_LAMP_SMALL);
        tryResolve("schedulemc:klimalampe_medium",
                de.rolandsw.schedulemc.mushroom.blocks.MushroomBlocks.CLIMATE_LAMP_MEDIUM);
        tryResolve("schedulemc:klimalampe_large",
                de.rolandsw.schedulemc.mushroom.blocks.MushroomBlocks.CLIMATE_LAMP_LARGE);
        tryResolve("schedulemc:wassertank",
                de.rolandsw.schedulemc.mushroom.blocks.MushroomBlocks.WATER_TANK);

        // Meth Blocks
        tryResolve("schedulemc:chemie_mixer",
                de.rolandsw.schedulemc.meth.blocks.MethBlocks.CHEMICAL_MIXER);
        tryResolve("schedulemc:reduktionskessel",
                de.rolandsw.schedulemc.meth.blocks.MethBlocks.REDUCTION_KETTLE);
        tryResolve("schedulemc:kristallisator",
                de.rolandsw.schedulemc.meth.blocks.MethBlocks.CRYSTALLIZER);
        tryResolve("schedulemc:vakuum_trockner",
                de.rolandsw.schedulemc.meth.blocks.MethBlocks.VACUUM_DRYER);

        // LSD Blocks
        tryResolve("schedulemc:fermentations_tank",
                de.rolandsw.schedulemc.lsd.blocks.LSDBlocks.FERMENTATION_TANK);
        tryResolve("schedulemc:destillations_apparat",
                de.rolandsw.schedulemc.lsd.blocks.LSDBlocks.DISTILLATION_APPARATUS);
        tryResolve("schedulemc:mikro_dosierer",
                de.rolandsw.schedulemc.lsd.blocks.LSDBlocks.MICRO_DOSER);
        tryResolve("schedulemc:perforations_presse",
                de.rolandsw.schedulemc.lsd.blocks.LSDBlocks.PERFORATION_PRESS);

        // MDMA Blocks
        tryResolve("schedulemc:reaktions_kessel",
                de.rolandsw.schedulemc.mdma.blocks.MDMABlocks.REACTION_KETTLE);
        tryResolve("schedulemc:trocknungs_ofen",
                de.rolandsw.schedulemc.mdma.blocks.MDMABlocks.DRYING_OVEN);
        tryResolve("schedulemc:pillen_presse",
                de.rolandsw.schedulemc.mdma.blocks.MDMABlocks.PILL_PRESS);

        // Cannabis Blocks
        tryResolve("schedulemc:cannabis_trimm_station",
                de.rolandsw.schedulemc.cannabis.blocks.CannabisBlocks.TRIM_STATION);
        tryResolve("schedulemc:cannabis_curing_glas",
                de.rolandsw.schedulemc.cannabis.blocks.CannabisBlocks.CURING_JAR);
        tryResolve("schedulemc:cannabis_hash_presse",
                de.rolandsw.schedulemc.cannabis.blocks.CannabisBlocks.HASH_PRESS);
        tryResolve("schedulemc:cannabis_oel_extraktor",
                de.rolandsw.schedulemc.cannabis.blocks.CannabisBlocks.OIL_EXTRACTOR);

        // Beer Blocks
        tryResolve("schedulemc:malting_station",
                de.rolandsw.schedulemc.beer.blocks.BeerBlocks.MALTING_STATION);
        tryResolve("schedulemc:mash_tun",
                de.rolandsw.schedulemc.beer.blocks.BeerBlocks.MASH_TUN);
        tryResolve("schedulemc:small_brew_kettle",
                de.rolandsw.schedulemc.beer.blocks.BeerBlocks.SMALL_BREW_KETTLE);
        tryResolve("schedulemc:medium_brew_kettle",
                de.rolandsw.schedulemc.beer.blocks.BeerBlocks.MEDIUM_BREW_KETTLE);
        tryResolve("schedulemc:large_brew_kettle",
                de.rolandsw.schedulemc.beer.blocks.BeerBlocks.LARGE_BREW_KETTLE);
        tryResolve("schedulemc:small_beer_fermentation_tank",
                de.rolandsw.schedulemc.beer.blocks.BeerBlocks.SMALL_FERMENTATION_TANK);
        tryResolve("schedulemc:medium_beer_fermentation_tank",
                de.rolandsw.schedulemc.beer.blocks.BeerBlocks.MEDIUM_FERMENTATION_TANK);
        tryResolve("schedulemc:large_beer_fermentation_tank",
                de.rolandsw.schedulemc.beer.blocks.BeerBlocks.LARGE_FERMENTATION_TANK);
        tryResolve("schedulemc:small_conditioning_tank",
                de.rolandsw.schedulemc.beer.blocks.BeerBlocks.SMALL_CONDITIONING_TANK);
        tryResolve("schedulemc:medium_conditioning_tank",
                de.rolandsw.schedulemc.beer.blocks.BeerBlocks.MEDIUM_CONDITIONING_TANK);
        tryResolve("schedulemc:large_conditioning_tank",
                de.rolandsw.schedulemc.beer.blocks.BeerBlocks.LARGE_CONDITIONING_TANK);
        tryResolve("schedulemc:beer_bottling_station",
                de.rolandsw.schedulemc.beer.blocks.BeerBlocks.BOTTLING_STATION);

        // Wine Blocks
        tryResolve("schedulemc:crushing_station",
                de.rolandsw.schedulemc.wine.blocks.WineBlocks.CRUSHING_STATION);
        tryResolve("schedulemc:small_wine_press",
                de.rolandsw.schedulemc.wine.blocks.WineBlocks.SMALL_WINE_PRESS);
        tryResolve("schedulemc:medium_wine_press",
                de.rolandsw.schedulemc.wine.blocks.WineBlocks.MEDIUM_WINE_PRESS);
        tryResolve("schedulemc:large_wine_press",
                de.rolandsw.schedulemc.wine.blocks.WineBlocks.LARGE_WINE_PRESS);
        tryResolve("schedulemc:small_fermentation_tank",
                de.rolandsw.schedulemc.wine.blocks.WineBlocks.SMALL_FERMENTATION_TANK);
        tryResolve("schedulemc:medium_fermentation_tank",
                de.rolandsw.schedulemc.wine.blocks.WineBlocks.MEDIUM_FERMENTATION_TANK);
        tryResolve("schedulemc:large_fermentation_tank",
                de.rolandsw.schedulemc.wine.blocks.WineBlocks.LARGE_FERMENTATION_TANK);
        tryResolve("schedulemc:small_aging_barrel",
                de.rolandsw.schedulemc.wine.blocks.WineBlocks.SMALL_AGING_BARREL);
        tryResolve("schedulemc:medium_aging_barrel",
                de.rolandsw.schedulemc.wine.blocks.WineBlocks.MEDIUM_AGING_BARREL);
        tryResolve("schedulemc:large_aging_barrel",
                de.rolandsw.schedulemc.wine.blocks.WineBlocks.LARGE_AGING_BARREL);
        tryResolve("schedulemc:wine_bottling_station",
                de.rolandsw.schedulemc.wine.blocks.WineBlocks.WINE_BOTTLING_STATION);

        // Honey Blocks
        tryResolve("schedulemc:beehive",
                de.rolandsw.schedulemc.honey.blocks.HoneyBlocks.BEEHIVE);
        tryResolve("schedulemc:advanced_beehive",
                de.rolandsw.schedulemc.honey.blocks.HoneyBlocks.ADVANCED_BEEHIVE);
        tryResolve("schedulemc:apiary",
                de.rolandsw.schedulemc.honey.blocks.HoneyBlocks.APIARY);
        tryResolve("schedulemc:honey_extractor",
                de.rolandsw.schedulemc.honey.blocks.HoneyBlocks.HONEY_EXTRACTOR);
        tryResolve("schedulemc:centrifugal_extractor",
                de.rolandsw.schedulemc.honey.blocks.HoneyBlocks.CENTRIFUGAL_EXTRACTOR);
        tryResolve("schedulemc:filtering_station",
                de.rolandsw.schedulemc.honey.blocks.HoneyBlocks.FILTERING_STATION);
        tryResolve("schedulemc:small_aging_chamber",
                de.rolandsw.schedulemc.honey.blocks.HoneyBlocks.SMALL_AGING_CHAMBER);
        tryResolve("schedulemc:medium_aging_chamber",
                de.rolandsw.schedulemc.honey.blocks.HoneyBlocks.MEDIUM_AGING_CHAMBER);
        tryResolve("schedulemc:large_aging_chamber",
                de.rolandsw.schedulemc.honey.blocks.HoneyBlocks.LARGE_AGING_CHAMBER);
        tryResolve("schedulemc:processing_station",
                de.rolandsw.schedulemc.honey.blocks.HoneyBlocks.PROCESSING_STATION);
        tryResolve("schedulemc:creaming_station",
                de.rolandsw.schedulemc.honey.blocks.HoneyBlocks.CREAMING_STATION);
        tryResolve("schedulemc:bottling_station",
                de.rolandsw.schedulemc.honey.blocks.HoneyBlocks.BOTTLING_STATION);

        // Cheese Blocks
        tryResolve("schedulemc:pasteurization_station",
                de.rolandsw.schedulemc.cheese.blocks.CheeseBlocks.PASTEURIZATION_STATION);
        tryResolve("schedulemc:curdling_vat",
                de.rolandsw.schedulemc.cheese.blocks.CheeseBlocks.CURDLING_VAT);
        tryResolve("schedulemc:small_cheese_press",
                de.rolandsw.schedulemc.cheese.blocks.CheeseBlocks.SMALL_CHEESE_PRESS);
        tryResolve("schedulemc:medium_cheese_press",
                de.rolandsw.schedulemc.cheese.blocks.CheeseBlocks.MEDIUM_CHEESE_PRESS);
        tryResolve("schedulemc:large_cheese_press",
                de.rolandsw.schedulemc.cheese.blocks.CheeseBlocks.LARGE_CHEESE_PRESS);
        tryResolve("schedulemc:small_aging_cave",
                de.rolandsw.schedulemc.cheese.blocks.CheeseBlocks.SMALL_AGING_CAVE);
        tryResolve("schedulemc:medium_aging_cave",
                de.rolandsw.schedulemc.cheese.blocks.CheeseBlocks.MEDIUM_AGING_CAVE);
        tryResolve("schedulemc:large_aging_cave",
                de.rolandsw.schedulemc.cheese.blocks.CheeseBlocks.LARGE_AGING_CAVE);
        tryResolve("schedulemc:packaging_station",
                de.rolandsw.schedulemc.cheese.blocks.CheeseBlocks.PACKAGING_STATION);

        // Chocolate Blocks
        tryResolve("schedulemc:roasting_station",
                de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks.ROASTING_STATION);
        tryResolve("schedulemc:winnowing_machine",
                de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks.WINNOWING_MACHINE);
        tryResolve("schedulemc:grinding_mill",
                de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks.GRINDING_MILL);
        tryResolve("schedulemc:pressing_station",
                de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks.PRESSING_STATION);
        tryResolve("schedulemc:small_conching_machine",
                de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks.SMALL_CONCHING_MACHINE);
        tryResolve("schedulemc:medium_conching_machine",
                de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks.MEDIUM_CONCHING_MACHINE);
        tryResolve("schedulemc:large_conching_machine",
                de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks.LARGE_CONCHING_MACHINE);
        tryResolve("schedulemc:tempering_station",
                de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks.TEMPERING_STATION);
        tryResolve("schedulemc:small_molding_station",
                de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks.SMALL_MOLDING_STATION);
        tryResolve("schedulemc:medium_molding_station",
                de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks.MEDIUM_MOLDING_STATION);
        tryResolve("schedulemc:large_molding_station",
                de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks.LARGE_MOLDING_STATION);
        tryResolve("schedulemc:enrobing_machine",
                de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks.ENROBING_MACHINE);
        tryResolve("schedulemc:cooling_tunnel",
                de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks.COOLING_TUNNEL);
        tryResolve("schedulemc:wrapping_station",
                de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks.WRAPPING_STATION);

        // Coffee Blocks
        tryResolve("schedulemc:wet_processing_station",
                de.rolandsw.schedulemc.coffee.blocks.CoffeeBlocks.WET_PROCESSING_STATION);
        tryResolve("schedulemc:small_coffee_roaster",
                de.rolandsw.schedulemc.coffee.blocks.CoffeeBlocks.SMALL_COFFEE_ROASTER);
        tryResolve("schedulemc:medium_coffee_roaster",
                de.rolandsw.schedulemc.coffee.blocks.CoffeeBlocks.MEDIUM_COFFEE_ROASTER);
        tryResolve("schedulemc:large_coffee_roaster",
                de.rolandsw.schedulemc.coffee.blocks.CoffeeBlocks.LARGE_COFFEE_ROASTER);
        tryResolve("schedulemc:coffee_grinder",
                de.rolandsw.schedulemc.coffee.blocks.CoffeeBlocks.COFFEE_GRINDER);
        tryResolve("schedulemc:coffee_packaging_table",
                de.rolandsw.schedulemc.coffee.blocks.CoffeeBlocks.COFFEE_PACKAGING_TABLE);
    }

    private static void tryResolve(String id, net.minecraftforge.registries.RegistryObject<? extends Block> registryObject) {
        try {
            // Nur registrieren wenn der Block in der Config-Liste steht
            if (!isEnabledByConfig(id)) return;

            if (registryObject.isPresent()) {
                UtilityConsumptionData data = CONSUMPTION_BY_ID.get(id);
                if (data != null) {
                    CONSUMPTION_MAP.put(registryObject.get(), data);
                }
            }
        } catch (Exception ex) {
            if (DEFERRED_RESOLVE_LOGGED.add(id)) {
                LOGGER.debug("UtilityRegistry: block '{}' not yet resolved (deferred)", id, ex);
            }
        }
    }

    /**
     * Prüft ob ein Block in der UTILITY_CONSUMER_BLOCKS-Config-Liste steht.
     * Unterstützt sowohl volle IDs ("schedulemc:block") als auch Kurznamen ("block").
     */
    private static boolean isEnabledByConfig(String fullId) {
        try {
            List<? extends String> configList = ModConfigHandler.COMMON.UTILITY_CONSUMER_BLOCKS.get();
            if (configList == null || configList.isEmpty()) return true;
            String path = fullId.contains(":") ? fullId.substring(fullId.indexOf(':') + 1) : fullId;
            for (String entry : configList) {
                String e = entry.trim();
                if (e.equalsIgnoreCase(fullId) || e.equalsIgnoreCase(path)) return true;
            }
            return false;
        } catch (Exception ex) {
            return true; // Config noch nicht geladen → Standard: alles aktiv
        }
    }
}
