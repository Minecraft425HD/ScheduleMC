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
    SELL_CANNABIS("Cannabis sold", 8, true),
    SELL_TOBACCO("Tobacco sold", 5, true),
    SELL_COCAINE("Cocaine sold", 15, true),
    SELL_HEROIN("Heroin sold", 18, true),
    SELL_METH("Meth sold", 16, true),
    SELL_MDMA("MDMA sold", 12, true),
    SELL_LSD("LSD sold", 14, true),
    SELL_MUSHROOM("Mushrooms sold", 10, true),

    // ═══════════════════════════════════════════════════════════
    // LEGALE PRODUKTION (niedrigere XP, kein Risiko)
    // ═══════════════════════════════════════════════════════════
    SELL_WINE("Wine sold", 6, false),
    SELL_BEER("Beer sold", 4, false),
    SELL_COFFEE("Coffee sold", 5, false),
    SELL_CHEESE("Cheese sold", 5, false),
    SELL_CHOCOLATE("Chocolate sold", 4, false),
    SELL_HONEY("Honey sold", 4, false),

    // ═══════════════════════════════════════════════════════════
    // HERSTELLUNG (zusätzliche XP für Verarbeitung)
    // ═══════════════════════════════════════════════════════════
    HARVEST("Harvest", 2, false),
    PROCESS("Processing", 3, false),
    CRAFT_MACHINE("Machine crafted", 10, false),

    // ═══════════════════════════════════════════════════════════
    // SONSTIGES
    // ═══════════════════════════════════════════════════════════
    DAILY_LOGIN("Daily login", 5, false),
    COMPLETE_ACHIEVEMENT("Achievement completed", 25, false),
    SURVIVE_RAID("Survived raid", 50, true),

    // Generische Quellen (Fallback wenn spezifische Quelle nicht bekannt)
    SELL_ILLEGAL("Illegal item sold", 10, true),
    SELL_LEGAL("Legal item sold", 4, false),

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
