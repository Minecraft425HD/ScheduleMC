package de.rolandsw.schedulemc.tobacco.business;

/**
 * TobaccoBusinessConstants - Zentrale Konstanten für das Tabak-Handelssystem
 *
 * Enthält alle konfigurierbaren Werte für:
 * - Preisberechnung
 * - Verhandlungsparameter
 * - Score-Gewichtungen
 * - Suchtverhalten
 * - Persönlichkeitstraits
 * - Beziehungsmodifikatoren
 */
public final class TobaccoBusinessConstants {

    private TobaccoBusinessConstants() {} // Utility class

    // ═══════════════════════════════════════════════════════════
    // PRICE CALCULATION (PriceCalculator.java)
    // ═══════════════════════════════════════════════════════════

    public static final class Price {
        /** Basis-Multiplikator für Reputation (0.85-1.20 Range) */
        public static final double REPUTATION_BASE_MULTIPLIER = 0.85;
        public static final double REPUTATION_RANGE_MULTIPLIER = 0.35;

        /** Basis-Multiplikator für Zufriedenheit (0.80-1.10 Range) */
        public static final double SATISFACTION_BASE_MULTIPLIER = 0.80;
        public static final double SATISFACTION_RANGE_MULTIPLIER = 0.30;

        /** Preisgrenzen relativ zum fairen Preis */
        public static final double MIN_PRICE_RATIO = 0.7;   // 70% des fairen Preises
        public static final double MAX_PRICE_RATIO = 1.0;   // 100% des fairen Preises
        public static final double IDEAL_PRICE_RATIO = 0.85; // 85% des fairen Preises

        /** Geschätzter Durchschnittspreis pro Gramm */
        public static final double ESTIMATED_PRICE_PER_GRAM = 5.0;
    }

    // ═══════════════════════════════════════════════════════════
    // NEGOTIATION ENGINE (NegotiationEngine.java)
    // ═══════════════════════════════════════════════════════════

    public static final class Negotiation {
        /** Schwelle für absurde Angebote (> 130% Max = sofortige Ablehnung) */
        public static final double ABSURD_OFFER_THRESHOLD = 1.3;

        /** Maximaler Rabatt bei Ablehnung */
        public static final double MAX_ACCEPTABLE_DISCOUNT = 0.8;

        /** Counter-Offer Multiplikator */
        public static final double COUNTER_OFFER_MULTIPLIER = 0.9;

        /** Counter-Offer wenn Spielerangebot zu hoch */
        public static final double HIGH_OFFER_COUNTER_MULTIPLIER = 0.95;

        /** Accept-Chance Faktoren */
        public static final double ACCEPT_CHANCE_BASE = 0.3;
        public static final double ACCEPT_CHANCE_SATISFACTION_WEIGHT = 0.4;
        public static final double ACCEPT_CHANCE_REPUTATION_WEIGHT = 0.3;
    }

    // ═══════════════════════════════════════════════════════════
    // NEGOTIATION STATE (NegotiationState.java)
    // ═══════════════════════════════════════════════════════════

    public static final class Heat {
        /** Heat-Zonen Multiplikatoren */
        public static final float RED_ZONE_MULTIPLIER = 25.0f;
        public static final float YELLOW_ZONE_MULTIPLIER = 10.0f;
        public static final float GREEN_ZONE_MULTIPLIER = -5.0f; // Cooling

        /** Trait-basierte Heat-Multiplikatoren */
        public static final float TRAIT_MULTIPLIER_VERY_PATIENT = 0.6f;
        public static final float TRAIT_MULTIPLIER_PATIENT = 0.8f;
        public static final float TRAIT_MULTIPLIER_NEUTRAL = 1.0f;
        public static final float TRAIT_MULTIPLIER_IMPATIENT = 1.3f;
        public static final float TRAIT_MULTIPLIER_VERY_IMPATIENT = 1.5f;

        /** Heat-Schwellenwerte */
        public static final float ABORT_THRESHOLD = 100.0f;
        public static final float PER_ROUND_BASE = 2.0f;
        public static final float ROUND_FACTOR_FOR_ABORT = 20.0f;

        /** Preis-Zonen Grenzen */
        public static final double GREEN_ZONE_MIN = 0.80;
        public static final double GREEN_ZONE_MAX = 1.20;
        public static final double YELLOW_ZONE_MIN = 0.60;
        public static final double YELLOW_ZONE_MAX = 1.50;
    }

