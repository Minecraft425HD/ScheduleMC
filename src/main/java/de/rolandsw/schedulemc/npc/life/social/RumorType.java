package de.rolandsw.schedulemc.npc.life.social;

/**
 * RumorType - Typen von Gerüchten die NPCs verbreiten können
 *
 * Gerüchte beeinflussen wie NPCs auf Spieler reagieren,
 * auch ohne direkten Kontakt.
 */
public enum RumorType {

    // ═══════════════════════════════════════════════════════════
    // CRIME-RELATED RUMORS
    // ═══════════════════════════════════════════════════════════

    /**
     * Spieler hat gestohlen
     */
    THEFT("Diebstahl", -30, 0.7f, 7),

    /**
     * Spieler hat angegriffen
     */
    ASSAULT("Angriff", -50, 0.8f, 14),

    /**
     * Spieler hat Drogen gehandelt
     */
    DRUG_DEALING("Drogenhandel", -40, 0.6f, 10),

    /**
     * Spieler wurde von Polizei gesucht
     */
    WANTED_BY_POLICE("Von Polizei gesucht", -60, 0.9f, 21),

    /**
     * Spieler hat Vandalismus begangen
     */
    VANDALISM("Vandalismus", -20, 0.5f, 5),

    /**
     * Spieler hat Einbruch begangen
     */
    BURGLARY("Einbruch", -45, 0.75f, 12),

    // ═══════════════════════════════════════════════════════════
    // REPUTATION-RELATED RUMORS
    // ═══════════════════════════════════════════════════════════

    /**
     * Spieler ist großzügig
     */
    GENEROUS("Großzügig", 25, 0.5f, 7),

    /**
     * Spieler zahlt faire Preise
     */
    FAIR_TRADER("Fairer Händler", 20, 0.4f, 5),

    /**
     * Spieler hilft NPCs
     */
    HELPFUL("Hilfsbereit", 30, 0.6f, 10),

    /**
     * Spieler hat eine Quest erfolgreich abgeschlossen
     */
    QUEST_COMPLETED("Quest erfüllt", 15, 0.3f, 3),

    /**
     * Spieler ist unzuverlässig
     */
    UNRELIABLE("Unzuverlässig", -15, 0.4f, 5),

    /**
     * Spieler hat Versprechen gebrochen
     */
    PROMISE_BREAKER("Wortbrüchig", -25, 0.5f, 7),

    /**
     * Spieler handelt unfair
     */
    UNFAIR_TRADER("Unfairer Händler", -20, 0.4f, 5),

    /**
     * Spieler ist reich
     */
    WEALTHY("Wohlhabend", 0, 0.3f, 3),

    /**
     * Spieler ist arm
     */
    POOR("Mittellos", 0, 0.2f, 2);

    private final String displayName;
    private final int reputationImpact;
    private final float spreadChance;
    private final int baseDurationDays;

    RumorType(String displayName, int reputationImpact, float spreadChance, int baseDurationDays) {
        this.displayName = displayName;
        this.reputationImpact = reputationImpact;
        this.spreadChance = spreadChance;
        this.baseDurationDays = baseDurationDays;
    }

    /**
     * Anzeigename für UI und Dialoge
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Wie stark beeinflusst dieses Gerücht die Reputation?
     * Positiv = gut, Negativ = schlecht
     */
    public int getReputationImpact() {
        return reputationImpact;
    }

    /**
     * Wie wahrscheinlich ist es, dass das Gerücht weiterverbreitet wird?
     * 0.0 - 1.0
     */
    public float getSpreadChance() {
        return spreadChance;
    }

    /**
     * Wie viele Tage hält das Gerücht an (Basiswert)?
     */
    public int getBaseDurationDays() {
        return baseDurationDays;
    }

    /**
     * Ist dies ein kriminelles Gerücht?
     */
    public boolean isCrimeRelated() {
        return reputationImpact < -25;
    }

    /**
     * Ist dies ein positives Gerücht?
     */
    public boolean isPositive() {
        return reputationImpact > 0;
    }

    /**
     * Ist dies ein negatives Gerücht?
     */
    public boolean isNegative() {
        return reputationImpact < 0;
    }

    /**
     * Übersetzungsschlüssel für Lokalisierung
     */
    public String getTranslationKey() {
        return "npc.rumor." + name().toLowerCase();
    }

    /**
     * Gibt RumorType aus Name zurück (mit Fallback)
     */
    public static RumorType fromName(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNRELIABLE;
        }
    }
}
