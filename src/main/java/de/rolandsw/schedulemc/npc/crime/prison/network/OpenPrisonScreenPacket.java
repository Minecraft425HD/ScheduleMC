package de.rolandsw.schedulemc.npc.crime.prison.network;

import de.rolandsw.schedulemc.npc.crime.prison.client.PrisonScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Öffnet das Gefängnis-GUI auf dem Client
 */
public class OpenPrisonScreenPacket {

    private final int cellNumber;
    private final long totalSentenceTicks;
    private final long releaseTime;
    private final double bailAmount;
    private final double playerBalance;
    private final long bailAvailableAtTick;

    public OpenPrisonScreenPacket(int cellNumber, long totalSentenceTicks, long releaseTime,
                                   double bailAmount, double playerBalance, long bailAvailableAtTick) {
        this.cellNumber = cellNumber;
        this.totalSentenceTicks = totalSentenceTicks;
        this.releaseTime = releaseTime;
        this.bailAmount = bailAmount;
        this.playerBalance = playerBalance;
        this.bailAvailableAtTick = bailAvailableAtTick;
    }

    public static void encode(OpenPrisonScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.cellNumber);
        buf.writeLong(msg.totalSentenceTicks);
        buf.writeLong(msg.releaseTime);
        buf.writeDouble(msg.bailAmount);
        buf.writeDouble(msg.playerBalance);
        buf.writeLong(msg.bailAvailableAtTick);
    }

    public static OpenPrisonScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenPrisonScreenPacket(
            buf.readInt(),
            buf.readLong(),
            buf.readLong(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readLong()
        );
    }

    public static void handle(OpenPrisonScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                PrisonScreen.open(
                    msg.cellNumber,
                    msg.totalSentenceTicks,
                    msg.releaseTime,
                    msg.bailAmount,
                    msg.playerBalance,
                    msg.bailAvailableAtTick
                );
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
