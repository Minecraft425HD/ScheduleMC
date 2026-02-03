package de.rolandsw.schedulemc.economy;

/**
 * Kategorisiert alle handelbaren Items im Wirtschaftssystem.
 *
 * Jede Kategorie hat:
 * - Basis-Preis-Grenzen (min/max Multiplikator)
 * - Angebot/Nachfrage-Empfindlichkeit
 * - Ob es ein illegales Produkt ist (für Risiko-Aufschläge)
 * - Steuersatz-Kategorie
 */
public enum ItemCategory {

    // ═══════════════════════════════════════════════════════════
    // ILLEGALE PRODUKTE (hohes Risiko, hohe Marge)
    // ═══════════════════════════════════════════════════════════
    CANNABIS("Cannabis", true, 0.4, 4.0, 0.5, 0.35),
    TOBACCO_PRODUCT("Tabak-Produkt", true, 0.5, 3.5, 0.4, 0.30),
    COCAINE("Kokain", true, 0.3, 5.0, 0.6, 0.50),
    HEROIN("Heroin", true, 0.3, 5.0, 0.6, 0.50),
    METH("Methamphetamin", true, 0.3, 5.0, 0.6, 0.45),
    MDMA("MDMA/Ecstasy", true, 0.4, 4.5, 0.5, 0.40),
    LSD("LSD", true, 0.3, 5.0, 0.5, 0.45),
    MUSHROOM("Pilze", true, 0.4, 4.0, 0.5, 0.35),

    // ═══════════════════════════════════════════════════════════
    // LEGALE PRODUKTE (niedriges Risiko, niedrigere Marge)
    // ═══════════════════════════════════════════════════════════
    WINE("Wein", false, 0.5, 3.0, 0.4, 0.19),
    BEER("Bier", false, 0.5, 3.0, 0.4, 0.19),
    COFFEE("Kaffee", false, 0.5, 3.0, 0.4, 0.19),
    CHEESE("Käse", false, 0.5, 3.0, 0.4, 0.19),
    CHOCOLATE("Schokolade", false, 0.5, 3.0, 0.4, 0.19),
    HONEY("Honig", false, 0.5, 3.0, 0.4, 0.19),

    // ═══════════════════════════════════════════════════════════
    // ROHSTOFFE & SAMEN
    // ═══════════════════════════════════════════════════════════
    SEED_ILLEGAL("Illegale Samen/Sporen", true, 0.6, 2.5, 0.3, 0.0),
    SEED_LEGAL("Legale Samen/Setzlinge", false, 0.6, 2.5, 0.3, 0.0),
    RAW_MATERIAL("Rohstoff", false, 0.7, 2.0, 0.2, 0.0),
    CHEMICAL("Chemikalie", true, 0.5, 3.0, 0.4, 0.0),

    // ═══════════════════════════════════════════════════════════
    // MASCHINEN & AUSRÜSTUNG
    // ═══════════════════════════════════════════════════════════
    MACHINE_ILLEGAL("Illegale Maschine", true, 0.7, 2.0, 0.2, 0.0),
    MACHINE_LEGAL("Legale Maschine", false, 0.7, 2.0, 0.2, 0.0),
    POT("Topf/Behälter", false, 0.8, 1.5, 0.15, 0.0),

    // ═══════════════════════════════════════════════════════════
    // FAHRZEUGE
    // ═══════════════════════════════════════════════════════════
    VEHICLE("Fahrzeug", false, 0.7, 1.8, 0.15, 0.19),
    VEHICLE_UPGRADE("Fahrzeug-Upgrade", false, 0.8, 1.5, 0.1, 0.19),

