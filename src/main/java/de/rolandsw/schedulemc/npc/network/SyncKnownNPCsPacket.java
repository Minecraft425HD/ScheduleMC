package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.network.AbstractPacket;
import de.rolandsw.schedulemc.npc.client.ClientKnownNPCCache;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Voll-Sync der bekannten NPCs beim Login (NPC-Data-UUIDs).
 */
public class SyncKnownNPCsPacket extends AbstractPacket {

    private final Set<UUID> knownNpcIds;

    public SyncKnownNPCsPacket(Set<UUID> knownNpcIds) {
        this.knownNpcIds = knownNpcIds;
    }

    @Override
    protected void writeData(FriendlyByteBuf buf) {
        buf.writeVarInt(knownNpcIds.size());
        for (UUID id : knownNpcIds) {
            buf.writeUUID(id);
        }
    }

    public static SyncKnownNPCsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Set<UUID> ids = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(buf.readUUID());
        }
        return new SyncKnownNPCsPacket(ids);
    }

    @Override
    protected void handleOnClient() {
        ClientKnownNPCCache.setAll(knownNpcIds);
    }
}
