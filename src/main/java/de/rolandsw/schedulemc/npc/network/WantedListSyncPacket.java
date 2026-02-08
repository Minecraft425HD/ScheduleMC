package de.rolandsw.schedulemc.npc.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Feature 5: Fahndungsplakate - Netzwerk-Paket
 *
 * Synchronisiert die Liste gesuchter Spieler vom Server zum Client
 * fuer die Wanted-Posters App auf dem Smartphone.
 */
public class WantedListSyncPacket {

    /** Gesuchte Spieler: UUID -> WantedInfo */
    private final Map<UUID, WantedPlayerInfo> wantedPlayers;

    public static class WantedPlayerInfo {
        public final String playerName;
        public final int wantedLevel;
        public final double bountyAmount;

        public WantedPlayerInfo(String playerName, int wantedLevel, double bountyAmount) {
            this.playerName = playerName;
            this.wantedLevel = wantedLevel;
            this.bountyAmount = bountyAmount;
        }
    }

    public WantedListSyncPacket(Map<UUID, WantedPlayerInfo> wantedPlayers) {
        this.wantedPlayers = wantedPlayers;
    }

    /**
     * Liest Paket aus Buffer
     */
    public WantedListSyncPacket(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        this.wantedPlayers = new HashMap<>(count);

        for (int i = 0; i < count; i++) {
            UUID uuid = buf.readUUID();
            String name = buf.readUtf(64);
            int level = buf.readVarInt();
            double bounty = buf.readDouble();
            wantedPlayers.put(uuid, new WantedPlayerInfo(name, level, bounty));
        }
    }

    /**
     * Schreibt Paket in Buffer
     */
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(wantedPlayers.size());

        for (Map.Entry<UUID, WantedPlayerInfo> entry : wantedPlayers.entrySet()) {
            buf.writeUUID(entry.getKey());
            buf.writeUtf(entry.getValue().playerName, 64);
            buf.writeVarInt(entry.getValue().wantedLevel);
            buf.writeDouble(entry.getValue().bountyAmount);
        }
    }

    /**
     * Client-seitige Verarbeitung
     */
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Speichere in Client-Cache fuer Smartphone-App
            WantedListClientCache.updateCache(wantedPlayers);
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Client-seitiger Cache fuer Fahndungsliste
     */
    public static class WantedListClientCache {
        private static volatile Map<UUID, WantedPlayerInfo> cachedList = Map.of();

        public static void updateCache(Map<UUID, WantedPlayerInfo> newList) {
            cachedList = Map.copyOf(newList);
        }

        public static Map<UUID, WantedPlayerInfo> getCache() {
            return cachedList;
        }

        public static int getWantedCount() {
            return cachedList.size();
        }
    }
}
