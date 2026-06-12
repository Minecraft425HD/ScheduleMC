package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.network.AbstractPacket;
import de.rolandsw.schedulemc.npc.client.ClientKnownNPCCache;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * Delta: Der Spieler hat einen weiteren NPC kennengelernt.
 */
public class KnownNPCAddedPacket extends AbstractPacket {

    private final UUID npcDataId;

    public KnownNPCAddedPacket(UUID npcDataId) {
        this.npcDataId = npcDataId;
    }

    @Override
    protected void writeData(FriendlyByteBuf buf) {
        buf.writeUUID(npcDataId);
    }

    public static KnownNPCAddedPacket decode(FriendlyByteBuf buf) {
        return new KnownNPCAddedPacket(buf.readUUID());
    }

    @Override
    protected void handleOnClient() {
        ClientKnownNPCCache.addKnown(npcDataId);
    }
}
