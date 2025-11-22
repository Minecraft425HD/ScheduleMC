package de.rolandsw.schedulemc.config;

import de.rolandsw.schedulemc.tobacco.config.TobaccoConfig;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * ScheduleMC 3.0 - Vollständige Konfiguration inkl. Tabak-System
 */
public class ModConfigHandler {

    public static final ForgeConfigSpec SPEC;
    public static final Common COMMON;
    public static final TobaccoConfig TOBACCO;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        TOBACCO = new TobaccoConfig(builder);
        SPEC = builder.build();
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
        }
    }
}
