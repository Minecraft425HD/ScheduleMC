package de.rolandsw.schedulemc.level;

/**
 * Alle freischaltbaren Inhalte im Level-System.
 *
 * Jedes Unlockable hat:
 * - Benötigtes Level zum Freischalten
 * - Kategorie (Produktionskette, Maschine, etc.)
 * - Beschreibung
 *
 * Progression:
 * Level 1-5:   Basis-Produkte (Tabak, Cannabis-Autoflower, Bier, Honig)
 * Level 6-10:  Erweiterte Sorten, Keramik-Topf, einfache Verarbeitung
 * Level 11-15: Harte Drogen freigeschaltet, Eisen-Topf, Maschinen-Upgrades
 * Level 16-20: Premium-Sorten, Gold-Topf, erweiterte Verarbeitung
 * Level 21-25: Experten-Sorten, Premium-Maschinen, Wirtschafts-Features
 * Level 26-30: Alles freigeschaltet, Endgame-Content
 */
import net.minecraft.network.chat.Component;

public enum Unlockable {

    // ═══════════════════════════════════════════════════════════
    // LEVEL 1-5: EINSTIEG
    // ═══════════════════════════════════════════════════════════
    TOBACCO_VIRGINIA(1, UnlockCategory.PRODUCTION_CHAIN, "gui.app.level.unlock.tobacco_virginia.desc"),
    CANNABIS_AUTOFLOWER(1, UnlockCategory.PRODUCTION_CHAIN, "gui.app.level.unlock.cannabis_autoflower.desc"),
    BEER_PILSNER(1, UnlockCategory.PRODUCTION_CHAIN, "gui.app.level.unlock.beer_pilsner.desc"),
    HONEY_ACACIA(1, UnlockCategory.PRODUCTION_CHAIN, "gui.app.level.unlock.honey_acacia.desc"),
    POT_TERRACOTTA(1, UnlockCategory.POT, "gui.app.level.unlock.pot_terracotta.desc"),

    TOBACCO_BURLEY(3, UnlockCategory.STRAIN, "gui.app.level.unlock.tobacco_burley.desc"),
    COFFEE_ARABICA(3, UnlockCategory.PRODUCTION_CHAIN, "gui.app.level.unlock.coffee_arabica.desc"),
    CHEESE_GOUDA(3, UnlockCategory.PRODUCTION_CHAIN, "gui.app.level.unlock.cheese_gouda.desc"),
    CHOCOLATE_MILK(3, UnlockCategory.PRODUCTION_CHAIN, "gui.app.level.unlock.chocolate_milk.desc"),

