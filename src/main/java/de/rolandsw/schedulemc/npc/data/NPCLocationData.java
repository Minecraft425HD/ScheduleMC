package de.rolandsw.schedulemc.npc.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class NPCLocationData {
    @Nullable private BlockPos homeLocation;
    @Nullable private BlockPos workLocation;
    @Nullable private BlockPos assignedWarehouse;
    private List<BlockPos> leisureLocations;

    public NPCLocationData() {
        this.leisureLocations = new ArrayList<>();
    }

    public void save(CompoundTag tag) {
        if (homeLocation != null) tag.putLong("HomeLocation", homeLocation.asLong());
        if (workLocation != null) tag.putLong("WorkLocation", workLocation.asLong());
        if (assignedWarehouse != null) tag.putLong("AssignedWarehouse", assignedWarehouse.asLong());
        ListTag leisureList = new ListTag();
        for (BlockPos pos : leisureLocations) {
            CompoundTag posTag = new CompoundTag();
            posTag.putLong("Pos", pos.asLong());
            leisureList.add(posTag);
        }
        tag.put("LeisureLocations", leisureList);
    }

    public void load(CompoundTag tag) {
        homeLocation = tag.contains("HomeLocation") ? BlockPos.of(tag.getLong("HomeLocation")) : null;
        workLocation = tag.contains("WorkLocation") ? BlockPos.of(tag.getLong("WorkLocation")) : null;
        assignedWarehouse = tag.contains("AssignedWarehouse") ? BlockPos.of(tag.getLong("AssignedWarehouse")) : null;
        leisureLocations.clear();
        if (tag.contains("LeisureLocations")) {
            ListTag list = tag.getList("LeisureLocations", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                leisureLocations.add(BlockPos.of(list.getCompound(i).getLong("Pos")));
            }
        }
    }

    @Nullable public BlockPos getHomeLocation() { return homeLocation; }
    public void setHomeLocation(@Nullable BlockPos pos) { this.homeLocation = pos; }
    @Nullable public BlockPos getWorkLocation() { return workLocation; }
    public void setWorkLocation(@Nullable BlockPos pos) { this.workLocation = pos; }
    @Nullable public BlockPos getAssignedWarehouse() { return assignedWarehouse; }
    public void setAssignedWarehouse(@Nullable BlockPos pos) { this.assignedWarehouse = pos; }
    public List<BlockPos> getLeisureLocations() { return leisureLocations; }
    public void addLeisureLocation(BlockPos location) {
        if (leisureLocations.size() < 10) leisureLocations.add(location);
    }
    public void removeLeisureLocation(int index) {
        if (index >= 0 && index < leisureLocations.size()) leisureLocations.remove(index);
    }
    public void clearLeisureLocations() { leisureLocations.clear(); }
}
