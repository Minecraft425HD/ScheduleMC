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
public enum Unlockable {

    // ═══════════════════════════════════════════════════════════
    // LEVEL 1-5: EINSTIEG
    // ═══════════════════════════════════════════════════════════
    TOBACCO_VIRGINIA(1, UnlockCategory.PRODUCTION_CHAIN, "Tobacco: Virginia strain"),
    CANNABIS_AUTOFLOWER(1, UnlockCategory.PRODUCTION_CHAIN, "Cannabis: Autoflower strain"),
    BEER_PILSNER(1, UnlockCategory.PRODUCTION_CHAIN, "Beer: Pilsner"),
    HONEY_ACACIA(1, UnlockCategory.PRODUCTION_CHAIN, "Honey: Acacia"),
    POT_TERRACOTTA(1, UnlockCategory.POT, "Terracotta pot"),

    TOBACCO_BURLEY(3, UnlockCategory.STRAIN, "Tobacco: Burley strain"),
    COFFEE_ARABICA(3, UnlockCategory.PRODUCTION_CHAIN, "Coffee: Arabica"),
    CHEESE_GOUDA(3, UnlockCategory.PRODUCTION_CHAIN, "Cheese: Gouda"),
    CHOCOLATE_MILK(3, UnlockCategory.PRODUCTION_CHAIN, "Chocolate: Milk"),

