package de.rolandsw.schedulemc.npc.life.companion;

/**
 * CompanionType - Die 4 Begleiter-Typen des Systems
 *
 * Jeder Typ hat unterschiedliche Fähigkeiten und Verhaltensweisen.
 */
public enum CompanionType {

    // ═══════════════════════════════════════════════════════════
    // COMPANION TYPES
    // ═══════════════════════════════════════════════════════════

    /**
     * Kämpfer - Fokus auf Kampf und Schutz
     */
    FIGHTER("Kämpfer", "Schützt dich im Kampf", 0.8f, 1.5f, 0.5f),

    /**
     * Händler - Trägt Items und kann handeln
     */
    TRADER("Händler", "Hilft beim Handel und trägt Waren", 0.5f, 0.8f, 1.5f),

    /**
     * Späher - Erkundet und warnt vor Gefahren
     */
    SCOUT("Späher", "Erkundet die Umgebung und warnt vor Gefahren", 1.2f, 0.6f, 0.8f),

    /**
     * Heiler - Unterstützt und heilt
     */
    HEALER("Heiler", "Heilt und unterstützt dich", 0.6f, 0.5f, 1.2f);

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final String displayName;
    private final String description;
    private final float speedMultiplier;
    private final float combatMultiplier;
    private final float utilityMultiplier;

    CompanionType(String displayName, String description,
                  float speedMultiplier, float combatMultiplier, float utilityMultiplier) {
        this.displayName = displayName;
        this.description = description;
        this.speedMultiplier = speedMultiplier;
        this.combatMultiplier = combatMultiplier;
        this.utilityMultiplier = utilityMultiplier;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public float getSpeedMultiplier() { return speedMultiplier; }
    public float getCombatMultiplier() { return combatMultiplier; }
    public float getUtilityMultiplier() { return utilityMultiplier; }

    /**
     * Gibt die maximale Distanz zum Spieler zurück
     */
    public double getMaxFollowDistance() {
        return switch (this) {
            case FIGHTER -> 5.0;  // Bleibt nah
            case TRADER -> 8.0;   // Mittlere Distanz
            case SCOUT -> 20.0;   // Weite Erkundung
            case HEALER -> 6.0;   // Nah für Heilung
        };
    }

    /**
     * Gibt die ideale Distanz zum Spieler zurück
     */
    public double getIdealDistance() {
        return switch (this) {
            case FIGHTER -> 3.0;
            case TRADER -> 4.0;
            case SCOUT -> 10.0;
            case HEALER -> 4.0;
        };
    }

    /**
     * Gibt die Inventargröße des Begleiters zurück
     */
    public int getInventorySize() {
        return switch (this) {
            case FIGHTER -> 9;    // Klein
            case TRADER -> 36;    // Groß
            case SCOUT -> 18;     // Mittel
            case HEALER -> 18;    // Mittel
        };
    }

    /**
     * Kann dieser Begleitertyp kämpfen?
     */
    public boolean canFight() {
        return this == FIGHTER || this == SCOUT;
    }

    /**
     * Kann dieser Begleitertyp handeln?
     */
    public boolean canTrade() {
        return this == TRADER;
    }

    /**
     * Kann dieser Begleitertyp heilen?
     */
    public boolean canHeal() {
        return this == HEALER;
    }

    /**
     * Kann dieser Begleitertyp erkunden?
     */
    public boolean canScout() {
        return this == SCOUT;
    }

    /**
     * Basis-Angriffsstärke
     */
    public float getBaseAttack() {
        return switch (this) {
            case FIGHTER -> 6.0f;
            case TRADER -> 2.0f;
            case SCOUT -> 4.0f;
            case HEALER -> 1.0f;
        };
    }

    /**
     * Basis-Gesundheit
     */
    public float getBaseHealth() {
        return switch (this) {
            case FIGHTER -> 30.0f;
            case TRADER -> 20.0f;
            case SCOUT -> 20.0f;
            case HEALER -> 25.0f;
        };
    }

    /**
     * Benötigte Reputation um diesen Begleiter zu rekrutieren
     */
    public int getRequiredReputation() {
        return switch (this) {
            case FIGHTER -> 30;
            case TRADER -> 20;
            case SCOUT -> 25;
            case HEALER -> 40;
        };
    }

    /**
     * Kosten um diesen Begleiter zu rekrutieren
     */
    public int getRecruitmentCost() {
        return switch (this) {
            case FIGHTER -> 500;
            case TRADER -> 300;
            case SCOUT -> 400;
            case HEALER -> 600;
        };
    }
}
