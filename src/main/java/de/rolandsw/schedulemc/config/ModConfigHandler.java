package de.rolandsw.schedulemc.config;

import de.rolandsw.schedulemc.config.TobaccoConfig;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * ScheduleMC 3.0 - Vollständige Konfiguration inkl. Tabak-System
 */
public class ModConfigHandler {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Common COMMON;
    public static final TobaccoConfig TOBACCO;
    public static final ServerConfig VEHICLE_SERVER;
    public static final ClientConfig VEHICLE_CLIENT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        TOBACCO = new TobaccoConfig(builder);
        VEHICLE_SERVER = new ServerConfig(builder);
        SPEC = builder.build();

        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        VEHICLE_CLIENT = new ClientConfig(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();
    }

    public static class Common {
        
        // ═══════════════════════════════════════════════════════════
        // ECONOMY
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.DoubleValue START_BALANCE;
        public final ForgeConfigSpec.IntValue SAVE_INTERVAL_MINUTES;

        // Savings Accounts (Sparkonten)
        public final ForgeConfigSpec.DoubleValue SAVINGS_MAX_PER_PLAYER;
        public final ForgeConfigSpec.DoubleValue SAVINGS_MIN_DEPOSIT;
        public final ForgeConfigSpec.DoubleValue SAVINGS_INTEREST_RATE;
        public final ForgeConfigSpec.IntValue SAVINGS_LOCK_PERIOD_WEEKS;
        public final ForgeConfigSpec.DoubleValue SAVINGS_EARLY_WITHDRAWAL_PENALTY;

        // Overdraft (Dispo)
        public final ForgeConfigSpec.DoubleValue OVERDRAFT_INTEREST_RATE;

        // Recurring Payments (Daueraufträge)
        public final ForgeConfigSpec.IntValue RECURRING_MAX_PER_PLAYER;

        // Tax System
        public final ForgeConfigSpec.DoubleValue TAX_PROPERTY_PER_CHUNK;
        public final ForgeConfigSpec.DoubleValue TAX_SALES_RATE;
        
        // ═══════════════════════════════════════════════════════════
        // PLOTS
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.LongValue MIN_PLOT_SIZE;
        public final ForgeConfigSpec.LongValue MAX_PLOT_SIZE;
        public final ForgeConfigSpec.DoubleValue MIN_PLOT_PRICE;
        public final ForgeConfigSpec.DoubleValue MAX_PLOT_PRICE;
        public final ForgeConfigSpec.IntValue MAX_TRUSTED_PLAYERS;
        public final ForgeConfigSpec.BooleanValue ALLOW_PLOT_TRANSFER;
        public final ForgeConfigSpec.DoubleValue REFUND_ON_ABANDON;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> RESIDENTIAL_PLOT_BLOCKS;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> COMMERCIAL_PLOT_BLOCKS;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> INDUSTRIAL_PLOT_BLOCKS;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> SHOP_PLOT_BLOCKS;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> PUBLIC_PLOT_BLOCKS;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> GOVERNMENT_PLOT_BLOCKS;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> PRISON_PLOT_BLOCKS;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> TOWING_YARD_PLOT_BLOCKS;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> SECRET_DOOR_ALLOWED_PLOT_TYPES;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> UTILITY_CONSUMER_BLOCKS;

        // ═══════════════════════════════════════════════════════════
        // PRODUCT PRICES (EconomyController Referenzpreise)
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> PRODUCT_PRICES;

        // ═══════════════════════════════════════════════════════════
        // BLOCK PRICES (Produktionsblock-Katalog: Preis + Level)
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> BLOCK_PRICES;

        // ═══════════════════════════════════════════════════════════
        // DAILY REWARDS
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.DoubleValue DAILY_REWARD;
        public final ForgeConfigSpec.DoubleValue DAILY_REWARD_STREAK_BONUS;
        public final ForgeConfigSpec.IntValue MAX_STREAK_DAYS;
        
        // ═══════════════════════════════════════════════════════════
        // RENT SYSTEM
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.BooleanValue RENT_ENABLED;
        public final ForgeConfigSpec.DoubleValue MIN_RENT_PRICE;
        public final ForgeConfigSpec.IntValue MIN_RENT_DAYS;
        public final ForgeConfigSpec.IntValue MAX_RENT_DAYS;
        public final ForgeConfigSpec.BooleanValue AUTO_EVICT_EXPIRED;
        
        // ═══════════════════════════════════════════════════════════
        // SHOP SYSTEM
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.BooleanValue SHOP_ENABLED;
        public final ForgeConfigSpec.DoubleValue BUY_MULTIPLIER;
        public final ForgeConfigSpec.DoubleValue SELL_MULTIPLIER;
        
        // ═══════════════════════════════════════════════════════════
        // RATINGS
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.BooleanValue RATINGS_ENABLED;
        public final ForgeConfigSpec.BooleanValue ALLOW_MULTIPLE_RATINGS;
        public final ForgeConfigSpec.IntValue MIN_RATING;
        public final ForgeConfigSpec.IntValue MAX_RATING;

        // ═══════════════════════════════════════════════════════════
        // NPC SYSTEM
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> NPC_WALKABLE_BLOCKS;

        // ═══════════════════════════════════════════════════════════
        // MAP NAVIGATION SYSTEM
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> NAVIGATION_ROAD_BLOCKS;
        public final ForgeConfigSpec.IntValue NAVIGATION_SCAN_RADIUS;
        public final ForgeConfigSpec.IntValue NAVIGATION_PATH_UPDATE_INTERVAL;
        public final ForgeConfigSpec.DoubleValue NAVIGATION_ARRIVAL_DISTANCE;

        // ═══════════════════════════════════════════════════════════
        // POLICE SYSTEM
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.IntValue POLICE_ARREST_COOLDOWN_SECONDS;
        public final ForgeConfigSpec.IntValue POLICE_DETECTION_RADIUS;
        public final ForgeConfigSpec.DoubleValue POLICE_ARREST_DISTANCE;
        public final ForgeConfigSpec.IntValue POLICE_SEARCH_DURATION_SECONDS;
        public final ForgeConfigSpec.IntValue POLICE_SEARCH_RADIUS;
        public final ForgeConfigSpec.IntValue POLICE_SEARCH_TARGET_UPDATE_SECONDS;
        public final ForgeConfigSpec.IntValue POLICE_BACKUP_SEARCH_RADIUS;
        public final ForgeConfigSpec.BooleanValue POLICE_INDOOR_HIDING_ENABLED;
        public final ForgeConfigSpec.BooleanValue POLICE_BLOCK_DOORS_DURING_PURSUIT;

        // Police Raid System
        public final ForgeConfigSpec.IntValue POLICE_RAID_SCAN_RADIUS;
        public final ForgeConfigSpec.DoubleValue POLICE_ILLEGAL_CASH_THRESHOLD;
        public final ForgeConfigSpec.DoubleValue POLICE_RAID_ACCOUNT_PERCENTAGE;
        public final ForgeConfigSpec.DoubleValue POLICE_RAID_MIN_FINE;

        // Police Room-Based Scanning (Smart Search)
        public final ForgeConfigSpec.BooleanValue POLICE_ROOM_SCAN_ENABLED;
        public final ForgeConfigSpec.IntValue POLICE_ROOM_SCAN_MAX_SIZE;
        public final ForgeConfigSpec.IntValue POLICE_ROOM_SCAN_MAX_DEPTH;
        public final ForgeConfigSpec.IntValue POLICE_ROOM_SCAN_MAX_ADDITIONAL_ROOMS;

        // Police Patrol System
        public final ForgeConfigSpec.IntValue POLICE_STATION_WAIT_MINUTES;
        public final ForgeConfigSpec.IntValue POLICE_STATION_RADIUS;
        public final ForgeConfigSpec.IntValue POLICE_PATROL_WAIT_MINUTES;
        public final ForgeConfigSpec.IntValue POLICE_PATROL_RADIUS;

        // NEW: Police Overhaul Features
        public final ForgeConfigSpec.BooleanValue POLICE_VEHICLE_PURSUIT_ENABLED;
        public final ForgeConfigSpec.DoubleValue POLICE_VEHICLE_SPEED_MULTIPLIER;
        public final ForgeConfigSpec.BooleanValue POLICE_SIREN_ENABLED;
        public final ForgeConfigSpec.IntValue POLICE_SIREN_SOUND_RADIUS;
        public final ForgeConfigSpec.BooleanValue POLICE_ROADBLOCK_ENABLED;
        public final ForgeConfigSpec.IntValue POLICE_MAX_ROADBLOCKS;
        public final ForgeConfigSpec.IntValue POLICE_ROADBLOCK_DURATION_SECONDS;
        public final ForgeConfigSpec.BooleanValue POLICE_WARNING_ENABLED;
        public final ForgeConfigSpec.IntValue POLICE_WARNING_TIMEOUT_SECONDS;
        public final ForgeConfigSpec.BooleanValue POLICE_TRAFFIC_VIOLATIONS_ENABLED;
        public final ForgeConfigSpec.DoubleValue POLICE_SPEED_LIMIT_DEFAULT;
        public final ForgeConfigSpec.IntValue POLICE_CONTAINER_SCAN_DEPTH;
        public final ForgeConfigSpec.BooleanValue POLICE_EVIDENCE_MULTIPLIER_ENABLED;
        public final ForgeConfigSpec.BooleanValue POLICE_FLANKING_ENABLED;
        public final ForgeConfigSpec.IntValue POLICE_WANTED_POSTERS_MIN_LEVEL;

