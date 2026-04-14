package de.rolandsw.schedulemc.mission.editor;

import de.rolandsw.schedulemc.gang.network.OpenScenarioEditorPacket;
import de.rolandsw.schedulemc.secretdoors.blockentity.HiddenSwitchBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blockentity.SecretDoorBlockEntity;
import de.rolandsw.schedulemc.secretdoors.mission.SecretBlockRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

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