    GANG_JOIN(5, UnlockCategory.GANG, "Join gang (with invitation, \u20AC2,500)"),
    BEER_WEIZEN(5, UnlockCategory.STRAIN, "Beer: Wheat"),
    WINE_RIESLING(5, UnlockCategory.PRODUCTION_CHAIN, "Wine: Riesling"),
    HONEY_WILDFLOWER(5, UnlockCategory.STRAIN, "Honey: Wildflower"),
    CANNABIS_INDICA(5, UnlockCategory.STRAIN, "Cannabis: Indica strain"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 6-10: ERWEITERUNG
    // ═══════════════════════════════════════════════════════════
    POT_CERAMIC(6, UnlockCategory.POT, "Ceramic pot (4 plants)"),
    TOBACCO_ORIENTAL(6, UnlockCategory.STRAIN, "Tobacco: Oriental strain"),
    COFFEE_ROBUSTA(6, UnlockCategory.STRAIN, "Coffee: Robusta"),
    CHEESE_EMMENTAL(6, UnlockCategory.STRAIN, "Cheese: Emmental"),
    CHOCOLATE_DARK(6, UnlockCategory.STRAIN, "Chocolate: Dark"),

    CANNABIS_SATIVA(8, UnlockCategory.STRAIN, "Cannabis: Sativa strain"),
    MUSHROOM_MEXICANA(8, UnlockCategory.PRODUCTION_CHAIN, "Mushroom: Mexicana"),
    BEER_ALE(8, UnlockCategory.STRAIN, "Beer: Ale"),
    WINE_CHARDONNAY(8, UnlockCategory.STRAIN, "Wine: Chardonnay"),
    HONEY_FOREST(8, UnlockCategory.STRAIN, "Honey: Forest"),

    COFFEE_ROAST_MEDIUM(10, UnlockCategory.PROCESSING, "Coffee: Medium roast"),
    CHEESE_PROCESSING_SMOKED(10, UnlockCategory.PROCESSING, "Cheese: Smoked"),
    WINE_SEMI_DRY(10, UnlockCategory.PROCESSING, "Wine: Semi-dry"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 11-15: HARTE DROGEN & MITTELSTUFE
    // ═══════════════════════════════════════════════════════════
    POT_IRON(11, UnlockCategory.POT, "Iron pot (5 plants)"),
    COCA_BOLIVIANISCH(11, UnlockCategory.PRODUCTION_CHAIN, "Cocaine: Bolivian"),
    MDMA_PRODUCTION(11, UnlockCategory.PRODUCTION_CHAIN, "MDMA production"),
    CHEESE_CAMEMBERT(11, UnlockCategory.STRAIN, "Cheese: Camembert"),
    BEER_STOUT(11, UnlockCategory.STRAIN, "Beer: Stout"),

    CANNABIS_HYBRID(13, UnlockCategory.STRAIN, "Cannabis: Hybrid strain"),
    MUSHROOM_CUBENSIS(13, UnlockCategory.STRAIN, "Mushroom: Cubensis"),
    LSD_PRODUCTION(13, UnlockCategory.PRODUCTION_CHAIN, "LSD production"),
    WINE_SPAETBURGUNDER(13, UnlockCategory.STRAIN, "Wine: Pinot Noir"),
    COFFEE_LIBERICA(13, UnlockCategory.STRAIN, "Coffee: Liberica"),

    GANG_CREATE(15, UnlockCategory.GANG, "Create gang (\u20AC25,000)"),
    POPPY_INDISCH(15, UnlockCategory.PRODUCTION_CHAIN, "Poppy: Indian (heroin)"),
    COCA_PERUANISCH(15, UnlockCategory.STRAIN, "Cocaine: Peruvian"),
    METH_PRODUCTION(15, UnlockCategory.PRODUCTION_CHAIN, "Meth production"),
    CHOCOLATE_RUBY(15, UnlockCategory.STRAIN, "Chocolate: Ruby"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 16-20: PREMIUM
    // ═══════════════════════════════════════════════════════════
    POT_GOLDEN(16, UnlockCategory.POT, "Golden pot (5 plants + quality boost)"),
    TOBACCO_HAVANA(16, UnlockCategory.STRAIN, "Tobacco: Havana strain"),
    WINE_MERLOT(16, UnlockCategory.STRAIN, "Wine: Merlot"),
    COFFEE_EXCELSA(16, UnlockCategory.STRAIN, "Coffee: Excelsa"),
    CHEESE_PARMESAN(16, UnlockCategory.STRAIN, "Cheese: Parmesan"),

    POPPY_TUERKISCH(18, UnlockCategory.STRAIN, "Poppy: Turkish"),
    COCA_KOLUMBIANISCH(18, UnlockCategory.STRAIN, "Cocaine: Colombian"),
    MUSHROOM_AZURESCENS(18, UnlockCategory.STRAIN, "Mushroom: Azurescens"),
    HONEY_MANUKA(18, UnlockCategory.STRAIN, "Honey: Manuka"),

    COFFEE_ROAST_ESPRESSO(20, UnlockCategory.PROCESSING, "Coffee: Espresso roast"),
    WINE_DESSERT(20, UnlockCategory.PROCESSING, "Wine: Dessert"),
    CHEESE_PROCESSING_HERB(20, UnlockCategory.PROCESSING, "Cheese: Herb processing"),
    CHOCOLATE_FILLED(20, UnlockCategory.PROCESSING, "Chocolate: Filled"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 21-25: EXPERTE
    // ═══════════════════════════════════════════════════════════
    POPPY_AFGHANISCH(22, UnlockCategory.STRAIN, "Poppy: Afghan (highest potency)"),
    MACHINE_SIZE_MEDIUM(22, UnlockCategory.MACHINE, "Medium production facilities"),
    VEHICLE_SUV(22, UnlockCategory.VEHICLE, "SUV vehicle"),
    VEHICLE_TRANSPORTER(22, UnlockCategory.VEHICLE, "Transport van"),

    MACHINE_SIZE_BIG(25, UnlockCategory.MACHINE, "Large production facilities"),
    VEHICLE_SPORT(25, UnlockCategory.VEHICLE, "Sports vehicle"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 26-30: ENDGAME
    // ═══════════════════════════════════════════════════════════
    ECONOMY_MARKET_ACCESS(26, UnlockCategory.ECONOMY_FEATURE, "Market access (view supply/demand)"),
    ECONOMY_PRICE_ALERTS(28, UnlockCategory.ECONOMY_FEATURE, "Price alerts"),
    ECONOMY_BULK_TRADING(30, UnlockCategory.ECONOMY_FEATURE, "Bulk trading (volume discounts)");

    private final int requiredLevel;
    private final UnlockCategory category;
    private final String description;

    Unlockable(int requiredLevel, UnlockCategory category, String description) {
        this.requiredLevel = requiredLevel;
        this.category = category;
        this.description = description;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public UnlockCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
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

        return String.format("%s %s%s §7(Level %s%d§7) §8- %s",
                status, category.getColorCode(), description,
                levelColor, requiredLevel, category.getDisplayName());
    }
}
