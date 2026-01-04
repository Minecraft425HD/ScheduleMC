package de.rolandsw.schedulemc.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Konfiguration für das Tabak-System
 */
public class TobaccoConfig {
    
    public final ForgeConfigSpec.BooleanValue TOBACCO_ENABLED;
    public final ForgeConfigSpec.DoubleValue TOBACCO_GROWTH_SPEED_MULTIPLIER;
    public final ForgeConfigSpec.IntValue TOBACCO_DRYING_TIME;
    public final ForgeConfigSpec.IntValue TOBACCO_FERMENTING_TIME;
    public final ForgeConfigSpec.DoubleValue FERMENTATION_QUALITY_CHANCE;

    // Trocknungsgestell-Kapazitäten
    public final ForgeConfigSpec.IntValue SMALL_DRYING_RACK_CAPACITY;
    public final ForgeConfigSpec.IntValue MEDIUM_DRYING_RACK_CAPACITY;
    public final ForgeConfigSpec.IntValue BIG_DRYING_RACK_CAPACITY;

    // Fermentierungsfass-Kapazitäten
    public final ForgeConfigSpec.IntValue SMALL_FERMENTATION_BARREL_CAPACITY;
    public final ForgeConfigSpec.IntValue MEDIUM_FERMENTATION_BARREL_CAPACITY;
    public final ForgeConfigSpec.IntValue BIG_FERMENTATION_BARREL_CAPACITY;

    // Topf-Kapazitäten
    public final ForgeConfigSpec.IntValue TERRACOTTA_WATER_CAPACITY;
    public final ForgeConfigSpec.IntValue CERAMIC_WATER_CAPACITY;
    public final ForgeConfigSpec.IntValue IRON_WATER_CAPACITY;
    public final ForgeConfigSpec.IntValue GOLDEN_WATER_CAPACITY;
    
    public final ForgeConfigSpec.IntValue TERRACOTTA_SOIL_CAPACITY;
    public final ForgeConfigSpec.IntValue CERAMIC_SOIL_CAPACITY;
    public final ForgeConfigSpec.IntValue IRON_SOIL_CAPACITY;
    public final ForgeConfigSpec.IntValue GOLDEN_SOIL_CAPACITY;
    
    // Flaschen-Effekte
    public final ForgeConfigSpec.DoubleValue FERTILIZER_YIELD_BONUS;
    public final ForgeConfigSpec.DoubleValue GROWTH_BOOSTER_SPEED_MULTIPLIER;

    // Grow Lights
    public final ForgeConfigSpec.BooleanValue REQUIRE_LIGHT_FOR_GROWTH;
    public final ForgeConfigSpec.IntValue MIN_LIGHT_LEVEL;
    public final ForgeConfigSpec.IntValue BASIC_GROW_LIGHT_LEVEL;
    public final ForgeConfigSpec.DoubleValue BASIC_GROW_LIGHT_SPEED;
    public final ForgeConfigSpec.IntValue ADVANCED_GROW_LIGHT_LEVEL;
    public final ForgeConfigSpec.DoubleValue ADVANCED_GROW_LIGHT_SPEED;
    public final ForgeConfigSpec.IntValue PREMIUM_GROW_LIGHT_LEVEL;
    public final ForgeConfigSpec.DoubleValue PREMIUM_GROW_LIGHT_SPEED;
    public final ForgeConfigSpec.DoubleValue PREMIUM_GROW_LIGHT_QUALITY_BONUS;
    
    public TobaccoConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Tobacco System Settings")
                .push("tobacco");
        
        TOBACCO_ENABLED = builder
                .comment("Aktiviert das Tabak-Anbau-System")
                .define("enabled", true);
        
        TOBACCO_GROWTH_SPEED_MULTIPLIER = builder
                .comment("Wachstumsgeschwindigkeit (1.0 = normal, 2.0 = doppelt so schnell)")
                .defineInRange("growth_speed_multiplier", 1.0, 0.1, 10.0);
        
        TOBACCO_DRYING_TIME = builder
                .comment("Trocknungszeit in Ticks (6000 = 5 Minuten)")
                .defineInRange("drying_time", 6000, 100, 72000);
        
        TOBACCO_FERMENTING_TIME = builder
                .comment("Fermentierungszeit in Ticks (12000 = 10 Minuten)")
                .defineInRange("fermenting_time", 12000, 100, 72000);
        
        FERMENTATION_QUALITY_CHANCE = builder
                .comment("Chance auf Qualitätsverbesserung bei Fermentierung (0.3 = 30%)")
                .defineInRange("fermentation_quality_chance", 0.3, 0.0, 1.0);

        builder.pop();

        // Trocknungsgestell-Kapazitäten
        builder.comment("Drying Rack Capacities")
                .push("drying_rack_capacities");

        SMALL_DRYING_RACK_CAPACITY = builder
                .comment("Kapazität des kleinen Trocknungsgestells")
                .defineInRange("small_capacity", 6, 1, 64);

        MEDIUM_DRYING_RACK_CAPACITY = builder
                .comment("Kapazität des mittleren Trocknungsgestells")
                .defineInRange("medium_capacity", 8, 1, 64);

