package de.rolandsw.schedulemc.npc.life.world;

import de.rolandsw.schedulemc.npc.life.economy.MarketCondition;

import java.util.concurrent.ThreadLocalRandom;

/**
 * WorldEventType - Typen von Welt-Events
 *
 * Events beeinflussen die gesamte Spielwelt:
 * - Markt-Events (Boom, Crash, etc.)
 * - Krisen-Events (Hunger, Krankheit, etc.)
 * - Sonder-Events (Feste, Feiertage)
 */
public enum WorldEventType {

    // ═══════════════════════════════════════════════════════════
    // MARKET EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Wirtschaftlicher Aufschwung - Preise steigen
     */
    ECONOMIC_BOOM(
        "Wirtschaftsboom",
        "Die Wirtschaft blüht! Händler sind optimistisch.",
        EventCategory.MARKET,
        MarketCondition.BOOM,
        3, 7,  // 3-7 Tage
        0.15f  // 15% Wahrscheinlichkeit
    ),

    /**
     * Wirtschaftskrise - Preise fallen
     */
    ECONOMIC_CRISIS(
        "Wirtschaftskrise",
        "Die Wirtschaft leidet. Händler sind besorgt.",
        EventCategory.MARKET,
        MarketCondition.RECESSION,
        2, 5,
        0.10f
    ),

    /**
     * Handelsüberschuss - Viele Waren verfügbar
     */
    TRADE_SURPLUS(
        "Handelsüberschuss",
        "Die Märkte sind gut gefüllt!",
        EventCategory.MARKET,
        MarketCondition.SURPLUS,
        2, 4,
        0.20f
    ),

    /**
     * Handelsknappheit - Wenig Waren verfügbar
     */
    TRADE_SHORTAGE(
        "Warenknappheit",
        "Die Waren werden knapp!",
        EventCategory.MARKET,
        MarketCondition.SHORTAGE,
        2, 4,
        0.15f
    ),

    /**
     * Markttag - Sonderangebote
     */
    MARKET_DAY(
        "Markttag",
        "Heute ist Markttag! Besondere Angebote!",
        EventCategory.MARKET,
        MarketCondition.SALE,
        1, 1,
        0.25f
    ),

    // ═══════════════════════════════════════════════════════════
    // CRISIS EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Hungersnot - Lebensmittel werden teuer
     */
    FAMINE(
        "Hungersnot",
        "Eine Hungersnot droht! Lebensmittel werden knapp.",
        EventCategory.CRISIS,
        MarketCondition.SHORTAGE,
        5, 10,
        0.05f
    ),

    /**
     * Seuche - NPCs werden krank
     */
    PLAGUE(
        "Seuche",
        "Eine Krankheit breitet sich aus!",
        EventCategory.CRISIS,
        MarketCondition.HIGH_DEMAND,
        4, 8,
        0.05f
    ),

    /**
     * Banditenüberfall - Karawanen werden überfallen
     */
    BANDIT_RAID(
        "Banditenüberfall",
        "Banditen überfallen Handelskarawanen!",
        EventCategory.CRISIS,
        MarketCondition.SHORTAGE,
        2, 4,
        0.10f
    ),

    /**
     * Naturkatastrophe - Allgemeine Krise
     */
    NATURAL_DISASTER(
        "Naturkatastrophe",
        "Eine Naturkatastrophe hat die Region getroffen!",
        EventCategory.CRISIS,
        MarketCondition.CRISIS,
        3, 7,
        0.03f
    ),

    /**
     * Kriminelle Aktivität - Untergrund wird aktiver
     */
    CRIME_WAVE(
        "Kriminalitätswelle",
        "Die Kriminalität steigt! Vorsicht!",
        EventCategory.CRISIS,
        MarketCondition.NORMAL,
        3, 6,
        0.08f
    ),

    // ═══════════════════════════════════════════════════════════
    // SPECIAL EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Fest - Positive Stimmung
     */
    FESTIVAL(
        "Fest",
        "Ein Fest findet statt! Die Stimmung ist ausgelassen.",
        EventCategory.SPECIAL,
        MarketCondition.SALE,
        1, 2,
        0.12f
    ),

    /**
     * Feiertag - Geschäfte geschlossen
     */
    HOLIDAY(
        "Feiertag",
        "Heute ist Feiertag. Viele Geschäfte sind geschlossen.",
        EventCategory.SPECIAL,
        MarketCondition.NORMAL,
        1, 1,
        0.10f
    ),

