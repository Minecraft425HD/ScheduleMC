package de.rolandsw.schedulemc.mission.network;

import de.rolandsw.schedulemc.mission.client.ClientMissionCache;
import de.rolandsw.schedulemc.mission.client.PlayerMissionDto;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * S→C: Server sendet aktuellen Missionsstatus an den Client.
 */
public class SyncMissionsPacket {

    private final List<PlayerMissionDto> missions;

    public SyncMissionsPacket(List<PlayerMissionDto> missions) {
        this.missions = missions;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(missions.size());
        for (PlayerMissionDto dto : missions) {
            dto.encode(buf);
        }
    }

    public static SyncMissionsPacket decode(FriendlyByteBuf buf) {
        int size = Math.min(buf.readInt(), 500);
        List<PlayerMissionDto> missions = new ArrayList<>(Math.min(size, 32));
        for (int i = 0; i < size; i++) {
            missions.add(PlayerMissionDto.decode(buf));
        }
        return new SyncMissionsPacket(missions);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handleClient)
        );
        ctx.get().setPacketHandled(true);
    }

    @net.minecraftforge.api.distmarker.OnlyIn(Dist.CLIENT)
    private void handleClient() {
        ClientMissionCache.update(missions);
    }

    public List<PlayerMissionDto> getMissions() { return missions; }
}
