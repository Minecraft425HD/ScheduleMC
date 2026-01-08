package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.network.AbstractPacket;
import de.rolandsw.schedulemc.npc.client.ClientNPCNameCache;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Set;

/**
 * Packet zum Synchronisieren der NPC-Namen vom Server zum Client
 * Wird verwendet um im Creator GUI doppelte Namen rot anzuzeigen
 *
 * OPTIMIERT: Nutzt AbstractPacket für reduzierte Code-Duplikation
 */
public class SyncNPCNamesPacket extends AbstractPacket {
    private final Set<String> npcNames;

    public SyncNPCNamesPacket(Set<String> npcNames) {
        this.npcNames = npcNames;
    }

    @Override
    protected void writeData(FriendlyByteBuf buf) {
        writeStringSet(buf, npcNames);
    }

    public static SyncNPCNamesPacket decode(FriendlyByteBuf buf) {
        return new SyncNPCNamesPacket(readStringSet(buf));
    }

    @Override
    protected void handleOnClient() {
        ClientNPCNameCache.setNPCNames(npcNames);
    }
}
