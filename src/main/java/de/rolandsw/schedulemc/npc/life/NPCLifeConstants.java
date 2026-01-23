package de.rolandsw.schedulemc.npc.life;

/**
 * NPCLifeConstants - Zentrale Konstanten für das NPC Life System
 *
 * Alle hardcodierten Werte sind hier gesammelt für einfache Konfiguration.
 */
public final class NPCLifeConstants {

    private NPCLifeConstants() {} // Utility-Klasse

    // ═══════════════════════════════════════════════════════════════════════════
    // PLAYER TAGS - Strings die im NPC-Gedächtnis gespeichert werden
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class PlayerTags {
        public static final String GOOD_CUSTOMER = "GutKunde";
        public static final String REGULAR_CUSTOMER = "Stammkunde";
        public static final String SUSPICIOUS = "Verdächtig";
        public static final String CRIMINAL = "Kriminell";
        public static final String DANGEROUS = "Gefährlich";
        public static final String VIOLENT = "Gewalttätig";
        public static final String HELPFUL = "Hilfsbereit";
        public static final String BENEFACTOR = "Wohltäter";
        public static final String FRIEND = "Freund";
        public static final String WANTED = "Gesucht";
        public static final String POLICE_CALLED = "PolizeiGerufen";
        public static final String QUEST_COMPLETER = "QuestErfüller";
        public static final String UNRELIABLE = "Unzuverlässig";
        public static final String BRIBABLE = "Bestechlich";
        public static final String COMPANION_OWNER = "Begleiter-Besitzer";

        private PlayerTags() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TIMING - Ticks und Zeitintervalle
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class Timing {
        // Grundlegende Zeiteinheiten
        public static final int TICKS_PER_SECOND = 20;
        public static final int TICKS_PER_MINUTE = 1200;
        public static final int TICKS_PER_HOUR = 72000;
        public static final int TICKS_PER_GAME_DAY = 24000;

        // Update-Intervalle
        public static final int NEEDS_UPDATE_INTERVAL = 20;           // 1 Sekunde
        public static final int BEHAVIOR_DECISION_INTERVAL = 10;      // 0.5 Sekunden
        public static final int REPORT_CHECK_INTERVAL = 200;          // 10 Sekunden
        public static final int MARKET_UPDATE_INTERVAL = 24000;       // 1 Spieltag
        public static final int EVENT_CHECK_INTERVAL = 2400;          // 2 Minuten

        // Cooldowns
        public static final int INTERACTION_COOLDOWN = 600;           // 30 Sekunden
        public static final int NPC_INTERACTION_COOLDOWN = 60;        // 3 Sekunden
        public static final int QUEST_REPEAT_COOLDOWN_DAYS = 3;
        public static final int EVENT_COOLDOWN_DAYS = 5;
        public static final int COMPANION_RESPAWN_COOLDOWN = 6000;    // 5 Minuten

        // Durations
        public static final int EMERGENCY_MODE_DURATION = 6000;       // 5 Minuten
        public static final int MAX_FLEE_DURATION_SECONDS = 60;
        public static final int MAX_ALERT_POLICE_DURATION = 10;
        public static final int ALERT_TRIGGER_DELAY = 40;             // 2 Sekunden
        public static final int TOTAL_ALERT_DURATION = 200;           // 10 Sekunden
        public static final int MAX_INVESTIGATE_DURATION_SECONDS = 30;
        public static final int INVESTIGATE_OBSERVATION_DURATION = 100; // 5 Sekunden
        public static final int MAX_HIDE_DURATION_SECONDS = 120;      // 2 Minuten

        // Companion Timing
        public static final int COMPANION_NEGLECT_THRESHOLD = 6000;   // 5 Minuten
        public static final int COMPANION_LOYALTY_CHECK_INTERVAL = 1200; // 1 Minute

        private Timing() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EMOTIONS - Emotionssystem-Konstanten
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class Emotions {
        // Decay
        public static final float DECAY_PER_TICK = 0.02f;
        public static final int FINAL_DECAY_START = 1200;             // 1 Minute vor Ende

        // Default Durations (Ticks)
        public static final int DURATION_HAPPY = 6000;                // 5 Minuten
        public static final int DURATION_SAD = 12000;                 // 10 Minuten
        public static final int DURATION_ANGRY = 9000;                // 7.5 Minuten
        public static final int DURATION_FEARFUL = 4800;              // 4 Minuten
        public static final int DURATION_SUSPICIOUS = 12000;          // 10 Minuten

        // Thresholds
        public static final float STRONG_THRESHOLD = 70.0f;
        public static final float ACTIVE_THRESHOLD = 20.0f;
        public static final float FLEE_INTENSITY_THRESHOLD = 50.0f;
        public static final float FIGHT_INTENSITY_THRESHOLD = 70.0f;
        public static final float TRADE_BLOCK_THRESHOLD = 50.0f;
        public static final float POLICE_CALL_THRESHOLD = 60.0f;

        private Emotions() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // NEEDS - Bedürfnissystem-Konstanten
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class Needs {
        // Thresholds
        public static final float CRITICAL_THRESHOLD = 20.0f;
        public static final float LOW_THRESHOLD = 40.0f;
        public static final float NORMAL_THRESHOLD = 60.0f;
        public static final float BASE_SAFETY_LEVEL = 50.0f;

        // Regen
        public static final float ENERGY_REGEN_PER_HOUR = 20.0f;

        // Safety Modifiers
        public static final float SAFETY_HOME_BONUS = 40.0f;
        public static final float SAFETY_POLICE_NEARBY_BONUS = 20.0f;
        public static final float SAFETY_FRIEND_NEARBY_BONUS = 10.0f;
        public static final float SAFETY_NIGHT_OUTDOOR_PENALTY = -20.0f;
        public static final float SAFETY_CRIME_NEARBY_PENALTY = -30.0f;
        public static final float SAFETY_CRIMINAL_NEARBY_PENALTY = -50.0f;
        public static final float SAFETY_WEAPON_VISIBLE_PENALTY = -40.0f;

        // Distance checks (squared)
        public static final int HOME_SAFETY_CHECK_RADIUS_SQR = 25;    // 5 Blöcke
        public static final int NIGHT_OUTDOOR_CHECK_RADIUS_SQR = 25;  // 5 Blöcke

        private Needs() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MEMORY - Gedächtnissystem-Konstanten
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class Memory {
        // Limits
        public static final int MAX_MEMORIES_PER_PLAYER = 10;
        public static final int MAX_DAILY_SUMMARIES = 30;
        public static final int MAX_PLAYER_PROFILES = 50;
        public static final int MAX_DAILY_SUMMARY_DAYS = 30;

        // Thresholds
        public static final int IMPORTANCE_HIGHLIGHT_THRESHOLD = 7;
        public static final int POSITIVE_NEGATIVE_RATIO = 2;

        // Trade Thresholds for Tags
        public static final int TRADE_VOLUME_GOOD_CUSTOMER = 10000;
        public static final int TRANSACTIONS_REGULAR_CUSTOMER = 50;

        // Crime thresholds for Tags
        public static final int CRIMES_FOR_SUSPICIOUS = 1;
        public static final int CRIMES_FOR_CRIMINAL = 3;
        public static final int CRIMES_FOR_DANGEROUS = 5;

        // Help thresholds for Tags
        public static final int HELPS_FOR_HELPFUL = 3;
        public static final int HELPS_FOR_BENEFACTOR = 10;

        private Memory() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TRAITS - Charaktereigenschaften-Konstanten
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class Traits {
        // Value Range
        public static final int MIN_VALUE = -100;
        public static final int MAX_VALUE = 100;
        public static final int RANDOMIZE_STDDEV = 40;

        // Thresholds
        public static final int HIGH_THRESHOLD = 50;
        public static final int VERY_BRAVE_THRESHOLD = 70;
        public static final int BRAVE_THRESHOLD = 30;
        public static final int FEARFUL_THRESHOLD = -30;
        public static final int VERY_FEARFUL_THRESHOLD = -70;
        public static final int INVESTIGATE_COURAGE_THRESHOLD = 20;
        public static final int INVESTIGATE_HONESTY_THRESHOLD = -30;

        // Modifiers
        public static final float TRADE_MODIFIER_POSITIVE = 0.3f;
        public static final float TRADE_MODIFIER_NEGATIVE = 0.2f;
        public static final float FEAR_THRESHOLD_MULTIPLIER = 30.0f;
        public static final float REPORT_CHANCE_MULTIPLIER = 0.45f;
        public static final float BRIBERY_DISHONESTY_FACTOR = 0.3f;
        public static final float BRIBERY_GREED_FACTOR = 0.3f;
        public static final float CRIME_SEVERITY_REPORT_BONUS = 0.03f;
        public static final float COURAGEOUS_REPORT_BONUS = 0.1f;

        private Traits() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WITNESS - Zeugensystem-Konstanten
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class Witness {
        // Range
        public static final double DETECTION_RANGE = 20.0;
        public static final double DETECTION_RANGE_SQR = 400.0;

        // Limits
        public static final int MAX_REPORTS_PER_PLAYER = 50;

        // Thresholds
        public static final int SEVERITY_FOR_WANTED_LIST = 7;
        public static final int REPEAT_OFFENDER_THRESHOLD = 3;

        // Multipliers
        public static final int MEMORY_IMPORTANCE_BONUS = 2;
        public static final float FEARFUL_INTENSITY_MULTIPLIER = 10.0f;
        public static final float SUSPICIOUS_INTENSITY_MULTIPLIER = 0.7f;

        private Witness() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RUMORS - Gerüchtesystem-Konstanten
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class Rumors {
        // Limits
        public static final int MAX_RUMORS_PER_PLAYER = 20;
        public static final int MAX_RUMOR_DURATION_DAYS = 365;

        // Spread mechanics
        public static final float SPREAD_DECAY_FACTOR = 0.9f;
        public static final float CREDIBILITY_LOSS_PER_SPREAD = 5.0f;
        public static final float REINFORCEMENT_BONUS = 20.0f;
        public static final float MIN_CREDIBILITY_THRESHOLD = 20.0f;

        private Rumors() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FACTIONS - Fraktionssystem-Konstanten
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class Factions {
        // Thresholds
        public static final int ALLY_THRESHOLD = 50;
        public static final int ENEMY_THRESHOLD = -50;

        // Transaction reputation
        public static final int MAX_TRANSACTION_REP_GAIN = 5;
        public static final int UNFAIR_TRANSACTION_REP_LOSS = 3;

        // Crime reputation multipliers
        public static final int CRIME_ORDER_REP_LOSS_MULT = 5;
        public static final int CRIME_CITIZEN_REP_LOSS_MULT = 3;
        public static final int CRIME_MERCHANT_REP_LOSS_MULT = 2;
        public static final int CRIME_UNDERGROUND_REP_GAIN_MULT = 1;

        // Reputation sharing
        public static final int ALLIED_FACTION_REP_SHARE_DIVISOR = 2;
        public static final int ENEMY_FACTION_NEGATIVE_REP_DIVISOR = 3;
        public static final int ENEMY_FACTION_CRIME_REP_DIVISOR = 4;

        private Factions() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPANION - Begleitersystem-Konstanten
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class Companion {
        // Limits
        public static final int MAX_ACTIVE_COMPANIONS = 2;
        public static final int MAX_TOTAL_COMPANIONS = 5;
        public static final int MAX_LEVEL = 10;

        // Initial values
        public static final int INITIAL_EXPERIENCE = 0;
        public static final int INITIAL_LEVEL = 1;
        public static final int INITIAL_LOYALTY = 50;
        public static final float INITIAL_SATISFACTION = 100.0f;

        // Level up bonuses
        public static final int LEVEL_UP_LOYALTY_BONUS = 5;
        public static final int LEVEL_UP_HEALTH_BONUS = 2;
        public static final int EXPERIENCE_LEVEL_BASE = 100;

        // Decay and changes
        public static final float SATISFACTION_DECAY_PER_SECOND = 0.1f;
        public static final int SATISFACTION_INCREASE = 5;
        public static final int TRANSFER_LOYALTY_PENALTY = 20;
        public static final float INCAPACITY_HEALTH_RECOVERY_RATIO = 0.5f;

        private Companion() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // QUESTS - Quest-System-Konstanten
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class Quests {
        // Difficulty scaling
        public static final float DIFFICULTY_SCALING_FACTOR = 0.25f;

        // Collection items
        public static final int COLLECTION_BASE_ITEMS = 5;
        public static final int COLLECTION_ITEMS_PER_DIFFICULTY = 5;

        // Elimination targets
        public static final int ELIMINATION_BASE_KILLS = 3;
        public static final int ELIMINATION_KILLS_PER_DIFFICULTY = 2;

        // Location offsets
        public static final int LOCATION_RANDOM_OFFSET_LARGE = 50;
        public static final int LOCATION_RANDOM_OFFSET_MEDIUM = 20;
        public static final int LOCATION_RANDOM_OFFSET_SMALL = 5;

        // Rewards - Basic Delivery
        public static final int DELIVERY_BASIC_MONEY = 50;
        public static final int DELIVERY_BASIC_REP = 2;

        // Rewards - Urgent Delivery
        public static final int DELIVERY_URGENT_MONEY = 150;
        public static final int DELIVERY_URGENT_REP = 5;

        // Rewards - Collection
        public static final int COLLECTION_MONEY = 75;
        public static final int COLLECTION_EXPERIENCE = 50;

        // Rewards - Escort
        public static final int ESCORT_MONEY = 200;
        public static final int ESCORT_REP = 10;

        // Rewards - Elimination
        public static final int ELIMINATION_MONEY = 300;
        public static final int ELIMINATION_REP = 15;

        // Rewards - Investigation
        public static final int INVESTIGATION_MONEY = 150;
        public static final int INVESTIGATION_REP = 8;

        // Rewards - Negotiation
        public static final int NEGOTIATION_MONEY = 250;
        public static final int NEGOTIATION_REP = 12;

        // Rewards - Underground Delivery
        public static final int UNDERGROUND_DELIVERY_MONEY = 200;
        public static final int UNDERGROUND_DELIVERY_REP = 8;

        private Quests() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WORLD EVENTS - Welt-Event-Konstanten
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class WorldEvents {
        public static final int MAX_ACTIVE_EVENTS = 3;

        private WorldEvents() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRICES - Dynamisches Preissystem-Konstanten
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class Prices {
        public static final float CRISIS_MULTIPLIER = 1.5f;
        public static final float SURPLUS_MULTIPLIER = 0.6f;
        public static final float SHORTAGE_MULTIPLIER = 1.8f;

        private Prices() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DISTANCES - Entfernungen in Blöcken
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class Distances {
        public static final double INTERACTION_RANGE = 8.0;
        public static final double FLEE_TARGET_DISTANCE = 30.0;
        public static final double THREAT_CLOSE_DISTANCE = 10.0;
        public static final double INVESTIGATE_RANDOM_SEARCH_RADIUS = 10.0;
        public static final double HIDE_SPOT_SEARCH_RANGE_MIN = 10.0;
        public static final double HIDE_SPOT_SEARCH_RANGE_MAX = 20.0;

        // Squared distances for fast checks
        public static final double INVESTIGATE_REACH_RADIUS_SQR = 9.0;    // 3 Blöcke
        public static final double HIDE_SPOT_REACH_RADIUS_SQR = 4.0;      // 2 Blöcke

        private Distances() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BEHAVIOR - Verhaltens-Konstanten
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class Behavior {
        // Probabilities
        public static final float INVESTIGATE_BASE_PROBABILITY = 0.3f;

        // Safety thresholds
        public static final int FLEE_SAFETY_THRESHOLD = 20;
        public static final int SAFE_SAFETY_THRESHOLD = 70;
        public static final int HIDE_SAFETY_THRESHOLD = 40;
        public static final int HIDING_SAFETY_THRESHOLD = 60;
        public static final int HIDE_COURAGE_THRESHOLD = 0;

        // Crime multipliers
        public static final int CRIME_MEMORY_IMPORTANCE_BONUS = 2;
        public static final float SEVERE_CRIME_FEARFUL_MULT = 10.0f;
        public static final float CRIME_SUSPICIOUS_INTENSITY_MULT = 8.0f;

        // Threat multipliers
        public static final int THREAT_SAFETY_REDUCTION_MULT = 20;
        public static final int THREAT_MEMORY_IMPORTANCE_BONUS = 5;
        public static final float THREAT_FEARFUL_INTENSITY_MULT = 15.0f;

        private Behavior() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INTEGRATION - Cross-System Event-Konstanten
    // ═══════════════════════════════════════════════════════════════════════════

    public static final class Integration {
        // Trade thresholds
        public static final int TRADE_REP_THRESHOLD = 500;
        public static final int TRADE_RUMOR_THRESHOLD = 2000;

        // Reputation changes
        public static final int COMPANION_RECRUIT_REP = 5;
        public static final int COMPANION_LOW_LOYALTY_REP_LOSS = 3;
        public static final int QUEST_DIFFICULT_EXTRA_REP = 3;
        public static final int QUEST_FAILED_REP_LOSS = 5;
        public static final int BRIBE_ACCEPTED_REP_LOSS = 2;
        public static final int BRIBE_REJECTED_REP_LOSS_ORDER = 10;
        public static final int BRIBE_REJECTED_REP_LOSS_CITIZEN = 5;
        public static final int NPC_DEATH_REP_LOSS_ORDER = 20;
        public static final int NPC_DEATH_REP_LOSS_CITIZEN = 15;
        public static final int NPC_DEATH_REP_LOSS_MERCHANT = 25;

        // Rumor parameters
        public static final int RUMOR_COMPANION_RECRUIT_IMPORTANCE = 3;
        public static final int RUMOR_COMPANION_RECRUIT_DURATION = 5;
        public static final int RUMOR_QUEST_COMPLETE_IMPORTANCE = 4;
        public static final int RUMOR_QUEST_COMPLETE_DURATION = 7;
        public static final int RUMOR_TRADE_IMPORTANCE = 2;
        public static final int RUMOR_TRADE_DURATION = 3;
        public static final int RUMOR_BRIBE_REJECTED_IMPORTANCE = 5;
        public static final int RUMOR_BRIBE_REJECTED_DURATION = 7;
        public static final int RUMOR_NPC_DEATH_IMPORTANCE = 5;
        public static final int RUMOR_NPC_DEATH_DURATION = 10;

        private Integration() {}
    }
}