        // Police Combat (weapons when arrest fails)
        public final ForgeConfigSpec.BooleanValue POLICE_COMBAT_ENABLED;
        public final ForgeConfigSpec.IntValue POLICE_MELEE_WANTED_LEVEL;
        public final ForgeConfigSpec.IntValue POLICE_RANGED_WANTED_LEVEL;
        public final ForgeConfigSpec.DoubleValue POLICE_RANGED_MIN_DISTANCE;
        public final ForgeConfigSpec.IntValue POLICE_ESCALATION_ESCAPE_COUNT;
        public final ForgeConfigSpec.IntValue POLICE_ESCALATION_PURSUIT_SECONDS;
        public final ForgeConfigSpec.IntValue POLICE_SHOT_COOLDOWN_TICKS;
        public final ForgeConfigSpec.DoubleValue POLICE_PISTOL_DAMAGE;
        public final ForgeConfigSpec.IntValue POLICE_MAX_SIMULTANEOUS_SHOOTERS;
        public final ForgeConfigSpec.BooleanValue POLICE_LETHAL_FORCE;
        public final ForgeConfigSpec.DoubleValue POLICE_ILLEGAL_ITEMS_CASH_PENALTY;
        public final ForgeConfigSpec.DoubleValue POLICE_ILLEGAL_ITEMS_ACCOUNT_PENALTY;

        // ═══════════════════════════════════════════════════════════
        // STEALING MINIGAME
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.DoubleValue STEALING_INDICATOR_SPEED;
        public final ForgeConfigSpec.IntValue STEALING_MAX_ATTEMPTS;
        public final ForgeConfigSpec.DoubleValue STEALING_MIN_ZONE_SIZE;
        public final ForgeConfigSpec.DoubleValue STEALING_MAX_ZONE_SIZE;

        // ═══════════════════════════════════════════════════════════
        // WAREHOUSE SYSTEM
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.IntValue WAREHOUSE_SLOT_COUNT;
        public final ForgeConfigSpec.IntValue WAREHOUSE_MAX_CAPACITY_PER_SLOT;
        public final ForgeConfigSpec.IntValue WAREHOUSE_DELIVERY_INTERVAL_DAYS;
        public final ForgeConfigSpec.IntValue WAREHOUSE_DEFAULT_DELIVERY_PRICE;

        // ═══════════════════════════════════════════════════════════
        // BANK SYSTEM
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.DoubleValue BANK_DEPOSIT_LIMIT;
        public final ForgeConfigSpec.DoubleValue BANK_TRANSFER_DAILY_LIMIT;

        // Stock Market (Börse)
        public final ForgeConfigSpec.DoubleValue STOCK_GOLD_BASE_PRICE;
        public final ForgeConfigSpec.DoubleValue STOCK_DIAMOND_BASE_PRICE;
        public final ForgeConfigSpec.DoubleValue STOCK_EMERALD_BASE_PRICE;
        public final ForgeConfigSpec.DoubleValue STOCK_MAX_PRICE_CHANGE_PERCENT;

        // ═══════════════════════════════════════════════════════════
        // DYNAMIC PRICING SYSTEM (UDPS)
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.BooleanValue DYNAMIC_PRICING_ENABLED;
        public final ForgeConfigSpec.DoubleValue DYNAMIC_PRICING_SD_FACTOR;
        public final ForgeConfigSpec.DoubleValue DYNAMIC_PRICING_MIN_MULTIPLIER;
        public final ForgeConfigSpec.DoubleValue DYNAMIC_PRICING_MAX_MULTIPLIER;
        public final ForgeConfigSpec.IntValue DYNAMIC_PRICING_UPDATE_INTERVAL_MINUTES;
        public final ForgeConfigSpec.DoubleValue DYNAMIC_PRICING_SD_DECAY_RATE;
        public final ForgeConfigSpec.DoubleValue DYNAMIC_PRICING_DAILY_FOOD_COST;
        public final ForgeConfigSpec.DoubleValue DYNAMIC_PRICING_DAILY_REFERENCE_INCOME;

        // ═══════════════════════════════════════════════════════════
        // ECONOMIC CYCLE
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.BooleanValue ECONOMY_CYCLE_ENABLED;
        public final ForgeConfigSpec.IntValue ECONOMY_CYCLE_MIN_DURATION_DAYS;
        public final ForgeConfigSpec.IntValue ECONOMY_CYCLE_MAX_DURATION_DAYS;
        public final ForgeConfigSpec.DoubleValue ECONOMY_CYCLE_EVENT_BASE_CHANCE;

        // ═══════════════════════════════════════════════════════════
        // PRODUCER LEVEL SYSTEM
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.BooleanValue LEVEL_SYSTEM_ENABLED;
        public final ForgeConfigSpec.IntValue LEVEL_MAX;
        public final ForgeConfigSpec.IntValue LEVEL_BASE_XP;
        public final ForgeConfigSpec.DoubleValue LEVEL_XP_EXPONENT;
        public final ForgeConfigSpec.DoubleValue LEVEL_ILLEGAL_XP_MULTIPLIER;
        public final ForgeConfigSpec.DoubleValue LEVEL_LEGAL_XP_MULTIPLIER;

        // ═══════════════════════════════════════════════════════════
        // RISK PREMIUM
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.DoubleValue RISK_BASE_CANNABIS;
        public final ForgeConfigSpec.DoubleValue RISK_BASE_COCAINE;
        public final ForgeConfigSpec.DoubleValue RISK_BASE_HEROIN;
        public final ForgeConfigSpec.DoubleValue RISK_BASE_METH;
        public final ForgeConfigSpec.DoubleValue RISK_CONFISCATION_MULTIPLIER;

        // ═══════════════════════════════════════════════════════════
        // ANTI-EXPLOIT
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.DoubleValue ANTI_EXPLOIT_DAILY_SELL_LIMIT;
        public final ForgeConfigSpec.IntValue ANTI_EXPLOIT_MASS_SELL_COOLDOWN_SECONDS;
        public final ForgeConfigSpec.IntValue ANTI_EXPLOIT_MASS_SELL_THRESHOLD;
        public final ForgeConfigSpec.DoubleValue ANTI_EXPLOIT_MASS_SELL_PENALTY;

        // ═══════════════════════════════════════════════════════════
        // UTILITY SYSTEM
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.DoubleValue UTILITY_ELECTRICITY_PRICE_PER_KWH;
        public final ForgeConfigSpec.DoubleValue UTILITY_WATER_PRICE_PER_LITER;

        // ═══════════════════════════════════════════════════════════
        // WORKSHOP SYSTEM
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.DoubleValue WORKSHOP_BASE_INSPECTION_FEE;
        public final ForgeConfigSpec.DoubleValue WORKSHOP_REPAIR_COST_PER_PERCENT;
        public final ForgeConfigSpec.DoubleValue WORKSHOP_BATTERY_COST_PER_PERCENT;
        public final ForgeConfigSpec.DoubleValue WORKSHOP_OIL_CHANGE_COST;

        // Upgrade costs
        public final ForgeConfigSpec.DoubleValue WORKSHOP_MOTOR_UPGRADE_COST_LVL2;
        public final ForgeConfigSpec.DoubleValue WORKSHOP_MOTOR_UPGRADE_COST_LVL3;
        public final ForgeConfigSpec.DoubleValue WORKSHOP_TANK_UPGRADE_COST_LVL2;
        public final ForgeConfigSpec.DoubleValue WORKSHOP_TANK_UPGRADE_COST_LVL3;
        public final ForgeConfigSpec.DoubleValue WORKSHOP_TIRE_UPGRADE_COST;
        public final ForgeConfigSpec.DoubleValue WORKSHOP_PAINT_CHANGE_COST;
        public final ForgeConfigSpec.DoubleValue WORKSHOP_FENDER_UPGRADE_COST_LVL2;
        public final ForgeConfigSpec.DoubleValue WORKSHOP_FENDER_UPGRADE_COST_LVL3;

