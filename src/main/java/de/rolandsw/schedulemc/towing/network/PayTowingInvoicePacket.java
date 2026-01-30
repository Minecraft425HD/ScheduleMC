package de.rolandsw.schedulemc.towing.network;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.towing.TowingInvoiceData;
import de.rolandsw.schedulemc.towing.TowingYardManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet sent from client to server when player pays a towing invoice
 */
public class PayTowingInvoicePacket {
    private final UUID invoiceId;

    public PayTowingInvoicePacket(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(invoiceId);
    }

    public static PayTowingInvoicePacket decode(FriendlyByteBuf buf) {
        return new PayTowingInvoicePacket(buf.readUUID());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, sender -> {
            TowingInvoiceData invoice = TowingYardManager.getInvoice(invoiceId);

            if (invoice == null) {
                sender.displayClientMessage(
                    Component.translatable("towing.error.no_invoice"),
                    false
                );
                return;
            }

            // Verify this invoice belongs to the player
            if (!invoice.getPlayerId().equals(sender.getUUID())) {
                sender.displayClientMessage(
                    Component.translatable("towing.error.not_your_invoice"),
                    false
                );
                return;
            }

            // Check if already paid
            if (invoice.isPaid()) {
                sender.displayClientMessage(
                    Component.translatable("towing.error.already_paid"),
                    false
                );
                return;
            }

            double amount = invoice.getAmount();

            // Check if player has enough money in bank account
            if (EconomyManager.getBalance(sender.getUUID()) < amount) {
                sender.displayClientMessage(
                    Component.translatable("towing.error.not_enough_money"),
                    false
                );
                return;
            }

            // Process payment from bank account
            if (!EconomyManager.withdraw(sender.getUUID(), amount, TransactionType.WERKSTATT_FEE,
                    "Towing invoice payment: " + invoice.getTowingYardPlotId())) {
                sender.displayClientMessage(
                    Component.translatable("towing.error.not_enough_money"),
                    false
                );
                return;
            }
            TowingYardManager.payInvoice(invoiceId);

            sender.displayClientMessage(
                Component.translatable("towing.invoice.paid", String.format("%.0f", amount)),
                false
            );
        });
    }
}