    // ═══════════════════════════════════════════════════════════
    // SHOP-WAREN
    // ═══════════════════════════════════════════════════════════
    FOOD("Nahrung", false, 0.6, 2.0, 0.3, 0.07),
    BUILDING_MATERIAL("Baumaterial", false, 0.7, 1.8, 0.2, 0.19),
    WEAPON("Waffe", false, 0.6, 2.5, 0.3, 0.19),
    TOOL("Werkzeug", false, 0.7, 2.0, 0.2, 0.19),
    RARE_ITEM("Seltenes Item", false, 0.4, 4.0, 0.5, 0.19),

    // ═══════════════════════════════════════════════════════════
    // SONSTIGES
    // ═══════════════════════════════════════════════════════════
    SERVICE("Dienstleistung", false, 0.8, 1.5, 0.1, 0.19),
    OTHER("Sonstiges", false, 0.5, 2.0, 0.3, 0.19);

    private final String displayName;
    private final boolean illegal;
    private final double minPriceMultiplier;
    private final double maxPriceMultiplier;
    private final double supplyDemandSensitivity;
    private final double taxRate;

    ItemCategory(String displayName, boolean illegal, double minPriceMultiplier,
                 double maxPriceMultiplier, double supplyDemandSensitivity, double taxRate) {
        this.displayName = displayName;
        this.illegal = illegal;
        this.minPriceMultiplier = minPriceMultiplier;
        this.maxPriceMultiplier = maxPriceMultiplier;
        this.supplyDemandSensitivity = supplyDemandSensitivity;
        this.taxRate = taxRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return true wenn das Produkt illegal ist (Konfiszierung bei Razzia möglich)
     */
    public boolean isIllegal() {
        return illegal;
    }

    /**
     * @return Minimaler Preis-Multiplikator (z.B. 0.3 = Preis kann auf 30% fallen)
     */
    public double getMinPriceMultiplier() {
        return minPriceMultiplier;
    }

    /**
     * @return Maximaler Preis-Multiplikator (z.B. 5.0 = Preis kann auf 500% steigen)
     */
    public double getMaxPriceMultiplier() {
        return maxPriceMultiplier;
    }

    /**
     * @return Wie stark Angebot/Nachfrage den Preis beeinflusst (0.0-1.0)
     *         Höher = volatiler, niedriger = stabiler
     */
    public double getSupplyDemandSensitivity() {
        return supplyDemandSensitivity;
    }

    /**
     * @return Steuersatz für Verkäufe (0.0-1.0, z.B. 0.19 = 19% MwSt)
     */
    public double getTaxRate() {
        return taxRate;
    }

    /**
     * @return true wenn dies ein fertiges Endprodukt ist (Cannabis, Wein, etc.)
     */
    public boolean isFinishedProduct() {
        return switch (this) {
            case CANNABIS, TOBACCO_PRODUCT, COCAINE, HEROIN, METH, MDMA, LSD, MUSHROOM,
                 WINE, BEER, COFFEE, CHEESE, CHOCOLATE, HONEY -> true;
            default -> false;
        };
    }

    /**
     * @return true wenn dies ein Produktionsmittel ist (Maschine, Topf, etc.)
     */
    public boolean isProductionEquipment() {
        return switch (this) {
            case MACHINE_ILLEGAL, MACHINE_LEGAL, POT -> true;
            default -> false;
        };
    }

    /**
     * Mappt eine MerchantCategory auf die passende ItemCategory.
     *
     * @param merchantCategory die NPC-Shop-Kategorie
     * @return entsprechende ItemCategory für dynamische Preisberechnung
     */
    public static ItemCategory fromMerchantCategory(de.rolandsw.schedulemc.npc.data.MerchantCategory merchantCategory) {
        return switch (merchantCategory) {
            case BAUMARKT -> BUILDING_MATERIAL;
            case WAFFENHAENDLER -> WEAPON;
            case TANKSTELLE -> SERVICE;
            case LEBENSMITTEL -> FOOD;
            case PERSONALMANAGEMENT -> SERVICE;
            case ILLEGALER_HAENDLER -> CHEMICAL;
            case AUTOHAENDLER -> VEHICLE;
        };
    }
}