    /**
     * Besonderer Besucher - VIP in der Stadt
     */
    VIP_VISIT(
        "Hoher Besuch",
        "Ein wichtiger Besucher ist in der Stadt!",
        EventCategory.SPECIAL,
        MarketCondition.HIGH_DEMAND,
        1, 3,
        0.05f
    ),

    /**
     * Wettkampf - Turniere und Wettbewerbe
     */
    COMPETITION(
        "Wettkampf",
        "Ein großer Wettkampf findet statt!",
        EventCategory.SPECIAL,
        MarketCondition.HIGH_DEMAND,
        1, 2,
        0.08f
    );

    // ═══════════════════════════════════════════════════════════
    // EVENT CATEGORY
    // ═══════════════════════════════════════════════════════════

    public enum EventCategory {
        MARKET,   // Wirtschaftsevents
        CRISIS,   // Krisenereignisse
        SPECIAL   // Besondere Ereignisse
    }

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final String displayName;
    private final String description;
    private final EventCategory category;
    private final MarketCondition marketEffect;
    private final int minDuration;
    private final int maxDuration;
    private final float probability;

    WorldEventType(String displayName, String description, EventCategory category,
                   MarketCondition marketEffect, int minDuration, int maxDuration, float probability) {
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.marketEffect = marketEffect;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.probability = probability;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public EventCategory getCategory() { return category; }
    public MarketCondition getMarketEffect() { return marketEffect; }
    public int getMinDuration() { return minDuration; }
    public int getMaxDuration() { return maxDuration; }
    public float getProbability() { return probability; }

    /**
     * Generiert eine zufällige Dauer innerhalb des Bereichs
     */
    public int getRandomDuration() {
        if (minDuration == maxDuration) return minDuration;
        return minDuration + ThreadLocalRandom.current().nextInt(maxDuration - minDuration + 1);
    }

    /**
     * Ist dies ein negatives Event?
     */
    public boolean isNegative() {
        return category == EventCategory.CRISIS;
    }

    /**
     * Ist dies ein positives Event?
     */
    public boolean isPositive() {
        return this == ECONOMIC_BOOM || this == TRADE_SURPLUS ||
               this == MARKET_DAY || this == FESTIVAL;
    }

    /**
     * Beeinflusst dieses Event die NPC-Stimmung?
     */
    public float getMoodEffect() {
        return switch (this) {
            case ECONOMIC_BOOM, TRADE_SURPLUS, FESTIVAL -> 20.0f;
            case MARKET_DAY, COMPETITION -> 10.0f;
            case ECONOMIC_CRISIS, TRADE_SHORTAGE -> -15.0f;
            case FAMINE, PLAGUE -> -30.0f;
            case BANDIT_RAID, CRIME_WAVE -> -20.0f;
            case NATURAL_DISASTER -> -40.0f;
            default -> 0.0f;
        };
    }

    /**
     * Preismodifikator für dieses Event
     */
    public float getPriceModifier() {
        return marketEffect.getPriceMultiplier();
    }

    /**
     * Beeinflusst dieses Event die Sicherheit?
     */
    public float getSafetyEffect() {
        return switch (this) {
            case BANDIT_RAID -> -30.0f;
            case CRIME_WAVE -> -20.0f;
            case NATURAL_DISASTER -> -25.0f;
            case PLAGUE -> -15.0f;
            case FESTIVAL, VIP_VISIT -> 10.0f;
            default -> 0.0f;
        };
    }

    /**
     * Kann dieses Event gleichzeitig mit einem anderen auftreten?
     */
    public boolean canCoexistWith(WorldEventType other) {
        // Krisentypen können nicht koexistieren
        if (this.category == EventCategory.CRISIS && other.category == EventCategory.CRISIS) {
            return false;
        }

        // Gegensätzliche Marktevents
        if ((this == ECONOMIC_BOOM && other == ECONOMIC_CRISIS) ||
            (this == ECONOMIC_CRISIS && other == ECONOMIC_BOOM) ||
            (this == TRADE_SURPLUS && other == TRADE_SHORTAGE) ||
            (this == TRADE_SHORTAGE && other == TRADE_SURPLUS)) {
            return false;
        }

        return true;
    }
}
