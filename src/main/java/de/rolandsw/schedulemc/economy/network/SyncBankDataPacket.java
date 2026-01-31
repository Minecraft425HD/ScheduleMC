package de.rolandsw.schedulemc.economy.network;

import de.rolandsw.schedulemc.economy.Transaction;
import de.rolandsw.schedulemc.economy.TransactionType;
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
                t.getTransactionId(),
                t.getType(),
                t.getFromPlayer(),
                t.getToPlayer(),
                t.getAmount(),
                t.getTimestamp(),
                t.getDescription(),
                t.getBalanceAfter()
            ));
        }
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
    }

    private SyncBankDataPacket(double balance, List<TransactionData> transactions, double totalIncome, double totalExpenses, boolean internal) {
        this.balance = balance;
        this.transactions = transactions;
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(balance);
        buf.writeInt(transactions.size());
        for (TransactionData t : transactions) {
            buf.writeUtf(t.transactionId);
            buf.writeEnum(t.type);
            buf.writeBoolean(t.fromPlayer != null);
            if (t.fromPlayer != null) {
                buf.writeUUID(t.fromPlayer);
            }
            buf.writeBoolean(t.toPlayer != null);
            if (t.toPlayer != null) {
                buf.writeUUID(t.toPlayer);
            }
            buf.writeDouble(t.amount);
            buf.writeLong(t.timestamp);
            buf.writeUtf(t.description != null ? t.description : "");
            buf.writeDouble(t.balanceAfter);
        }
        buf.writeDouble(totalIncome);
        buf.writeDouble(totalExpenses);
    }

    public static SyncBankDataPacket decode(FriendlyByteBuf buf) {
        double balance = buf.readDouble();
        int size = buf.readInt();
        List<TransactionData> transactionDataList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String transactionId = buf.readUtf();
            TransactionType type = buf.readEnum(TransactionType.class);
            UUID fromPlayer = buf.readBoolean() ? buf.readUUID() : null;
            UUID toPlayer = buf.readBoolean() ? buf.readUUID() : null;
            double amount = buf.readDouble();
            long timestamp = buf.readLong();
            String description = buf.readUtf();
            double balanceAfter = buf.readDouble();
            transactionDataList.add(new TransactionData(transactionId, type, fromPlayer, toPlayer, amount, timestamp, description, balanceAfter));
        }
        double totalIncome = buf.readDouble();
        double totalExpenses = buf.readDouble();
        return new SyncBankDataPacket(balance, transactionDataList, totalIncome, totalExpenses, true);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Convert TransactionData back to Transaction for the cache
            List<Transaction> transactionList = new ArrayList<>();
            for (TransactionData td : transactions) {
                transactionList.add(new Transaction(
                    td.type,
                    td.fromPlayer,
                    td.toPlayer,
                    td.amount,
                    td.description,
                    td.balanceAfter
                ));
            }
            ClientBankDataCache.updateData(balance, transactionList, totalIncome, totalExpenses);
        });
        ctx.get().setPacketHandled(true);
    }

    private static class TransactionData {
        final String transactionId;
        final TransactionType type;
        final UUID fromPlayer;
        final UUID toPlayer;
        final double amount;
        final long timestamp;
        final String description;
        final double balanceAfter;

        TransactionData(String transactionId, TransactionType type, UUID fromPlayer, UUID toPlayer,
                       double amount, long timestamp, String description, double balanceAfter) {
            this.transactionId = transactionId;
            this.type = type;
            this.fromPlayer = fromPlayer;
            this.toPlayer = toPlayer;
            this.amount = amount;
            this.timestamp = timestamp;
            this.description = description;
            this.balanceAfter = balanceAfter;
        }
    }
}
