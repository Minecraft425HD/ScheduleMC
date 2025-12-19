package de.rolandsw.schedulemc.cannabis.data;

import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.CannabisStrain;

/**
 * Speichert Daten einer Cannabis-Pflanze
 */
public class CannabisPlantData {

    private CannabisStrain strain;
    private CannabisQuality quality;
    private int growthStage; // 0-7 (7 = ausgewachsen)
    private int ticksGrown; // Wie lange die Pflanze bereits wächst
    private boolean hasFertilizer; // Dünger angewendet
    private boolean hasGrowthBooster; // Wachstumsbeschleuniger angewendet
    private boolean hasQualityBooster; // Qualitätsverbesserer angewendet

    public CannabisPlantData(CannabisStrain strain) {
        this.strain = strain;
        this.quality = CannabisQuality.DANK; // Standard-Qualität
        this.growthStage = 0;
        this.ticksGrown = 0;
        this.hasFertilizer = false;
        this.hasGrowthBooster = false;
        this.hasQualityBooster = false;
    }

    public CannabisStrain getStrain() {
        return strain;
    }

    public CannabisQuality getQuality() {
        return quality;
    }

    public void setQuality(CannabisQuality quality) {
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
            this.quality = (CannabisQuality) quality.downgrade();
        }
    }

    public boolean hasGrowthBooster() {
        return hasGrowthBooster;
    }

    public void applyGrowthBooster() {
        if (!hasGrowthBooster) {
            this.hasGrowthBooster = true;
            // Qualität verschlechtern
            this.quality = (CannabisQuality) quality.downgrade();
        }
    }

    public boolean hasQualityBooster() {
        return hasQualityBooster;
    }

    public void applyQualityBooster() {
        if (!hasQualityBooster) {
            this.hasQualityBooster = true;
            // Qualität verbessern
            this.quality = (CannabisQuality) quality.upgrade();
        }
    }

    /**
     * Berechnet Wachstumsgeschwindigkeit
     */
    public int getGrowthSpeed() {
        int baseSpeed = strain.getGrowthTicks();
        if (hasGrowthBooster) {
            return baseSpeed / 2; // Doppelte Geschwindigkeit
        }
        return baseSpeed;
    }

    /**
     * Berechnet Ertrag beim Ernten
     */
    public int getHarvestYield() {
        int baseYield = strain.getBaseYield();
        if (hasFertilizer) {
            return Math.min(10, (int) Math.ceil(baseYield * 1.67)); // Max 10 Gramm mit Fertilizer
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
