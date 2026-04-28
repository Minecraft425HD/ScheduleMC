package de.rolandsw.schedulemc.coffee.data;

import de.rolandsw.schedulemc.coffee.CoffeeType;
import de.rolandsw.schedulemc.coffee.CoffeeQuality;

public class CoffeePlantData {
    private final CoffeeType type;
    private CoffeeQuality quality;
    private int growthStage; // 0-9
    private int ticksGrown;

    public CoffeePlantData(CoffeeType type) {
        this.type = type;
        this.quality = CoffeeQuality.GUT;
        this.growthStage = 0;
        this.ticksGrown = 0;
    }

    public CoffeeType getType() { return type; }
    public CoffeeQuality getQuality() { return quality; }
    public void setQuality(CoffeeQuality q) { this.quality = q; }
    public int getGrowthStage() { return growthStage; }
    public void setGrowthStage(int stage) { this.growthStage = Math.min(9, Math.max(0, stage)); }
    public int getTicksGrown() { return ticksGrown; }
    public void setTicksGrown(int t) { this.ticksGrown = t; }
    public boolean isFullyGrown() { return growthStage >= 9; }

    public void tick() {
        if (isFullyGrown()) return;
        ticksGrown++;
        int ticksPerStage = type.getGrowthTicks() / 9;
        int targetTicks = (growthStage + 1) * ticksPerStage;
        if (ticksGrown >= targetTicks) {
            growthStage++;
        }
    }
}
