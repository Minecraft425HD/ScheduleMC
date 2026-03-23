package de.rolandsw.schedulemc.mission.client;

import de.rolandsw.schedulemc.mission.MissionCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Client-seitiger Cache für Spieler-Missionen.
 * Wird via {@link de.rolandsw.schedulemc.mission.network.SyncMissionsPacket} befüllt.
 */
@OnlyIn(Dist.CLIENT)
public class ClientMissionCache {

    private static volatile List<PlayerMissionDto> missions = Collections.emptyList();

    public static void update(List<PlayerMissionDto> newMissions) {
        missions = new ArrayList<>(newMissions);
    }

    public static List<PlayerMissionDto> getAll() {
        return Collections.unmodifiableList(missions);
    }

    public static List<PlayerMissionDto> getByCategory(MissionCategory category) {
        return missions.stream()
            .filter(m -> m.getCategory() == category)
            .collect(Collectors.toList());
    }

    public static void clear() {
        missions = Collections.emptyList();
    }
}