    GANG_JOIN(5, UnlockCategory.GANG, "gui.app.level.unlock.gang_join.desc"),
    BEER_WEIZEN(5, UnlockCategory.STRAIN, "gui.app.level.unlock.beer_weizen.desc"),
    WINE_RIESLING(5, UnlockCategory.PRODUCTION_CHAIN, "gui.app.level.unlock.wine_riesling.desc"),
    HONEY_WILDFLOWER(5, UnlockCategory.STRAIN, "gui.app.level.unlock.honey_wildflower.desc"),
    CANNABIS_INDICA(5, UnlockCategory.STRAIN, "gui.app.level.unlock.cannabis_indica.desc"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 6-10: ERWEITERUNG
    // ═══════════════════════════════════════════════════════════
    POT_CERAMIC(6, UnlockCategory.POT, "gui.app.level.unlock.pot_ceramic.desc"),
    TOBACCO_ORIENTAL(6, UnlockCategory.STRAIN, "gui.app.level.unlock.tobacco_oriental.desc"),
    COFFEE_ROBUSTA(6, UnlockCategory.STRAIN, "gui.app.level.unlock.coffee_robusta.desc"),
    CHEESE_EMMENTAL(6, UnlockCategory.STRAIN, "gui.app.level.unlock.cheese_emmental.desc"),
    CHOCOLATE_DARK(6, UnlockCategory.STRAIN, "gui.app.level.unlock.chocolate_dark.desc"),

    CANNABIS_SATIVA(8, UnlockCategory.STRAIN, "gui.app.level.unlock.cannabis_sativa.desc"),
    MUSHROOM_MEXICANA(8, UnlockCategory.PRODUCTION_CHAIN, "gui.app.level.unlock.mushroom_mexicana.desc"),
    BEER_ALE(8, UnlockCategory.STRAIN, "gui.app.level.unlock.beer_ale.desc"),
    WINE_CHARDONNAY(8, UnlockCategory.STRAIN, "gui.app.level.unlock.wine_chardonnay.desc"),
    HONEY_FOREST(8, UnlockCategory.STRAIN, "gui.app.level.unlock.honey_forest.desc"),

    COFFEE_ROAST_MEDIUM(10, UnlockCategory.PROCESSING, "gui.app.level.unlock.coffee_roast_medium.desc"),
    CHEESE_PROCESSING_SMOKED(10, UnlockCategory.PROCESSING, "gui.app.level.unlock.cheese_processing_smoked.desc"),
    WINE_SEMI_DRY(10, UnlockCategory.PROCESSING, "gui.app.level.unlock.wine_semi_dry.desc"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 11-15: HARTE DROGEN & MITTELSTUFE
    // ═══════════════════════════════════════════════════════════
    POT_IRON(11, UnlockCategory.POT, "gui.app.level.unlock.pot_iron.desc"),
    COCA_BOLIVIANISCH(11, UnlockCategory.PRODUCTION_CHAIN, "gui.app.level.unlock.coca_bolivianisch.desc"),
    MDMA_PRODUCTION(11, UnlockCategory.PRODUCTION_CHAIN, "gui.app.level.unlock.mdma_production.desc"),
    CHEESE_CAMEMBERT(11, UnlockCategory.STRAIN, "gui.app.level.unlock.cheese_camembert.desc"),
    BEER_STOUT(11, UnlockCategory.STRAIN, "gui.app.level.unlock.beer_stout.desc"),

    CANNABIS_HYBRID(13, UnlockCategory.STRAIN, "gui.app.level.unlock.cannabis_hybrid.desc"),
    MUSHROOM_CUBENSIS(13, UnlockCategory.STRAIN, "gui.app.level.unlock.mushroom_cubensis.desc"),
    LSD_PRODUCTION(13, UnlockCategory.PRODUCTION_CHAIN, "gui.app.level.unlock.lsd_production.desc"),
    WINE_SPAETBURGUNDER(13, UnlockCategory.STRAIN, "gui.app.level.unlock.wine_spaetburgunder.desc"),
    COFFEE_LIBERICA(13, UnlockCategory.STRAIN, "gui.app.level.unlock.coffee_liberica.desc"),

    GANG_CREATE(15, UnlockCategory.GANG, "gui.app.level.unlock.gang_create.desc"),
    POPPY_INDISCH(15, UnlockCategory.PRODUCTION_CHAIN, "gui.app.level.unlock.poppy_indisch.desc"),
    COCA_PERUANISCH(15, UnlockCategory.STRAIN, "gui.app.level.unlock.coca_peruanisch.desc"),
    METH_PRODUCTION(15, UnlockCategory.PRODUCTION_CHAIN, "gui.app.level.unlock.meth_production.desc"),
    CHOCOLATE_RUBY(15, UnlockCategory.STRAIN, "gui.app.level.unlock.chocolate_ruby.desc"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 16-20: PREMIUM
    // ═══════════════════════════════════════════════════════════
    POT_GOLDEN(16, UnlockCategory.POT, "gui.app.level.unlock.pot_golden.desc"),
    TOBACCO_HAVANA(16, UnlockCategory.STRAIN, "gui.app.level.unlock.tobacco_havana.desc"),
    WINE_MERLOT(16, UnlockCategory.STRAIN, "gui.app.level.unlock.wine_merlot.desc"),
    COFFEE_EXCELSA(16, UnlockCategory.STRAIN, "gui.app.level.unlock.coffee_excelsa.desc"),
    CHEESE_PARMESAN(16, UnlockCategory.STRAIN, "gui.app.level.unlock.cheese_parmesan.desc"),

    POPPY_TUERKISCH(18, UnlockCategory.STRAIN, "gui.app.level.unlock.poppy_tuerkisch.desc"),
    COCA_KOLUMBIANISCH(18, UnlockCategory.STRAIN, "gui.app.level.unlock.coca_kolumbianisch.desc"),
    MUSHROOM_AZURESCENS(18, UnlockCategory.STRAIN, "gui.app.level.unlock.mushroom_azurescens.desc"),
    HONEY_MANUKA(18, UnlockCategory.STRAIN, "gui.app.level.unlock.honey_manuka.desc"),

    COFFEE_ROAST_ESPRESSO(20, UnlockCategory.PROCESSING, "gui.app.level.unlock.coffee_roast_espresso.desc"),
    WINE_DESSERT(20, UnlockCategory.PROCESSING, "gui.app.level.unlock.wine_dessert.desc"),
    CHEESE_PROCESSING_HERB(20, UnlockCategory.PROCESSING, "gui.app.level.unlock.cheese_processing_herb.desc"),
    CHOCOLATE_FILLED(20, UnlockCategory.PROCESSING, "gui.app.level.unlock.chocolate_filled.desc"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 21-25: EXPERTE
    // ═══════════════════════════════════════════════════════════
    POPPY_AFGHANISCH(22, UnlockCategory.STRAIN, "gui.app.level.unlock.poppy_afghanisch.desc"),
    MACHINE_SIZE_MEDIUM(22, UnlockCategory.MACHINE, "gui.app.level.unlock.machine_size_medium.desc"),
    VEHICLE_SUV(22, UnlockCategory.VEHICLE, "gui.app.level.unlock.vehicle_suv.desc"),
    VEHICLE_TRANSPORTER(22, UnlockCategory.VEHICLE, "gui.app.level.unlock.vehicle_transporter.desc"),

    MACHINE_SIZE_BIG(25, UnlockCategory.MACHINE, "gui.app.level.unlock.machine_size_big.desc"),
    VEHICLE_SPORT(25, UnlockCategory.VEHICLE, "gui.app.level.unlock.vehicle_sport.desc"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 26-30: ENDGAME
    // ═══════════════════════════════════════════════════════════
    ECONOMY_MARKET_ACCESS(26, UnlockCategory.ECONOMY_FEATURE, "gui.app.level.unlock.economy_market_access.desc"),
    ECONOMY_PRICE_ALERTS(28, UnlockCategory.ECONOMY_FEATURE, "gui.app.level.unlock.economy_price_alerts.desc"),
    ECONOMY_BULK_TRADING(30, UnlockCategory.ECONOMY_FEATURE, "gui.app.level.unlock.economy_bulk_trading.desc");

    private final int requiredLevel;
    private final UnlockCategory category;
    private final String descriptionKey;

    Unlockable(int requiredLevel, UnlockCategory category, String descriptionKey) {
        this.requiredLevel = requiredLevel;
        this.category = category;
        this.descriptionKey = descriptionKey;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public UnlockCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return Component.translatable(descriptionKey).getString();
    }

    /**
     * Prüft ob ein Spieler mit dem gegebenen Level diesen Inhalt freigeschaltet hat.
     */
    public boolean isUnlockedAt(int playerLevel) {
        return playerLevel >= requiredLevel;
    }

    /**
     * @return Formatierte Anzeige mit Level-Anforderung
     */
    public String getFormattedInfo(int playerLevel) {
        boolean unlocked = isUnlockedAt(playerLevel);
        String status = unlocked ? "§a✔" : "§c✘";
        String levelColor = unlocked ? "§a" : "§c";

        String format = Component.translatable("gui.app.level.unlock.formatted_info_pattern").getString();
        return String.format(format, status, category.getColorCode(), getDescription(),
                levelColor, requiredLevel, category.getDisplayName());
    }
}
