package de.rolandsw.schedulemc.territory.network;

import de.rolandsw.schedulemc.territory.Territory;
import de.rolandsw.schedulemc.territory.TerritoryType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Synchronisiert alle Territories vom Server zum Client
 */
public class SyncTerritoriesPacket {

    private Map<Long, TerritoryData> territories;

    public SyncTerritoriesPacket(Map<Long, Territory> territories) {
        this.territories = new HashMap<>();
        for (Map.Entry<Long, Territory> entry : territories.entrySet()) {
            Territory t = entry.getValue();
            this.territories.put(entry.getKey(), new TerritoryData(
                t.getChunkX(), t.getChunkZ(), t.getType(), t.getName()
            ));
        }
    }

    private SyncTerritoriesPacket() {
        this.territories = new HashMap<>();
    }

    public static void encode(SyncTerritoriesPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.territories.size());
        for (Map.Entry<Long, TerritoryData> entry : msg.territories.entrySet()) {
            buf.writeLong(entry.getKey());
            TerritoryData data = entry.getValue();
            buf.writeInt(data.chunkX);
            buf.writeInt(data.chunkZ);
            buf.writeEnum(data.type);
            buf.writeUtf(data.name);
        }
    }

    /**
     * SICHERHEIT: Max-Länge für Territory-Name + max Territory count gegen DoS
     */
    public static SyncTerritoriesPacket decode(FriendlyByteBuf buf) {
        SyncTerritoriesPacket packet = new SyncTerritoriesPacket();
        int size = buf.readInt();

        // SICHERHEIT: Max 10000 Territories pro Sync (DoS-Prävention)
        if (size > 10000) size = 10000;

        for (int i = 0; i < size; i++) {
            long key = buf.readLong();
            int chunkX = buf.readInt();
            int chunkZ = buf.readInt();
            TerritoryType type = buf.readEnum(TerritoryType.class);
            String name = buf.readUtf(64); // Max 64 Zeichen für Territory-Name

            packet.territories.put(key, new TerritoryData(chunkX, chunkZ, type, name));
        }

        return packet;
    }

    public static void handle(SyncTerritoriesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-seitig: Update territoryCache in WorldMapScreen
            TerritoryClientCache.updateCache(msg.territories);
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Einfache Datenklasse für Territory-Sync
     */
    public static class TerritoryData {
        public final int chunkX;
        public final int chunkZ;
        public final TerritoryType type;
        public final String name;

        public TerritoryData(int chunkX, int chunkZ, TerritoryType type, String name) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.type = type;
            this.name = name;
        }
    }

    /**
     * Client-seitiger Cache für Territories
     * Unterstützt sowohl Full Sync als auch Delta Updates
     */
    public static class TerritoryClientCache {
        private static Map<Long, TerritoryData> cache = new ConcurrentHashMap<>();

        /**
         * Full Sync: Ersetzt kompletten Cache
         */
        public static void updateCache(Map<Long, TerritoryData> territories) {
            cache = new ConcurrentHashMap<>(territories);
        }

        /**
         * Delta Update: Aktualisiert einzelnes Territory
         * Performance-Optimierung für SyncTerritoryDeltaPacket
         */
        public static void updateSingle(long chunkKey, TerritoryData territory) {
            cache.put(chunkKey, territory);
        }

        /**
         * Delta Update: Entfernt einzelnes Territory
         * Performance-Optimierung für SyncTerritoryDeltaPacket
         */
        public static void removeSingle(long chunkKey) {
            cache.remove(chunkKey);
        }

        public static Map<Long, TerritoryData> getCache() {
            return cache;
        }

        public static void clear() {
            cache.clear();
        }
    }
}
