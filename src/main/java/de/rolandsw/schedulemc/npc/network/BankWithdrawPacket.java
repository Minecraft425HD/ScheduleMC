package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für Abhebung vom Girokonto (Konto → Bargeld)
 * Spieler hebt Geld vom Girokonto ab und erhält Bargeld
 */
public class BankWithdrawPacket {
    private final double amount;

    public BankWithdrawPacket(double amount) {
        this.amount = amount;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(amount);
    }

    public static BankWithdrawPacket decode(FriendlyByteBuf buf) {
        return new BankWithdrawPacket(buf.readDouble());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Prüfe ob Betrag positiv
            if (amount <= 0) {
                player.sendSystemMessage(Component.translatable("message.bank.amount_must_positive")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Prüfe ob Spieler genug Geld auf Girokonto hat
            double giroBalance = EconomyManager.getBalance(player.getUUID());
            if (giroBalance < amount) {
                player.sendSystemMessage(Component.translatable("message.bank.insufficient_checking")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable("message.bank.available")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", giroBalance))
                        .withStyle(ChatFormatting.YELLOW)));
                return;
            }

            // Transaktion durchführen: Girokonto → Wallet
            if (EconomyManager.withdraw(player.getUUID(), amount, TransactionType.ATM_WITHDRAW, "Bank-Abhebung")) {
                WalletManager.addMoney(player.getUUID(), amount);

                // Erfolgs-Nachricht
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("🏦 ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.translatable("message.bank.withdrawal_successful"))
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.translatable("message.bank.amount_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("-%.2f€", amount))
                        .withStyle(ChatFormatting.RED)));
                player.sendSystemMessage(Component.translatable("message.bank.new_checking_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID())))
                        .withStyle(ChatFormatting.AQUA)));
                player.sendSystemMessage(Component.literal("Neues Bargeld: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", WalletManager.getBalance(player.getUUID())))
                        .withStyle(ChatFormatting.YELLOW)));
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
            } else {
                player.sendSystemMessage(Component.translatable("message.bank.debit_error")
                    .withStyle(ChatFormatting.RED));
            }
        });
    }
}