    // ═══════════════════════════════════════════════════════════
    // ADDICTION PROFILE (NPCAddictionProfile.java)
    // ═══════════════════════════════════════════════════════════

    public static final class Addiction {
        /** Interest-Level Schwellenwerte */
        public static final int THRESHOLD_NO_INTEREST = 10;
        public static final int THRESHOLD_LOW_INTEREST = 30;
        public static final int THRESHOLD_MEDIUM_INTEREST = 60;
        public static final int THRESHOLD_HIGH_INTEREST = 80;

        /** Addiction Score Boni */
        public static final int SCORE_BONUS_NO_INTEREST = -20;
        public static final int SCORE_BONUS_LOW_INTEREST = 0;
        public static final int SCORE_BONUS_MEDIUM_INTEREST = 10;
        public static final int SCORE_BONUS_HIGH_INTEREST = 20;
        public static final int SCORE_BONUS_VERY_HIGH_INTEREST = 25;

        /** Preis-Toleranz Multiplikatoren */
        public static final float PRICE_TOLERANCE_NO_INTEREST = 0.7f;
        public static final float PRICE_TOLERANCE_LOW_INTEREST = 0.9f;
        public static final float PRICE_TOLERANCE_MEDIUM_INTEREST = 1.0f;
        public static final float PRICE_TOLERANCE_HIGH_INTEREST = 1.1f;
        public static final float PRICE_TOLERANCE_VERY_HIGH_INTEREST = 1.3f;

        /** Random Roll Schwellenwerte für Kaufentscheidung */
        public static final float ROLL_THRESHOLD_NO_BUY = 0.40f;
        public static final float ROLL_THRESHOLD_MAYBE_BUY = 0.70f;
        public static final float ROLL_THRESHOLD_LIKELY_BUY = 0.90f;
    }

    // ═══════════════════════════════════════════════════════════
    // PURCHASE DECISION (NPCPurchaseDecision.java)
    // ═══════════════════════════════════════════════════════════

    public static final class PurchaseDecision {
        /** Mood-Faktoren Divisor */
        public static final float MOOD_FACTORS_COUNT = 3.0f;

        /** Demand Score Werte */
        public static final float DEMAND_SCORE_LOW = 0.3f;
        public static final float DEMAND_SCORE_MEDIUM = 0.6f;
        public static final float DEMAND_SCORE_HIGH = 1.0f;

        /** Reputation/Satisfaction Score Multiplikatoren */
        public static final float REPUTATION_SCORE_MULTIPLIER = 20.0f;
        public static final float SATISFACTION_SCORE_MULTIPLIER = 15.0f;

        /** Deal-History Parameter */
        public static final int DEAL_HISTORY_DAYS = 7;
        public static final float DEAL_HISTORY_BASE_SCORE = 2.0f;
        public static final float DEAL_HISTORY_MAX_SCORE = 10.0f;

        /** Tageszeit-Schwellenwerte (in Ticks) */
        public static final int TIME_MORNING_END = 3000;      // 09:00
        public static final int TIME_AFTERNOON_END = 9000;    // 15:00
        public static final int TIME_EVENING_END = 15000;     // 21:00
        public static final int TIME_NIGHT_END = 18000;       // 00:00

        /** Tageszeit-Scores */
        public static final float TIME_SCORE_MORNING = 2.0f;
        public static final float TIME_SCORE_AFTERNOON = 1.0f;
        public static final float TIME_SCORE_EVENING = 0.0f;
        public static final float TIME_SCORE_NIGHT = -1.0f;
        public static final float TIME_SCORE_LATE_NIGHT = -3.0f;

        /** Random Bonus Range */
        public static final float RANDOM_BONUS_CENTER = 20.0f;
        public static final float RANDOM_BONUS_RANGE = 10.0f;

        /** Max Gramm nach Reputation */
        public static final int MAX_GRAMS_REP_0 = 1;
        public static final int MAX_GRAMS_REP_20 = 2;
        public static final int MAX_GRAMS_REP_40 = 4;
        public static final int MAX_GRAMS_REP_60 = 7;
        public static final int MAX_GRAMS_REP_80 = 10;

        /** Score Normalisierung */
        public static final float SCORE_PERCENT_NORMALIZER = 100.0f;
    }

