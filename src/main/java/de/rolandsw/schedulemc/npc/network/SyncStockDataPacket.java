package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.network.ClientBankDataCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet to sync stock market data from server to client
 */
public class SyncStockDataPacket {
    private final double goldPrice;
    private final int goldTrend;
    private final double diamondPrice;
    private final int diamondTrend;
    private final double emeraldPrice;
    private final int emeraldTrend;
    private final double balance;

    public SyncStockDataPacket(double goldPrice, int goldTrend, double diamondPrice, int diamondTrend,
                               double emeraldPrice, int emeraldTrend, double balance) {
        this.goldPrice = goldPrice;
        this.goldTrend = goldTrend;
        this.diamondPrice = diamondPrice;
        this.diamondTrend = diamondTrend;
        this.emeraldPrice = emeraldPrice;
        this.emeraldTrend = emeraldTrend;
        this.balance = balance;
    }

    public static void encode(SyncStockDataPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.goldPrice);
        buf.writeInt(msg.goldTrend);
        buf.writeDouble(msg.diamondPrice);
        buf.writeInt(msg.diamondTrend);
        buf.writeDouble(msg.emeraldPrice);
        buf.writeInt(msg.emeraldTrend);
        buf.writeDouble(msg.balance);
    }

    public static SyncStockDataPacket decode(FriendlyByteBuf buf) {
        return new SyncStockDataPacket(
            buf.readDouble(),
            buf.readInt(),
            buf.readDouble(),
            buf.readInt(),
            buf.readDouble(),
            buf.readInt(),
            buf.readDouble()
        );
    }

    public static void handle(SyncStockDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Update client cache
            ClientBankDataCache.updateStockData(
                msg.goldPrice, msg.goldTrend,
                msg.diamondPrice, msg.diamondTrend,
                msg.emeraldPrice, msg.emeraldTrend
            );
            // Also update balance
            ClientBankDataCache.updateATMData(msg.balance, ClientBankDataCache.getWalletBalance());
        });
        ctx.get().setPacketHandled(true);
    }
}
