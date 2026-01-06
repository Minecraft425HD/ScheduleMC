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
        public final ForgeConfigSpec.DoubleValue OVERDRAFT_MAX_LIMIT;
        public final ForgeConfigSpec.DoubleValue OVERDRAFT_WARNING_THRESHOLD;
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
        // GARAGE SYSTEM
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.DoubleValue GARAGE_BASE_INSPECTION_FEE;
        public final ForgeConfigSpec.DoubleValue GARAGE_REPAIR_COST_PER_PERCENT;
        public final ForgeConfigSpec.DoubleValue GARAGE_BATTERY_COST_PER_PERCENT;
        public final ForgeConfigSpec.DoubleValue GARAGE_OIL_CHANGE_COST;

        // Upgrade costs
        public final ForgeConfigSpec.DoubleValue GARAGE_MOTOR_UPGRADE_COST_LVL2;
        public final ForgeConfigSpec.DoubleValue GARAGE_MOTOR_UPGRADE_COST_LVL3;
        public final ForgeConfigSpec.DoubleValue GARAGE_TANK_UPGRADE_COST_LVL2;
        public final ForgeConfigSpec.DoubleValue GARAGE_TANK_UPGRADE_COST_LVL3;
        public final ForgeConfigSpec.DoubleValue GARAGE_TIRE_UPGRADE_COST;
        public final ForgeConfigSpec.DoubleValue GARAGE_PAINT_CHANGE_COST;
        public final ForgeConfigSpec.DoubleValue GARAGE_FENDER_UPGRADE_COST_LVL2;
        public final ForgeConfigSpec.DoubleValue GARAGE_FENDER_UPGRADE_COST_LVL3;

        public Common(ForgeConfigSpec.Builder builder) {
            
            builder.comment("ScheduleMC 3.0 - Economy Settings")
                    .push("economy");

            START_BALANCE = builder
                    .comment("Startguthaben für neue Spieler")
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
                    .comment("Mindesteinlage für neues Sparkonto")
                    .defineInRange("min_deposit", 1000.0, 100.0, 100000.0);

            SAVINGS_INTEREST_RATE = builder
                    .comment("Zinssatz pro Woche (0.05 = 5%)")
                    .defineInRange("interest_rate", 0.05, 0.0, 0.5);

            SAVINGS_LOCK_PERIOD_WEEKS = builder
                    .comment("Sperrfrist in Wochen")
                    .defineInRange("lock_period_weeks", 4, 1, 52);

            SAVINGS_EARLY_WITHDRAWAL_PENALTY = builder
                    .comment("Strafe für vorzeitige Abhebung (0.10 = 10%)")
                    .defineInRange("early_withdrawal_penalty", 0.10, 0.0, 0.5);

            builder.pop();

            builder.comment("Overdraft Settings (Dispo)")
                    .push("overdraft");

            OVERDRAFT_MAX_LIMIT = builder
                    .comment("Maximales Dispo-Limit (negativ, z.B. -5000)")
                    .defineInRange("max_limit", -5000.0, -100000.0, 0.0);

            OVERDRAFT_WARNING_THRESHOLD = builder
                    .comment("Warnschwelle (negativ, z.B. -2500)")
                    .defineInRange("warning_threshold", -2500.0, -100000.0, 0.0);

            OVERDRAFT_INTEREST_RATE = builder
                    .comment("Dispo-Zinssatz pro Woche (0.25 = 25%)")
                    .defineInRange("interest_rate", 0.25, 0.0, 1.0);

            builder.pop();

            builder.comment("Recurring Payments Settings (Daueraufträge)")
                    .push("recurring");

            RECURRING_MAX_PER_PLAYER = builder
                    .comment("Maximale Daueraufträge pro Spieler")
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
                    .comment("Minimale Plot-Größe in Blöcken")
                    .defineInRange("min_plot_size", 64L, 1L, 1000000L);

            MAX_PLOT_SIZE = builder
                    .comment("Maximale Plot-Größe in Blöcken")
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
                    .comment("Plots können übertragen werden")
                    .define("allow_plot_transfer", true);

            REFUND_ON_ABANDON = builder
                    .comment("Rückerstattung beim Aufgeben (0.0-1.0, 0.5 = 50%)")
                    .defineInRange("refund_on_abandon", 0.5, 0.0, 1.0);

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
                    .comment("Maximale Streak-Tage für Bonus")
                    .defineInRange("max_streak", 30, 1, 365);

            builder.pop();

            builder.comment("Plot Rental System Settings")
                    .push("rent");

            RENT_ENABLED = builder
                    .comment("Mietsystem aktiviert")
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
                    .comment("Automatisch räumen bei abgelaufener Miete")
                    .define("auto_evict", true);

            builder.pop();

            builder.comment("Shop System Settings")
                    .push("shop");

            SHOP_ENABLED = builder
                    .comment("Shop-System aktiviert")
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
                    .comment("Rating-System aktiviert")
                    .define("enabled", true);

            ALLOW_MULTIPLE_RATINGS = builder
                    .comment("Spieler können mehrfach bewerten")
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
                    .comment("Blocktypen, auf denen NPCs laufen dürfen (Beispiel: minecraft:stone, minecraft:grass_block)")
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

            builder.comment("Map Navigation System Settings - Straßen-Navigation auf der Karte")
                    .push("navigation");

            NAVIGATION_ROAD_BLOCKS = builder
                    .comment("Blocktypen, die als Straße für die Karten-Navigation erkannt werden")
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
                    .comment("Radius in Blöcken, in dem Straßen für die Navigation gescannt werden")
                    .defineInRange("scan_radius", 500, 100, 2000);

            NAVIGATION_PATH_UPDATE_INTERVAL = builder
                    .comment("Intervall in Millisekunden, wie oft der Pfad bei beweglichen Zielen aktualisiert wird")
                    .defineInRange("path_update_interval", 2000, 500, 10000);

            NAVIGATION_ARRIVAL_DISTANCE = builder
                    .comment("Distanz in Blöcken, ab der das Ziel als erreicht gilt")
                    .defineInRange("arrival_distance", 5.0, 1.0, 50.0);

            builder.pop();

            builder.comment("Police System Settings")
                    .push("police");

            POLICE_ARREST_COOLDOWN_SECONDS = builder
                    .comment("Cooldown in Sekunden, bevor Polizei einen Spieler verhaften kann")
                    .defineInRange("arrest_cooldown_seconds", 5, 1, 60);

            POLICE_DETECTION_RADIUS = builder
                    .comment("Erkennungsradius der Polizei in Blöcken")
                    .defineInRange("detection_radius", 32, 8, 128);

            POLICE_ARREST_DISTANCE = builder
                    .comment("Distanz in Blöcken, bei der Festnahme möglich ist")
                    .defineInRange("arrest_distance", 2.0, 1.0, 10.0);

            POLICE_SEARCH_DURATION_SECONDS = builder
                    .comment("Dauer in Sekunden, wie lange Polizei nach entkommenem Spieler sucht")
                    .defineInRange("search_duration_seconds", 60, 10, 300);

            POLICE_SEARCH_RADIUS = builder
                    .comment("Suchradius in Blöcken, in dem Polizei nach Spieler sucht")
                    .defineInRange("search_radius", 50, 10, 100);

            POLICE_SEARCH_TARGET_UPDATE_SECONDS = builder
                    .comment("Intervall in Sekunden, wie oft Polizei ein neues Suchziel wählt")
                    .defineInRange("search_target_update_seconds", 10, 5, 60);

            POLICE_BACKUP_SEARCH_RADIUS = builder
                    .comment("Suchradius für Backup-Polizei in Blöcken (Performance: kleiner = besser)")
                    .defineInRange("backup_search_radius", 50, 20, 100);

            POLICE_INDOOR_HIDING_ENABLED = builder
                    .comment("Aktiviert Versteck-System in Gebäuden (Spieler können sich vor Polizei verstecken)")
                    .define("indoor_hiding_enabled", true);

            POLICE_BLOCK_DOORS_DURING_PURSUIT = builder
                    .comment("Blockiert Türöffnen während aktiver Verfolgung")
                    .define("block_doors_during_pursuit", true);

            POLICE_RAID_SCAN_RADIUS = builder
                    .comment("Scan-Radius für illegale Items bei Verhaftung (in Blöcken)")
                    .defineInRange("raid_scan_radius", 20, 5, 50);

            POLICE_ILLEGAL_CASH_THRESHOLD = builder
                    .comment("Bargeld-Schwellenwert für illegales Bargeld (über diesem Wert ist es illegal)")
                    .defineInRange("illegal_cash_threshold", 10000.0, 1000.0, 100000.0);

            POLICE_RAID_ACCOUNT_PERCENTAGE = builder
                    .comment("Prozentsatz vom Kontostand für Geldstrafe (0.1 = 10%)")
                    .defineInRange("raid_account_percentage", 0.1, 0.01, 0.5);

            POLICE_RAID_MIN_FINE = builder
                    .comment("Mindest-Geldstrafe bei Raid in Euro")
                    .defineInRange("raid_min_fine", 1000.0, 100.0, 50000.0);

            POLICE_ROOM_SCAN_ENABLED = builder
                    .comment("Aktiviert intelligentes Raum-basiertes Scannen (true = nur gesehene Räume, false = kompletter Radius)")
                    .define("room_scan_enabled", true);

            POLICE_ROOM_SCAN_MAX_SIZE = builder
                    .comment("Maximale Raum-Größe in Blöcken (Sicherheits-Limit gegen Performance-Probleme)")
                    .defineInRange("room_scan_max_size", 500, 50, 2000);

            POLICE_ROOM_SCAN_MAX_DEPTH = builder
                    .comment("Maximale Y-Achsen Tiefe für Raum-Suche (verhindert vertikale Explosionen)")
                    .defineInRange("room_scan_max_depth", 50, 10, 100);

            POLICE_ROOM_SCAN_MAX_ADDITIONAL_ROOMS = builder
                    .comment("Maximale Anzahl zusätzlicher Räume, die durchsucht werden wenn Konterband gefunden wurde")
                    .defineInRange("room_scan_max_additional_rooms", 3, 0, 10);

            POLICE_STATION_WAIT_MINUTES = builder
                    .comment("Wartezeit in Minuten, die Polizisten an der Polizeistation bleiben")
                    .defineInRange("station_wait_minutes", 5, 1, 60);

            POLICE_STATION_RADIUS = builder
                    .comment("Radius in Blöcken, in dem sich Polizisten um die Station bewegen")
                    .defineInRange("station_radius", 10, 3, 50);

            POLICE_PATROL_WAIT_MINUTES = builder
                    .comment("Wartezeit in Minuten an jedem Patrouillenpunkt (nutze /time add zum Beschleunigen)")
                    .defineInRange("patrol_wait_minutes", 1, 1, 30);

            POLICE_PATROL_RADIUS = builder
                    .comment("Radius in Blöcken, in dem sich Polizisten um Patrouillenpunkte bewegen")
                    .defineInRange("patrol_radius", 3, 1, 20);

            builder.pop();

            builder.comment("Stealing Minigame Settings")
                    .push("stealing");

            STEALING_INDICATOR_SPEED = builder
                    .comment("Geschwindigkeit des roten Indikators (höher = schneller, Standard: 0.04)")
                    .defineInRange("indicator_speed", 0.04, 0.001, 0.2);

            STEALING_MAX_ATTEMPTS = builder
                    .comment("Maximale Anzahl der Versuche")
                    .defineInRange("max_attempts", 3, 1, 10);

            STEALING_MIN_ZONE_SIZE = builder
                    .comment("Minimale Größe der Erfolgszone (schwer, hoher Wert, 0.05 = 5%)")
                    .defineInRange("min_zone_size", 0.05, 0.01, 0.5);

            STEALING_MAX_ZONE_SIZE = builder
                    .comment("Maximale Größe der Erfolgszone (einfach, niedriger Wert, 0.15 = 15%)")
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
                    .comment("Standard-Lieferpreis für Items ohne spezifischen Preis")
                    .defineInRange("default_delivery_price", 5, 1, 10000);

            builder.pop();

            builder.comment("Garage System Settings")
                    .push("garage");

            GARAGE_BASE_INSPECTION_FEE = builder
                    .comment("Basis-Inspektionsgebühr in Euro (wird immer berechnet)")
                    .defineInRange("base_inspection_fee", 25.0, 0.0, 1000.0);

            GARAGE_REPAIR_COST_PER_PERCENT = builder
                    .comment("Reparaturkosten pro Prozent Schaden in Euro")
                    .defineInRange("repair_cost_per_percent", 2.0, 0.1, 100.0);

            GARAGE_BATTERY_COST_PER_PERCENT = builder
                    .comment("Batterieladungskosten pro Prozent in Euro")
                    .defineInRange("battery_cost_per_percent", 0.5, 0.1, 50.0);

            GARAGE_OIL_CHANGE_COST = builder
                    .comment("Ölwechsel-Kosten in Euro")
                    .defineInRange("oil_change_cost", 15.0, 1.0, 500.0);

            GARAGE_MOTOR_UPGRADE_COST_LVL2 = builder
                    .comment("Kosten für Motor-Upgrade Level 2 (Normal -> Performance)")
                    .defineInRange("motor_upgrade_cost_lvl2", 500.0, 10.0, 10000.0);

            GARAGE_MOTOR_UPGRADE_COST_LVL3 = builder
                    .comment("Kosten für Motor-Upgrade Level 3 (Performance -> Industrial)")
                    .defineInRange("motor_upgrade_cost_lvl3", 1000.0, 10.0, 20000.0);

            GARAGE_TANK_UPGRADE_COST_LVL2 = builder
                    .comment("Kosten für Tank-Upgrade Level 2 (15L -> 30L)")
                    .defineInRange("tank_upgrade_cost_lvl2", 200.0, 10.0, 5000.0);

            GARAGE_TANK_UPGRADE_COST_LVL3 = builder
                    .comment("Kosten für Tank-Upgrade Level 3 (30L -> 50L)")
                    .defineInRange("tank_upgrade_cost_lvl3", 400.0, 10.0, 10000.0);

            GARAGE_TIRE_UPGRADE_COST = builder
                    .comment("Kosten für Reifen-Upgrade (pro Level)")
                    .defineInRange("tire_upgrade_cost", 150.0, 10.0, 5000.0);

            GARAGE_PAINT_CHANGE_COST = builder
                    .comment("Kosten für Lackierungswechsel")
                    .defineInRange("paint_change_cost", 100.0, 10.0, 5000.0);

            GARAGE_FENDER_UPGRADE_COST_LVL2 = builder
                    .comment("Kosten für Fender-Upgrade Level 2 (Basic -> Chrome)")
                    .defineInRange("fender_upgrade_cost_lvl2", 250.0, 10.0, 5000.0);

            GARAGE_FENDER_UPGRADE_COST_LVL3 = builder
                    .comment("Kosten für Fender-Upgrade Level 3 (Chrome -> Sport)")
                    .defineInRange("fender_upgrade_cost_lvl3", 500.0, 10.0, 10000.0);

            builder.pop();

            builder.comment("Bank System Settings")
                    .push("bank");

            BANK_DEPOSIT_LIMIT = builder
                    .comment("Maximaler Einzahlungsbetrag pro Transaktion")
                    .defineInRange("deposit_limit", 9999.0, 100.0, 1000000.0);

            BANK_TRANSFER_DAILY_LIMIT = builder
                    .comment("Maximaler Überweisungsbetrag pro Tag")
                    .defineInRange("transfer_daily_limit", 999.0, 10.0, 100000.0);

            builder.pop();

            builder.comment("Stock Market Settings (Börse)")
                    .push("stock_market");

            STOCK_GOLD_BASE_PRICE = builder
                    .comment("Basispreis für Goldbarren")
                    .defineInRange("gold_base_price", 250.0, 10.0, 10000.0);

            STOCK_DIAMOND_BASE_PRICE = builder
                    .comment("Basispreis für Diamanten")
                    .defineInRange("diamond_base_price", 450.0, 10.0, 10000.0);

            STOCK_EMERALD_BASE_PRICE = builder
                    .comment("Basispreis für Smaragde")
                    .defineInRange("emerald_base_price", 180.0, 10.0, 10000.0);

            STOCK_MAX_PRICE_CHANGE_PERCENT = builder
                    .comment("Maximale Preisänderung pro Tag in Prozent (0.10 = 10%)")
                    .defineInRange("max_price_change_percent", 0.10, 0.01, 0.50);

            builder.pop();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CONFIG VALIDATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Validates all config values for logical consistency and safety.
     * Should be called after config load to prevent invalid game states.
     *
     * @throws IllegalStateException if critical config values are invalid
     */
    public static void validateConfig() {
        com.mojang.logging.LogUtils.getLogger().info("Validating ScheduleMC configuration...");

        // Economy Validation
        validateRange("START_BALANCE", COMMON.START_BALANCE.get(), 0.0, 1_000_000_000.0);
        validateRange("SAVINGS_INTEREST_RATE", COMMON.SAVINGS_INTEREST_RATE.get(), 0.0, 100.0);
        validateRange("OVERDRAFT_INTEREST_RATE", COMMON.OVERDRAFT_INTEREST_RATE.get(), 0.0, 100.0);
        validateRange("TAX_SALES_RATE", COMMON.TAX_SALES_RATE.get(), 0.0, 100.0);
        validatePositive("SAVINGS_MAX_PER_PLAYER", COMMON.SAVINGS_MAX_PER_PLAYER.get());
        validatePositive("OVERDRAFT_MAX_LIMIT", COMMON.OVERDRAFT_MAX_LIMIT.get());

        // Plot Validation
        validateRange("MIN_PLOT_SIZE", COMMON.MIN_PLOT_SIZE.get(), 1L, 1_000_000L);
        validateRange("MAX_PLOT_SIZE", COMMON.MAX_PLOT_SIZE.get(), COMMON.MIN_PLOT_SIZE.get(), 10_000_000L);
        validatePositive("MIN_PLOT_PRICE", COMMON.MIN_PLOT_PRICE.get());
        validatePositive("MAX_PLOT_PRICE", COMMON.MAX_PLOT_PRICE.get());
        validateRange("REFUND_ON_ABANDON", COMMON.REFUND_ON_ABANDON.get(), 0.0, 1.0);

        if (COMMON.MAX_PLOT_PRICE.get() < COMMON.MIN_PLOT_PRICE.get()) {
            throw new IllegalStateException("MAX_PLOT_PRICE must be >= MIN_PLOT_PRICE");
        }

        // Daily Reward Validation
        validatePositive("DAILY_REWARD", COMMON.DAILY_REWARD.get());
        validatePositive("DAILY_REWARD_STREAK_BONUS", COMMON.DAILY_REWARD_STREAK_BONUS.get());
        validateRange("MAX_STREAK_DAYS", COMMON.MAX_STREAK_DAYS.get(), 1, 365);

        // Rent Validation
        validatePositive("MIN_RENT_PRICE", COMMON.MIN_RENT_PRICE.get());
        validateRange("MIN_RENT_DAYS", COMMON.MIN_RENT_DAYS.get(), 1, 365);
        validateRange("MAX_RENT_DAYS", COMMON.MAX_RENT_DAYS.get(), COMMON.MIN_RENT_DAYS.get(), 3650);

        // Shop Validation
        validateRange("BUY_MULTIPLIER", COMMON.BUY_MULTIPLIER.get(), 0.01, 100.0);
        validateRange("SELL_MULTIPLIER", COMMON.SELL_MULTIPLIER.get(), 0.01, 100.0);

        // Rating Validation
        validateRange("MIN_RATING", COMMON.MIN_RATING.get(), -10, 10);
        validateRange("MAX_RATING", COMMON.MAX_RATING.get(), COMMON.MIN_RATING.get(), 10);

        // NPC Validation
        validateRange("NPC_SALARY", COMMON.NPC_SALARY.get(), 0.0, 1_000_000.0);
        validateRange("NPC_WANTED_DECAY", COMMON.NPC_WANTED_DECAY.get(), 0.0, 5.0);
        validateRange("MAX_WANTED_LEVEL", COMMON.MAX_WANTED_LEVEL.get(), 1, 10);

        // Warehouse Validation
        validateRange("WAREHOUSE_DELIVERY_INTERVAL", COMMON.WAREHOUSE_DELIVERY_INTERVAL.get(), 1, 365);
        validateRange("WAREHOUSE_MAX_CAPACITY", COMMON.WAREHOUSE_MAX_CAPACITY.get(), 1, 100000);

        // Vehicle Validation
        if (VEHICLE_SERVER != null) {
            validatePositive("FUEL_CAPACITY", VEHICLE_SERVER.FUEL_CAPACITY.get());
            validateRange("FUEL_CONSUMPTION_RATE", VEHICLE_SERVER.FUEL_CONSUMPTION_RATE.get(), 0.0001, 1.0);
        }

        // Tobacco/Production Validation
        if (TOBACCO != null) {
            validateRange("TOBACCO_GROWTH_STAGES", TOBACCO.GROWTH_STAGES.get(), 1, 20);
            validatePositive("TOBACCO_GROWTH_SPEED", TOBACCO.GROWTH_SPEED.get());
        }

        com.mojang.logging.LogUtils.getLogger().info("✓ Configuration validation passed!");
    }

    /**
     * Validates that a value is within a specified range (inclusive).
     */
    private static void validateRange(String name, double value, double min, double max) {
        if (value < min || value > max) {
            throw new IllegalStateException(String.format(
                "Config '%s' has invalid value %.2f (must be between %.2f and %.2f)",
                name, value, min, max
            ));
        }
    }

    /**
     * Validates that a value is within a specified range (inclusive).
     */
    private static void validateRange(String name, long value, long min, long max) {
        if (value < min || value > max) {
            throw new IllegalStateException(String.format(
                "Config '%s' has invalid value %d (must be between %d and %d)",
                name, value, min, max
            ));
        }
    }

    /**
     * Validates that a value is within a specified range (inclusive).
     */
    private static void validateRange(String name, int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalStateException(String.format(
                "Config '%s' has invalid value %d (must be between %d and %d)",
                name, value, min, max
            ));
        }
    }

    /**
     * Validates that a value is positive (> 0).
     */
    private static void validatePositive(String name, double value) {
        if (value <= 0) {
            throw new IllegalStateException(String.format(
                "Config '%s' must be positive (> 0), but is %.2f",
                name, value
            ));
        }
    }
}
