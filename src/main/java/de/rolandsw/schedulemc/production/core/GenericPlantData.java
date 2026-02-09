package de.rolandsw.schedulemc.production.core;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;

/**
 * Generic Plant Data - Vereinheitlicht alle Pflanzensysteme
 *
 * Ersetzt:
 * - TobaccoPlantData
 * - CannabisPlantData
 * - CocaPlantData
 * - PoppyPlantData
 *
 * Spezialisierte Systeme (MushroomPlantData) können durch Vererbung erweitert werden
 *
 * @param <T> ProductionType (TobaccoType, CannabisStrain, etc.)
 * @param <Q> ProductionQuality (TobaccoQuality, CannabisQuality, etc.)
 */
public class GenericPlantData<T extends ProductionType, Q extends ProductionQuality> {

    private static final Logger LOGGER = LogUtils.getLogger();

    // ═══════════════════════════════════════════════════════════
    // CORE DATA (identisch in allen Systemen)
    // ═══════════════════════════════════════════════════════════

    protected T type;
    protected Q quality;
    protected int growthStage;      // 0-7 (MAX_GROWTH_STAGE = 7)
    protected int ticksGrown;       // Ticks seit Pflanzung

    // Boosters
    protected boolean hasFertilizer;
    protected boolean hasGrowthBooster;
    protected boolean hasQualityBooster;

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    public static final int MAX_GROWTH_STAGE = 7;
    public static final int MIN_GROWTH_STAGE = 0;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public GenericPlantData(T type, Q quality) {
        this.type = type;
        this.quality = quality;
        this.growthStage = 0;
        this.ticksGrown = 0;
        this.hasFertilizer = false;
        this.hasGrowthBooster = false;
        this.hasQualityBooster = false;
    }

    // ═══════════════════════════════════════════════════════════
    // GROWTH LOGIC (vereinheitlicht)
    // ═══════════════════════════════════════════════════════════

    /**
     * Standard Linear Growth (Tobacco, Cannabis, Coca)
     *
     * Override für spezielle Systeme:
     * - PoppyPlantData: tryUpgradeQuality() bei bestimmten Stages
     * - MushroomPlantData: flush mechanics
     */
    public void tick() {
        if (isFullyGrown()) {
            return;
        }

        incrementTicks();

        // Berechne aktuelle Stage basierend auf Ticks
        int requiredTicks = getGrowthSpeed();
        int ticksPerStage = Math.max(1, requiredTicks / 8);  // 8 Stages (0-7), min 1 tick
        int newStage = Math.min(MAX_GROWTH_STAGE, ticksGrown / ticksPerStage);

        setGrowthStage(newStage);
    }

    /**
     * Gibt Wachstumsgeschwindigkeit zurück (in Ticks bis Stage 7)
     * Berücksichtigt Growth Booster
     */
    public int getGrowthSpeed() {
        int baseTicks = type.getGrowthTicks();

        if (hasGrowthBooster) {
            // Growth Booster: 30% schneller
            baseTicks = (int) (baseTicks * 0.7);
        }

        return baseTicks;
    }

    /**
     * Inkrement-Logik (kann überschrieben werden)
     */
    protected void incrementTicks() {
        ticksGrown++;
    }

    // ═══════════════════════════════════════════════════════════
    // HARVEST LOGIC (vereinheitlicht)
    // ═══════════════════════════════════════════════════════════

    /**
     * Standard Harvest Yield Calculation
     *
     * Override für spezielle Systeme:
     * - PoppyPlantData: Quality multiplier unterschiedlich
     * - MushroomPlantData: Flush reduction
     */
    public int getHarvestYield() {
        int baseYield = type.getBaseYield();

        // Fertilizer Bonus: +1-2 Yield
        if (hasFertilizer) {
            baseYield += 1 + ThreadLocalRandom.current().nextInt(2);
        }

        // Quality Multiplier (Standard)
        double qualityMultiplier = getQualityYieldMultiplier();

        return (int) (baseYield * qualityMultiplier);
    }

    /**
     * Standard Quality Yield Multiplier
     * Kann überschrieben werden für system-spezifische Multiplier
     */
    protected double getQualityYieldMultiplier() {
        return switch (quality.getLevel()) {
            case 0 -> 0.7;   // SCHLECHT
            case 1 -> 1.0;   // GUT
            case 2 -> 1.3;   // SEHR_GUT
            case 3 -> 1.6;   // LEGENDAER
            default -> 1.0;
        };
    }

