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
    public static final ServerConfig CAR_SERVER;
    public static final ClientConfig CAR_CLIENT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        TOBACCO = new TobaccoConfig(builder);
        CAR_SERVER = new ServerConfig(builder);
        SPEC = builder.build();

        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        CAR_CLIENT = new ClientConfig(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();
    }

    public static class Common {
        
        // ═══════════════════════════════════════════════════════════
        // ECONOMY
        // ═══════════════════════════════════════════════════════════
        public final ForgeConfigSpec.DoubleValue START_BALANCE;
        public final ForgeConfigSpec.IntValue SAVE_INTERVAL_MINUTES;
        
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
        }
    }
}
