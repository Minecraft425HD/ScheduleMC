package de.rolandsw.schedulemc.npc.crime.prison.network;

import de.rolandsw.schedulemc.npc.crime.prison.client.PrisonScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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

    public static ClosePrisonScreenPacket decode(FriendlyByteBuf buf) {
        return new ClosePrisonScreenPacket(buf.readUtf());
    }

    public static void handle(ClosePrisonScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft mc = Minecraft.getInstance();

                if (mc.screen instanceof PrisonScreen prisonScreen) {
                    prisonScreen.allowClose();
                }

                if (mc.player != null) {
                    mc.player.displayClientMessage(
                        Component.literal("§a✓ ENTLASSEN: " + msg.reason),
                        false
                    );
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
