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
    TOBACCO_VIRGINIA(1, UnlockCategory.PRODUCTION_CHAIN, "Tabak: Virginia-Sorte"),
    CANNABIS_AUTOFLOWER(1, UnlockCategory.PRODUCTION_CHAIN, "Cannabis: Autoflower-Sorte"),
    BEER_PILSNER(1, UnlockCategory.PRODUCTION_CHAIN, "Bier: Pilsner"),
    HONEY_ACACIA(1, UnlockCategory.PRODUCTION_CHAIN, "Honig: Akazie"),
    POT_TERRACOTTA(1, UnlockCategory.POT, "Terracotta-Topf"),

    TOBACCO_BURLEY(3, UnlockCategory.STRAIN, "Tabak: Burley-Sorte"),
    COFFEE_ARABICA(3, UnlockCategory.PRODUCTION_CHAIN, "Kaffee: Arabica"),
    CHEESE_GOUDA(3, UnlockCategory.PRODUCTION_CHAIN, "Käse: Gouda"),
    CHOCOLATE_MILK(3, UnlockCategory.PRODUCTION_CHAIN, "Schokolade: Milch"),

    BEER_WEIZEN(5, UnlockCategory.STRAIN, "Bier: Weizen"),
    WINE_RIESLING(5, UnlockCategory.PRODUCTION_CHAIN, "Wein: Riesling"),
    HONEY_WILDFLOWER(5, UnlockCategory.STRAIN, "Honig: Wildblumen"),
    CANNABIS_INDICA(5, UnlockCategory.STRAIN, "Cannabis: Indica-Sorte"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 6-10: ERWEITERUNG
    // ═══════════════════════════════════════════════════════════
    POT_CERAMIC(6, UnlockCategory.POT, "Keramik-Topf (4 Pflanzen)"),
    TOBACCO_ORIENTAL(6, UnlockCategory.STRAIN, "Tabak: Oriental-Sorte"),
    COFFEE_ROBUSTA(6, UnlockCategory.STRAIN, "Kaffee: Robusta"),
    CHEESE_EMMENTAL(6, UnlockCategory.STRAIN, "Käse: Emmentaler"),
    CHOCOLATE_DARK(6, UnlockCategory.STRAIN, "Schokolade: Dunkel"),

    CANNABIS_SATIVA(8, UnlockCategory.STRAIN, "Cannabis: Sativa-Sorte"),
    MUSHROOM_MEXICANA(8, UnlockCategory.PRODUCTION_CHAIN, "Pilze: Mexicana"),
    BEER_ALE(8, UnlockCategory.STRAIN, "Bier: Ale"),
    WINE_CHARDONNAY(8, UnlockCategory.STRAIN, "Wein: Chardonnay"),
    HONEY_FOREST(8, UnlockCategory.STRAIN, "Honig: Waldhonig"),

    COFFEE_ROAST_MEDIUM(10, UnlockCategory.PROCESSING, "Kaffee: Mittlere Röstung"),
    CHEESE_PROCESSING_SMOKED(10, UnlockCategory.PROCESSING, "Käse: Räucherung"),
    WINE_SEMI_DRY(10, UnlockCategory.PROCESSING, "Wein: Halbtrocken"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 11-15: HARTE DROGEN & MITTELSTUFE
    // ═══════════════════════════════════════════════════════════
    POT_IRON(11, UnlockCategory.POT, "Eisen-Topf (5 Pflanzen)"),
    COCA_BOLIVIANISCH(11, UnlockCategory.PRODUCTION_CHAIN, "Kokain: Bolivianisch"),
    MDMA_PRODUCTION(11, UnlockCategory.PRODUCTION_CHAIN, "MDMA-Produktion"),
    CHEESE_CAMEMBERT(11, UnlockCategory.STRAIN, "Käse: Camembert"),
    BEER_STOUT(11, UnlockCategory.STRAIN, "Bier: Stout"),

    CANNABIS_HYBRID(13, UnlockCategory.STRAIN, "Cannabis: Hybrid-Sorte"),
    MUSHROOM_CUBENSIS(13, UnlockCategory.STRAIN, "Pilze: Cubensis"),
    LSD_PRODUCTION(13, UnlockCategory.PRODUCTION_CHAIN, "LSD-Produktion"),
    WINE_SPAETBURGUNDER(13, UnlockCategory.STRAIN, "Wein: Spätburgunder"),
    COFFEE_LIBERICA(13, UnlockCategory.STRAIN, "Kaffee: Liberica"),

    POPPY_INDISCH(15, UnlockCategory.PRODUCTION_CHAIN, "Mohn: Indisch (Heroin)"),
    COCA_PERUANISCH(15, UnlockCategory.STRAIN, "Kokain: Peruanisch"),
    METH_PRODUCTION(15, UnlockCategory.PRODUCTION_CHAIN, "Meth-Produktion"),
    CHOCOLATE_RUBY(15, UnlockCategory.STRAIN, "Schokolade: Ruby"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 16-20: PREMIUM
    // ═══════════════════════════════════════════════════════════
    POT_GOLDEN(16, UnlockCategory.POT, "Gold-Topf (5 Pflanzen + Qualitäts-Boost)"),
    TOBACCO_HAVANA(16, UnlockCategory.STRAIN, "Tabak: Havana-Sorte"),
    WINE_MERLOT(16, UnlockCategory.STRAIN, "Wein: Merlot"),
    COFFEE_EXCELSA(16, UnlockCategory.STRAIN, "Kaffee: Excelsa"),
    CHEESE_PARMESAN(16, UnlockCategory.STRAIN, "Käse: Parmesan"),

    POPPY_TUERKISCH(18, UnlockCategory.STRAIN, "Mohn: Türkisch"),
    COCA_KOLUMBIANISCH(18, UnlockCategory.STRAIN, "Kokain: Kolumbianisch"),
    MUSHROOM_AZURESCENS(18, UnlockCategory.STRAIN, "Pilze: Azurescens"),
    HONEY_MANUKA(18, UnlockCategory.STRAIN, "Honig: Manuka"),

    COFFEE_ROAST_ESPRESSO(20, UnlockCategory.PROCESSING, "Kaffee: Espresso-Röstung"),
    WINE_DESSERT(20, UnlockCategory.PROCESSING, "Wein: Dessertwein"),
    CHEESE_PROCESSING_HERB(20, UnlockCategory.PROCESSING, "Käse: Kräuterverarbeitung"),
    CHOCOLATE_FILLED(20, UnlockCategory.PROCESSING, "Schokolade: Gefüllt"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 21-25: EXPERTE
    // ═══════════════════════════════════════════════════════════
    POPPY_AFGHANISCH(22, UnlockCategory.STRAIN, "Mohn: Afghanisch (höchste Potenz)"),
    MACHINE_SIZE_MEDIUM(22, UnlockCategory.MACHINE, "Mittlere Produktionsanlagen"),
    VEHICLE_SUV(22, UnlockCategory.VEHICLE, "SUV-Fahrzeug"),
    VEHICLE_TRANSPORTER(22, UnlockCategory.VEHICLE, "Transporter-Fahrzeug"),

    MACHINE_SIZE_BIG(25, UnlockCategory.MACHINE, "Große Produktionsanlagen"),
    VEHICLE_SPORT(25, UnlockCategory.VEHICLE, "Sport-Fahrzeug"),

    // ═══════════════════════════════════════════════════════════
    // LEVEL 26-30: ENDGAME
    // ═══════════════════════════════════════════════════════════
    ECONOMY_MARKET_ACCESS(26, UnlockCategory.ECONOMY_FEATURE, "Markt-Zugang (Angebot/Nachfrage einsehen)"),
    ECONOMY_PRICE_ALERTS(28, UnlockCategory.ECONOMY_FEATURE, "Preis-Benachrichtigungen"),
    ECONOMY_BULK_TRADING(30, UnlockCategory.ECONOMY_FEATURE, "Großhandel (Mengenrabatte)");

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
