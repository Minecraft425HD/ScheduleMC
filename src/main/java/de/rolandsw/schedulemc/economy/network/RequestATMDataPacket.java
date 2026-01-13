package de.rolandsw.schedulemc.economy.network;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.items.CashItem;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Packet sent from client to server to request ATM data
 * Server responds with SyncATMDataPacket
 */
public class RequestATMDataPacket {

    public RequestATMDataPacket() {
    }

    public static void encode(RequestATMDataPacket msg, FriendlyByteBuf buf) {
        // No data needed
    }

    public static RequestATMDataPacket decode(FriendlyByteBuf buf) {
        return new RequestATMDataPacket();
    }

    public static void handle(RequestATMDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Get player's bank data
            double balance = EconomyManager.getBalance(player.getUUID());

            // Get wallet balance
            double walletBalance = 0.0;
            ItemStack wallet = player.getInventory().getItem(8);
            if (wallet.getItem() instanceof CashItem) {
                walletBalance = CashItem.getValue(wallet);
            }

            // Send response
            SyncATMDataPacket response = new SyncATMDataPacket(
                balance,
                walletBalance,
                true,
                ""
            );
            EconomyNetworkHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                response
            );
        });
    }
}
