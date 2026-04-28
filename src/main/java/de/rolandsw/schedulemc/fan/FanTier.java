package de.rolandsw.schedulemc.fan;

/**
 * Drei Leistungsstufen des Ventilators.
 *
 * Multiplikatoren wirken additiv: mehrere Ventilatoren stapeln sich.
 *   1× Tier-1 = 1.5×  |  1× Tier-2 = 2.5×  |  1× Tier-3 = 4.0×
 *   2× Tier-1 = 2.0×  |  Tier-1 + Tier-2 = 3.0×
 *   (Gesamtmultiplikator: 1.0 + Σ(tierMultiplier - 1.0), max 8×)
 */
public enum FanTier {

    /** Kleiner Ventilator — einfaches Eisengehäuse, leichter Boost */
    TIER_1(1.5f),

    /** Großer Ventilator — verstärktes Gehäuse, deutlicher Boost */
    TIER_2(2.5f),

    /** Industrieventilator — schweres Stahlgehäuse, maximaler Boost */
    TIER_3(4.0f);

    private final float multiplier;

    FanTier(float multiplier) {
        this.multiplier = multiplier;
    }

    /** Rückgabe des Boost-Multiplikators (z.B. 1.5 für +50 % Geschwindigkeit) */
    public float getMultiplier() {
        return multiplier;
    }

    /** Numerische Stufe (1-3) für Anzeige */
    public int getLevel() {
        return ordinal() + 1;
    }
}
