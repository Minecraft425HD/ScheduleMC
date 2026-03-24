package de.rolandsw.schedulemc.npc.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Patrol- und Stationsdaten für Polizei-NPCs.
 * Ausgelagert aus NPCData als Teil der God-Class-Aufteilung.
 */
public class NPCPoliceData {

    @Nullable
    private BlockPos policeStation;       // Polizeistation
    private List<BlockPos> patrolPoints;  // Bis zu 16 Patrouillenpunkte
    private int currentPatrolIndex;       // Aktueller Patrol-Index
    private long patrolArrivalTime;       // Letzte Ankunftszeit am Patrol-Punkt
    private long stationArrivalTime;      // Letzte Ankunftszeit an der Station

    public NPCPoliceData() {
        this.policeStation = null;
        this.patrolPoints = new ArrayList<>();
        this.currentPatrolIndex = 0;
        this.patrolArrivalTime = 0L;
        this.stationArrivalTime = 0L;
    }

    // ═══════════════════════════════════════════════════════════
    // NBT Serialization — identische Keys wie bisher in NPCData
    // ═══════════════════════════════════════════════════════════

    public void save(CompoundTag tag) {
        if (policeStation != null) {
            tag.putLong("PoliceStation", policeStation.asLong());
        }
        ListTag patrolList = new ListTag();
        for (BlockPos pos : patrolPoints) {
            CompoundTag posTag = new CompoundTag();
            posTag.putLong("Pos", pos.asLong());
            patrolList.add(posTag);
        }
        tag.put("PatrolPoints", patrolList);
        tag.putInt("CurrentPatrolIndex", currentPatrolIndex);
        tag.putLong("PatrolArrivalTime", patrolArrivalTime);
        tag.putLong("StationArrivalTime", stationArrivalTime);
    }

    public void load(CompoundTag tag) {
        if (tag.contains("PoliceStation")) {
            policeStation = BlockPos.of(tag.getLong("PoliceStation"));
        }
        patrolPoints.clear();
        if (tag.contains("PatrolPoints")) {
            ListTag patrolList = tag.getList("PatrolPoints", Tag.TAG_COMPOUND);
            for (int i = 0; i < patrolList.size(); i++) {
                CompoundTag posTag = patrolList.getCompound(i);
                patrolPoints.add(BlockPos.of(posTag.getLong("Pos")));
            }
        }
        if (tag.contains("CurrentPatrolIndex")) {
            currentPatrolIndex = tag.getInt("CurrentPatrolIndex");
        }
        if (tag.contains("PatrolArrivalTime")) {
            patrolArrivalTime = tag.getLong("PatrolArrivalTime");
        }
        if (tag.contains("StationArrivalTime")) {
            stationArrivalTime = tag.getLong("StationArrivalTime");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Getters & Setters
    // ═══════════════════════════════════════════════════════════

    @Nullable
    public BlockPos getPoliceStation() {
        return policeStation;
    }

    public void setPoliceStation(@Nullable BlockPos policeStation) {
        this.policeStation = policeStation;
    }

    public List<BlockPos> getPatrolPoints() {
        return patrolPoints;
    }

    public void addPatrolPoint(BlockPos point) {
        if (patrolPoints.size() < 16) {
            patrolPoints.add(point);
        }
    }

    public void removePatrolPoint(int index) {
        if (index >= 0 && index < patrolPoints.size()) {
            patrolPoints.remove(index);
        }
    }

    public void clearPatrolPoints() {
        patrolPoints.clear();
        currentPatrolIndex = 0;
    }

    public int getCurrentPatrolIndex() {
        return currentPatrolIndex;
    }

    public void setCurrentPatrolIndex(int index) {
        this.currentPatrolIndex = index;
    }

    public void incrementPatrolIndex() {
        if (!patrolPoints.isEmpty()) {
            currentPatrolIndex = (currentPatrolIndex + 1) % patrolPoints.size();
        }
    }

    public long getPatrolArrivalTime() {
        return patrolArrivalTime;
    }

    public void setPatrolArrivalTime(long time) {
        this.patrolArrivalTime = time;
    }

    public long getStationArrivalTime() {
        return stationArrivalTime;
    }

    public void setStationArrivalTime(long time) {
        this.stationArrivalTime = time;
    }
}
