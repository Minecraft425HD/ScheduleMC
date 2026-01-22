package de.rolandsw.schedulemc.npc.life.economy;

/**
 * MarketCondition - Verschiedene Marktzustände die Preise beeinflussen
 *
 * Marktzustände können global oder für bestimmte Warengruppen gelten.
 */
public enum MarketCondition {

    // ═══════════════════════════════════════════════════════════
    // CONDITIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Normaler Markt - Standardpreise
     */
    NORMAL("Normal", 1.0f, "Normale Marktbedingungen"),

    /**
     * Boom - Hohe Nachfrage, hohe Preise
     */
    BOOM("Boom", 1.3f, "Hohe Nachfrage - Preise steigen"),

    /**
     * Rezession - Niedrige Nachfrage, niedrige Preise
     */
    RECESSION("Rezession", 0.8f, "Wirtschaftskrise - Preise fallen"),

    /**
     * Knappheit - Waren sind rar, sehr hohe Preise
     */
    SHORTAGE("Knappheit", 1.8f, "Warenknappheit - Extreme Preise"),

    /**
     * Überfluss - Zu viele Waren, sehr niedrige Preise
     */
    SURPLUS("Überfluss", 0.6f, "Warenüberfluss - Tiefstpreise"),

    /**
     * Krise - Extreme Preisschwankungen
     */
    CRISIS("Krise", 2.0f, "Marktkrise - Unberechenbare Preise"),

    /**
     * Erholung - Markt erholt sich
     */
    RECOVERY("Erholung", 1.1f, "Markt erholt sich"),

    /**
     * Inflation - Allgemeine Preissteigerung
     */
    INFLATION("Inflation", 1.4f, "Geldwert sinkt - Preise steigen"),

    /**
     * Deflation - Allgemeiner Preisverfall
     */
    DEFLATION("Deflation", 0.7f, "Geldwert steigt - Preise fallen");

    private final String displayName;
    private final float priceMultiplier;
    private final String description;

    MarketCondition(String displayName, float priceMultiplier, String description) {
        this.displayName = displayName;
        this.priceMultiplier = priceMultiplier;
        this.description = description;
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
     * Basis-Preis-Multiplikator
     */
    public float getPriceMultiplier() {
        return priceMultiplier;
    }

    /**
     * Beschreibung des Zustands
     */
    public String getDescription() {
        return description;
    }

    // ═══════════════════════════════════════════════════════════
    // CALCULATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den angepassten Preis
     */
    public int applyToPrice(int basePrice) {
        return Math.max(1, (int) (basePrice * priceMultiplier));
    }

    /**
     * Berechnet den angepassten Preis mit zusätzlichem Modifikator
     */
    public int applyToPrice(int basePrice, float additionalModifier) {
        return Math.max(1, (int) (basePrice * priceMultiplier * additionalModifier));
    }

    /**
     * Ist dies ein günstiger Markt für Käufer?
     */
    public boolean isBuyerFavorable() {
        return priceMultiplier < 1.0f;
    }

    /**
     * Ist dies ein günstiger Markt für Verkäufer?
     */
    public boolean isSellerFavorable() {
        return priceMultiplier > 1.0f;
    }

    /**
     * Ist dies ein kritischer Marktzustand?
     */
    public boolean isCritical() {
        return this == CRISIS || this == SHORTAGE;
    }

    // ═══════════════════════════════════════════════════════════
    // TRANSITIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt mögliche natürliche Übergänge zurück
     */
    public MarketCondition[] getPossibleTransitions() {
        return switch (this) {
            case NORMAL -> new MarketCondition[]{BOOM, RECESSION, SHORTAGE, SURPLUS};
            case BOOM -> new MarketCondition[]{NORMAL, INFLATION, RECESSION};
            case RECESSION -> new MarketCondition[]{NORMAL, RECOVERY, CRISIS, DEFLATION};
            case SHORTAGE -> new MarketCondition[]{NORMAL, CRISIS, RECOVERY};
            case SURPLUS -> new MarketCondition[]{NORMAL, RECESSION, DEFLATION};
            case CRISIS -> new MarketCondition[]{RECESSION, SHORTAGE, RECOVERY};
            case RECOVERY -> new MarketCondition[]{NORMAL, BOOM};
            case INFLATION -> new MarketCondition[]{NORMAL, CRISIS, BOOM};
            case DEFLATION -> new MarketCondition[]{NORMAL, RECESSION, RECOVERY};
        };
    }

    /**
     * Berechnet die Wahrscheinlichkeit eines Übergangs
     */
    public float getTransitionChance(MarketCondition target) {
        MarketCondition[] possible = getPossibleTransitions();
        for (MarketCondition c : possible) {
            if (c == target) {
                // Normal ist immer wahrscheinlicher
                if (target == NORMAL) return 0.4f;
                // Kritische Zustände seltener
                if (target.isCritical()) return 0.1f;
                return 0.2f;
            }
        }
        return 0.0f; // Übergang nicht möglich
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    /**
     * Übersetzungsschlüssel für Lokalisierung
     */
    public String getTranslationKey() {
        return "market." + name().toLowerCase();
    }

    /**
     * Gibt MarketCondition aus Name zurück (mit Fallback)
     */
    public static MarketCondition fromName(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NORMAL;
        }
    }

    /**
     * Gibt MarketCondition aus Ordinal zurück (mit Fallback)
     */
    public static MarketCondition fromOrdinal(int ordinal) {
        MarketCondition[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return NORMAL;
    }
}
