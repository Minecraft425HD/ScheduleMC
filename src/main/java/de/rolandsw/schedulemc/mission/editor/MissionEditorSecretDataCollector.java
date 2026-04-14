package de.rolandsw.schedulemc.mission.editor;

import de.rolandsw.schedulemc.gang.network.OpenScenarioEditorPacket;
import de.rolandsw.schedulemc.lock.LockData;
import de.rolandsw.schedulemc.lock.LockManager;
import de.rolandsw.schedulemc.secretdoors.mission.SecretBlockRegistry;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Sammelt Secret-Door/Hatch/Hidden-Switch Daten für den Mission-Editor.
 */
public final class MissionEditorSecretDataCollector {

    private MissionEditorSecretDataCollector() {
    }

    public static List<OpenScenarioEditorPacket.LockInfo> collectSecretLockInfos(MinecraftServer server) {
        List<OpenScenarioEditorPacket.LockInfo> result = new ArrayList<>();
        LockManager lockManager = LockManager.getInstance();

        for (SecretBlockRegistry.SecretBlockEntry entry : SecretBlockRegistry.getAllLoadedEntries(server)) {
            String lockId = "NO_LOCK";
            boolean hasCode = false;
            if (lockManager != null) {
                LockData data = lockManager.getLock(entry.dimension(), entry.x(), entry.y(), entry.z());
                if (data != null) {
                    lockId = data.getLockId();
                    hasCode = data.getType().hasCode();
                }
            }
            result.add(new OpenScenarioEditorPacket.LockInfo(
                lockId,
                entry.type(),
                entry.dimension(),
                entry.x(),
                entry.y(),
                entry.z(),
                hasCode
            ));
        }

        return result;
    }
}
