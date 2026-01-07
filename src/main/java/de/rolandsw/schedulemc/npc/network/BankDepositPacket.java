package de.rolandsw.schedulemc.npc.network;
nimport de.rolandsw.schedulemc.util.StringUtils;

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
                player.sendSystemMessage(Component.literal("⚠ Betrag muss positiv sein!")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Prüfe Einzahlungslimit
            double depositLimit = ModConfigHandler.COMMON.BANK_DEPOSIT_LIMIT.get();
            if (amount > depositLimit) {
                player.sendSystemMessage(Component.literal("⚠ Einzahlungslimit überschritten!")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.literal("Maximum: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(StringUtils.formatMoney(depositLimit))
                        .withStyle(ChatFormatting.YELLOW)));
                return;
            }

            // Prüfe ob Spieler genug Bargeld hat
            double walletBalance = WalletManager.getBalance(player.getUUID());
            if (walletBalance < amount) {
                player.sendSystemMessage(Component.literal("⚠ Nicht genug Bargeld!")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.literal("Bargeld: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(StringUtils.formatMoney(walletBalance))
                        .withStyle(ChatFormatting.YELLOW)));
                return;
            }

            // Transaktion durchführen: Wallet → Girokonto
            if (WalletManager.removeMoney(player.getUUID(), amount)) {
                EconomyManager.deposit(player.getUUID(), amount, TransactionType.ATM_DEPOSIT, "Bank-Einzahlung");

                // Erfolgs-Nachricht
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("🏦 ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("EINZAHLUNG ERFOLGREICH")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.literal("Betrag: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("+%.2f€", amount))
                        .withStyle(ChatFormatting.GOLD)));
                player.sendSystemMessage(Component.literal("Neues Girokonto: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(StringUtils.formatMoney(EconomyManager.getBalance(player.getUUID())))
                        .withStyle(ChatFormatting.AQUA)));
                player.sendSystemMessage(Component.literal("Restliches Bargeld: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(StringUtils.formatMoney(WalletManager.getBalance(player.getUUID())))
                        .withStyle(ChatFormatting.YELLOW)));
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
            } else {
                player.sendSystemMessage(Component.literal("⚠ Fehler bei der Einzahlung!")
                    .withStyle(ChatFormatting.RED));
            }
        });
    }
}
