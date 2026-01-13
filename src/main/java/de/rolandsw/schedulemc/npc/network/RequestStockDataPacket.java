package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.npc.bank.StockMarketData;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Packet sent from client to server to request stock market data
 */
public class RequestStockDataPacket {

    public RequestStockDataPacket() {
    }

    public static void encode(RequestStockDataPacket msg, FriendlyByteBuf buf) {
        // No data needed
    }

    public static RequestStockDataPacket decode(FriendlyByteBuf buf) {
        return new RequestStockDataPacket();
    }

    public static void handle(RequestStockDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            MinecraftServer server = player.getServer();
            if (server == null) return;

            // Get stock market data
            StockMarketData stockMarket = StockMarketData.getInstance(server);

            double goldPrice = stockMarket.getCurrentPrice(Items.GOLD_INGOT);
            int goldTrend = stockMarket.getTrend(Items.GOLD_INGOT);

            double diamondPrice = stockMarket.getCurrentPrice(Items.DIAMOND);
            int diamondTrend = stockMarket.getTrend(Items.DIAMOND);

            double emeraldPrice = stockMarket.getCurrentPrice(Items.EMERALD);
            int emeraldTrend = stockMarket.getTrend(Items.EMERALD);

            double balance = EconomyManager.getBalance(player.getUUID());

            // Send response
            SyncStockDataPacket response = new SyncStockDataPacket(
                goldPrice, goldTrend,
                diamondPrice, diamondTrend,
                emeraldPrice, emeraldTrend,
                balance
            );

            NPCNetworkHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                response
            );
        });
    }
}
