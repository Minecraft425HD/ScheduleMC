package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.network.ClientBankDataCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
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

    // History & Statistics
    private final List<Double> goldHistory;
    private final double goldHigh, goldLow, goldAvg;
    private final List<Double> diamondHistory;
    private final double diamondHigh, diamondLow, diamondAvg;
    private final List<Double> emeraldHistory;
    private final double emeraldHigh, emeraldLow, emeraldAvg;

    public SyncStockDataPacket(double goldPrice, int goldTrend, double diamondPrice, int diamondTrend,
                               double emeraldPrice, int emeraldTrend, double balance,
                               List<Double> goldHistory, double goldHigh, double goldLow, double goldAvg,
                               List<Double> diamondHistory, double diamondHigh, double diamondLow, double diamondAvg,
                               List<Double> emeraldHistory, double emeraldHigh, double emeraldLow, double emeraldAvg) {
        this.goldPrice = goldPrice;
        this.goldTrend = goldTrend;
        this.diamondPrice = diamondPrice;
        this.diamondTrend = diamondTrend;
        this.emeraldPrice = emeraldPrice;
        this.emeraldTrend = emeraldTrend;
        this.balance = balance;

        this.goldHistory = goldHistory;
        this.goldHigh = goldHigh;
        this.goldLow = goldLow;
        this.goldAvg = goldAvg;

        this.diamondHistory = diamondHistory;
        this.diamondHigh = diamondHigh;
        this.diamondLow = diamondLow;
        this.diamondAvg = diamondAvg;

        this.emeraldHistory = emeraldHistory;
        this.emeraldHigh = emeraldHigh;
        this.emeraldLow = emeraldLow;
        this.emeraldAvg = emeraldAvg;
    }

    public static void encode(SyncStockDataPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.goldPrice);
        buf.writeInt(msg.goldTrend);
        buf.writeDouble(msg.diamondPrice);
        buf.writeInt(msg.diamondTrend);
        buf.writeDouble(msg.emeraldPrice);
        buf.writeInt(msg.emeraldTrend);
        buf.writeDouble(msg.balance);

        // Gold history & stats
        buf.writeInt(msg.goldHistory.size());
        for (double price : msg.goldHistory) {
            buf.writeDouble(price);
        }
        buf.writeDouble(msg.goldHigh);
        buf.writeDouble(msg.goldLow);
        buf.writeDouble(msg.goldAvg);

        // Diamond history & stats
        buf.writeInt(msg.diamondHistory.size());
        for (double price : msg.diamondHistory) {
            buf.writeDouble(price);
        }
        buf.writeDouble(msg.diamondHigh);
        buf.writeDouble(msg.diamondLow);
        buf.writeDouble(msg.diamondAvg);

        // Emerald history & stats
        buf.writeInt(msg.emeraldHistory.size());
        for (double price : msg.emeraldHistory) {
            buf.writeDouble(price);
        }
        buf.writeDouble(msg.emeraldHigh);
        buf.writeDouble(msg.emeraldLow);
        buf.writeDouble(msg.emeraldAvg);
    }

    public static SyncStockDataPacket decode(FriendlyByteBuf buf) {
        double goldPrice = buf.readDouble();
        int goldTrend = buf.readInt();
        double diamondPrice = buf.readDouble();
        int diamondTrend = buf.readInt();
        double emeraldPrice = buf.readDouble();
        int emeraldTrend = buf.readInt();
        double balance = buf.readDouble();

        // Gold history & stats
        int goldHistorySize = buf.readInt();
        List<Double> goldHistory = new ArrayList<>();
        for (int i = 0; i < goldHistorySize; i++) {
            goldHistory.add(buf.readDouble());
        }
        double goldHigh = buf.readDouble();
        double goldLow = buf.readDouble();
        double goldAvg = buf.readDouble();

        // Diamond history & stats
        int diamondHistorySize = buf.readInt();
        List<Double> diamondHistory = new ArrayList<>();
        for (int i = 0; i < diamondHistorySize; i++) {
            diamondHistory.add(buf.readDouble());
        }
        double diamondHigh = buf.readDouble();
        double diamondLow = buf.readDouble();
        double diamondAvg = buf.readDouble();

        // Emerald history & stats
        int emeraldHistorySize = buf.readInt();
        List<Double> emeraldHistory = new ArrayList<>();
        for (int i = 0; i < emeraldHistorySize; i++) {
            emeraldHistory.add(buf.readDouble());
        }
        double emeraldHigh = buf.readDouble();
        double emeraldLow = buf.readDouble();
        double emeraldAvg = buf.readDouble();

        return new SyncStockDataPacket(
            goldPrice, goldTrend, diamondPrice, diamondTrend, emeraldPrice, emeraldTrend, balance,
            goldHistory, goldHigh, goldLow, goldAvg,
            diamondHistory, diamondHigh, diamondLow, diamondAvg,
            emeraldHistory, emeraldHigh, emeraldLow, emeraldAvg
        );
    }

    public static void handle(SyncStockDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Update client cache with full stock data
            ClientBankDataCache.updateStockDataFull(
                msg.goldPrice, msg.goldTrend, msg.goldHistory, msg.goldHigh, msg.goldLow, msg.goldAvg,
                msg.diamondPrice, msg.diamondTrend, msg.diamondHistory, msg.diamondHigh, msg.diamondLow, msg.diamondAvg,
                msg.emeraldPrice, msg.emeraldTrend, msg.emeraldHistory, msg.emeraldHigh, msg.emeraldLow, msg.emeraldAvg
            );
            // Also update balance
            ClientBankDataCache.updateATMData(msg.balance, ClientBankDataCache.getWalletBalance());
        });
        ctx.get().setPacketHandled(true);
    }
}
