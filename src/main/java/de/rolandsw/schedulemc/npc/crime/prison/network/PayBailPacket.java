package de.rolandsw.schedulemc.npc.crime.prison.network;

import de.rolandsw.schedulemc.npc.crime.prison.PrisonManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Spieler möchte Kaution zahlen
 */
public class PayBailPacket {

    public PayBailPacket() {}

    public static void encode(PayBailPacket msg, FriendlyByteBuf buf) {
        // Keine Daten nötig
    }

    public static PayBailPacket decode(FriendlyByteBuf buf) {
        return new PayBailPacket();
    }

    public static void handle(PayBailPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                PrisonManager.getInstance().payBail(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
