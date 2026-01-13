package de.rolandsw.schedulemc.economy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet to sync ATM data from server to client
 * Sent after ATM transactions to update client-side display
 */
public class SyncATMDataPacket {
    private final double balance;
    private final double walletBalance;
    private final boolean success;
    private final String message;

    public SyncATMDataPacket(double balance, double walletBalance, boolean success, String message) {
        this.balance = balance;
        this.walletBalance = walletBalance;
        this.success = success;
        this.message = message;
    }

    public static void encode(SyncATMDataPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.balance);
        buf.writeDouble(msg.walletBalance);
        buf.writeBoolean(msg.success);
        buf.writeUtf(msg.message);
    }

    public static SyncATMDataPacket decode(FriendlyByteBuf buf) {
        double balance = buf.readDouble();
        double walletBalance = buf.readDouble();
        boolean success = buf.readBoolean();
        String message = buf.readUtf();
        return new SyncATMDataPacket(balance, walletBalance, success, message);
    }

    public static void handle(SyncATMDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Update client cache
            ClientBankDataCache.updateATMData(msg.balance, msg.walletBalance);
        });
        ctx.get().setPacketHandled(true);
    }

    public double getBalance() {
        return balance;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
