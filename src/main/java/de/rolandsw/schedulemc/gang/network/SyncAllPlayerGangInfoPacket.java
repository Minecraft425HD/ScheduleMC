package de.rolandsw.schedulemc.gang.network;

import de.rolandsw.schedulemc.gang.client.ClientGangCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Synchronisiert Gang- und Level-Info ALLER Online-Spieler an ALLE Clients.
 * Wird gesendet bei: Login, Gang-Aenderungen, periodisch.
 *
 * Dieses Packet ermoeglicht die Nametag- und TAB-Anzeige.
 */
public class SyncAllPlayerGangInfoPacket {

    private static final int MAX_PLAYERS = 200;
    private final List<PlayerGangInfo> playerInfos;

    public SyncAllPlayerGangInfoPacket(List<PlayerGangInfo> playerInfos) {
        this.playerInfos = playerInfos;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(Math.min(playerInfos.size(), MAX_PLAYERS));
        int count = 0;
        for (PlayerGangInfo info : playerInfos) {
            if (count >= MAX_PLAYERS) break;
            info.encode(buf);
            count++;
        }
    }

    public static SyncAllPlayerGangInfoPacket decode(FriendlyByteBuf buf) {
        int size = Math.min(buf.readInt(), MAX_PLAYERS);
        List<PlayerGangInfo> infos = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            infos.add(PlayerGangInfo.decode(buf));
        }
        return new SyncAllPlayerGangInfoPacket(infos);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient());
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        ClientGangCache.updateAllPlayerInfos(playerInfos);
    }
}
