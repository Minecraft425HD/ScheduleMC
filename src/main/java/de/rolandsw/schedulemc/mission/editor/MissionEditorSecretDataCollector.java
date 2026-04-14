package de.rolandsw.schedulemc.mission.editor;

import de.rolandsw.schedulemc.gang.network.OpenScenarioEditorPacket;
import de.rolandsw.schedulemc.secretdoors.blockentity.HiddenSwitchBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blockentity.SecretDoorBlockEntity;
import de.rolandsw.schedulemc.secretdoors.mission.SecretBlockRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sammelt Secret-Door/Hatch/Hidden-Switch Daten für den Mission-Editor.
 */
public final class MissionEditorSecretDataCollector {

    private MissionEditorSecretDataCollector() {
    }

    public static List<OpenScenarioEditorPacket.LockInfo> collectSecretLockInfos(MinecraftServer server) {
        Map<String, OpenScenarioEditorPacket.LockInfo> deduped = new LinkedHashMap<>();

        for (SecretBlockRegistry.SecretBlockEntry entry : SecretBlockRegistry.getAllLoadedEntries(server)) {
            ServerLevel level = server.getLevel(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                new net.minecraft.resources.ResourceLocation(entry.dimension())));
            if (level == null) continue;

            String lockId = "NO_LOCK";
            boolean hasCode = false;
            net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(entry.x(), entry.y(), entry.z());
            if (level.getBlockEntity(pos) instanceof SecretDoorBlockEntity doorBe) {
                lockId = doorBe.getLockId();
                hasCode = true;
            } else if (level.getBlockEntity(pos) instanceof HiddenSwitchBlockEntity switchBe) {
                lockId = switchBe.getLockId();
                hasCode = true;
            }
            OpenScenarioEditorPacket.LockInfo info = new OpenScenarioEditorPacket.LockInfo(
                lockId,
                entry.type(),
                entry.dimension(),
                entry.x(),
                entry.y(),
                entry.z(),
                hasCode
            );

            String dedupeKey = lockId == null || lockId.isBlank() || "NO_LOCK".equals(lockId)
                ? ("POS:" + entry.dimension() + ":" + entry.x() + ":" + entry.y() + ":" + entry.z())
                : ("LOCK:" + lockId);

            OpenScenarioEditorPacket.LockInfo existing = deduped.get(dedupeKey);
            if (existing == null) {
                deduped.put(dedupeKey, info);
            } else if (isSwitch(existing) && !isSwitch(info)) {
                // Wenn Door/Hatch + HiddenSwitch dieselbe lock_id haben:
                // im Editor nur einmal anzeigen, Door/Hatch bevorzugen.
                deduped.put(dedupeKey, info);
            }
        }

        return new ArrayList<>(deduped.values());
    }

    private static boolean isSwitch(OpenScenarioEditorPacket.LockInfo info) {
        return info != null && "HIDDEN_SWITCH".equalsIgnoreCase(info.lockType());
    }
}
