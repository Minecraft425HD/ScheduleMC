package de.rolandsw.schedulemc.npc.crime.prison.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Schließt das Gefängnis-GUI (Entlassung)
 */
public class ClosePrisonScreenPacket {

    private final String reason;

    public ClosePrisonScreenPacket(String reason) {
        this.reason = reason;
    }

    public static void encode(ClosePrisonScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.reason);
    }

    /**
     * SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
     */
    public static ClosePrisonScreenPacket decode(FriendlyByteBuf buf) {
        return new ClosePrisonScreenPacket(buf.readUtf(256)); // Release reason max 256 chars
    }

    public static void handle(ClosePrisonScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientPrisonScreenHandler.closePrisonScreen(msg.reason);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