    // ═══════════════════════════════════════════════════════════
    // SCORE CALCULATOR (NegotiationScoreCalculator.java)
    // ═══════════════════════════════════════════════════════════

    public static final class Score {
        /** Gewichtungen für Gesamtscore */
        public static final float WEIGHT_GLOBAL_REP = 0.20f;
        public static final float WEIGHT_NPC_RELATION = 0.25f;
        public static final float WEIGHT_PERSONALITY = 0.15f;
        public static final float WEIGHT_ADDICTION = 0.25f;
        public static final float WEIGHT_WALLET = 0.15f;

        /** NPC Relationship Normalisierung */
        public static final float RELATIONSHIP_NORMALIZER = 200.0f;

        /** Addiction Score Berechnung */
        public static final int ADDICTION_THRESHOLD = 20;
        public static final float ADDICTION_NORMALIZER = 45.0f;

        /** Neutral Reputation für neue Spieler */
        public static final float DEFAULT_NEUTRAL_REPUTATION = 50.0f;

        /** Budget Usage Schwellenwerte */
        public static final float BUDGET_USAGE_LOW = 0.3f;
        public static final float BUDGET_USAGE_MEDIUM = 0.5f;
        public static final float BUDGET_USAGE_HIGH = 0.7f;
        public static final float BUDGET_USAGE_VERY_HIGH = 1.0f;

        /** Budget Usage Punktemultiplikatoren */
        public static final float BUDGET_POINTS_LOW = 1.0f;
        public static final float BUDGET_POINTS_MEDIUM = 0.8f;
        public static final float BUDGET_POINTS_HIGH = 0.5f;
        public static final float BUDGET_POINTS_VERY_HIGH = 0.3f;

        /** Trait-basierte Abort-Risk Multiplikatoren */
        public static final float ABORT_RISK_VERY_PATIENT = 0.6f;
        public static final float ABORT_RISK_PATIENT = 0.8f;
        public static final float ABORT_RISK_NEUTRAL = 1.0f;
        public static final float ABORT_RISK_IMPATIENT = 1.2f;
        public static final float ABORT_RISK_VERY_IMPATIENT = 1.4f;
    }

    // ═══════════════════════════════════════════════════════════
    // DEMAND LEVEL (DemandLevel.java)
    // ═══════════════════════════════════════════════════════════

    public static final class Demand {
        public static final double LOW_MULTIPLIER = 0.7;
        public static final double MEDIUM_MULTIPLIER = 1.0;
        public static final double HIGH_MULTIPLIER = 1.3;
    }

    // ═══════════════════════════════════════════════════════════
    // PERSONALITY TRAITS (NPCPersonalityTrait.java)
    // ═══════════════════════════════════════════════════════════

    public static final class Personality {
        /** GENEROUS Trait */
        public static final double GENEROUS_DISCOUNT = 0.80;      // 20% Rabatt
        public static final double GENEROUS_FREE_CHANCE = 0.05;   // 5% Gratis
        public static final double GENEROUS_POLICE_CHANCE = 0.30; // 30% meldet Polizei

        /** SUSPICIOUS Trait */
        public static final double SUSPICIOUS_FREE_CHANCE = 0.0;   // Keine Gratis
        public static final double SUSPICIOUS_POLICE_CHANCE = 0.90; // 90% meldet Polizei

        /** FRIENDLY Trait */
        public static final double FRIENDLY_DISCOUNT = 0.90;       // 10% Rabatt
        public static final double FRIENDLY_FREE_CHANCE = 0.15;    // 15% Gratis
        public static final double FRIENDLY_POLICE_CHANCE = 0.50;  // 50% meldet Polizei

        /** HOSTILE Trait */
        public static final double HOSTILE_FREE_CHANCE = 0.0;      // Keine Gratis
        public static final double HOSTILE_POLICE_CHANCE = 0.95;   // 95% meldet Polizei

        /** GREEDY Trait */
        public static final double GREEDY_FREE_CHANCE = 0.02;      // 2% Gratis
        public static final double GREEDY_POLICE_CHANCE = 0.70;    // 70% meldet Polizei

        /** Random Trait Generation Schwellenwerte */
        public static final double TRAIT_ROLL_NEUTRAL = 0.40;      // 40% NEUTRAL
        public static final double TRAIT_ROLL_FRIENDLY = 0.60;     // 20% FRIENDLY
        public static final double TRAIT_ROLL_SUSPICIOUS = 0.75;   // 15% SUSPICIOUS
        public static final double TRAIT_ROLL_GREEDY = 0.90;       // 15% GREEDY
        // Rest = GENEROUS oder HOSTILE
    }

