package de.rolandsw.schedulemc.secretdoors.mission;

import de.rolandsw.schedulemc.gang.scenario.ScenarioObjective;
import de.rolandsw.schedulemc.lock.LockData;
import de.rolandsw.schedulemc.lock.LockManager;
import de.rolandsw.schedulemc.mission.scenario.PlayerMissionScenarioExecutor;
import de.rolandsw.schedulemc.secretdoors.blockentity.SecretDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporäre Zugriffsverwaltung für SecretDoor/Hatch/HiddenSwitch.
 *
 * Zugriff kann über:
 * - aktive Missions-Objectives (lock_id Param)
 * - explizite Event/Missions-Grant-Calls
 * erfolgen.
 */
public final class SecretDoorMissionAccessManager {

    private static final ConcurrentHashMap<UUID, Set<String>> TEMP_LOCK_ACCESS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Set<Long>> USED_SECRET_DOORS = new ConcurrentHashMap<>();

    private SecretDoorMissionAccessManager() {
    }

    public static void grantTemporaryLockAccess(UUID playerUUID, String lockId) {
        if (playerUUID == null || lockId == null || lockId.isBlank()) {
            return;
        }
        TEMP_LOCK_ACCESS.computeIfAbsent(playerUUID, ignored -> ConcurrentHashMap.newKeySet())
            .add(lockId.trim());
    }

    public static void revokeTemporaryLockAccess(UUID playerUUID, String lockId) {
        if (playerUUID == null || lockId == null || lockId.isBlank()) {
            return;
        }
        Set<String> set = TEMP_LOCK_ACCESS.get(playerUUID);
        if (set == null) return;
        set.remove(lockId.trim());
        if (set.isEmpty()) {
            TEMP_LOCK_ACCESS.remove(playerUUID);
        }
    }

    public static void clearPlayerAccess(UUID playerUUID) {
        if (playerUUID == null) return;
        TEMP_LOCK_ACCESS.remove(playerUUID);
        USED_SECRET_DOORS.remove(playerUUID);
    }

    public static void clearPlayerAccess(ServerPlayer player) {
        if (player == null) return;
        UUID playerUUID = player.getUUID();
        Set<Long> usedDoors = USED_SECRET_DOORS.remove(playerUUID);
        TEMP_LOCK_ACCESS.remove(playerUUID);

        if (usedDoors == null || usedDoors.isEmpty()) return;

        for (Long encodedPos : new HashSet<>(usedDoors)) {
            BlockPos pos = BlockPos.of(encodedPos);
            if (player.serverLevel().getBlockEntity(pos) instanceof SecretDoorBlockEntity doorBe && doorBe.isOpen()) {
                doorBe.close(player.serverLevel());
            }
        }
    }

    public static void markMissionDoorUsed(ServerPlayer player, BlockPos pos) {
        if (player == null || pos == null) return;
        USED_SECRET_DOORS.computeIfAbsent(player.getUUID(), ignored -> ConcurrentHashMap.newKeySet())
            .add(pos.asLong());
    }

    public static boolean hasMissionOrEventAccess(ServerPlayer player, BlockPos pos) {
        if (player == null || pos == null) {
            return false;
        }
        LockManager lockManager = LockManager.getInstance();
        if (lockManager == null) {
            return false;
        }

        String dimension = player.serverLevel().dimension().location().toString();
        LockData lock = lockManager.getLock(dimension, pos.getX(), pos.getY(), pos.getZ());
        if (lock == null) {
            return false;
        }
        String lockId = lock.getLockId();

        Set<String> tempAccess = TEMP_LOCK_ACCESS.get(player.getUUID());
        if (tempAccess != null && tempAccess.contains(lockId)) {
            return true;
        }

        for (ScenarioObjective objective : PlayerMissionScenarioExecutor.getActiveObjectivesForPlayer(player.getUUID())) {
            String objectiveLockId = objective.getParam("lock_id");
            if (objectiveLockId != null && objectiveLockId.equalsIgnoreCase(lockId)) {
                return true;
            }
        }

        return false;
    }
}
