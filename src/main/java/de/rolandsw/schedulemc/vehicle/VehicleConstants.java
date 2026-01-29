package de.rolandsw.schedulemc.vehicle;

/**
 * Zentrale Konstanten für das Vehicle-System
 *
 * Eliminiert Magic Numbers und verbessert Wartbarkeit.
 * Alle Werte sind dokumentiert und können später in Config verschoben werden.
 */
public final class VehicleConstants {

    private VehicleConstants() {
        // Utility class - no instantiation
    }

    // ═══════════════════════════════════════════════════════════
    // PHYSICS & COLLISION
    // ═══════════════════════════════════════════════════════════

    /**
     * Mindestgeschwindigkeit für Entity-Schaden bei Kollision
     */
    public static final float MIN_DAMAGE_SPEED = 0.35F;

    /**
     * Multiplikator für Schaden bei Entity-Kollision
     * damage = speed * DAMAGE_MULTIPLIER
     */
    public static final float DAMAGE_MULTIPLIER = 10F;

    /**
     * Geschwindigkeit nach Kollision (nahe 0, aber nicht exakt 0)
     */
    public static final float POST_COLLISION_SPEED = 0.01F;

    /**
     * Mindestgeschwindigkeit für Rotation
     */
    public static final float MIN_ROTATION_THRESHOLD = 0.02F;

    // ═══════════════════════════════════════════════════════════
    // DAMAGE & TEMPERATURE
    // ═══════════════════════════════════════════════════════════

    /**
     * Schaden-Schwellwerte für Partikel-Effekte
     */
    public static final int DAMAGE_THRESHOLD_LOW = 50;
    public static final int DAMAGE_THRESHOLD_MEDIUM = 70;
    public static final int DAMAGE_THRESHOLD_HIGH = 80;
    public static final int DAMAGE_THRESHOLD_CRITICAL = 90;

    /**
     * Partikel-Anzahl basierend auf Schaden
     */
    public static final int PARTICLES_LOW = 1;
    public static final int PARTICLES_MEDIUM = 2;
    public static final int PARTICLES_HIGH = 3;

    /**
     * Schaden-Schwellwerte für Kollisions-Effekte
     */
    public static final float COLLISION_DAMAGE_THRESHOLD = 0.8F;
    public static final float COLLISION_ENGINE_STOP_THRESHOLD = 0.9F;
    public static final float COLLISION_DAMAGE_MULTIPLIER = 5F;

    /**
     * Maximaler Schaden-Wert
     */
    public static final float MAX_DAMAGE = 100F;

    /**
     * Schaden-Schwellwerte für Motorstart-Verzögerung
     */
    public static final int DAMAGE_CRITICAL_START = 95;
    public static final int DAMAGE_HIGH_START = 90;
    public static final int DAMAGE_MEDIUM_START = 80;
    public static final int DAMAGE_LOW_START = 50;

    /**
     * Motorstart-Verzögerungen (in Ticks) basierend auf Schaden
     */
    public static final int START_DELAY_CRITICAL_MIN = 50;
    public static final int START_DELAY_CRITICAL_RANGE = 25;
    public static final int START_DELAY_HIGH_MIN = 30;
    public static final int START_DELAY_HIGH_RANGE = 15;
    public static final int START_DELAY_MEDIUM_MIN = 10;
    public static final int START_DELAY_MEDIUM_RANGE = 15;
    public static final int START_DELAY_LOW_MIN = 5;
    public static final int START_DELAY_LOW_RANGE = 10;

    /**
     * Temperatur-Konstanten
     */
    public static final float BIOME_TEMP_HOT_THRESHOLD = 45F;
    public static final float BIOME_TEMP_COLD_THRESHOLD = 0F;
    public static final float TEMP_HOT_ENGINE_TARGET = 100F;
    public static final float TEMP_COLD_ENGINE_TARGET = 80F;
    public static final float BIOME_TEMP_OFFSET = 0.3F;
    public static final float BIOME_TEMP_MULTIPLIER = 30F;

