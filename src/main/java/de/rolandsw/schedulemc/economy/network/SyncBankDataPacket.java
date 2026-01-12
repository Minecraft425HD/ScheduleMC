package de.rolandsw.schedulemc.economy.network;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.Transaction;
import de.rolandsw.schedulemc.economy.TransactionHistory;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet to synchronize bank data from server to client
 */
public class SyncBankDataPacket {
    private final double balance;
    private final List<TransactionData> transactions;
    private final double totalIncome;
    private final double totalExpenses;

    public SyncBankDataPacket(double balance, List<Transaction> transactions, double totalIncome, double totalExpenses) {
        this.balance = balance;
        this.transactions = new ArrayList<>();
        for (Transaction t : transactions) {
            this.transactions.add(new TransactionData(
                t.getId(),
                t.getSender(),
                t.getRecipient(),
                t.getAmount(),
                t.getTimestamp(),
                t.getDescription()
            ));
        }
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(balance);
        buf.writeInt(transactions.size());
        for (TransactionData t : transactions) {
            buf.writeUUID(t.id);
            buf.writeUUID(t.sender);
            buf.writeUUID(t.recipient);
            buf.writeDouble(t.amount);
            buf.writeLong(t.timestamp);
            buf.writeUtf(t.description);
        }
        buf.writeDouble(totalIncome);
        buf.writeDouble(totalExpenses);
    }

    public static SyncBankDataPacket decode(FriendlyByteBuf buf) {
        double balance = buf.readDouble();
        int size = buf.readInt();
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            UUID id = buf.readUUID();
            UUID sender = buf.readUUID();
            UUID recipient = buf.readUUID();
            double amount = buf.readDouble();
            long timestamp = buf.readLong();
            String description = buf.readUtf();
            transactions.add(new Transaction(id, sender, recipient, amount, timestamp, description));
        }
        double totalIncome = buf.readDouble();
        double totalExpenses = buf.readDouble();
        return new SyncBankDataPacket(balance, transactions, totalIncome, totalExpenses);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Update client-side cache in BankAppScreen
            // This will be handled by ClientBankDataCache
            ClientBankDataCache.updateData(balance, transactions, totalIncome, totalExpenses);
        });
        ctx.get().setPacketHandled(true);
    }

    private static class TransactionData {
        UUID id;
        UUID sender;
        UUID recipient;
        double amount;
        long timestamp;
        String description;

        TransactionData(UUID id, UUID sender, UUID recipient, double amount, long timestamp, String description) {
            this.id = id;
            this.sender = sender;
            this.recipient = recipient;
            this.amount = amount;
            this.timestamp = timestamp;
            this.description = description;
        }
    }
}
