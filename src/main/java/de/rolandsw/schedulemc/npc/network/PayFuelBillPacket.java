package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.WalletManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet für Tankrechnungs-Zahlung am NPC
 * Bezahlt alle offenen Tankrechnungen des Spielers
 */
public class PayFuelBillPacket {

    public PayFuelBillPacket() {
    }

    public void encode(FriendlyByteBuf buf) {
        // Kein Inhalt nötig
    }

    public static PayFuelBillPacket decode(FriendlyByteBuf buf) {
        return new PayFuelBillPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // TODO: Re-implement fuel bill payment for new vehicle system
                player.sendSystemMessage(Component.literal("§cTankrechnungen sind vorübergehend deaktiviert."));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
