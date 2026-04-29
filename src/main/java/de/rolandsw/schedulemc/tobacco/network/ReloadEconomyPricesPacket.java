package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.economy.EconomyController;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C→S Packet: Spieler (Operator) signalisiert dem Server, die EconomyController-Preise
 * aus der gespeicherten Config neu zu laden.
 * Wird vom EconomyPricesConfigScreen nach dem Speichern gesendet.
 */
public class ReloadEconomyPricesPacket {

    public ReloadEconomyPricesPacket() {}

    public void encode(FriendlyByteBuf buf) {
        // kein Payload nötig
    }

    public static ReloadEconomyPricesPacket decode(FriendlyByteBuf buf) {
        return new ReloadEconomyPricesPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Nur Operatoren (Permission Level ≥ 2) oder Singleplayer-Host
            if (player.hasPermissions(2) || player.getServer() != null && !player.getServer().isDedicatedServer()) {
                EconomyController.getInstance().applyConfigPrices();
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.translatable(
                        "message.economy.prices_reloaded"
                    )
                );
            }
        });
    }
}
