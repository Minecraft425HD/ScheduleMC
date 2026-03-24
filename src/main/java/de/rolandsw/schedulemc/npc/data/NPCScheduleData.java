package de.rolandsw.schedulemc.npc.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

/**
 * Zeitplan-Daten eines NPCs (Arbeits- und Heimzeiten).
 * Ausgelagert aus NPCData als Teil der God-Class-Aufteilung.
 */
public class NPCScheduleData {

    // Zeiteinstellungen in Minecraft Ticks (24000 = 1 Tag)
    private long workStartTime; // Wann geht NPC zur Arbeit (Standard: 0 = 6:00 Uhr)
    private long workEndTime;   // Wann endet die Arbeit (Standard: 13000 = 19:00 Uhr)
    private long homeTime;      // Wann muss NPC nach Hause (Standard: 23000 = 5:00 Uhr morgens)

    public NPCScheduleData() {
        // Minecraft Ticks: 0 = 6:00, 6000 = 12:00, 12000 = 18:00, 18000 = 0:00
        this.workStartTime = 0;      // 6:00 Uhr morgens
        this.workEndTime = 13000;    // 19:00 Uhr abends
        this.homeTime = 23000;       // 5:00 Uhr morgens (Zeit zum Schlafen)
    }

    // ═══════════════════════════════════════════════════════════
    // NBT Serialization — identische Keys wie bisher in NPCData
    // ═══════════════════════════════════════════════════════════

    public void save(CompoundTag tag) {
        tag.putLong("WorkStartTime", workStartTime);
        tag.putLong("WorkEndTime", workEndTime);
        tag.putLong("HomeTime", homeTime);
    }

    public void load(CompoundTag tag) {
        if (tag.contains("WorkStartTime")) {
            workStartTime = tag.getLong("WorkStartTime");
        }
        if (tag.contains("WorkEndTime")) {
            workEndTime = tag.getLong("WorkEndTime");
        }
        if (tag.contains("HomeTime")) {
            homeTime = tag.getLong("HomeTime");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Getters & Setters
    // ═══════════════════════════════════════════════════════════

    public long getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(long workStartTime) {
        this.workStartTime = workStartTime;
    }

    public long getWorkEndTime() {
        return workEndTime;
    }

    public void setWorkEndTime(long workEndTime) {
        this.workEndTime = workEndTime;
    }

    public long getHomeTime() {
        return homeTime;
    }

    public void setHomeTime(long homeTime) {
        this.homeTime = homeTime;
    }

    /**
     * Prüft ob der NPC aktuell während seiner Arbeitszeit ist.
     * Unterstützt Arbeitszeiten, die über Mitternacht gehen (z.B. 22:00–6:00).
     *
     * @param level Die Welt (für die aktuelle Tageszeit)
     * @return true wenn innerhalb der Arbeitszeit
     */
    public boolean isWithinWorkingHours(Level level) {
        long currentTime = level.getDayTime() % 24000;

        // Spezialfall: Arbeitszeit geht über Mitternacht (z.B. workStart=22000, workEnd=6000)
        if (workEndTime < workStartTime) {
            return currentTime >= workStartTime || currentTime <= workEndTime;
        }

        // Normalfall: Arbeitszeit innerhalb eines Tages
        return currentTime >= workStartTime && currentTime <= workEndTime;
    }
}
