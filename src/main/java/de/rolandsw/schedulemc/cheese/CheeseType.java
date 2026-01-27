package de.rolandsw.schedulemc.cheese;

import de.rolandsw.schedulemc.production.core.ProductionType;

/**
 * Kasetypen mit verschiedenen Eigenschaften
 *
 * Eigenschaften:
 * - Name und Color Code (fur Display)
 * - Basis-Preis pro Kilogramm
 * - Reifungszeit in Tagen (Minecraft-Tage)
 * - Qualitatsfaktor (Chance auf hohere Qualitat)
 */
public enum CheeseType implements ProductionType {
    GOUDA("Gouda", "§e", 15.0, 30, 1.0),
    EMMENTAL("Emmental", "§6", 22.0, 35, 1.1),
    CAMEMBERT("Camembert", "§f", 28.0, 25, 1.2),
    PARMESAN("Parmesan", "§c", 35.0, 40, 1.3);

    private final String displayName;
    private final String colorCode;
    private final double basePricePerKg;
    private final int agingTimeDays;
    private final double qualityFactor;

    CheeseType(String displayName, String colorCode, double basePricePerKg, int agingTimeDays, double qualityFactor) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.basePricePerKg = basePricePerKg;
        this.agingTimeDays = agingTimeDays;
        this.qualityFactor = qualityFactor;
    }

    @Override
    public String getDisplayName() {
        return colorCode + displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getBasePricePerKg() {
        return basePricePerKg;
    }

    public int getAgingTimeDays() {
        return agingTimeDays;
    }

    public double getQualityFactor() {
        return qualityFactor;
    }

    @Override
    public double calculatePrice(de.rolandsw.schedulemc.production.core.ProductionQuality quality, int amount) {
        return basePricePerKg * quality.getPriceMultiplier() * amount;
    }

    @Override
    public int getBaseYield() {
        return 1;  // Default yield for cheese production
    }

    @Override
    public int getGrowthTicks() {
        return agingTimeDays * 24000;  // Convert days to ticks (1 day = 24000 ticks)
    }

    @Override
    public double getBasePrice() {
        return basePricePerKg;
    }
}