        BIG_DRYING_RACK_CAPACITY = builder
                .comment("Kapazität des großen Trocknungsgestells")
                .defineInRange("big_capacity", 10, 1, 64);

        builder.pop();

        // Fermentierungsfass-Kapazitäten
        builder.comment("Fermentation Barrel Capacities")
                .push("fermentation_barrel_capacities");

        SMALL_FERMENTATION_BARREL_CAPACITY = builder
                .comment("Kapazität des kleinen Fermentierungsfasses")
                .defineInRange("small_capacity", 6, 1, 64);

        MEDIUM_FERMENTATION_BARREL_CAPACITY = builder
                .comment("Kapazität des mittleren Fermentierungsfasses")
                .defineInRange("medium_capacity", 8, 1, 64);

        BIG_FERMENTATION_BARREL_CAPACITY = builder
                .comment("Kapazität des großen Fermentierungsfasses")
                .defineInRange("big_capacity", 10, 1, 64);

        builder.pop();
        
        // Topf-Kapazitäten
        builder.comment("Pot Capacities")
                .push("pot_capacities");
        
        TERRACOTTA_WATER_CAPACITY = builder
                .comment("Water capacity for terracotta pot (in mb)")
                .defineInRange("terracotta_water", 100, 10, 10000);
        CERAMIC_WATER_CAPACITY = builder
                .comment("Water capacity for ceramic pot (in mb)")
                .defineInRange("ceramic_water", 200, 10, 10000);
        IRON_WATER_CAPACITY = builder
                .comment("Water capacity for iron pot (in mb)")
                .defineInRange("iron_water", 400, 10, 10000);
        GOLDEN_WATER_CAPACITY = builder
                .comment("Water capacity for golden pot (in mb)")
                .defineInRange("golden_water", 800, 10, 10000);

        TERRACOTTA_SOIL_CAPACITY = builder
                .comment("Soil capacity for terracotta pot (in units)")
                .defineInRange("terracotta_soil", 50, 10, 10000);
        CERAMIC_SOIL_CAPACITY = builder
                .comment("Soil capacity for ceramic pot (in units)")
                .defineInRange("ceramic_soil", 100, 10, 10000);
        IRON_SOIL_CAPACITY = builder
                .comment("Soil capacity for iron pot (in units)")
                .defineInRange("iron_soil", 200, 10, 10000);
        GOLDEN_SOIL_CAPACITY = builder
                .comment("Soil capacity for golden pot (in units)")
                .defineInRange("golden_soil", 400, 10, 10000);
        
        builder.pop();
        
        // Flaschen-Effekte
        builder.comment("Bottle Effects")
                .push("bottle_effects");
        
        FERTILIZER_YIELD_BONUS = builder
                .comment("Dünger Ertrags-Bonus (0.5 = +50%)")
                .defineInRange("fertilizer_yield_bonus", 0.5, 0.0, 5.0);
        
        GROWTH_BOOSTER_SPEED_MULTIPLIER = builder
                .comment("Wachstumsbeschleuniger Multiplikator (2.0 = doppelt so schnell)")
                .defineInRange("growth_booster_speed", 2.0, 1.0, 10.0);

        builder.pop();

        // Grow Lights
        builder.comment("Grow Light System")
                .push("grow_lights");

        REQUIRE_LIGHT_FOR_GROWTH = builder
                .comment("Pflanzen benötigen Licht zum Wachsen")
                .define("require_light", true);

        MIN_LIGHT_LEVEL = builder
                .comment("Minimales Lichtlevel für Wachstum (9 = Fackeln ausreichend)")
                .defineInRange("min_light_level", 9, 0, 15);

        BASIC_GROW_LIGHT_LEVEL = builder
                .comment("Lichtlevel der Basic Grow Light Slab")
                .defineInRange("basic_light_level", 12, 0, 15);

        BASIC_GROW_LIGHT_SPEED = builder
                .comment("Wachstumsgeschwindigkeit unter Basic Grow Light (1.0 = normal)")
                .defineInRange("basic_speed", 1.0, 0.1, 10.0);

        ADVANCED_GROW_LIGHT_LEVEL = builder
                .comment("Lichtlevel der Advanced Grow Light Slab")
                .defineInRange("advanced_light_level", 14, 0, 15);

        ADVANCED_GROW_LIGHT_SPEED = builder
                .comment("Wachstumsgeschwindigkeit unter Advanced Grow Light (1.25 = 25% schneller)")
                .defineInRange("advanced_speed", 1.25, 0.1, 10.0);

        PREMIUM_GROW_LIGHT_LEVEL = builder
                .comment("Lichtlevel der Premium UV Grow Light Slab")
                .defineInRange("premium_light_level", 15, 0, 15);

        PREMIUM_GROW_LIGHT_SPEED = builder
                .comment("Wachstumsgeschwindigkeit unter Premium UV Grow Light (1.5 = 50% schneller)")
                .defineInRange("premium_speed", 1.5, 0.1, 10.0);

        PREMIUM_GROW_LIGHT_QUALITY_BONUS = builder
                .comment("Qualitätsbonus unter Premium UV Grow Light (0.1 = +10% Chance)")
                .defineInRange("premium_quality_bonus", 0.1, 0.0, 1.0);

        builder.pop();
    }
}
