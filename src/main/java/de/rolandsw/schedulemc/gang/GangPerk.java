package de.rolandsw.schedulemc.gang;

/**
 * Gang-Perks im 4-Zweige Perk-Tree.
 *
 * Zweige:
 * - TERRITORY: Gebiets-Kontrolle und Verteidigung
 * - ECONOMY: Finanzielle Vorteile
 * - CRIME: Kriminelle Vorteile
 * - PRODUCTION: Produktions-Boni
 *
 * Jeder Perk hat ein benoetigtes Gang-Level und kostet 1 Perk-Punkt.
 * Pro Gang-Level erhaelt die Gang 1 Perk-Punkt.
 */
public enum GangPerk {

    // ═══════════════════════════════════════════════════════════
    // TERRITORY-ZWEIG
    // ═══════════════════════════════════════════════════════════
    TERRITORY_EXPAND("Gebietserweiterung", PerkBranch.TERRITORY, 3,
            "Erlaubt mehr Territory-Chunks (4 → 9)"),
    TERRITORY_FORTIFY("Festung", PerkBranch.TERRITORY, 8,
            "Warnung wenn Fremde Gang-Territory betreten"),
    TERRITORY_DOMINANCE("Dominanz", PerkBranch.TERRITORY, 15,
            "Max Territory-Chunks (9 → 16)"),
    TERRITORY_STRONGHOLD("Hochburg", PerkBranch.TERRITORY, 22,
            "Max Territory-Chunks (16 → 25), Verkaufsboni im Gebiet"),
    TERRITORY_EMPIRE("Imperium", PerkBranch.TERRITORY, 28,
            "Keine Territory-Limitierung"),

    // ═══════════════════════════════════════════════════════════
    // ECONOMY-ZWEIG
    // ═══════════════════════════════════════════════════════════
    ECONOMY_BANK("Gang-Konto", PerkBranch.ECONOMY, 4,
            "Gemeinsames Gang-Bankkonto"),
    ECONOMY_TAX("Schutzgeld", PerkBranch.ECONOMY, 10,
            "5% Einnahmen von Shops in Gang-Territory"),
    ECONOMY_TRADE("Handelsrouten", PerkBranch.ECONOMY, 16,
            "10% Rabatt auf Warehouse-Lieferungen"),
    ECONOMY_LAUNDERING("Geldwaesche", PerkBranch.ECONOMY, 21,
            "Reduzierte Steuern fuer Gang-Mitglieder"),
    ECONOMY_MONOPOLY("Monopol", PerkBranch.ECONOMY, 27,
            "15% Preisbonus beim Verkauf in eigenem Territory"),

    // ═══════════════════════════════════════════════════════════
    // CRIME-ZWEIG
    // ═══════════════════════════════════════════════════════════
    CRIME_PROTECTION("Schutz", PerkBranch.CRIME, 5,
            "Wanted-Level sinkt 20% schneller in Gang-Territory"),
    CRIME_BRIBERY("Bestechung", PerkBranch.CRIME, 11,
            "30% Rabatt auf Bestechungskosten"),
    CRIME_ESCAPE("Fluchtrouten", PerkBranch.CRIME, 17,
            "Flucht-Timer 25% kuerzer"),
    CRIME_INTIMIDATION("Einschuechterung", PerkBranch.CRIME, 23,
            "NPCs in Gang-Territory melden keine Verbrechen"),
    CRIME_UNTOUCHABLE("Unberuehrbar", PerkBranch.CRIME, 29,
            "Max Wanted-Level in eigenem Territory: 3 statt 5"),

    // ═══════════════════════════════════════════════════════════
    // PRODUCTION-ZWEIG
    // ═══════════════════════════════════════════════════════════
    PRODUCTION_XP_BOOST("XP-Boost", PerkBranch.PRODUCTION, 3,
            "+15% ProducerLevel-XP fuer alle Mitglieder"),
    PRODUCTION_QUALITY("Qualitaetsboost", PerkBranch.PRODUCTION, 9,
            "+10% Qualitaet bei Produktion"),
    PRODUCTION_SHARED_STORAGE("Gemeinsames Lager", PerkBranch.PRODUCTION, 14,
            "Gang-Warehouse fuer alle Mitglieder"),
    PRODUCTION_EFFICIENCY("Effizienz", PerkBranch.PRODUCTION, 20,
            "Produktionszeit -15%"),
    PRODUCTION_MASTERY("Meisterschaft", PerkBranch.PRODUCTION, 26,
            "+25% XP und +15% Qualitaet");

    private final String displayName;
    private final PerkBranch branch;
    private final int requiredGangLevel;
    private final String description;

    GangPerk(String displayName, PerkBranch branch, int requiredGangLevel, String description) {
        this.displayName = displayName;
        this.branch = branch;
        this.requiredGangLevel = requiredGangLevel;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public PerkBranch getBranch() { return branch; }
    public int getRequiredGangLevel() { return requiredGangLevel; }
    public String getDescription() { return description; }

    /**
     * Prueft ob dieser Perk mit dem gegebenen Gang-Level und Punktestand freigeschaltet werden kann.
     */
    public boolean canUnlock(int gangLevel) {
        return gangLevel >= requiredGangLevel;
    }

    /**
     * Perk-Zweige
     */
    public enum PerkBranch {
        TERRITORY("Territory", "\u00A72", "Gebiets-Kontrolle"),
        ECONOMY("Wirtschaft", "\u00A76", "Finanzielle Vorteile"),
        CRIME("Crime", "\u00A7c", "Kriminelle Vorteile"),
        PRODUCTION("Produktion", "\u00A7b", "Produktions-Boni");

        private final String displayName;
        private final String colorCode;
        private final String description;

        PerkBranch(String displayName, String colorCode, String description) {
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getColorCode() { return colorCode; }
        public String getDescription() { return description; }
    }
}