    /**
     * Temperatur-Änderungs-Rate
     */
    public static final int TEMP_RATE_MAX = 5;
    public static final float TEMP_RATE_BASE = 0.2F;
    public static final float TEMP_RATE_RANDOMNESS = 0.1F;

    // ═══════════════════════════════════════════════════════════
    // SOUND & BATTERY
    // ═══════════════════════════════════════════════════════════

    /**
     * Batterie-Kosten für Hupe
     */
    public static final int HORN_BATTERY_COST = 10;

    /**
     * Minimale Batterie-Level für Hupe
     */
    public static final int MIN_BATTERY_FOR_HORN = 10;

    // ═══════════════════════════════════════════════════════════
    // MONSTER FLEE SYSTEM
    // ═══════════════════════════════════════════════════════════

    /**
     * Radius für Monster-Flucht bei Hupe (in Blöcken)
     */
    public static final double HORN_FLEE_RADIUS = 15.0;

    /**
     * Distanz für Monster-Flucht
     */
    public static final double FLEE_DISTANCE = 10.0;

    /**
     * Flucht-Geschwindigkeit für Monster
     */
    public static final double FLEE_SPEED = 2.5;

    // ═══════════════════════════════════════════════════════════
    // REPAIR SYSTEM
    // ═══════════════════════════════════════════════════════════

    /**
     * Zeit-Fenster für Doppelklick-Repair (in Ticks)
     */
    public static final long REPAIR_DOUBLE_CLICK_WINDOW = 10L;

    /**
     * Haltbarkeits-Kosten für Repair-Kit bei Destroy
     */
    public static final int REPAIR_KIT_DESTROY_COST = 50;

    // ═══════════════════════════════════════════════════════════
    // PARTICLE EFFECTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Zufalls-Chancen für Partikel (1 in X)
     */
    public static final int PARTICLE_CHANCE_LOW = 10;
    public static final int PARTICLE_CHANCE_MEDIUM = 5;

    // ═══════════════════════════════════════════════════════════
    // ODOMETER & VEHICLE AGING
    // ═══════════════════════════════════════════════════════════

    /**
     * Kilometer-Schwellwerte für maximale Fahrzeug-Gesundheit.
     * Werte in Blöcken (1 Block ≈ 1 Meter).
     *
     * 0 - 250.000: 100% max health
     * 250.000 - 500.000: 75% max health
     * 500.000 - 750.000: 50% max health
     * 750.000 - 1.000.000: 25% max health
     */
    public static final long ODOMETER_TIER_1 = 250_000L;
    public static final long ODOMETER_TIER_2 = 500_000L;
    public static final long ODOMETER_TIER_3 = 750_000L;
    public static final long ODOMETER_TIER_4 = 1_000_000L;

    /**
     * Maximale Gesundheit in Prozent pro Altersstufe (0.0 - 1.0)
     */
    public static final float AGING_MAX_HEALTH_TIER_0 = 1.00F;  // 100%
    public static final float AGING_MAX_HEALTH_TIER_1 = 0.75F;  //  75%
    public static final float AGING_MAX_HEALTH_TIER_2 = 0.50F;  //  50%
    public static final float AGING_MAX_HEALTH_TIER_3 = 0.25F;  //  25%

    // ═══════════════════════════════════════════════════════════
    // TIRE SEASONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Speed-Modifier wenn falsche Reifen für die Jahreszeit montiert sind.
     * 1.0 = volle Geschwindigkeit, 0.5 = halbe Geschwindigkeit
     */
    public static final float TIRE_SEASON_CORRECT_MODIFIER = 1.0F;
    public static final float TIRE_SEASON_WRONG_MODIFIER = 0.5F;
    public static final float TIRE_SEASON_ALLSEASON_MODIFIER = 0.85F;
}
