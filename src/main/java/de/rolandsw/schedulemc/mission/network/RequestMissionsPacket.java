package de.rolandsw.schedulemc.mission.network;

import de.rolandsw.schedulemc.mission.PlayerMissionManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C→S: Spieler öffnet die Missions-App und fordert seine Missionsdaten an.
 */
public class RequestMissionsPacket {

    public RequestMissionsPacket() {}

    public void encode(FriendlyByteBuf buf) {
        // kein Payload
    }

    public static RequestMissionsPacket decode(FriendlyByteBuf buf) {
        return new RequestMissionsPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, (ServerPlayer player) -> {
            PlayerMissionManager mm = PlayerMissionManager.getInstance();
            if (mm != null) {
                mm.syncToPlayer(player);
            }
        });
    }
}
