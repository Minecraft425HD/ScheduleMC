package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager;
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
                // Get total unpaid amount
                double totalAmount = FuelBillManager.getTotalUnpaidAmount(player.getUUID());

                if (totalAmount <= 0) {
                    player.sendSystemMessage(Component.literal("✓ Sie haben keine offenen Rechnungen!")
                        .withStyle(ChatFormatting.GREEN));
                    return;
                }

                // Check if player has enough money
                double balance = EconomyManager.getBalance(player.getUUID());
                if (balance < totalAmount) {
                    player.sendSystemMessage(Component.literal("§cNicht genug Geld! Benötigt: " + String.format("%.2f€", totalAmount)));
                    return;
                }

                // Withdraw money
                if (!EconomyManager.withdraw(player.getUUID(), totalAmount)) {
                    player.sendSystemMessage(Component.literal("§cFehler beim Abbuchung!"));
                    return;
                }

                // Mark all bills as paid
                FuelBillManager.payAllBills(player.getUUID());

                // Send success message
                player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("✓ ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("ALLE RECHNUNGEN BEZAHLT").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.literal("Betrag: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", totalAmount)).withStyle(ChatFormatting.GOLD)));
                player.sendSystemMessage(Component.literal("Restguthaben: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID()))).withStyle(ChatFormatting.YELLOW)));
                player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
