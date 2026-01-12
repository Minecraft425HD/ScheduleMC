package de.rolandsw.schedulemc.npc.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum Synchronisieren von Kredit-Daten
 * Server → Client
 */
public class SyncCreditDataPacket {
    private final int creditScore;
    private final int ratingOrdinal;
    private final boolean hasActiveLoan;
    private final String loanType;
    private final double remaining;
    private final double daily;
    private final int progress;
    private final int remainingDays;

    public SyncCreditDataPacket(int creditScore, int ratingOrdinal, boolean hasActiveLoan,
                                 String loanType, double remaining, double daily,
                                 int progress, int remainingDays) {
        this.creditScore = creditScore;
        this.ratingOrdinal = ratingOrdinal;
        this.hasActiveLoan = hasActiveLoan;
        this.loanType = loanType;
        this.remaining = remaining;
        this.daily = daily;
        this.progress = progress;
        this.remainingDays = remainingDays;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(creditScore);
        buf.writeInt(ratingOrdinal);
        buf.writeBoolean(hasActiveLoan);
        buf.writeUtf(loanType);
        buf.writeDouble(remaining);
        buf.writeDouble(daily);
        buf.writeInt(progress);
        buf.writeInt(remainingDays);
    }

    /**
     * SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
     */
    public static SyncCreditDataPacket decode(FriendlyByteBuf buf) {
        return new SyncCreditDataPacket(
            buf.readInt(),
            buf.readInt(),
            buf.readBoolean(),
            buf.readUtf(64), // Loan type max 64 chars
            buf.readDouble(),
            buf.readDouble(),
            buf.readInt(),
            buf.readInt()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientCreditScreenHandler.updateCreditData(
                    creditScore,
                    ratingOrdinal,
                    hasActiveLoan,
                    loanType,
                    remaining,
                    daily,
                    progress,
                    remainingDays
                );
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