    /**
     * Kann geerntet werden?
     */
    public boolean canHarvest() {
        return growthStage >= MAX_GROWTH_STAGE;
    }

    /**
     * Harvest ausführen (Standard-Implementation)
     * Override für spezielle Systeme (z.B. Mushroom mit flush)
     *
     * @return true wenn erfolgreich geerntet
     */
    public boolean harvest() {
        if (!canHarvest()) {
            return false;
        }

        // Standard: Pflanze wird komplett entfernt
        // (In der Praxis wird der PlantPot geleert)
        return true;
    }

    // ═══════════════════════════════════════════════════════════
    // BOOSTERS
    // ═══════════════════════════════════════════════════════════

    public void applyFertilizer() {
        if (!hasFertilizer) {
            hasFertilizer = true;
            LOGGER.debug("Fertilizer angewendet auf {} ({})", type.getDisplayName(), quality.getDisplayName());
        }
    }

    public void applyGrowthBooster() {
        if (!hasGrowthBooster) {
            hasGrowthBooster = true;
            LOGGER.debug("Growth Booster angewendet auf {} ({})", type.getDisplayName(), quality.getDisplayName());
        }
    }

    public void applyQualityBooster() {
        if (!hasQualityBooster) {
            hasQualityBooster = true;
            LOGGER.debug("Quality Booster angewendet auf {} ({})", type.getDisplayName(), quality.getDisplayName());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Speichert PlantData in NBT
     *
     * WICHTIG: Type und Quality müssen von Subklasse gespeichert werden
     * (da Enums system-spezifisch sind)
     */
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("GrowthStage", growthStage);
        tag.putInt("TicksGrown", ticksGrown);
        tag.putBoolean("HasFertilizer", hasFertilizer);
        tag.putBoolean("HasGrowthBooster", hasGrowthBooster);
        tag.putBoolean("HasQualityBooster", hasQualityBooster);

        // Type und Quality speichern (via ordinal)
        tag.putString("TypeName", type.getDisplayName());
        tag.putInt("QualityLevel", quality.getLevel());

        return tag;
    }

    /**
     * Lädt PlantData aus NBT
     *
     * WICHTIG: Type und Quality müssen von Subklasse geladen werden
     */
    public void load(CompoundTag tag) {
        growthStage = tag.getInt("GrowthStage");
        ticksGrown = tag.getInt("TicksGrown");
        hasFertilizer = tag.getBoolean("HasFertilizer");
        hasGrowthBooster = tag.getBoolean("HasGrowthBooster");
        hasQualityBooster = tag.getBoolean("HasQualityBooster");

        // Type und Quality müssen von Subklasse geladen werden
        // (da Enum-Lookup system-spezifisch ist)
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════

    public T getType() {
        return type;
    }

    public void setType(T type) {
        this.type = type;
    }

    public Q getQuality() {
        return quality;
    }

    public void setQuality(Q quality) {
        this.quality = quality;
    }

    public int getGrowthStage() {
        return growthStage;
    }

    public void setGrowthStage(int stage) {
        this.growthStage = Math.max(MIN_GROWTH_STAGE, Math.min(MAX_GROWTH_STAGE, stage));
    }

    public int getTicksGrown() {
        return ticksGrown;
    }

    public void setTicksGrown(int ticks) {
        this.ticksGrown = Math.max(0, ticks);
    }

    public boolean hasFertilizer() {
        return hasFertilizer;
    }

    public boolean hasGrowthBooster() {
        return hasGrowthBooster;
    }

    public boolean hasQualityBooster() {
        return hasQualityBooster;
    }

    public boolean isFullyGrown() {
        return growthStage >= MAX_GROWTH_STAGE;
    }

    // ═══════════════════════════════════════════════════════════
    // DISPLAY INFO
    // ═══════════════════════════════════════════════════════════

    public String getDisplayInfo() {
        return String.format("%s%s§r - Stage %d/7 - %s",
            type.getColorCode(),
            type.getDisplayName(),
            growthStage,
            quality.getDisplayName()
        );
    }

    public float getGrowthProgress() {
        return (float) growthStage / MAX_GROWTH_STAGE;
    }

    @Override
    public String toString() {
        return String.format("GenericPlantData{type=%s, quality=%s, stage=%d/%d, ticks=%d, boosters=[F:%b,G:%b,Q:%b]}",
            type.getDisplayName(),
            quality.getDisplayName(),
            growthStage,
            MAX_GROWTH_STAGE,
            ticksGrown,
            hasFertilizer,
            hasGrowthBooster,
            hasQualityBooster
        );
    }
}
