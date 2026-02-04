package de.rolandsw.schedulemc.gang.network;

import de.rolandsw.schedulemc.gang.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

/**
 * Client fordert die Liste aller Gangs an (fuer Rivalen-Seite in der App).
 * Client -> Server
 *
 * Alle Gangs werden nach Level sortiert und mit Rang + Bedrohungslevel versehen.
 */
public class RequestGangListPacket {

    public RequestGangListPacket() {}

    public void encode(FriendlyByteBuf buf) {}

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

            // Alle Gangs nach Level sortieren
            List<Gang> allGangs = new ArrayList<>(manager.getAllGangs());
            allGangs.sort((a, b) -> {
                int cmp = Integer.compare(b.getGangLevel(), a.getGangLevel());
                if (cmp != 0) return cmp;
                return Integer.compare(b.getGangXP(), a.getGangXP());
            });

            // Eigene Gang fuer Bedrohungsberechnung
            Gang myGang = manager.getPlayerGang(player.getUUID());
            int myLevel = myGang != null ? myGang.getGangLevel() : 0;

            // Eintraege mit Rang und Bedrohungslevel
            List<SyncGangListPacket.GangListEntry> entries = new ArrayList<>();
            int rank = 0;
            for (Gang gang : allGangs) {
                rank++;

                // Eigene Gang wird mitgesendet (fuer Rangposition)
                GangReputation rep = GangReputation.getForLevel(gang.getGangLevel());

                int threatLevel = 0; // niedrig
                if (myGang != null && !gang.getGangId().equals(myGang.getGangId())) {
                    int levelDiff = gang.getGangLevel() - myLevel;
                    if (levelDiff >= 3 || (levelDiff >= 0 && gang.getMemberCount() > myGang.getMemberCount() + 2)) {
                        threatLevel = 2; // hoch
                    } else if (levelDiff >= -2 && levelDiff < 3) {
                        threatLevel = 1; // mittel
                    }
                }

                entries.add(new SyncGangListPacket.GangListEntry(
                        gang.getGangId(),
                        gang.getName(),
                        gang.getTag(),
                        gang.getGangLevel(),
                        gang.getMemberCount(),
                        gang.getMaxMembers(),
                        gang.getTerritoryCount(),
                        gang.getColor().ordinal(),
                        rep.getDisplayName(),
                        rank,
                        0, // rankChange - TODO: persistentes Tracking
                        threatLevel,
                        gang.getGangBalance()
                ));
            }

            GangNetworkHandler.sendToPlayer(new SyncGangListPacket(entries), player);
        });
        ctx.get().setPacketHandled(true);
    }
}
