package de.rolandsw.schedulemc.npc.life.witness;

import de.rolandsw.schedulemc.npc.life.social.RumorType;

/**
 * CrimeType - Typen von Verbrechen die NPCs beobachten können
 *
 * Jedes Verbrechen hat eine Schwere und löst unterschiedliche
 * Reaktionen bei NPCs aus.
 */
public enum CrimeType {

    // ═══════════════════════════════════════════════════════════
    // THEFT
    // ═══════════════════════════════════════════════════════════

    /**
     * Kleindiebstahl (z.B. Taschendiebstahl)
     */
    PETTY_THEFT("Kleindiebstahl", 2, 50, RumorType.THEFT),

    /**
     * Ladendiebstahl
     */
    SHOPLIFTING("Ladendiebstahl", 3, 100, RumorType.THEFT),

    /**
     * Einbruch
     */
    BURGLARY("Einbruch", 6, 500, RumorType.BURGLARY),

    /**
     * Raub (mit Gewaltandrohung)
     */
    ROBBERY("Raub", 7, 750, RumorType.ASSAULT),

    // ═══════════════════════════════════════════════════════════
    // VIOLENCE
    // ═══════════════════════════════════════════════════════════

    /**
     * Bedrohung
     */
    THREAT("Bedrohung", 3, 100, RumorType.ASSAULT),

    /**
     * Körperverletzung
     */
    ASSAULT("Körperverletzung", 5, 300, RumorType.ASSAULT),

    /**
     * Schwere Körperverletzung
     */
    AGGRAVATED_ASSAULT("Schwere Körperverletzung", 8, 1000, RumorType.ASSAULT),

    /**
     * Waffengewalt
     */
    ARMED_VIOLENCE("Bewaffnete Gewalt", 9, 2000, RumorType.ASSAULT),

    // ═══════════════════════════════════════════════════════════
    // DRUGS
    // ═══════════════════════════════════════════════════════════

    /**
     * Drogenkonsum
     */
    DRUG_USE("Drogenkonsum", 2, 50, RumorType.DRUG_DEALING),

    /**
     * Drogenhandel (klein)
     */
    DRUG_DEALING_SMALL("Drogenhandel (klein)", 4, 200, RumorType.DRUG_DEALING),

    /**
     * Drogenhandel (groß)
     */
    DRUG_DEALING_LARGE("Drogenhandel (groß)", 7, 1000, RumorType.DRUG_DEALING),

    // ═══════════════════════════════════════════════════════════
    // OTHER
    // ═══════════════════════════════════════════════════════════

    /**
     * Vandalismus
     */
    VANDALISM("Vandalismus", 2, 75, RumorType.VANDALISM),

    /**
     * Hausfriedensbruch
     */
    TRESPASSING("Hausfriedensbruch", 3, 100, RumorType.UNRELIABLE),

    /**
     * Bestechung
     */
    BRIBERY("Bestechung", 4, 250, RumorType.UNFAIR_TRADER),

    /**
     * Betrug
     */
    FRAUD("Betrug", 5, 400, RumorType.UNFAIR_TRADER),

    /**
     * Flucht vor Polizei
     */
    EVADING_POLICE("Flucht vor Polizei", 4, 200, RumorType.WANTED_BY_POLICE);

    private final String displayName;
    private final int severity; // 1-10
    private final int baseBounty; // Standard-Kopfgeld
    private final RumorType associatedRumor;

    CrimeType(String displayName, int severity, int baseBounty, RumorType associatedRumor) {
        this.displayName = displayName;
        this.severity = severity;
        this.baseBounty = baseBounty;
        this.associatedRumor = associatedRumor;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Anzeigename für UI
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Schwere des Verbrechens (1-10)
     * Beeinflusst NPC-Reaktionen und Strafen
     */
    public int getSeverity() {
        return severity;
    }

    /**
     * Basis-Kopfgeld/Strafe in Währungseinheiten
     */
    public int getBaseBounty() {
        return baseBounty;
    }

    /**
     * Welches Gerücht wird durch dieses Verbrechen ausgelöst?
     */
    public RumorType getAssociatedRumor() {
        return associatedRumor;
    }

    // ═══════════════════════════════════════════════════════════
    // CALCULATED VALUES
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet das tatsächliche Kopfgeld basierend auf Faktoren
     *
     * @param repeatOffender Wiederholungstäter?
     * @param attemptedFlight Hat versucht zu fliehen?
     * @return Angepasstes Kopfgeld
     */
    public int calculateBounty(boolean repeatOffender, boolean attemptedFlight) {
        int bounty = baseBounty;

        if (repeatOffender) {
            bounty = (int) (bounty * 1.5);
        }
        if (attemptedFlight) {
            bounty = (int) (bounty * 1.2);
        }

        return bounty;
    }

    /**
     * Berechnet die Chance dass ein NPC dieses Verbrechen meldet
     * basierend auf Schwere und NPC-Eigenschaften
     */
    public float getBaseReportChance() {
        // Höhere Schwere = höhere Meldechance
        return Math.min(0.95f, 0.2f + (severity * 0.08f));
    }

    /**
     * Wie lange bleibt der Spieler "gesucht" (in Spieltagen)?
     */
    public int getWantedDuration() {
        return severity * 2;
    }

    /**
     * Gibt die Sicherheits-Reduktion für Zeugen zurück
     */
    public float getSafetyImpact() {
        return severity * 5.0f;
    }

    /**
     * Ist dies ein Gewaltverbrechen?
     */
    public boolean isViolent() {
        return this == THREAT || this == ASSAULT ||
               this == AGGRAVATED_ASSAULT || this == ARMED_VIOLENCE ||
               this == ROBBERY;
    }

    /**
     * Ist dies ein Eigentumsdelikt?
     */
    public boolean isPropertyCrime() {
        return this == PETTY_THEFT || this == SHOPLIFTING ||
               this == BURGLARY || this == ROBBERY ||
               this == VANDALISM || this == FRAUD;
    }

    /**
     * Ist dies ein Drogendelikt?
     */
    public boolean isDrugRelated() {
        return this == DRUG_USE || this == DRUG_DEALING_SMALL ||
               this == DRUG_DEALING_LARGE;
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    /**
     * Übersetzungsschlüssel für Lokalisierung
     */
    public String getTranslationKey() {
        return "crime." + name().toLowerCase();
    }

    /**
     * Gibt CrimeType aus Name zurück (mit Fallback)
     */
    public static CrimeType fromName(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PETTY_THEFT;
        }
    }

    /**
     * Gibt CrimeType aus Ordinal zurück (mit Fallback)
     */
    public static CrimeType fromOrdinal(int ordinal) {
        CrimeType[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return PETTY_THEFT;
    }
}
