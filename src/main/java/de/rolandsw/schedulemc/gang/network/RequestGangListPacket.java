package de.rolandsw.schedulemc.gang.network;

import de.rolandsw.schedulemc.gang.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

/**
 * Client fordert die Liste aller Gangs an (fuer "Andere Gangs" Sektion in der App).
 * Client -> Server
 */
public class RequestGangListPacket {

    public RequestGangListPacket() {}

    public void encode(FriendlyByteBuf buf) {
        // Leer - keine Parameter noetig
    }

    public static RequestGangListPacket decode(FriendlyByteBuf buf) {
        return new RequestGangListPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            GangManager manager = GangManager.getInstance();
            if (manager == null) {
                GangNetworkHandler.sendToPlayer(new SyncGangListPacket(List.of()), player);
                return;
            }

            List<SyncGangListPacket.GangListEntry> entries = new ArrayList<>();
            UUID playerGangId = manager.getPlayerGangId(player.getUUID());

            for (Gang gang : manager.getAllGangs()) {
                // Eigene Gang ueberspringen (wird separat angezeigt)
                if (gang.getGangId().equals(playerGangId)) continue;

                GangReputation rep = GangReputation.getForLevel(gang.getGangLevel());

                entries.add(new SyncGangListPacket.GangListEntry(
                        gang.getGangId(),
                        gang.getName(),
                        gang.getTag(),
                        gang.getGangLevel(),
                        gang.getMemberCount(),
                        gang.getMaxMembers(),
                        gang.getTerritoryCount(),
                        gang.getColor().ordinal(),
                        rep.getDisplayName()
                ));
            }

            // Nach Level absteigend sortieren
            entries.sort((a, b) -> Integer.compare(b.level(), a.level()));

            GangNetworkHandler.sendToPlayer(new SyncGangListPacket(entries), player);
        });
        ctx.get().setPacketHandled(true);
    }
}
