package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.SavingsAccount;
import de.rolandsw.schedulemc.economy.SavingsAccountManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Packet für Einzahlung auf Sparkonto (Girokonto → Sparkonto)
 * Spieler zahlt Geld vom Girokonto auf Sparkonto ein
 */
public class SavingsDepositPacket {
    private final double amount;

    public SavingsDepositPacket(double amount) {
        this.amount = amount;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(amount);
    }

    public static SavingsDepositPacket decode(FriendlyByteBuf buf) {
        return new SavingsDepositPacket(buf.readDouble());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Prüfe ob Betrag positiv
            if (amount <= 0) {
                player.sendSystemMessage(Component.translatable("message.bank.amount_must_positive")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Minimum-Einzahlung prüfen
            double minDeposit = ModConfigHandler.COMMON.SAVINGS_MIN_DEPOSIT.get();
            if (amount < minDeposit) {
                player.sendSystemMessage(Component.translatable("message.bank.minimum_deposit_label")
                    .withStyle(ChatFormatting.RED)
                    .append(Component.literal(String.format("%.2f€", minDeposit))
                        .withStyle(ChatFormatting.YELLOW)));
                return;
            }

            // HINWEIS: Balance-Prüfung erfolgt atomar in SavingsAccountManager.depositToSavings()
            // und SavingsAccountManager.createSavingsAccount()
            // Separate Prüfung hier entfernt wegen TOCTOU Race Condition

            SavingsAccountManager manager = SavingsAccountManager.getInstance(player.getServer());
            List<SavingsAccount> accounts = manager.getAccounts(player.getUUID());

            // Wenn kein Sparkonto existiert, erstelle ein neues
            if (accounts.isEmpty()) {
                if (manager.createSavingsAccount(player.getUUID(), amount)) {
                    // Erfolgs-Nachricht
                    player.sendSystemMessage(Component.literal("═══════════════════════════════")
                        .withStyle(ChatFormatting.GREEN));
                    player.sendSystemMessage(Component.literal("💰 ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.translatable("message.bank.savings_opened")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                    player.sendSystemMessage(Component.translatable("message.bank.initial_deposit_label")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("+%.2f€", amount))
                            .withStyle(ChatFormatting.GOLD)));
                    player.sendSystemMessage(Component.translatable("message.bank.interest_label")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("message.bank.interest_rate"))
                            .withStyle(ChatFormatting.GREEN)));
                    player.sendSystemMessage(Component.translatable("message.bank.lock_period_label")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("message.bank.lock_period_duration"))
                            .withStyle(ChatFormatting.YELLOW)));
                    player.sendSystemMessage(Component.literal("═══════════════════════════════")
                        .withStyle(ChatFormatting.GREEN));
                } else {
                    // Atomare Prüfung fehlgeschlagen - nicht genug Geld
                    player.sendSystemMessage(Component.translatable("message.bank.insufficient_checking")
                        .withStyle(ChatFormatting.RED));
                    player.sendSystemMessage(Component.translatable("message.bank.available")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID())))
                            .withStyle(ChatFormatting.YELLOW)));
                }
            } else {
                // Auf existierendes Sparkonto einzahlen
                SavingsAccount account = accounts.get(0);
                if (manager.depositToSavings(player.getUUID(), account.getAccountId(), amount)) {
                    // Erfolgs-Nachricht
                    player.sendSystemMessage(Component.literal("═══════════════════════════════")
                        .withStyle(ChatFormatting.GREEN));
                    player.sendSystemMessage(Component.literal("💰 ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.translatable("message.bank.deposit_successful"))
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                    player.sendSystemMessage(Component.translatable("message.bank.amount_label")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("+%.2f€", amount))
                            .withStyle(ChatFormatting.GOLD)));
                    player.sendSystemMessage(Component.translatable("message.bank.new_savings_label")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.2f€", account.getBalance()))
                            .withStyle(ChatFormatting.LIGHT_PURPLE)));
                    player.sendSystemMessage(Component.translatable("message.bank.new_checking_label")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID())))
                            .withStyle(ChatFormatting.AQUA)));
                    player.sendSystemMessage(Component.literal("═══════════════════════════════")
                        .withStyle(ChatFormatting.GREEN));
                } else {
                    // Atomare Prüfung fehlgeschlagen - nicht genug Geld
                    player.sendSystemMessage(Component.translatable("message.bank.insufficient_checking")
                        .withStyle(ChatFormatting.RED));
                    player.sendSystemMessage(Component.translatable("message.bank.available")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID())))
                            .withStyle(ChatFormatting.YELLOW)));
                }
            }
        });
    }
}
