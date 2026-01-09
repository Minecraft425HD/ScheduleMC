package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager;
import de.rolandsw.schedulemc.economy.WalletManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
        PacketHandler.handleServerPacket(ctx, player -> {
            UUID playerUUID = player.getUUID();

            // Hole alle unbezahlten Rechnungen
            var unpaidBills = FuelBillManager.getUnpaidBills(playerUUID);
            double totalAmount = FuelBillManager.getTotalUnpaidAmount(playerUUID);

            if (totalAmount <= 0 || unpaidBills.isEmpty()) {
                player.sendSystemMessage(Component.literal("⚠ ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.translatable("message.bank.no_open_bills_plain").withStyle(ChatFormatting.GRAY)));
                return;
            }

            // Prüfe ob Spieler genug Geld hat
            double balance = WalletManager.getBalance(playerUUID);
            if (balance < totalAmount) {
                player.sendSystemMessage(Component.literal("⚠ ").withStyle(ChatFormatting.RED)
                    .append(Component.translatable("network.fuel_bill.insufficient_funds").withStyle(ChatFormatting.RED)));
                player.sendSystemMessage(Component.translatable("message.bank.required").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", totalAmount)).withStyle(ChatFormatting.RED))
                    .append(Component.translatable("message.stock.available_separator").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.format("%.2f€", balance)).withStyle(ChatFormatting.YELLOW)));
                return;
            }

            // Bezahle alle Rechnungen
            if (WalletManager.removeMoney(playerUUID, totalAmount)) {
                // Markiere alle Rechnungen als bezahlt
                for (var bill : unpaidBills) {
                    bill.paid = true;
                }
                FuelBillManager.saveIfNeeded();

                int billCount = unpaidBills.size();

                // Erfolgs-Nachricht
                player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));
                player.sendSystemMessage(Component.literal("✓ ").withStyle(ChatFormatting.GREEN)
                    .append(Component.translatable("message.fuel.all_bills_paid").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.translatable("message.fuel.bill_count_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.valueOf(billCount)).withStyle(ChatFormatting.AQUA)));
                player.sendSystemMessage(Component.translatable("message.common.total_amount_label").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", totalAmount)).withStyle(ChatFormatting.GOLD)));
                player.sendSystemMessage(Component.literal("Neues Guthaben: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", WalletManager.getBalance(playerUUID))).withStyle(ChatFormatting.YELLOW)));
                player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GOLD));
            } else {
                player.sendSystemMessage(Component.literal("⚠ ").withStyle(ChatFormatting.RED)
                    .append(Component.literal("Fehler bei der Zahlung!").withStyle(ChatFormatting.RED)));
            }
        });
    }
}
