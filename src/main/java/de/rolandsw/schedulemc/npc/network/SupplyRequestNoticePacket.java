package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.network.AbstractPacket;
import de.rolandsw.schedulemc.npc.client.ClientSupplyRequestCache;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * Zeigt/versteckt den "!"-Indikator über einem NPC, der eine offene
 * Warenanfrage an diesen Spieler hat.
 */
public class SupplyRequestNoticePacket extends AbstractPacket {

    private final UUID npcDataId;
    private final boolean active;

    public SupplyRequestNoticePacket(UUID npcDataId, boolean active) {
        this.npcDataId = npcDataId;
        this.active = active;
    }

    @Override
    protected void writeData(FriendlyByteBuf buf) {
        buf.writeUUID(npcDataId);
        buf.writeBoolean(active);
    }

    public static SupplyRequestNoticePacket decode(FriendlyByteBuf buf) {
        return new SupplyRequestNoticePacket(buf.readUUID(), buf.readBoolean());
    }

    @Override
    protected void handleOnClient() {
        if (active) {
            ClientSupplyRequestCache.add(npcDataId);
        } else {
            ClientSupplyRequestCache.remove(npcDataId);
        }
    }
}
