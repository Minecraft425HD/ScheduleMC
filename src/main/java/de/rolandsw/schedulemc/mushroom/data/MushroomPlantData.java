package de.rolandsw.schedulemc.mushroom.data;

import de.rolandsw.schedulemc.util.SecureRandomUtil;

import de.rolandsw.schedulemc.mushroom.MushroomType;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;

/**
 * Speichert Daten über eine wachsende Pilzkultur
 */
public class MushroomPlantData {
    private final MushroomType type;
    private int growthStage; // 0-7
    private int ticksGrown;
    private TobaccoQuality quality;
    private boolean hasFertilizer;
    private boolean hasGrowthBooster;
    private boolean hasQualityBooster;
    private int flushCount; // Anzahl der Ernten (max 4)
    private int currentFlush; // Aktuelle Ernte-Welle

    public MushroomPlantData(MushroomType type) {
        this.type = type;
        this.growthStage = 0;
        this.ticksGrown = 0;
        this.quality = TobaccoQuality.GUT;
        this.hasFertilizer = false;
        this.hasGrowthBooster = false;
        this.hasQualityBooster = false;
        this.flushCount = type.getMaxFlushes();
        this.currentFlush = 0;
    }

    /**
     * Prüft ob Inkubations-Phase (Stage 0-3)
     */
    public boolean isIncubating() {
        return growthStage < 4;
    }

    /**
     * Prüft ob Fruchtungs-Phase (Stage 4-7)
     */
    public boolean isFruiting() {
        return growthStage >= 4 && growthStage < 8;
    }

    /**
     * Prüft ob das Lichtlevel passend ist
     */
    public boolean isLightLevelValid(int lightLevel) {
        if (isIncubating()) {
            return lightLevel < type.getMaxLightIncubation();
        } else {
            return lightLevel < type.getMaxLightFruiting();
        }
    }

    /**
     * Gibt benötigtes maximales Lichtlevel zurück
     */
    public int getRequiredMaxLight() {
        return isIncubating() ? type.getMaxLightIncubation() : type.getMaxLightFruiting();
    }

    /**
     * Prüft ob in dieser Phase Wasser benötigt wird
     */
    public boolean needsWater() {
        return isFruiting(); // Nur während Fruchtung
    }

    public void tick() {
        if (isFullyGrown()) return;

        ticksGrown++;

        int ticksPerStage = type.getGrowthTicks() / 8;
        if (hasGrowthBooster) {
            ticksPerStage = (int) (ticksPerStage * 0.7);
        }

        if (ticksGrown >= ticksPerStage) {
            ticksGrown = 0;
            growthStage++;

            // Qualitätsverbesserung bei Wachstum
            if (hasQualityBooster && SecureRandomUtil.chance(0.15)) {
                upgradeQuality();
            }
        }
    }

    public boolean isFullyGrown() {
        return growthStage >= 7;
    }

    public boolean canHarvest() {
        return isFullyGrown() && currentFlush < flushCount;
    }

    /**
     * Erntet die Pilze und startet nächsten Flush
     * @return true wenn noch weitere Flushes möglich
     */
    public boolean harvest() {
        if (!canHarvest()) return false;

        currentFlush++;

        // Nach Ernte: Pflanze wächst wieder (wenn noch Flushes übrig)
        if (currentFlush < flushCount) {
            growthStage = 3; // Startet bei Stage 3 für schnellere Folge-Ernten
            ticksGrown = 0;
            return true;
        }

        return false; // Substrat erschöpft
    }

    public int getHarvestYield() {
        int baseYield = type.getBaseYield();

        // Qualitätsbonus
        baseYield = (int) (baseYield * quality.getYieldMultiplier());

        // Dünger-Bonus (angepasst für max 10 Gramm)
        if (hasFertilizer) {
            baseYield = (int) (baseYield * 1.67);
        }

        // Flush-Reduktion (spätere Flushes geben weniger)
        double flushMultiplier = 1.0 - (currentFlush * 0.15); // -15% pro Flush
        baseYield = (int) (baseYield * flushMultiplier);

        // Zufällige Variation
        int variation = (int) (baseYield * 0.2);
        baseYield += SecureRandomUtil.nextInt(variation * 2 + 1) - variation;

        return Math.min(10, Math.max(1, baseYield));
    }

    private void upgradeQuality() {
        switch (quality) {
            case SCHLECHT -> quality = TobaccoQuality.GUT;
            case GUT -> quality = TobaccoQuality.SEHR_GUT;
            case SEHR_GUT -> quality = TobaccoQuality.LEGENDAER;
            default -> {}
        }
    }

    // Getters und Setters
    public MushroomType getType() {
        return type;
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
        ticksGrown++;
    }

    public TobaccoQuality getQuality() {
        return quality;
    }

    public void setQuality(TobaccoQuality quality) {
        this.quality = quality;
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

    public int getFlushCount() {
        return flushCount;
    }

    public int getCurrentFlush() {
        return currentFlush;
    }

    public void setCurrentFlush(int flush) {
        this.currentFlush = flush;
    }

    public int getRemainingFlushes() {
        return flushCount - currentFlush;
    }
}
