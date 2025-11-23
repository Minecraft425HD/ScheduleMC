package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Synchronisiert Wanted-Level und Escape-Timer vom Server zum Client
 */
public class WantedLevelSyncPacket {
    private final int wantedLevel;
    private final long escapeTimeRemaining; // in Ticks

    public WantedLevelSyncPacket(int wantedLevel, long escapeTimeRemaining) {
        this.wantedLevel = wantedLevel;
        this.escapeTimeRemaining = escapeTimeRemaining;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(wantedLevel);
        buf.writeLong(escapeTimeRemaining);
    }

    public static WantedLevelSyncPacket decode(FriendlyByteBuf buf) {
        return new WantedLevelSyncPacket(
            buf.readInt(),
            buf.readLong()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-seitig speichern
            CrimeManager.setClientWantedLevel(wantedLevel);
            CrimeManager.setClientEscapeTime(escapeTimeRemaining);
        });
        ctx.get().setPacketHandled(true);
    }
}