    // ═══════════════════════════════════════════════════════════
    // RELATIONSHIP (NPCRelationship.java)
    // ═══════════════════════════════════════════════════════════

    public static final class Relationship {
        /** Hilfe-Bonus */
        public static final int HELP_BONUS = 10;

        /** Große Kauf-Schwellenwerte */
        public static final int BIG_PURCHASE_THRESHOLD_1 = 1000;
        public static final int BIG_PURCHASE_THRESHOLD_2 = 5000;
        public static final int BIG_PURCHASE_BONUS_1 = 2;
        public static final int BIG_PURCHASE_BONUS_2 = 3;

        /** Große Verkauf-Schwellenwerte */
        public static final int BIG_SALE_THRESHOLD = 1000;

        /** Relationship Level Modifikatoren */
        public static final double RELATIONSHIP_NORMALIZER = 100.0;
        public static final double NEGATIVE_RELATIONSHIP_FACTOR = 0.5;
        public static final double POSITIVE_RELATIONSHIP_FACTOR = 0.2;
    }

    // ═══════════════════════════════════════════════════════════
    // CRIME & BOUNTY (BountyManager.java, PoliceAIHandler.java)
    // ═══════════════════════════════════════════════════════════

    public static final class Crime {
        /** Auto-Kopfgeld pro Wanted-Star */
        public static final double AUTO_BOUNTY_PER_STAR = 2000.0;

        /** Mindest-Wanted-Level für Kopfgeld */
        public static final int MIN_WANTED_LEVEL_FOR_BOUNTY = 3;

        /** Strafen pro Wanted-Level */
        public static final int FINE_PER_WANTED_LEVEL = 500;
        public static final int JAIL_SECONDS_PER_WANTED_LEVEL = 60;

        /** Arrest Timer */
        public static final long ARREST_TIMER_TIMEOUT_MS = 600000; // 10 Minuten
        public static final int MAX_CACHE_ENTRIES = 500;
    }

    // ═══════════════════════════════════════════════════════════
    // ECONOMY (FeeManager.java, RespawnHandler.java)
    // ═══════════════════════════════════════════════════════════

    public static final class Economy {
        /** ATM Gebühr */
        public static final double ATM_FEE = 5.0;

        /** Mindest-Überweisungsgebühr */
        public static final double MIN_TRANSFER_FEE = 10.0;

        /** Krankenhaus-Gebühr */
        public static final double HOSPITAL_FEE = 500.0;
    }

    // ═══════════════════════════════════════════════════════════
    // NPC DAILY SCHEDULE (NPCData.java)
    // ═══════════════════════════════════════════════════════════

    public static final class Schedule {
        /** Arbeitsende (19:00 Uhr in Ticks) */
        public static final int WORK_END_TIME = 13000;

        /** Heimgehzeit (05:00 Uhr morgens in Ticks) */
        public static final int HOME_TIME = 23000;
    }

    // ═══════════════════════════════════════════════════════════
    // NPC MOVEMENT (MoveToLeisureGoal.java)
    // ═══════════════════════════════════════════════════════════

    public static final class Movement {
        /** Freizeit-Radius in Blöcken */
        public static final int LEISURE_RADIUS = 15;

        /** Neuberechnungs-Intervall in Ticks (5 Sekunden) */
        public static final int RECALCULATE_INTERVAL = 100;

        /** Wander-Intervall in Ticks (10 Sekunden) */
        public static final int WANDER_INTERVAL = 200;

        /** Ortswechsel-Intervall in Ticks (5 Minuten) */
        public static final int LOCATION_CHANGE_INTERVAL = 6000;
    }

    // ═══════════════════════════════════════════════════════════
    // PRODUCTION (ProductionSize.java)
    // ═══════════════════════════════════════════════════════════

    public static final class Production {
        /** Small Production Size */
        public static final int SMALL_SLOTS = 6;
        public static final int SMALL_COST = 500;
        public static final double SMALL_MULTIPLIER = 1.0;

        /** Big Production Size */
        public static final int BIG_SLOTS = 24;
        public static final int BIG_COST = 2000;
        public static final double BIG_MULTIPLIER = 2.0;
    }
}
