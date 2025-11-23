package de.rolandsw.schedulemc.tobacco.config;

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
        
        // Topf-Kapazitäten
        builder.comment("Pot Capacities")
                .push("pot_capacities");
        
        TERRACOTTA_WATER_CAPACITY = builder
                .defineInRange("terracotta_water", 100, 10, 10000);
        CERAMIC_WATER_CAPACITY = builder
                .defineInRange("ceramic_water", 200, 10, 10000);
        IRON_WATER_CAPACITY = builder
                .defineInRange("iron_water", 400, 10, 10000);
        GOLDEN_WATER_CAPACITY = builder
                .defineInRange("golden_water", 800, 10, 10000);
        
        TERRACOTTA_SOIL_CAPACITY = builder
                .defineInRange("terracotta_soil", 50, 10, 10000);
        CERAMIC_SOIL_CAPACITY = builder
                .defineInRange("ceramic_soil", 100, 10, 10000);
        IRON_SOIL_CAPACITY = builder
                .defineInRange("iron_soil", 200, 10, 10000);
        GOLDEN_SOIL_CAPACITY = builder
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
    }
}
