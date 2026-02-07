package de.rolandsw.schedulemc.npc.driving;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Verwaltet welche NPCs ein Fahrzeug besitzen (20%).
 * Persistiert via WorldSavedData.
 */
public class NPCVehicleAssignment extends SavedData {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DATA_NAME = "schedulemc_npc_vehicles";
    private static final float VEHICLE_RATIO = 0.2f;

    private final Set<UUID> npcsWithVehicle = new HashSet<>();
    private final Map<UUID, Integer> vehicleColors = new HashMap<>();

    public NPCVehicleAssignment() {
    }

    public NPCVehicleAssignment(CompoundTag tag) {
        load(tag);
    }

    /**
     * Prueft ob ein NPC ein Fahrzeug besitzt
     */
    public boolean hasVehicle(UUID npcId) {
        return npcsWithVehicle.contains(npcId);
    }

    /**
     * Gibt die Fahrzeugfarbe des NPCs zurueck (0-4)
     */
    public int getVehicleColor(UUID npcId) {
        return vehicleColors.getOrDefault(npcId, 0);
    }

    /**
     * Weist Fahrzeuge an 20% der uebergebenen NPCs zu.
     * Bereits zugewiesene NPCs bleiben erhalten.
     */
    public void ensureAssignments(Collection<UUID> allNpcIds) {
        // Entferne NPCs die nicht mehr existieren
        npcsWithVehicle.retainAll(allNpcIds);
        vehicleColors.keySet().retainAll(allNpcIds);

        int targetCount = Math.max(1, (int) (allNpcIds.size() * VEHICLE_RATIO));
        if (npcsWithVehicle.size() >= targetCount) {
            return;
        }

        // Wuerfle zufaellig neue Fahrzeugbesitzer aus
        List<UUID> unassigned = new ArrayList<>();
        for (UUID id : allNpcIds) {
            if (!npcsWithVehicle.contains(id)) {
                unassigned.add(id);
            }
        }
        Collections.shuffle(unassigned);

        int toAssign = targetCount - npcsWithVehicle.size();
        for (int i = 0; i < toAssign && i < unassigned.size(); i++) {
            UUID npcId = unassigned.get(i);
            npcsWithVehicle.add(npcId);
            vehicleColors.put(npcId, ThreadLocalRandom.current().nextInt(5));
        }

        setDirty();
        LOGGER.info("[NPCVehicleAssignment] Assigned vehicles: {}/{} NPCs (target {}%)",
                npcsWithVehicle.size(), allNpcIds.size(), (int) (VEHICLE_RATIO * 100));
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENZ
    // ═══════════════════════════════════════════════════════════

    private void load(CompoundTag tag) {
        npcsWithVehicle.clear();
        vehicleColors.clear();

        if (tag.contains("Vehicles", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Vehicles", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                UUID id = entry.getUUID("NpcId");
                int color = entry.getInt("Color");
                npcsWithVehicle.add(id);
                vehicleColors.put(id, color);
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (UUID id : npcsWithVehicle) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("NpcId", id);
            entry.putInt("Color", vehicleColors.getOrDefault(id, 0));
            list.add(entry);
        }
        tag.put("Vehicles", list);
        return tag;
    }

    /**
     * Holt oder erstellt die NPCVehicleAssignment fuer eine ServerLevel
     */
    public static NPCVehicleAssignment get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                NPCVehicleAssignment::new,
                NPCVehicleAssignment::new,
                DATA_NAME
        );
    }
}
