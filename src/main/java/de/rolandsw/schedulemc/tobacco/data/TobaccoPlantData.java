package de.rolandsw.schedulemc.tobacco.data;

import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;

/**
 * Speichert Daten einer Tabakpflanze
 */
public class TobaccoPlantData {
    
    private TobaccoType type;
    private TobaccoQuality quality;
    private int growthStage; // 0-7 (7 = ausgewachsen)
    private int ticksGrown; // Wie lange die Pflanze bereits wächst
    private boolean hasFertilizer; // Dünger angewendet
    private boolean hasGrowthBooster; // Wachstumsbeschleuniger angewendet
    private boolean hasQualityBooster; // Qualitätsverbesserer angewendet
    
    public TobaccoPlantData(TobaccoType type) {
        this.type = type;
        this.quality = TobaccoQuality.GUT; // Standard-Qualität
        this.growthStage = 0;
        this.ticksGrown = 0;
        this.hasFertilizer = false;
        this.hasGrowthBooster = false;
        this.hasQualityBooster = false;
    }
    
    public TobaccoType getType() {
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
        this.growthStage = Math.max(0, Math.min(7, stage));
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
        if (!hasFertilizer) {
            this.hasFertilizer = true;
            // Qualität verschlechtern
            this.quality = quality.downgrade();
        }
    }
    
    public boolean hasGrowthBooster() {
        return hasGrowthBooster;
    }
    
    public void applyGrowthBooster() {
        if (!hasGrowthBooster) {
            this.hasGrowthBooster = true;
            // Qualität verschlechtern
            this.quality = quality.downgrade();
        }
    }
    
    public boolean hasQualityBooster() {
        return hasQualityBooster;
    }
    
    public void applyQualityBooster() {
        if (!hasQualityBooster) {
            this.hasQualityBooster = true;
            // Qualität verbessern
            this.quality = quality.upgrade();
        }
    }
    
    /**
     * Berechnet Wachstumsgeschwindigkeit
     */
    public int getGrowthSpeed() {
        int baseSpeed = type.getGrowthTicks();
        if (hasGrowthBooster) {
            return baseSpeed / 2; // Doppelte Geschwindigkeit
        }
        return baseSpeed;
    }
    
    /**
     * Berechnet Ertrag beim Ernten
     */
    public int getHarvestYield() {
        int baseYield = type.getBaseYield();
        if (hasFertilizer) {
            return (int) (baseYield * 1.5); // 50% mehr Ertrag
        }
        return baseYield;
    }
    
    /**
     * Aktualisiert Wachstum
     */
    public void tick() {
        if (isFullyGrown()) return;

        incrementTicks();

        int requiredTicks = getGrowthSpeed();
        int ticksPerStage = requiredTicks / 8; // 8 Wachstumsstufen (0-7)

        int newStage = Math.min(7, ticksGrown / ticksPerStage);
        setGrowthStage(newStage);
    }
}
