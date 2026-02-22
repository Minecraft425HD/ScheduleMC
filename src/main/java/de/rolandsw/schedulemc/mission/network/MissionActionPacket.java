package de.rolandsw.schedulemc.mission.network;

import de.rolandsw.schedulemc.mission.PlayerMissionManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C→S: Spieler führt eine Missions-Aktion aus (Annehmen, Aufgeben, Belohnung abholen).
 */
public class MissionActionPacket {

    public enum Action {
        ACCEPT,
        ABANDON,
        CLAIM
    }

    private final Action action;
    private final String missionId; // definitionId bei ACCEPT, missionId bei ABANDON/CLAIM

    public MissionActionPacket(Action action, String missionId) {
        this.action = action;
        this.missionId = missionId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeUtf(missionId);
    }

    public static MissionActionPacket decode(FriendlyByteBuf buf) {
        return new MissionActionPacket(
            buf.readEnum(Action.class),
            buf.readUtf()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, (ServerPlayer player) -> {
            PlayerMissionManager mm = PlayerMissionManager.getInstance();
            if (mm == null) return;

            switch (action) {
                case ACCEPT:
                    mm.acceptMission(player, missionId);
                    break;
                case ABANDON:
                    mm.abandonMission(player, missionId);
                    break;
                case CLAIM:
                    mm.claimMission(player, missionId);
                    break;
            }
        });
    }

    public Action getAction() { return action; }
    public String getMissionId() { return missionId; }
}
