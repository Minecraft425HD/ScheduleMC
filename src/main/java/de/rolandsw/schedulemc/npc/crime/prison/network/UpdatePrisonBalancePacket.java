package de.rolandsw.schedulemc.npc.crime.prison.network;

import de.rolandsw.schedulemc.npc.crime.prison.client.PrisonScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Aktualisiert den Kontostand im Gefängnis-GUI
 */
public class UpdatePrisonBalancePacket {

    private final double newBalance;

    public UpdatePrisonBalancePacket(double newBalance) {
        this.newBalance = newBalance;
    }

    public static void encode(UpdatePrisonBalancePacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.newBalance);
    }

    public static UpdatePrisonBalancePacket decode(FriendlyByteBuf buf) {
        return new UpdatePrisonBalancePacket(buf.readDouble());
    }

    public static void handle(UpdatePrisonBalancePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.screen instanceof PrisonScreen prisonScreen) {
                    prisonScreen.updateBalance(msg.newBalance);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
