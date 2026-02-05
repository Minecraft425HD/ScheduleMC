package de.rolandsw.schedulemc.level;

/**
 * XP-Quellen für das Produzenten-Level-System.
 *
 * Verschiedene Aktionen geben unterschiedlich viel XP.
 * Illegale Aktionen geben mehr XP pro Einheit (höheres Risiko = schnellerer Aufstieg),
 * aber legale Produktion ist sicherer (kein Konfiszierungs-Risiko).
 *
 * XP-Formel: baseXP × amountMultiplier × qualityMultiplier
 */
public enum XPSource {

    // ═══════════════════════════════════════════════════════════
    // ILLEGALE PRODUKTION (hohe XP, hohes Risiko)
    // ═══════════════════════════════════════════════════════════
    SELL_CANNABIS("Cannabis verkauft", 8, true),
    SELL_TOBACCO("Tabak verkauft", 5, true),
    SELL_COCAINE("Kokain verkauft", 15, true),
    SELL_HEROIN("Heroin verkauft", 18, true),
    SELL_METH("Meth verkauft", 16, true),
    SELL_MDMA("MDMA verkauft", 12, true),
    SELL_LSD("LSD verkauft", 14, true),
    SELL_MUSHROOM("Pilze verkauft", 10, true),

    // ═══════════════════════════════════════════════════════════
    // LEGALE PRODUKTION (niedrigere XP, kein Risiko)
    // ═══════════════════════════════════════════════════════════
    SELL_WINE("Wein verkauft", 6, false),
    SELL_BEER("Bier verkauft", 4, false),
    SELL_COFFEE("Kaffee verkauft", 5, false),
    SELL_CHEESE("Käse verkauft", 5, false),
    SELL_CHOCOLATE("Schokolade verkauft", 4, false),
    SELL_HONEY("Honig verkauft", 4, false),

    // ═══════════════════════════════════════════════════════════
    // HERSTELLUNG (zusätzliche XP für Verarbeitung)
    // ═══════════════════════════════════════════════════════════
    HARVEST("Ernte", 2, false),
    PROCESS("Verarbeitung", 3, false),
    CRAFT_MACHINE("Maschine hergestellt", 10, false),

    // ═══════════════════════════════════════════════════════════
    // SONSTIGES
    // ═══════════════════════════════════════════════════════════
    DAILY_LOGIN("Tägliches Login", 5, false),
    COMPLETE_ACHIEVEMENT("Achievement abgeschlossen", 25, false),
    SURVIVE_RAID("Razzia überlebt", 50, true),

    // Generische Quellen (Fallback wenn spezifische Quelle nicht bekannt)
    SELL_ILLEGAL("Illegalen Artikel verkauft", 10, true),
    SELL_LEGAL("Legalen Artikel verkauft", 4, false),

    // Admin
    ADMIN_GRANT("Admin-Vergabe", 1, false);

    private final String displayName;
    private final int baseXP;
    private final boolean illegal;

    XPSource(String displayName, int baseXP, boolean illegal) {
        this.displayName = displayName;
        this.baseXP = baseXP;
        this.illegal = illegal;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return Basis-XP die diese Aktion gibt
     */
    public int getBaseXP() {
        return baseXP;
    }

    /**
     * @return true wenn dies eine illegale Aktion ist
     */
    public boolean isIllegal() {
        return illegal;
    }

    /**
     * Berechnet die XP für eine bestimmte Menge und Qualität.
     *
     * @param amount            Menge der Items
     * @param qualityMultiplier Qualitäts-Multiplikator (1.0 = Standard)
     * @return Berechnete XP
     */
    public int calculateXP(int amount, double qualityMultiplier) {
        return (int) (baseXP * amount * qualityMultiplier);
    }
}
