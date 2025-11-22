package de.rolandsw.schedulemc.npc.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

/**
 * Ein einzelner Zeitplan-Eintrag für NPCs
 * Definiert eine Aktivität mit Start-/Endzeit und optionaler Location
 */
public class ScheduleEntry {
    private int startTime; // Tageszeit in Ticks (0-24000)
    private int endTime;   // Tageszeit in Ticks (0-24000)
    private ActivityType activityType;
    @Nullable
    private BlockPos targetLocation; // Optional: Spezifische Location für diese Aktivität
    private int locationIndex; // Index für Freetime-Locations (-1 = nicht verwendet)

    public ScheduleEntry() {
        this.startTime = 0;
        this.endTime = 12000;
        this.activityType = ActivityType.HOME;
        this.targetLocation = null;
        this.locationIndex = -1;
    }

    public ScheduleEntry(int startTime, int endTime, ActivityType activityType) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.activityType = activityType;
        this.targetLocation = null;
        this.locationIndex = -1;
    }

    public ScheduleEntry(int startTime, int endTime, ActivityType activityType, BlockPos targetLocation) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.activityType = activityType;
        this.targetLocation = targetLocation;
        this.locationIndex = -1;
    }

    public ScheduleEntry(int startTime, int endTime, ActivityType activityType, int locationIndex) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.activityType = activityType;
        this.targetLocation = null;
        this.locationIndex = locationIndex;
    }

    /**
     * Prüft ob eine bestimmte Tageszeit in diesem Schedule-Eintrag liegt
     */
    public boolean isActive(long dayTime) {
        int time = (int) (dayTime % 24000);

        // Normale Zeitspanne (z.B. 6000-18000)
        if (startTime <= endTime) {
            return time >= startTime && time < endTime;
        } else {
            // Zeitspanne über Mitternacht (z.B. 20000-6000)
            return time >= startTime || time < endTime;
        }
    }

    /**
     * Gibt die Zeit-Beschreibung als String zurück (z.B. "06:00-18:00")
     */
    public String getTimeDescription() {
        return ticksToTime(startTime) + "-" + ticksToTime(endTime);
    }

    /**
     * Konvertiert Minecraft Ticks zu einer Zeit-String (z.B. "06:00")
     */
    private String ticksToTime(int ticks) {
        // Minecraft: 0 ticks = 6:00 Uhr, 6000 = 12:00, 12000 = 18:00, 18000 = 00:00
        int hours = ((ticks / 1000) + 6) % 24;
        int minutes = (ticks % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hours, minutes);
    }

    // NBT Serialization
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("StartTime", startTime);
        tag.putInt("EndTime", endTime);
        tag.putInt("ActivityType", activityType.ordinal());
        if (targetLocation != null) {
            tag.putLong("TargetLocation", targetLocation.asLong());
        }
        tag.putInt("LocationIndex", locationIndex);
        return tag;
    }

    public void load(CompoundTag tag) {
        startTime = tag.getInt("StartTime");
        endTime = tag.getInt("EndTime");
        activityType = ActivityType.fromOrdinal(tag.getInt("ActivityType"));
        if (tag.contains("TargetLocation")) {
            targetLocation = BlockPos.of(tag.getLong("TargetLocation"));
        }
        locationIndex = tag.getInt("LocationIndex");
    }

    // Getters & Setters
    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    @Nullable
    public BlockPos getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(@Nullable BlockPos targetLocation) {
        this.targetLocation = targetLocation;
    }

    public int getLocationIndex() {
        return locationIndex;
    }

    public void setLocationIndex(int locationIndex) {
        this.locationIndex = locationIndex;
    }
}
