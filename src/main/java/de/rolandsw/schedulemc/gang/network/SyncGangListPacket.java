package de.rolandsw.schedulemc.gang.network;

import de.rolandsw.schedulemc.gang.client.ClientGangCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

/**
 * Server sendet Liste aller Gangs an den Client (fuer Rivalen-Seite in der App).
 * Server -> Client
 *
 * Erweitert mit Rival-Daten: Rang, Rang-Aenderung, Bedrohungslevel.
 */
public class SyncGangListPacket {

    private final List<GangListEntry> entries;

    public SyncGangListPacket(List<GangListEntry> entries) {
        this.entries = entries;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entries.size());
        for (GangListEntry e : entries) {
            buf.writeUUID(e.gangId);
            buf.writeUtf(e.name, 64);
            buf.writeUtf(e.tag, 10);
            buf.writeInt(e.level);
            buf.writeInt(e.memberCount);
            buf.writeInt(e.maxMembers);
            buf.writeInt(e.territoryCount);
            buf.writeInt(e.colorOrdinal);
            buf.writeUtf(e.reputationName, 32);
            // Rival-Felder
            buf.writeInt(e.rank);
            buf.writeInt(e.rankChange);
            buf.writeInt(e.threatLevel);
            buf.writeInt(e.balance);
        }
    }

    public static SyncGangListPacket decode(FriendlyByteBuf buf) {
        int count = Math.min(buf.readInt(), 100);
        List<GangListEntry> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(new GangListEntry(
                    buf.readUUID(),
                    buf.readUtf(64),
                    buf.readUtf(10),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readUtf(32),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt()
            ));
        }
        return new SyncGangListPacket(list);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient());
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        ClientGangCache.updateGangList(entries);
    }

    public List<GangListEntry> getEntries() {
        return entries;
    }

    /**
     * Zusammenfassung einer Gang fuer die Rivalen-/Listenansicht.
     *
     * rank: Position im Ranking (1 = bester)
     * rankChange: Aenderung seit letzter Woche (-1=abgestiegen, 0=gleich, +1=aufgestiegen)
     * threatLevel: 0=niedrig, 1=mittel, 2=hoch (basierend auf Level-Differenz)
     * balance: Ungefaehres Gang-Guthaben
     */
    public record GangListEntry(UUID gangId, String name, String tag,
                                 int level, int memberCount, int maxMembers,
                                 int territoryCount, int colorOrdinal,
                                 String reputationName,
                                 int rank, int rankChange, int threatLevel,
                                 int balance) {}
}
