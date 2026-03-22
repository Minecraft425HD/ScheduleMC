package de.rolandsw.schedulemc.poppy.data;

import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Speichert Daten einer Mohn-Pflanze
 */
public class PoppyPlantData {

    private final PoppyType type;
    private TobaccoQuality quality;
    private int growthStage; // 0-7 (0 = Samen, 7 = Erntereif)
    private int ticksGrown;
    private boolean hasFertilizer;
    private boolean hasGrowthBooster;
    private boolean hasQualityBooster;

    public PoppyPlantData(PoppyType type) {
        this.type = type;
        this.quality = TobaccoQuality.SCHLECHT;
        this.growthStage = 0;
        this.ticksGrown = 0;
        this.hasFertilizer = false;
        this.hasGrowthBooster = false;
        this.hasQualityBooster = false;
    }

    public PoppyType getType() {
        return type;
    }

    public TobaccoQuality getQuality() {
        return quality;
    }

    public void setQuality(TobaccoQuality quality) {
        this.quality = quality;
    }

    public int getGrowthStage() {
        return growthStage;
    }

    public void setGrowthStage(int stage) {
        this.growthStage = Math.min(7, Math.max(0, stage));
    }

    public int getTicksGrown() {
        return ticksGrown;
    }

    public void incrementTicks() {
        this.ticksGrown++;
    }

    public boolean isFullyGrown() {
        return growthStage >= 7;
    }

    public boolean hasFertilizer() {
        return hasFertilizer;
    }

    public void applyFertilizer() {
        this.hasFertilizer = true;
    }

    public boolean hasGrowthBooster() {
        return hasGrowthBooster;
    }

    public void applyGrowthBooster() {
        this.hasGrowthBooster = true;
    }

    public boolean hasQualityBooster() {
        return hasQualityBooster;
    }

    public void applyQualityBooster() {
        this.hasQualityBooster = true;
    }

    /**
     * Wachstums-Tick - prüft ob ein neuer Wachstumsschritt erreicht wurde
     */
    public void tick() {
        if (isFullyGrown()) return;

        ticksGrown++;

        // Berechne benötigte Ticks für nächste Stufe
        int ticksPerStage = type.getGrowthTicks();
        if (hasGrowthBooster) {
            ticksPerStage = (int) (ticksPerStage * 0.7); // 30% schneller
        }

        int targetTicks = (growthStage + 1) * ticksPerStage;

        if (ticksGrown >= targetTicks) {
            growthStage++;

            // Qualitätsverbesserung bei bestimmten Stufen
            if (growthStage == 3 || growthStage == 5 || growthStage == 7) {
                tryUpgradeQuality();
            }
        }
    }

    private void tryUpgradeQuality() {
        if (quality == TobaccoQuality.LEGENDAER) return;

        double upgradeChance = 0.25; // 25% Basis-Chance
        if (hasFertilizer) upgradeChance += 0.15;
        if (hasQualityBooster) upgradeChance += 0.20;

        // Afghanisch hat höhere Qualitäts-Chance
        upgradeChance *= type.getPotencyMultiplier();

        if (ThreadLocalRandom.current().nextDouble() < upgradeChance) {
            quality = quality.upgrade();
        }
    }

    /**
     * Berechnet Ernteertrag basierend auf Qualität und Typ
     */
    public int getHarvestYield() {
        int baseYield = type.getBaseYield();
        double qualityMultiplier = switch (quality) {
            case SCHLECHT -> 0.7;
            case GUT -> 1.0;
            case SEHR_GUT -> 1.3;
            case LEGENDAER -> 1.6;
        };

        if (hasFertilizer) {
            qualityMultiplier += 0.67; // Angepasst für max 10 Gramm
        }

        return Math.min(10, (int) Math.ceil(baseYield * qualityMultiplier));
    }
}
