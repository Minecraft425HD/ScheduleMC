package de.rolandsw.schedulemc.territory.network;

import de.rolandsw.schedulemc.territory.Territory;
import de.rolandsw.schedulemc.territory.TerritoryType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Delta-Update Packet für einzelne Territory-Änderungen
 *
 * Performance-Optimierung:
 * VORHER: SyncTerritoriesPacket sendet ALLE Territories (kann 1000+ sein)
 * NACHHER: Nur geänderte/neue/gelöschte Territories senden
 *
 * Paketgröße:
 * - Full Sync: ~40 bytes * Anzahl Territories (z.B. 40KB bei 1000 Territories)
 * - Delta Update: ~40 bytes pro Änderung (99%+ Reduktion bei einzelnen Änderungen)
 */
public class SyncTerritoryDeltaPacket {

    private final long chunkKey;
    private final DeltaType deltaType;

    // Nur bei SET/UPDATE verwendet
    @Nullable private final Integer chunkX;
    @Nullable private final Integer chunkZ;
    @Nullable private final TerritoryType type;
    @Nullable private final String name;

    /**
     * Erstellt ein Delta-Paket für Territory-Update
     */
    public static SyncTerritoryDeltaPacket update(Territory territory) {
        return new SyncTerritoryDeltaPacket(
            Territory.getChunkKey(territory.getChunkX(), territory.getChunkZ()),
            DeltaType.SET,
            territory.getChunkX(),
            territory.getChunkZ(),
            territory.getType(),
            territory.getName()
        );
    }

    /**
     * Erstellt ein Delta-Paket für Territory-Löschung
     */
    public static SyncTerritoryDeltaPacket remove(int chunkX, int chunkZ) {
        return new SyncTerritoryDeltaPacket(
            Territory.getChunkKey(chunkX, chunkZ),
            DeltaType.REMOVE,
            null, null, null, null
        );
    }

    private SyncTerritoryDeltaPacket(long chunkKey, DeltaType deltaType,
                                     @Nullable Integer chunkX, @Nullable Integer chunkZ,
                                     @Nullable TerritoryType type, @Nullable String name) {
        this.chunkKey = chunkKey;
        this.deltaType = deltaType;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.type = type;
        this.name = name;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(chunkKey);
        buf.writeEnum(deltaType);

        if (deltaType == DeltaType.SET) {
            buf.writeInt(chunkX);
            buf.writeInt(chunkZ);
            buf.writeEnum(type);
            buf.writeUtf(name);
        }
        // Bei REMOVE: nur chunkKey nötig (bereits geschrieben)
    }

    /**
     * SICHERHEIT: Max-Länge für Territory-Name gegen DoS/Memory-Angriffe
     */
    public static SyncTerritoryDeltaPacket decode(FriendlyByteBuf buf) {
        long chunkKey = buf.readLong();
        DeltaType deltaType = buf.readEnum(DeltaType.class);

        if (deltaType == DeltaType.SET) {
            int chunkX = buf.readInt();
            int chunkZ = buf.readInt();
            TerritoryType type = buf.readEnum(TerritoryType.class);
            String name = buf.readUtf(64); // Max 64 Zeichen für Territory-Name

            return new SyncTerritoryDeltaPacket(chunkKey, deltaType, chunkX, chunkZ, type, name);
        } else {
            return new SyncTerritoryDeltaPacket(chunkKey, deltaType, null, null, null, null);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-seitig: Update territoryCache in WorldMapScreen
            if (deltaType == DeltaType.SET) {
                SyncTerritoriesPacket.TerritoryClientCache.updateSingle(
                    chunkKey,
                    new SyncTerritoriesPacket.TerritoryData(chunkX, chunkZ, type, name)
                );
            } else if (deltaType == DeltaType.REMOVE) {
                SyncTerritoriesPacket.TerritoryClientCache.removeSingle(chunkKey);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum DeltaType {
        SET,    // Territory hinzufügen oder aktualisieren
        REMOVE  // Territory entfernen
    }
}
