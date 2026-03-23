package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.config.ModConfigHandler;
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
 * Packet für Einzahlung auf Girokonto (Bargeld → Konto)
 * Spieler zahlt Bargeld aus Wallet auf Girokonto ein
 */
public class BankDepositPacket {
    private final double amount;

    public BankDepositPacket(double amount) {
        this.amount = amount;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(amount);
    }

    public static BankDepositPacket decode(FriendlyByteBuf buf) {
        return new BankDepositPacket(buf.readDouble());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Prüfe ob Betrag positiv
            if (amount <= 0) {
                player.sendSystemMessage(Component.translatable("message.bank.amount_must_positive")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Prüfe Einzahlungslimit
            double depositLimit = ModConfigHandler.COMMON.BANK_DEPOSIT_LIMIT.get();
            if (amount > depositLimit) {
                player.sendSystemMessage(Component.translatable("message.bank.deposit_limit_exceeded")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable("network.bank.maximum_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", depositLimit))
                        .withStyle(ChatFormatting.YELLOW)));
                return;
            }

            // Prüfe ob Spieler genug Bargeld hat
            double walletBalance = WalletManager.getBalance(player.getUUID());
            if (walletBalance < amount) {
                player.sendSystemMessage(Component.translatable("message.bank.insufficient_cash")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable("network.bank.cash_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", walletBalance))
                        .withStyle(ChatFormatting.YELLOW)));
                return;
            }

            // Transaktion durchführen: Wallet → Girokonto
            if (WalletManager.removeMoney(player.getUUID(), amount)) {
                EconomyManager.deposit(player.getUUID(), amount, TransactionType.ATM_DEPOSIT, "Bank-Einzahlung");
                de.rolandsw.schedulemc.mission.MissionEventBridge.fireBankDeposit(player);

                // Aktualisiere Client-Daten
                de.rolandsw.schedulemc.economy.network.RequestBankDataPacket requestPacket =
                    new de.rolandsw.schedulemc.economy.network.RequestBankDataPacket();
                requestPacket.handle(() -> ctx.get());

                // Erfolgs-Nachricht
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("🏦 ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.translatable("message.bank.deposit_successful")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.translatable("message.bank.amount_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("+%.2f€", amount))
                        .withStyle(ChatFormatting.GOLD)));
                player.sendSystemMessage(Component.translatable("message.bank.new_checking_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID())))
                        .withStyle(ChatFormatting.AQUA)));
                player.sendSystemMessage(Component.translatable("network.bank.remaining_cash")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", WalletManager.getBalance(player.getUUID())))
                        .withStyle(ChatFormatting.YELLOW)));
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
            } else {
                player.sendSystemMessage(Component.translatable("network.bank.deposit_error")
                    .withStyle(ChatFormatting.RED));
            }
        });
    }
}