        public Common(ForgeConfigSpec.Builder builder) {
            
            builder.comment("ScheduleMC 3.0 - Economy Settings")
                    .push("economy");

            START_BALANCE = builder
                    .comment("Starting balance for new players")
                    .defineInRange("start_balance", 1000.0, 0.0, 1000000.0);

            SAVE_INTERVAL_MINUTES = builder
                    .comment("Auto-Save Intervall in Minuten")
                    .defineInRange("save_interval_minutes", 5, 1, 60);

            builder.pop();

            builder.comment("Savings Accounts Settings (Sparkonten)")
                    .push("savings");

            SAVINGS_MAX_PER_PLAYER = builder
                    .comment("Maximale Spareinlagen pro Spieler")
                    .defineInRange("max_per_player", 50000.0, 1000.0, 10000000.0);

            SAVINGS_MIN_DEPOSIT = builder
                    .comment("Minimum deposit for a new savings account")
                    .defineInRange("min_deposit", 1000.0, 100.0, 100000.0);

            SAVINGS_INTEREST_RATE = builder
                    .comment("Zinssatz pro Woche (0.05 = 5%)")
                    .defineInRange("interest_rate", 0.05, 0.0, 0.5);

            SAVINGS_LOCK_PERIOD_WEEKS = builder
                    .comment("Sperrfrist in Wochen")
                    .defineInRange("lock_period_weeks", 4, 1, 52);

            SAVINGS_EARLY_WITHDRAWAL_PENALTY = builder
                    .comment("Penalty for early withdrawal (0.10 = 10%)")
                    .defineInRange("early_withdrawal_penalty", 0.10, 0.0, 0.5);

            builder.pop();

            builder.comment("Overdraft Settings (Dispo - UNBEGRENZT!)",
                            "Players can go into unlimited overdraft.",
                            "Tag 7: Auto-Ausgleich (Bargeld -> Sparkonto)",
                            "Day 28: prison (1000€ = 1 minute)")
                    .push("overdraft");

            OVERDRAFT_INTEREST_RATE = builder
                    .comment("Dispo-Zinssatz pro Woche (0.25 = 25%)")
                    .defineInRange("interest_rate", 0.25, 0.0, 1.0);

            builder.pop();

            builder.comment("Recurring payments settings (standing orders)")
                    .push("recurring");

            RECURRING_MAX_PER_PLAYER = builder
                    .comment("Maximum standing orders per player")
                    .defineInRange("max_per_player", 10, 1, 100);

            builder.pop();

            builder.comment("Tax System Settings")
                    .push("tax");

            TAX_PROPERTY_PER_CHUNK = builder
                    .comment("Grundsteuer pro Chunk pro Monat")
                    .defineInRange("property_per_chunk", 100.0, 0.0, 10000.0);

            TAX_SALES_RATE = builder
                    .comment("Umsatzsteuer / MwSt (0.19 = 19%)")
                    .defineInRange("sales_rate", 0.19, 0.0, 1.0);

            builder.pop();

            builder.comment("Plot System Settings")
                    .push("plots");

            MIN_PLOT_SIZE = builder
                    .comment("Minimum plot size in blocks")
                    .defineInRange("min_plot_size", 64L, 1L, 1000000L);

            MAX_PLOT_SIZE = builder
                    .comment("Maximum plot size in blocks")
                    .defineInRange("max_plot_size", 1000000L, 1L, 100000000L);

            MIN_PLOT_PRICE = builder
                    .comment("Minimaler Plot-Preis")
                    .defineInRange("min_plot_price", 1.0, 0.01, 1000000.0);

            MAX_PLOT_PRICE = builder
                    .comment("Maximaler Plot-Preis")
                    .defineInRange("max_plot_price", 1000000.0, 1.0, 100000000.0);

            MAX_TRUSTED_PLAYERS = builder
                    .comment("Maximale Anzahl vertrauter Spieler pro Plot")
                    .defineInRange("max_trusted_players", 10, 1, 100);

            ALLOW_PLOT_TRANSFER = builder
                    .comment("Plots can be transferred")
                    .define("allow_plot_transfer", true);

            REFUND_ON_ABANDON = builder
                    .comment("Refund when abandoning (0.0-1.0, 0.5 = 50%)")
                    .defineInRange("refund_on_abandon", 0.5, 0.0, 1.0);

            builder.comment("Plot type block lists (ALL = no restriction for this type)")
                    .push("block_restrictions");

            RESIDENTIAL_PLOT_BLOCKS = builder
                    .comment("Blocks for RESIDENTIAL plots. Default: ALL")
                    .defineList("residential", java.util.List.of("ALL"), obj -> obj instanceof String);

            COMMERCIAL_PLOT_BLOCKS = builder
                    .comment("Blocks for COMMERCIAL plots. Default: ALL")
                    .defineList("commercial", java.util.List.of("ALL"), obj -> obj instanceof String);

            INDUSTRIAL_PLOT_BLOCKS = builder
                    .comment("Blocks that are ONLY allowed in INDUSTRIAL plots (factory floor required).")
                    .defineList("industrial", java.util.Arrays.asList(
                        "terracotta_pot", "ceramic_pot", "iron_pot", "golden_pot",
                        "small_drying_rack", "medium_drying_rack", "big_drying_rack",
                        "small_fermentation_barrel", "medium_fermentation_barrel", "big_fermentation_barrel",
                        "small_packaging_table", "medium_packaging_table", "large_packaging_table",
                        "sink", "cannabis_trim_station", "cannabis_curing_jar", "cannabis_hash_press",
                        "cannabis_oil_extractor", "crack_cooker", "scoring_machine", "opium_press", "cooking_station",
                        "heroin_refinery", "chemical_mixer", "reduction_kettle", "crystallizer",
                        "vacuum_dryer", "fermentation_tank", "distillation_apparatus", "micro_doser",
                        "perforation_press", "reaction_kettle", "drying_oven", "pill_press",
                        "water_tank", "wet_processing_station", "coffee_grinder", "coffee_packaging_table",
                        "crushing_station", "wine_bottling_station", "malting_station", "mash_tun",
                        "beer_bottling_station", "pasteurization_station", "curdling_vat", "packaging_station",
                        "honey_extractor", "centrifugal_extractor", "filtering_station", "processing_station",
                        "creaming_station", "bottling_station", "roasting_station", "winnowing_machine",
                        "grinding_mill", "pressing_station", "tempering_station", "cooling_tunnel",
                        "enrobing_machine", "wrapping_station", "virginia_plant", "burley_plant",
                        "oriental_plant", "havana_plant", "basic_grow_light_slab", "advanced_grow_light_slab",
                        "premium_grow_light_slab", "cannabis_indica_plant", "cannabis_sativa_plant",
                        "cannabis_hybrid_plant", "cannabis_autoflower_plant", "bolivian_coca_plant",
                        "colombian_coca_plant", "small_extraction_vat", "medium_extraction_vat",
                        "big_extraction_vat", "small_refinery", "medium_refinery", "big_refinery",
                        "afghan_poppy_plant", "turkish_poppy_plant", "indian_poppy_plant",
                        "climate_lamp_small", "climate_lamp_medium", "climate_lamp_large", "coffee_terracotta_pot",
                        "coffee_ceramic_pot", "coffee_iron_pot", "coffee_golden_pot", "arabica_plant",
                        "robusta_plant", "liberica_plant", "excelsa_plant", "small_coffee_drying_tray",
                        "medium_coffee_drying_tray", "large_coffee_drying_tray", "small_coffee_roaster",
                        "medium_coffee_roaster", "large_coffee_roaster", "small_wine_press",
                        "medium_wine_press", "large_wine_press", "small_fermentation_tank",
                        "medium_fermentation_tank", "large_fermentation_tank", "small_aging_barrel",
                        "medium_aging_barrel", "large_aging_barrel", "small_brew_kettle", "medium_brew_kettle",
                        "large_brew_kettle", "small_beer_fermentation_tank", "medium_beer_fermentation_tank",
                        "large_beer_fermentation_tank", "small_conditioning_tank", "medium_conditioning_tank",
                        "large_conditioning_tank", "small_cheese_press", "medium_cheese_press",
                        "large_cheese_press", "small_aging_cave", "medium_aging_cave", "large_aging_cave",
                        "beehive", "advanced_beehive", "apiary", "small_aging_chamber", "medium_aging_chamber",
                        "large_aging_chamber", "honey_storage_barrel",
                        "small_conching_machine", "medium_conching_machine", "large_conching_machine",
                        "small_molding_station", "medium_molding_station", "large_molding_station",
                        "chocolate_storage_cabinet",
                        "fan_tier1", "fan_tier2", "fan_tier3"
                    ), obj -> obj instanceof String);

            SHOP_PLOT_BLOCKS = builder
                    .comment("Blocks for SHOP plots. Default: ALL")
                    .defineList("shop", java.util.List.of("ALL"), obj -> obj instanceof String);

            PUBLIC_PLOT_BLOCKS = builder
                    .comment("Blocks for PUBLIC plots. Default: ALL")
                    .defineList("public", java.util.List.of("ALL"), obj -> obj instanceof String);

            GOVERNMENT_PLOT_BLOCKS = builder
                    .comment("Blocks for GOVERNMENT plots. Default: ALL")
                    .defineList("government", java.util.List.of("ALL"), obj -> obj instanceof String);

            PRISON_PLOT_BLOCKS = builder
                    .comment("Blocks for PRISON plots. Default: ALL")
                    .defineList("prison", java.util.List.of("ALL"), obj -> obj instanceof String);

            TOWING_YARD_PLOT_BLOCKS = builder
                    .comment("Blocks for TOWING_YARD plots. Default: ALL")
                    .defineList("towing_yard", java.util.List.of("ALL"), obj -> obj instanceof String);

            SECRET_DOOR_ALLOWED_PLOT_TYPES = builder
                    .comment("Allowed plot types for HiddenSwitch/SecretDoor/Hatch (for non-OP).",
                        "Nutzung/Platzierung nur auf besessenem/vermietetem Plot mit Access.",
                        "Standard: RESIDENTIAL, INDUSTRIAL.")
                    .defineList("secret_door_allowed_plot_types",
                        java.util.Arrays.asList("RESIDENTIAL", "INDUSTRIAL"),
                        obj -> obj instanceof String);

            builder.pop();

            builder.pop();

            builder.comment("Utility consumer blocks - blocks that consume power/water",
                            "Remove an entry to exclude the block from consumption billing.",
                            "New blocks can be added with their full registry ID (e.g. schedulemc:small_drying_rack).",
                            "Consumption values are still managed internally.")
                    .push("utility");

            UTILITY_CONSUMER_BLOCKS = builder
                    .comment("List of all blocks that consume power/water.")
                    .defineList("consumer_blocks", java.util.Arrays.asList(
                        "schedulemc:basic_grow_light_slab", "schedulemc:advanced_grow_light_slab", "schedulemc:premium_grow_light_slab",
                        "schedulemc:climate_lamp_small", "schedulemc:climate_lamp_medium", "schedulemc:climate_lamp_large",
                        "schedulemc:terracotta_pot", "schedulemc:ceramic_pot", "schedulemc:iron_pot", "schedulemc:golden_pot",
                        "schedulemc:water_tank",
                        "schedulemc:fan_tier1", "schedulemc:fan_tier2", "schedulemc:fan_tier3",
                        "schedulemc:small_drying_rack", "schedulemc:medium_drying_rack", "schedulemc:big_drying_rack",
                        "schedulemc:small_fermentation_barrel", "schedulemc:medium_fermentation_barrel", "schedulemc:big_fermentation_barrel",
                        "schedulemc:fermentation_tank",
                        "schedulemc:small_packaging_table", "schedulemc:medium_packaging_table", "schedulemc:large_packaging_table",
                        "schedulemc:sink",
                        "schedulemc:small_extraction_vat", "schedulemc:medium_extraction_vat", "schedulemc:big_extraction_vat",
                        "schedulemc:small_refinery", "schedulemc:medium_refinery", "schedulemc:big_refinery",
                        "schedulemc:crack_cooker",
                        "schedulemc:scoring_machine", "schedulemc:opium_press", "schedulemc:cooking_station", "schedulemc:heroin_refinery",
                        "schedulemc:chemical_mixer", "schedulemc:reduction_kettle", "schedulemc:crystallizer", "schedulemc:vacuum_dryer",
                        "schedulemc:distillation_apparatus", "schedulemc:micro_doser", "schedulemc:perforation_press",
                        "schedulemc:reaction_kettle", "schedulemc:drying_oven", "schedulemc:pill_press",
                        "schedulemc:cannabis_trim_station", "schedulemc:cannabis_curing_jar", "schedulemc:cannabis_hash_press", "schedulemc:cannabis_oil_extractor",
                        "schedulemc:malting_station", "schedulemc:mash_tun",
                        "schedulemc:small_brew_kettle", "schedulemc:medium_brew_kettle", "schedulemc:large_brew_kettle",
                        "schedulemc:small_beer_fermentation_tank", "schedulemc:medium_beer_fermentation_tank", "schedulemc:large_beer_fermentation_tank",
                        "schedulemc:small_conditioning_tank", "schedulemc:medium_conditioning_tank", "schedulemc:large_conditioning_tank",
                        "schedulemc:beer_bottling_station",
                        "schedulemc:crushing_station",
                        "schedulemc:small_wine_press", "schedulemc:medium_wine_press", "schedulemc:large_wine_press",
                        "schedulemc:small_fermentation_tank", "schedulemc:medium_fermentation_tank", "schedulemc:large_fermentation_tank",
                        "schedulemc:small_aging_barrel", "schedulemc:medium_aging_barrel", "schedulemc:large_aging_barrel",
                        "schedulemc:wine_bottling_station",
                        "schedulemc:beehive", "schedulemc:advanced_beehive", "schedulemc:apiary",
                        "schedulemc:honey_extractor", "schedulemc:centrifugal_extractor", "schedulemc:filtering_station",
                        "schedulemc:small_aging_chamber", "schedulemc:medium_aging_chamber", "schedulemc:large_aging_chamber",
                        "schedulemc:processing_station", "schedulemc:creaming_station", "schedulemc:bottling_station",
                        "schedulemc:pasteurization_station", "schedulemc:curdling_vat",
                        "schedulemc:small_cheese_press", "schedulemc:medium_cheese_press", "schedulemc:large_cheese_press",
                        "schedulemc:small_aging_cave", "schedulemc:medium_aging_cave", "schedulemc:large_aging_cave",
                        "schedulemc:packaging_station",
                        "schedulemc:roasting_station", "schedulemc:winnowing_machine", "schedulemc:grinding_mill", "schedulemc:pressing_station",
                        "schedulemc:small_conching_machine", "schedulemc:medium_conching_machine", "schedulemc:large_conching_machine",
                        "schedulemc:tempering_station",
                        "schedulemc:small_molding_station", "schedulemc:medium_molding_station", "schedulemc:large_molding_station",
                        "schedulemc:enrobing_machine", "schedulemc:cooling_tunnel", "schedulemc:wrapping_station",
                        "schedulemc:wet_processing_station",
                        "schedulemc:small_coffee_roaster", "schedulemc:medium_coffee_roaster", "schedulemc:large_coffee_roaster",
                        "schedulemc:coffee_grinder", "schedulemc:coffee_packaging_table"
                    ), obj -> obj instanceof String);

            builder.pop();

            builder.comment("Daily Reward Settings")
                    .push("daily");

            DAILY_REWARD = builder
                    .comment("Basis-Belohnung pro Tag")
                    .defineInRange("daily_reward", 50.0, 1.0, 10000.0);

            DAILY_REWARD_STREAK_BONUS = builder
                    .comment("Bonus pro Streak-Tag")
                    .defineInRange("streak_bonus", 10.0, 0.0, 1000.0);

            MAX_STREAK_DAYS = builder
                    .comment("Maximum streak days for bonus")
                    .defineInRange("max_streak", 30, 1, 365);

            builder.pop();

            builder.comment("Plot Rental System Settings")
                    .push("rent");

            RENT_ENABLED = builder
                    .comment("Rental system enabled")
                    .define("enabled", true);

            MIN_RENT_PRICE = builder
                    .comment("Minimaler Mietpreis pro Tag")
                    .defineInRange("min_rent_price", 10.0, 0.1, 10000.0);

            MIN_RENT_DAYS = builder
                    .comment("Minimale Mietdauer in Tagen")
                    .defineInRange("min_rent_days", 1, 1, 365);

            MAX_RENT_DAYS = builder
                    .comment("Maximale Mietdauer in Tagen")
                    .defineInRange("max_rent_days", 30, 1, 365);

            AUTO_EVICT_EXPIRED = builder
                    .comment("Automatically evict when rent has expired")
                    .define("auto_evict", true);

            builder.pop();

            builder.comment("Shop System Settings")
                    .push("shop");

            SHOP_ENABLED = builder
                    .comment("Shop system enabled")
                    .define("enabled", true);

            BUY_MULTIPLIER = builder
                    .comment("Kaufpreis-Multiplikator (Basispreis * Multiplikator)")
                    .defineInRange("buy_multiplier", 1.5, 0.1, 10.0);

            SELL_MULTIPLIER = builder
                    .comment("Verkaufspreis-Multiplikator")
                    .defineInRange("sell_multiplier", 0.5, 0.1, 10.0);

            builder.pop();

            builder.comment("Plot Rating System Settings")
                    .push("ratings");

            RATINGS_ENABLED = builder
                    .comment("Rating system enabled")
                    .define("enabled", true);

            ALLOW_MULTIPLE_RATINGS = builder
                    .comment("Players can rate multiple times")
                    .define("allow_multiple", false);

            MIN_RATING = builder
                    .comment("Minimales Rating (Sterne)")
                    .defineInRange("min_rating", 1, 1, 5);

            MAX_RATING = builder
                    .comment("Maximales Rating (Sterne)")
                    .defineInRange("max_rating", 5, 1, 5);

            builder.pop();

            builder.comment("NPC System Settings")
                    .push("npc");

            NPC_WALKABLE_BLOCKS = builder
                    .comment("Block types NPCs are allowed to walk on (example: minecraft:stone, minecraft:grass_block)")
                    .defineList("walkable_blocks",
                        java.util.Arrays.asList(
                            "minecraft:stone",
                            "minecraft:grass_block",
                            "minecraft:dirt",
                            "minecraft:cobblestone",
                            "minecraft:oak_planks",
                            "minecraft:spruce_planks",
                            "minecraft:birch_planks",
                            "minecraft:jungle_planks",
                            "minecraft:acacia_planks",
                            "minecraft:dark_oak_planks",
                            "minecraft:gravel",
                            "minecraft:sand",
                            "minecraft:stone_bricks",
                            "minecraft:bricks",
                            "minecraft:oak_stairs",
                            "minecraft:spruce_stairs",
                            "minecraft:birch_stairs",
                            "minecraft:jungle_stairs",
                            "minecraft:acacia_stairs",
                            "minecraft:dark_oak_stairs",
                            "minecraft:stone_stairs",
                            "minecraft:cobblestone_stairs",
                            "minecraft:brick_stairs",
                            "minecraft:stone_brick_stairs"
                        ),
                        obj -> obj instanceof String);

            builder.pop();

            builder.comment("Map navigation system settings - road navigation on the map")
                    .push("navigation");

            NAVIGATION_ROAD_BLOCKS = builder
                    .comment("Block types recognized as roads for map navigation")
                    .defineList("road_blocks",
                        java.util.Arrays.asList(
                            "minecraft:cobblestone",
                            "minecraft:stone_bricks",
                            "minecraft:gravel",
                            "minecraft:dirt_path",
                            "minecraft:smooth_stone",
                            "minecraft:polished_andesite",
                            "minecraft:polished_diorite",
                            "minecraft:polished_granite",
                            "minecraft:bricks",
                            "minecraft:stone",
                            "minecraft:granite",
                            "minecraft:andesite",
                            "minecraft:diorite"
                        ),
                        obj -> obj instanceof String);

            NAVIGATION_SCAN_RADIUS = builder
                    .comment("Radius in blocks in which roads are scanned for navigation")
                    .defineInRange("scan_radius", 500, 100, 2000);

            NAVIGATION_PATH_UPDATE_INTERVAL = builder
                    .comment("Interval in milliseconds for how often the path is updated for moving targets")
                    .defineInRange("path_update_interval", 2000, 500, 10000);

            NAVIGATION_ARRIVAL_DISTANCE = builder
                    .comment("Distance in blocks at which the target counts as reached")
                    .defineInRange("arrival_distance", 5.0, 1.0, 50.0);

            builder.pop();

            builder.comment("Police System Settings")
                    .push("police");

            POLICE_ARREST_COOLDOWN_SECONDS = builder
                    .comment("Cooldown in seconds before police can arrest a player")
                    .defineInRange("arrest_cooldown_seconds", 5, 1, 60);

            POLICE_DETECTION_RADIUS = builder
                    .comment("Police detection radius in blocks")
                    .defineInRange("detection_radius", 32, 8, 128);

            POLICE_ARREST_DISTANCE = builder
                    .comment("Distance in blocks at which an arrest is possible")
                    .defineInRange("arrest_distance", 2.0, 1.0, 10.0);

            POLICE_SEARCH_DURATION_SECONDS = builder
                    .comment("Dauer in Sekunden, wie lange Polizei nach entkommenem Spieler sucht")
                    .defineInRange("search_duration_seconds", 60, 10, 300);

            POLICE_SEARCH_RADIUS = builder
                    .comment("Search radius in blocks in which police search for players")
                    .defineInRange("search_radius", 50, 10, 100);

            POLICE_SEARCH_TARGET_UPDATE_SECONDS = builder
                    .comment("Interval in seconds for how often police pick a new search target")
                    .defineInRange("search_target_update_seconds", 10, 5, 60);

            POLICE_BACKUP_SEARCH_RADIUS = builder
                    .comment("Search radius for backup police in blocks (performance: smaller = better)")
                    .defineInRange("backup_search_radius", 50, 20, 100);

            POLICE_INDOOR_HIDING_ENABLED = builder
                    .comment("Enables hiding system in buildings (players can hide from police)")
                    .define("indoor_hiding_enabled", true);

            POLICE_BLOCK_DOORS_DURING_PURSUIT = builder
                    .comment("Blocks opening doors during active pursuit")
                    .define("block_doors_during_pursuit", true);

            POLICE_RAID_SCAN_RADIUS = builder
                    .comment("Scan radius for illegal items on arrest (in blocks)")
                    .defineInRange("raid_scan_radius", 20, 5, 50);

            POLICE_ILLEGAL_CASH_THRESHOLD = builder
                    .comment("Cash threshold for illegal cash (above this value it is illegal)")
                    .defineInRange("illegal_cash_threshold", 10000.0, 1000.0, 100000.0);

            POLICE_RAID_ACCOUNT_PERCENTAGE = builder
                    .comment("Percentage of account balance for fines (0.1 = 10%)")
                    .defineInRange("raid_account_percentage", 0.1, 0.01, 0.5);

            POLICE_RAID_MIN_FINE = builder
                    .comment("Mindest-Geldstrafe bei Raid in Euro")
                    .defineInRange("raid_min_fine", 1000.0, 100.0, 50000.0);

            POLICE_ROOM_SCAN_ENABLED = builder
                    .comment("Enables smart room-based scanning (true = only seen rooms, false = full radius)")
                    .define("room_scan_enabled", true);

            POLICE_ROOM_SCAN_MAX_SIZE = builder
                    .comment("Maximum room size in blocks (safety limit against performance issues)")
                    .defineInRange("room_scan_max_size", 500, 50, 2000);

            POLICE_ROOM_SCAN_MAX_DEPTH = builder
                    .comment("Maximum Y-axis depth for room search (prevents vertical explosions)")
                    .defineInRange("room_scan_max_depth", 50, 10, 100);

            POLICE_ROOM_SCAN_MAX_ADDITIONAL_ROOMS = builder
                    .comment("Maximum number of additional rooms searched when contraband was found")
                    .defineInRange("room_scan_max_additional_rooms", 3, 0, 10);

            POLICE_STATION_WAIT_MINUTES = builder
                    .comment("Wartezeit in Minuten, die Polizisten an der Polizeistation bleiben")
                    .defineInRange("station_wait_minutes", 5, 1, 60);

            POLICE_STATION_RADIUS = builder
                    .comment("Radius in blocks in which police officers move around the station")
                    .defineInRange("station_radius", 10, 3, 50);

            POLICE_PATROL_WAIT_MINUTES = builder
                    .comment("Wartezeit in Minuten an jedem Patrouillenpunkt (nutze /time add zum Beschleunigen)")
                    .defineInRange("patrol_wait_minutes", 1, 1, 30);

            POLICE_PATROL_RADIUS = builder
                    .comment("Radius in blocks in which police officers move around patrol points")
                    .defineInRange("patrol_radius", 3, 1, 20);

            // NEW: Police Overhaul Features
            POLICE_VEHICLE_PURSUIT_ENABLED = builder
                    .comment("Aktiviert Fahrzeugverfolgung durch Polizei")
                    .define("vehicle_pursuit_enabled", true);
            POLICE_VEHICLE_SPEED_MULTIPLIER = builder
                    .comment("Speed multiplier for police vehicles")
                    .defineInRange("vehicle_speed_multiplier", 1.3, 1.0, 3.0);
            POLICE_SIREN_ENABLED = builder
                    .comment("Enables police siren and emergency lights")
                    .define("siren_enabled", true);
            POLICE_SIREN_SOUND_RADIUS = builder
                    .comment("Hoerweite der Sirene in Bloecken")
                    .defineInRange("siren_sound_radius", 50, 10, 200);
            POLICE_ROADBLOCK_ENABLED = builder
                    .comment("Aktiviert Strassensperren bei Wanted >= 4")
                    .define("roadblock_enabled", true);
            POLICE_MAX_ROADBLOCKS = builder
                    .comment("Maximale Anzahl Strassensperren pro Spieler")
                    .defineInRange("max_roadblocks", 2, 1, 5);
            POLICE_ROADBLOCK_DURATION_SECONDS = builder
                    .comment("Dauer einer Strassensperre in Sekunden")
                    .defineInRange("roadblock_duration_seconds", 300, 60, 1200);
            POLICE_WARNING_ENABLED = builder
                    .comment("Aktiviert Verwarnungssystem bei Wanted 1-2")
                    .define("warning_enabled", true);
            POLICE_WARNING_TIMEOUT_SECONDS = builder
                    .comment("Seconds for the warning grace period")
                    .defineInRange("warning_timeout_seconds", 10, 5, 60);
            POLICE_TRAFFIC_VIOLATIONS_ENABLED = builder
                    .comment("Aktiviert Verkehrsdelikte (NPC ueberfahren etc.)")
                    .define("traffic_violations_enabled", true);
            POLICE_SPEED_LIMIT_DEFAULT = builder
                    .comment("Default speed limit for traffic violations")
                    .defineInRange("speed_limit_default", 0.5, 0.1, 2.0);
            POLICE_CONTAINER_SCAN_DEPTH = builder
                    .comment("Maximum recursion depth for container scanning (shulker boxes)")
                    .defineInRange("container_scan_depth", 2, 0, 5);
            POLICE_EVIDENCE_MULTIPLIER_ENABLED = builder
                    .comment("Aktiviert Strafmultiplikator basierend auf Beweisstaerke")
                    .define("evidence_multiplier_enabled", true);
            POLICE_FLANKING_ENABLED = builder
                    .comment("Enables strategic flanking coordination for police")
                    .define("flanking_enabled", true);
            POLICE_WANTED_POSTERS_MIN_LEVEL = builder
                    .comment("Minimum wanted level for wanted posters on smartphone")
                    .defineInRange("wanted_posters_min_level", 3, 1, 5);

            builder.comment("Police combat: weapons are only used when an arrest is not working and the wanted level is high. Arrest always stays the preferred outcome.")
                    .push("combat");
            POLICE_COMBAT_ENABLED = builder
                    .comment("Master switch for police weapon use")
                    .define("enabled", true);
            POLICE_MELEE_WANTED_LEVEL = builder
                    .comment("Minimum wanted level for melee (baseball bat)")
                    .defineInRange("melee_wanted_level", 3, 1, 5);
            POLICE_RANGED_WANTED_LEVEL = builder
                    .comment("Minimum wanted level for ranged (pistol)")
                    .defineInRange("ranged_wanted_level", 4, 1, 5);
            POLICE_RANGED_MIN_DISTANCE = builder
                    .comment("Pistol is only used beyond this distance (blocks)")
                    .defineInRange("ranged_min_distance", 8.0, 2.0, 32.0);
            POLICE_ESCALATION_ESCAPE_COUNT = builder
                    .comment("Arrest-range escapes before police draw weapons")
                    .defineInRange("escalation_escape_count", 2, 1, 10);
            POLICE_ESCALATION_PURSUIT_SECONDS = builder
                    .comment("Continuous fruitless pursuit (seconds) before police draw weapons")
                    .defineInRange("escalation_pursuit_seconds", 10, 1, 120);
            POLICE_SHOT_COOLDOWN_TICKS = builder
                    .comment("Cooldown between pistol shots per officer (ticks)")
                    .defineInRange("shot_cooldown_ticks", 40, 5, 200);
            POLICE_PISTOL_DAMAGE = builder
                    .comment("Base pistol bullet damage")
                    .defineInRange("pistol_damage", 4.0, 0.5, 20.0);
            POLICE_MAX_SIMULTANEOUS_SHOOTERS = builder
                    .comment("Maximum officers shooting one target at once")
                    .defineInRange("max_simultaneous_shooters", 2, 1, 8);
            POLICE_LETHAL_FORCE = builder
                    .comment("Allow lethal force only in emergencies (5 stars AND the player attacked police). Otherwise shots stop the player at half a heart and an arrest follows.")
                    .define("lethal_force_in_emergency", true);
            POLICE_ILLEGAL_ITEMS_CASH_PENALTY = builder
                    .comment("Penalty on total wallet cash when killed by police while carrying illegal items (0.20 = 20%)")
                    .defineInRange("illegal_items_cash_penalty", 0.20, 0.0, 1.0);
            POLICE_ILLEGAL_ITEMS_ACCOUNT_PENALTY = builder
                    .comment("Penalty on bank account when killed by police while carrying illegal items (0.05 = 5%)")
                    .defineInRange("illegal_items_account_penalty", 0.05, 0.0, 1.0);
            builder.pop();

            builder.pop();

            builder.comment("Stealing Minigame Settings")
                    .push("stealing");

            STEALING_INDICATOR_SPEED = builder
                    .comment("Speed of the red indicator (higher = faster, default: 0.04)")
                    .defineInRange("indicator_speed", 0.04, 0.001, 0.2);

            STEALING_MAX_ATTEMPTS = builder
                    .comment("Maximale Anzahl der Versuche")
                    .defineInRange("max_attempts", 3, 1, 10);

            STEALING_MIN_ZONE_SIZE = builder
                    .comment("Minimum size of the success zone (hard, high value, 0.05 = 5%)")
                    .defineInRange("min_zone_size", 0.05, 0.01, 0.5);

            STEALING_MAX_ZONE_SIZE = builder
                    .comment("Maximum size of the success zone (easy, low value, 0.15 = 15%)")
                    .defineInRange("max_zone_size", 0.15, 0.01, 0.5);

            builder.pop();

            builder.comment("Warehouse System Settings")
                    .push("warehouse");

            WAREHOUSE_SLOT_COUNT = builder
                    .comment("Anzahl verschiedener Item-Slots pro Warehouse")
                    .defineInRange("slot_count", 32, 8, 128);

            WAREHOUSE_MAX_CAPACITY_PER_SLOT = builder
                    .comment("Maximale Item-Menge pro Slot (16 Stacks = 1024)")
                    .defineInRange("max_capacity_per_slot", 1024, 64, 10000);

            WAREHOUSE_DELIVERY_INTERVAL_DAYS = builder
                    .comment("Lieferungs-Intervall in Minecraft-Tagen")
                    .defineInRange("delivery_interval_days", 3, 1, 30);

            WAREHOUSE_DEFAULT_DELIVERY_PRICE = builder
                    .comment("Default delivery price for items without a specific price")
                    .defineInRange("default_delivery_price", 5, 1, 10000);

            builder.pop();

            builder.comment("Plot Utility Pricing Settings",
                            "Power and water prices for the plot utility system")
                    .push("utility");

            UTILITY_ELECTRICITY_PRICE_PER_KWH = builder
                    .comment("Strompreis pro kWh in Euro (Standard: 0.35 €/kWh)")
                    .defineInRange("electricity_price_per_kwh", 0.35, 0.001, 100.0);

            UTILITY_WATER_PRICE_PER_LITER = builder
                    .comment("Wasserpreis pro Liter in Euro (Standard: 0.005 €/L = 0.50 €/100L)")
                    .defineInRange("water_price_per_liter", 0.005, 0.0001, 10.0);

            builder.pop();

            builder.comment("Workshop System Settings")
                    .push("workshop");

            WORKSHOP_BASE_INSPECTION_FEE = builder
                    .comment("Base inspection fee in euros (always charged)")
                    .defineInRange("base_inspection_fee", 25.0, 0.0, 1000.0);

            WORKSHOP_REPAIR_COST_PER_PERCENT = builder
                    .comment("Reparaturkosten pro Prozent Schaden in Euro")
                    .defineInRange("repair_cost_per_percent", 2.0, 0.1, 100.0);

            WORKSHOP_BATTERY_COST_PER_PERCENT = builder
                    .comment("Batterieladungskosten pro Prozent in Euro")
                    .defineInRange("battery_cost_per_percent", 0.5, 0.1, 50.0);

            WORKSHOP_OIL_CHANGE_COST = builder
                    .comment("Oil change cost in euros")
                    .defineInRange("oil_change_cost", 15.0, 1.0, 500.0);

            WORKSHOP_MOTOR_UPGRADE_COST_LVL2 = builder
                    .comment("Cost for motor upgrade level 2 (Normal -> Performance)")
                    .defineInRange("motor_upgrade_cost_lvl2", 500.0, 10.0, 10000.0);

            WORKSHOP_MOTOR_UPGRADE_COST_LVL3 = builder
                    .comment("Cost for motor upgrade level 3 (Performance -> Performance 2)")
                    .defineInRange("motor_upgrade_cost_lvl3", 1000.0, 10.0, 20000.0);

            WORKSHOP_TANK_UPGRADE_COST_LVL2 = builder
                    .comment("Cost for tank upgrade level 2 (15L -> 30L)")
                    .defineInRange("tank_upgrade_cost_lvl2", 200.0, 10.0, 5000.0);

            WORKSHOP_TANK_UPGRADE_COST_LVL3 = builder
                    .comment("Cost for tank upgrade level 3 (30L -> 50L)")
                    .defineInRange("tank_upgrade_cost_lvl3", 400.0, 10.0, 10000.0);

            WORKSHOP_TIRE_UPGRADE_COST = builder
                    .comment("Cost for tire upgrade (per level)")
                    .defineInRange("tire_upgrade_cost", 150.0, 10.0, 5000.0);

            WORKSHOP_PAINT_CHANGE_COST = builder
                    .comment("Cost for paint change")
                    .defineInRange("paint_change_cost", 100.0, 10.0, 5000.0);

            WORKSHOP_FENDER_UPGRADE_COST_LVL2 = builder
                    .comment("Cost for fender upgrade level 2 (Basic -> Chrome)")
                    .defineInRange("fender_upgrade_cost_lvl2", 250.0, 10.0, 5000.0);

            WORKSHOP_FENDER_UPGRADE_COST_LVL3 = builder
                    .comment("Cost for fender upgrade level 3 (Chrome -> Sport)")
                    .defineInRange("fender_upgrade_cost_lvl3", 500.0, 10.0, 10000.0);

            builder.pop();

            builder.comment("Bank System Settings")
                    .push("bank");

            BANK_DEPOSIT_LIMIT = builder
                    .comment("Maximaler Einzahlungsbetrag pro Transaktion")
                    .defineInRange("deposit_limit", 9999.0, 100.0, 1000000.0);

            BANK_TRANSFER_DAILY_LIMIT = builder
                    .comment("Maximum transfer amount per day")
                    .defineInRange("transfer_daily_limit", 999.0, 10.0, 100000.0);

            builder.pop();

            builder.comment("Stock market settings")
                    .push("stock_market");

            STOCK_GOLD_BASE_PRICE = builder
                    .comment("Base price for gold ingots")
                    .defineInRange("gold_base_price", 250.0, 10.0, 10000.0);

            STOCK_DIAMOND_BASE_PRICE = builder
                    .comment("Base price for diamonds")
                    .defineInRange("diamond_base_price", 450.0, 10.0, 10000.0);

            STOCK_EMERALD_BASE_PRICE = builder
                    .comment("Base price for emeralds")
                    .defineInRange("emerald_base_price", 180.0, 10.0, 10000.0);

            STOCK_MAX_PRICE_CHANGE_PERCENT = builder
                    .comment("Maximum price change per day in percent (0.10 = 10%)")
                    .defineInRange("max_price_change_percent", 0.10, 0.01, 0.50);

            builder.pop();

            // ═══════════════════════════════════════════════════════════
            // DYNAMIC PRICING SYSTEM (UDPS)
            // ═══════════════════════════════════════════════════════════

            builder.comment("Unified Dynamic Pricing System (UDPS)",
                            "Harmonisiert alle Preise dynamisch mit Angebot/Nachfrage")
                    .push("dynamic_pricing");

            DYNAMIC_PRICING_ENABLED = builder
                    .comment("Dynamisches Preissystem aktivieren")
                    .define("enabled", true);

            DYNAMIC_PRICING_SD_FACTOR = builder
                    .comment("Angebot/Nachfrage-Faktor (wie stark S&D Preise beeinflusst, 0.0-1.0)")
                    .defineInRange("sd_factor", 0.3, 0.0, 1.0);

            DYNAMIC_PRICING_MIN_MULTIPLIER = builder
                    .comment("Minimaler globaler Preis-Multiplikator")
                    .defineInRange("min_multiplier", 0.3, 0.1, 1.0);

            DYNAMIC_PRICING_MAX_MULTIPLIER = builder
                    .comment("Maximaler globaler Preis-Multiplikator")
                    .defineInRange("max_multiplier", 5.0, 1.0, 20.0);

            DYNAMIC_PRICING_UPDATE_INTERVAL_MINUTES = builder
                    .comment("Update interval for price calculation in minutes")
                    .defineInRange("update_interval_minutes", 5, 1, 30);

            DYNAMIC_PRICING_SD_DECAY_RATE = builder
                    .comment("Angebot/Nachfrage Decay-Rate pro Update (0.02 = 2%)")
                    .defineInRange("sd_decay_rate", 0.02, 0.001, 0.1);

            DYNAMIC_PRICING_DAILY_FOOD_COST = builder
                    .comment("Expected daily food costs on hard difficulty")
                    .defineInRange("daily_food_cost", 20.0, 5.0, 200.0);

            DYNAMIC_PRICING_DAILY_REFERENCE_INCOME = builder
                    .comment("Reference daily income for price calibration")
                    .defineInRange("daily_reference_income", 150.0, 50.0, 1000.0);

            builder.pop();

            builder.comment("Economic Cycle Settings",
                            "6-Phasen Wirtschaftszyklus: Normal -> Boom -> Ueberhitzung -> Rezession -> Depression -> Erholung")
                    .push("economy_cycle");

            ECONOMY_CYCLE_ENABLED = builder
                    .comment("Wirtschaftszyklus aktivieren")
                    .define("enabled", true);

            ECONOMY_CYCLE_MIN_DURATION_DAYS = builder
                    .comment("Minimale Phasendauer in MC-Tagen")
                    .defineInRange("min_duration_days", 2, 1, 30);

            ECONOMY_CYCLE_MAX_DURATION_DAYS = builder
                    .comment("Maximale Phasendauer in MC-Tagen")
                    .defineInRange("max_duration_days", 10, 2, 60);

            ECONOMY_CYCLE_EVENT_BASE_CHANCE = builder
                    .comment("Base chance for economy events per day (0.1 = 10%)")
                    .defineInRange("event_base_chance", 0.10, 0.0, 1.0);

            builder.pop();

            builder.comment("Producer Level System Settings",
                            "Player progression through production and sales")
                    .push("level_system");

            LEVEL_SYSTEM_ENABLED = builder
                    .comment("Level-System aktivieren")
                    .define("enabled", true);

            LEVEL_MAX = builder
                    .comment("Maximales Produzenten-Level")
                    .defineInRange("max_level", 30, 10, 100);

            LEVEL_BASE_XP = builder
                    .comment("Base XP for level 1")
                    .defineInRange("base_xp", 100, 10, 10000);

            LEVEL_XP_EXPONENT = builder
                    .comment("Exponent for XP curve (higher = steeper curve)")
                    .defineInRange("xp_exponent", 1.8, 1.0, 3.0);

            LEVEL_ILLEGAL_XP_MULTIPLIER = builder
                    .comment("XP multiplier for illegal sales (higher = faster progression)")
                    .defineInRange("illegal_xp_multiplier", 1.5, 0.5, 5.0);

            LEVEL_LEGAL_XP_MULTIPLIER = builder
                    .comment("XP multiplier for legal sales")
                    .defineInRange("legal_xp_multiplier", 1.0, 0.5, 5.0);

            builder.pop();

            builder.comment("Risk Premium Settings",
                            "Risk surcharges for illegal products")
                    .push("risk_premium");

            RISK_BASE_CANNABIS = builder
                    .comment("Base risk multiplier for cannabis (1.15 = 15% surcharge)")
                    .defineInRange("base_cannabis", 1.15, 1.0, 3.0);

            RISK_BASE_COCAINE = builder
                    .comment("Base risk multiplier for cocaine")
                    .defineInRange("base_cocaine", 1.40, 1.0, 3.0);

            RISK_BASE_HEROIN = builder
                    .comment("Base risk multiplier for heroin")
                    .defineInRange("base_heroin", 1.50, 1.0, 3.0);

            RISK_BASE_METH = builder
                    .comment("Base risk multiplier for methamphetamine")
                    .defineInRange("base_meth", 1.45, 1.0, 3.0);

            RISK_CONFISCATION_MULTIPLIER = builder
                    .comment("Confiscation risk surcharge for illegal machines")
                    .defineInRange("confiscation_multiplier", 1.25, 1.0, 3.0);

            builder.pop();

            builder.comment("Anti-Exploit Settings",
                            "Schutz gegen Wirtschafts-Exploits")
                    .push("anti_exploit");

            ANTI_EXPLOIT_DAILY_SELL_LIMIT = builder
                    .comment("Maximaler Tagesumsatz pro Spieler in Euro (0 = unbegrenzt)")
                    .defineInRange("daily_sell_limit", 5000.0, 0.0, 1000000.0);

            ANTI_EXPLOIT_MASS_SELL_COOLDOWN_SECONDS = builder
                    .comment("Cooldown in Sekunden nach Massenverkauf")
                    .defineInRange("mass_sell_cooldown_seconds", 30, 5, 300);

            ANTI_EXPLOIT_MASS_SELL_THRESHOLD = builder
                    .comment("Ab wie vielen Items pro Verkauf gilt es als Massenverkauf")
                    .defineInRange("mass_sell_threshold", 64, 10, 1000);

            ANTI_EXPLOIT_MASS_SELL_PENALTY = builder
                    .comment("Preisreduzierung bei Massenverkauf (0.8 = 20% weniger)")
                    .defineInRange("mass_sell_penalty", 0.80, 0.1, 1.0);

            builder.pop();

            // ═══════════════════════════════════════════════════════════
            // PRODUCT PRICES
            // ═══════════════════════════════════════════════════════════
            builder.comment("Referenzpreise aller Produkte im EconomyController (€ pro Einheit/Gramm)").push("product_prices");
            PRODUCT_PRICES = builder
                .comment("Format: PRODUKT_ID=Preis  |  Aenderungen gelten sofort nach dem Speichern.")
                .defineList("prices", java.util.Arrays.asList(
                    // Cannabis
                    "CANNABIS_INDICA=12.0", "CANNABIS_SATIVA=14.0",
                    "CANNABIS_HYBRID=16.0", "CANNABIS_AUTOFLOWER=9.0",
                    // Tabak
                    "TOBACCO_VIRGINIA=6.0", "TOBACCO_BURLEY=8.0",
                    "TOBACCO_ORIENTAL=10.0", "TOBACCO_HAVANA=14.0",
                    // Kokain
                    "COCA_BOLIVIAN=25.0", "COCA_PERUVIAN=35.0",
                    "COCA_COLOMBIAN=50.0", "CRACK_ROCK=40.0",
                    // Heroin (Mohn)
                    "POPPY_INDIAN=20.0", "POPPY_TURKISH=35.0", "POPPY_AFGHAN=55.0",
                    // Meth
                    "METH_STANDARD=30.0", "METH_GUT=50.0", "METH_BLUE_SKY=80.0",
                    // MDMA
                    "MDMA_SCHLECHT=8.0", "MDMA_STANDARD=18.0",
                    "MDMA_GUT=30.0", "MDMA_PREMIUM=50.0",
                    // LSD
                    "LSD_SCHWACH=15.0", "LSD_STANDARD=25.0",
                    "LSD_STARK=40.0", "LSD_BICYCLE_DAY=70.0",
                    // Pilze
                    "MUSHROOM_MEXICANA=10.0", "MUSHROOM_CUBENSIS=18.0", "MUSHROOM_AZURESCENS=35.0",
                    // Wein
                    "WINE_RIESLING=8.0", "WINE_CHARDONNAY=12.0",
                    "WINE_PINOT_NOIR=15.0", "WINE_MERLOT=20.0",
                    // Bier
                    "BEER_PILSNER=5.0", "BEER_WEIZEN=6.0",
                    "BEER_ALE=7.0", "BEER_STOUT=9.0",
                    // Kaffee
                    "COFFEE_ARABICA=6.0", "COFFEE_ROBUSTA=8.0",
                    "COFFEE_LIBERICA=12.0", "COFFEE_EXCELSA=18.0",
                    // Kaese
                    "CHEESE_GOUDA=7.0", "CHEESE_EMMENTAL=10.0",
                    "CHEESE_CAMEMBERT=13.0", "CHEESE_PARMESAN=17.0",
                    // Schokolade
                    "CHOCOLATE_WHITE=5.0", "CHOCOLATE_MILK=6.0",
                    "CHOCOLATE_DARK=9.0", "CHOCOLATE_RUBY=14.0",
                    // Honig
                    "HONEY_ACACIA=6.0", "HONEY_WILDFLOWER=8.0",
                    "HONEY_FOREST=11.0", "HONEY_MANUKA=18.0",
                    // Fahrzeuge
                    "VEHICLE_OAK=5000.0", "VEHICLE_BIG_OAK=7500.0",
                    "VEHICLE_SUV=10000.0", "VEHICLE_TRANSPORTER=12000.0",
                    "VEHICLE_SPORT=15000.0",
                    // Toepfe
                    "POT_TERRACOTTA=20.0", "POT_CERAMIC=40.0",
                    "POT_IRON=80.0", "POT_GOLDEN=150.0",
                    // Nahrung
                    "FOOD_BREAD=3.75", "FOOD_COOKED_BEEF=7.5",
                    "FOOD_COOKED_PORKCHOP=6.875", "FOOD_APPLE=1.25",
                    "FOOD_GOLDEN_APPLE=200.0", "FOOD_CARROT=0.9375",
                    "FOOD_POTATO=0.9375", "FOOD_CAKE=80.0", "FOOD_COOKIE=0.78125"
                ), obj -> obj instanceof String && ((String) obj).contains("="));
            builder.pop();

            // ═══════════════════════════════════════════════════════════
            // BLOCK PRICES
            // ═══════════════════════════════════════════════════════════
            builder.comment("Purchase prices and level requirements of all production blocks").push("block_prices");
            BLOCK_PRICES = builder
                .comment("Format: schedulemc:blockname=price:level  |  Level 0 = no requirement")
                .defineList("prices", java.util.Arrays.asList(
                    // Cannabis
                    "schedulemc:cannabis_trim_station=200:1",
                    "schedulemc:cannabis_curing_jar=150:1",
                    "schedulemc:cannabis_hash_press=500:8",
                    "schedulemc:cannabis_oil_extractor=800:13",
                    // Tabak – Toepfe
                    "schedulemc:terracotta_pot=20:1",
                    "schedulemc:ceramic_pot=40:6",
                    "schedulemc:iron_pot=80:11",
                    "schedulemc:golden_pot=150:16",
                    // Tabak – Trocknungsgestelle
                    "schedulemc:small_drying_rack=100:1",
                    "schedulemc:medium_drying_rack=300:22",
                    "schedulemc:big_drying_rack=600:25",
                    // Tabak – Faesser
                    "schedulemc:small_fermentation_barrel=150:1",
                    "schedulemc:medium_fermentation_barrel=450:22",
                    "schedulemc:big_fermentation_barrel=900:25",
                    // Tabak – Packtische
                    "schedulemc:small_packaging_table=80:1",
                    "schedulemc:medium_packaging_table=240:22",
                    "schedulemc:large_packaging_table=480:25",
                    // Tabak – Grow Lights
                    "schedulemc:basic_grow_light_slab=200:3",
                    "schedulemc:advanced_grow_light_slab=600:10",
                    "schedulemc:premium_grow_light_slab=1200:16",
                    // Tabak – Sonstiges
                    "schedulemc:sink=50:1",
                    // Wein
                    "schedulemc:crushing_station=300:5",
                    "schedulemc:small_wine_press=400:5",
                    "schedulemc:medium_wine_press=1200:22",
                    "schedulemc:large_wine_press=2400:25",
                    "schedulemc:small_fermentation_tank=350:5",
                    "schedulemc:medium_fermentation_tank=1050:22",
                    "schedulemc:large_fermentation_tank=2100:25",
                    "schedulemc:small_aging_barrel=450:5",
                    "schedulemc:medium_aging_barrel=1350:22",
                    "schedulemc:large_aging_barrel=2700:25",
                    "schedulemc:wine_bottling_station=200:5",
                    // Bier
                    "schedulemc:malting_station=250:1",
                    "schedulemc:mash_tun=300:1",
                    "schedulemc:small_brew_kettle=400:1",
                    "schedulemc:medium_brew_kettle=1200:22",
                    "schedulemc:large_brew_kettle=2400:25",
                    "schedulemc:small_beer_fermentation_tank=350:1",
                    "schedulemc:medium_beer_fermentation_tank=1050:22",
                    "schedulemc:large_beer_fermentation_tank=2100:25",
                    "schedulemc:small_conditioning_tank=300:1",
                    "schedulemc:medium_conditioning_tank=900:22",
                    "schedulemc:large_conditioning_tank=1800:25",
                    "schedulemc:beer_bottling_station=200:1",
                    // Kaffee
                    "schedulemc:wet_processing_station=300:3",
                    "schedulemc:small_coffee_roaster=400:3",
                    "schedulemc:medium_coffee_roaster=1200:22",
                    "schedulemc:large_coffee_roaster=2400:25",
                    "schedulemc:coffee_grinder=200:3",
                    "schedulemc:coffee_packaging_table=150:3",
                    // Kaese
                    "schedulemc:pasteurization_station=350:3",
                    "schedulemc:curdling_vat=300:3",
                    "schedulemc:small_cheese_press=400:3",
                    "schedulemc:medium_cheese_press=1200:22",
                    "schedulemc:large_cheese_press=2400:25",
                    "schedulemc:small_aging_cave=350:3",
                    "schedulemc:medium_aging_cave=1050:22",
                    "schedulemc:large_aging_cave=2100:25",
                    "schedulemc:packaging_station=200:3",
                    // Schokolade
                    "schedulemc:roasting_station=300:3",
                    "schedulemc:winnowing_machine=250:3",
                    "schedulemc:grinding_mill=350:3",
                    "schedulemc:pressing_station=300:3",
                    "schedulemc:small_conching_machine=400:3",
                    "schedulemc:medium_conching_machine=1200:22",
                    "schedulemc:large_conching_machine=2400:25",
                    "schedulemc:tempering_station=500:6",
                    "schedulemc:small_molding_station=350:3",
                    "schedulemc:medium_molding_station=1050:22",
                    "schedulemc:large_molding_station=2100:25",
                    "schedulemc:enrobing_machine=600:10",
                    "schedulemc:cooling_tunnel=400:6",
                    "schedulemc:wrapping_station=200:3",
                    // Honig
                    "schedulemc:beehive=100:1",
                    "schedulemc:advanced_beehive=300:8",
                    "schedulemc:apiary=800:18",
                    "schedulemc:honey_extractor=200:1",
                    "schedulemc:centrifugal_extractor=600:8",
                    "schedulemc:filtering_station=150:1",
                    "schedulemc:small_aging_chamber=200:1",
                    "schedulemc:medium_aging_chamber=600:22",
                    "schedulemc:large_aging_chamber=1200:25",
                    "schedulemc:processing_station=250:1",
                    "schedulemc:creaming_station=400:8",
                    "schedulemc:bottling_station=150:1",
                    // Pilze
                    "schedulemc:climate_lamp_small=400:8",
                    "schedulemc:climate_lamp_medium=1200:22",
                    "schedulemc:climate_lamp_large=2400:25",
                    "schedulemc:water_tank=200:8",
                    // Kokain (illegal)
                    "schedulemc:small_extraction_vat=800:11",
                    "schedulemc:medium_extraction_vat=2400:22",
                    "schedulemc:big_extraction_vat=4800:25",
                    "schedulemc:small_refinery=1000:11",
                    "schedulemc:medium_refinery=3000:22",
                    "schedulemc:big_refinery=6000:25",
                    "schedulemc:crack_cooker=1200:11",
                    // Heroin (illegal)
                    "schedulemc:scoring_machine=1500:15",
                    "schedulemc:opium_press=1800:15",
                    "schedulemc:cooking_station=2000:15",
                    "schedulemc:heroin_refinery=5000:22",
                    // Meth (illegal)
                    "schedulemc:chemical_mixer=2000:15",
                    "schedulemc:reduction_kettle=2500:15",
                    "schedulemc:crystallizer=3000:15",
                    "schedulemc:vacuum_dryer=4000:22",
                    // MDMA (illegal)
                    "schedulemc:reaction_kettle=1500:11",
                    "schedulemc:drying_oven=1800:11",
                    "schedulemc:pill_press=2000:11",
                    // LSD (illegal)
                    "schedulemc:fermentation_tank=2000:13",
                    "schedulemc:distillation_apparatus=2500:13",
                    "schedulemc:micro_doser=3000:13",
                    "schedulemc:perforation_press=2500:13",
                    // Fans
                    "schedulemc:fan_tier1=150:1",
                    "schedulemc:fan_tier2=500:8",
                    "schedulemc:fan_tier3=1500:16"
                ), obj -> obj instanceof String s && s.contains("=") && s.contains(":"));
            builder.pop();
        }
    }
}
