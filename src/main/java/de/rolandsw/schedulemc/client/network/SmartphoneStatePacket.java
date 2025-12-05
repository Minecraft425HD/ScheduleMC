package de.rolandsw.schedulemc.client.network;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.client.SmartphoneTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet um dem Server mitzuteilen, ob ein Spieler das Smartphone-GUI offen hat
 */
public class SmartphoneStatePacket {
    private final boolean isOpen;

    public SmartphoneStatePacket(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(isOpen);
    }

    public static SmartphoneStatePacket decode(FriendlyByteBuf buf) {
        return new SmartphoneStatePacket(buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // Aktualisiere den Server-seitigen Tracker
                SmartphoneTracker.setSmartphoneOpen(player.getUUID(), isOpen);

                ScheduleMC.LOGGER.debug("Player {} smartphone state: {}",
                    player.getName().getString(), isOpen ? "OPEN" : "CLOSED");
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
