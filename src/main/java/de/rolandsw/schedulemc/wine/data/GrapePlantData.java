package de.rolandsw.schedulemc.wine.data;

import de.rolandsw.schedulemc.wine.WineType;

public class GrapePlantData {
    private final WineType type;
    private int growthStage; // 0-7
    private int ticksGrown;

    public GrapePlantData(WineType type) {
        this.type = type;
        this.growthStage = 0;
        this.ticksGrown = 0;
    }

    public WineType getType() { return type; }
    public int getGrowthStage() { return growthStage; }
    public void setGrowthStage(int stage) { this.growthStage = Math.min(7, Math.max(0, stage)); }
    public int getTicksGrown() { return ticksGrown; }
    public void setTicksGrown(int t) { this.ticksGrown = t; }
    public boolean isFullyGrown() { return growthStage >= 7; }

    public void tick() {
        if (isFullyGrown()) return;
        ticksGrown++;
        int ticksPerStage = Math.max(1, type.getGrowthTimeDays() / 8);
        int newStage = Math.min(7, ticksGrown / ticksPerStage);
        setGrowthStage(newStage);
    }
}
