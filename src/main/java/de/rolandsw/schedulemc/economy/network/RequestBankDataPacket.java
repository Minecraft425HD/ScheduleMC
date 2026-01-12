package de.rolandsw.schedulemc.economy.network;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.Transaction;
import de.rolandsw.schedulemc.economy.TransactionHistory;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet sent from client to server to request bank data synchronization
 */
public class RequestBankDataPacket {

    public RequestBankDataPacket() {
    }

    public void encode(FriendlyByteBuf buf) {
        // No data to encode
    }

    public static RequestBankDataPacket decode(FriendlyByteBuf buf) {
        return new RequestBankDataPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            sendBankDataToClient(player);
        });
    }

    private void sendBankDataToClient(ServerPlayer player) {
        UUID playerUUID = player.getUUID();

        // Get balance
        double balance = EconomyManager.getBalance(playerUUID);

        // Get transaction history
        TransactionHistory history = TransactionHistory.getInstance();
        List<Transaction> transactions = List.of();
        double totalIncome = 0.0;
        double totalExpenses = 0.0;

        if (history != null) {
            transactions = history.getRecentTransactions(playerUUID, 20);
            totalIncome = history.getTotalIncome(playerUUID);
            totalExpenses = history.getTotalExpenses(playerUUID);
        }

        // Send data back to client
        SyncBankDataPacket responsePacket = new SyncBankDataPacket(balance, transactions, totalIncome, totalExpenses);
        de.rolandsw.schedulemc.npc.network.NPCNetworkHandler.INSTANCE.sendTo(responsePacket, player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
    }
}
