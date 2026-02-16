package de.rolandsw.schedulemc.utility;

import net.minecraft.world.level.block.Block;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

    // SICHERHEIT: ConcurrentHashMap für Thread-safe Zugriff
    private static final Map<Block, UtilityConsumptionData> CONSUMPTION_MAP = new ConcurrentHashMap<>();
    private static final Map<String, UtilityConsumptionData> CONSUMPTION_BY_ID = new ConcurrentHashMap<>();

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
        // KLIMALAMPEN (Mushroom - Klimatisierung)
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

        // Wassertank (Pilze)
        registerById("schedulemc:wassertank",
                UtilityConsumptionData.constant(5, 50, UtilityCategory.IRRIGATION));

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
    }

    /**
     * Initialisiert die Registry mit Block-Referenzen
     * Muss nach der Block-Registrierung aufgerufen werden
     */
    public static void resolveBlockReferences() {
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
                de.rolandsw.schedulemc.poppy.blocks.PoppyBlocks.RITZMASCHINE);
        tryResolve("schedulemc:opium_presse",
                de.rolandsw.schedulemc.poppy.blocks.PoppyBlocks.OPIUM_PRESSE);
        tryResolve("schedulemc:kochstation",
                de.rolandsw.schedulemc.poppy.blocks.PoppyBlocks.KOCHSTATION);
        tryResolve("schedulemc:heroin_raffinerie",
                de.rolandsw.schedulemc.poppy.blocks.PoppyBlocks.HEROIN_RAFFINERIE);

        // Mushroom Blocks
        tryResolve("schedulemc:klimalampe_small",
                de.rolandsw.schedulemc.mushroom.blocks.MushroomBlocks.KLIMALAMPE_SMALL);
        tryResolve("schedulemc:klimalampe_medium",
                de.rolandsw.schedulemc.mushroom.blocks.MushroomBlocks.KLIMALAMPE_MEDIUM);
        tryResolve("schedulemc:klimalampe_large",
                de.rolandsw.schedulemc.mushroom.blocks.MushroomBlocks.KLIMALAMPE_LARGE);
        tryResolve("schedulemc:wassertank",
                de.rolandsw.schedulemc.mushroom.blocks.MushroomBlocks.WASSERTANK);

        // Meth Blocks
        tryResolve("schedulemc:chemie_mixer",
                de.rolandsw.schedulemc.meth.blocks.MethBlocks.CHEMIE_MIXER);
        tryResolve("schedulemc:reduktionskessel",
                de.rolandsw.schedulemc.meth.blocks.MethBlocks.REDUKTIONSKESSEL);
        tryResolve("schedulemc:kristallisator",
                de.rolandsw.schedulemc.meth.blocks.MethBlocks.KRISTALLISATOR);
        tryResolve("schedulemc:vakuum_trockner",
                de.rolandsw.schedulemc.meth.blocks.MethBlocks.VAKUUM_TROCKNER);

        // LSD Blocks
        tryResolve("schedulemc:fermentations_tank",
                de.rolandsw.schedulemc.lsd.blocks.LSDBlocks.FERMENTATIONS_TANK);
        tryResolve("schedulemc:destillations_apparat",
                de.rolandsw.schedulemc.lsd.blocks.LSDBlocks.DESTILLATIONS_APPARAT);
        tryResolve("schedulemc:mikro_dosierer",
                de.rolandsw.schedulemc.lsd.blocks.LSDBlocks.MIKRO_DOSIERER);
        tryResolve("schedulemc:perforations_presse",
                de.rolandsw.schedulemc.lsd.blocks.LSDBlocks.PERFORATIONS_PRESSE);

        // MDMA Blocks
        tryResolve("schedulemc:reaktions_kessel",
                de.rolandsw.schedulemc.mdma.blocks.MDMABlocks.REAKTIONS_KESSEL);
        tryResolve("schedulemc:trocknungs_ofen",
                de.rolandsw.schedulemc.mdma.blocks.MDMABlocks.TROCKNUNGS_OFEN);
        tryResolve("schedulemc:pillen_presse",
                de.rolandsw.schedulemc.mdma.blocks.MDMABlocks.PILLEN_PRESSE);

        // Cannabis Blocks
        tryResolve("schedulemc:cannabis_trimm_station",
                de.rolandsw.schedulemc.cannabis.blocks.CannabisBlocks.TRIMM_STATION);
        tryResolve("schedulemc:cannabis_curing_glas",
                de.rolandsw.schedulemc.cannabis.blocks.CannabisBlocks.CURING_GLAS);
        tryResolve("schedulemc:cannabis_hash_presse",
                de.rolandsw.schedulemc.cannabis.blocks.CannabisBlocks.HASH_PRESSE);
        tryResolve("schedulemc:cannabis_oel_extraktor",
                de.rolandsw.schedulemc.cannabis.blocks.CannabisBlocks.OEL_EXTRAKTOR);
    }

    private static void tryResolve(String id, net.minecraftforge.registries.RegistryObject<? extends Block> registryObject) {
        try {
            if (registryObject.isPresent()) {
                UtilityConsumptionData data = CONSUMPTION_BY_ID.get(id);
                if (data != null) {
                    CONSUMPTION_MAP.put(registryObject.get(), data);
                }
            }
        } catch (Exception e) {
            // Block not yet registered, will be resolved later
        }
    }
}
