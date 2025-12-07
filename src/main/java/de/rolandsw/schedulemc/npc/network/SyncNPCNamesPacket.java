package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.client.ClientNPCNameCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Packet zum Synchronisieren der NPC-Namen vom Server zum Client
 * Wird verwendet um im Creator GUI doppelte Namen rot anzuzeigen
 */
public class SyncNPCNamesPacket {
    private final Set<String> npcNames;

    public SyncNPCNamesPacket(Set<String> npcNames) {
        this.npcNames = npcNames;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(npcNames.size());
        for (String name : npcNames) {
            buf.writeUtf(name);
        }
    }

    public static SyncNPCNamesPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<String> names = new HashSet<>();
        for (int i = 0; i < size; i++) {
            names.add(buf.readUtf());
        }
        return new SyncNPCNamesPacket(names);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-Seite: Update den lokalen Cache
            ClientNPCNameCache.setNPCNames(npcNames);
        });
        ctx.get().setPacketHandled(true);
    }
}
