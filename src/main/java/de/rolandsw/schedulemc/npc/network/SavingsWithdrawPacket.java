package de.rolandsw.schedulemc.npc.network;

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
 * Packet für Abhebung vom Sparkonto (Sparkonto → Girokonto)
 * Spieler hebt Geld vom Sparkonto ab und erhält es auf Girokonto
 */
public class SavingsWithdrawPacket {
    private final double amount;

    public SavingsWithdrawPacket(double amount) {
        this.amount = amount;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(amount);
    }

    public static SavingsWithdrawPacket decode(FriendlyByteBuf buf) {
        return new SavingsWithdrawPacket(buf.readDouble());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Prüfe ob Betrag positiv
            if (amount <= 0) {
                player.sendSystemMessage(Component.literal("⚠ Betrag muss positiv sein!")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            SavingsAccountManager manager = SavingsAccountManager.getInstance(player.getServer());
            List<SavingsAccount> accounts = manager.getAccounts(player.getUUID());

            // Prüfe ob Sparkonto existiert
            if (accounts.isEmpty()) {
                player.sendSystemMessage(Component.literal("⚠ Du hast kein Sparkonto!")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            SavingsAccount account = accounts.get(0);

            // HINWEIS: Balance-Prüfung erfolgt atomar in SavingsAccountManager.withdrawFromSavings()
            // Separate Prüfung entfernt wegen TOCTOU Race Condition

            // Versuche Abhebung (erzwingen = false, damit Sperrfrist geprüft wird)
            if (manager.withdrawFromSavings(player.getUUID(), account.getAccountId(), amount, false)) {
                // OPTIMIERT: Hole aktuelle Werte NACH der Transaktion
                double newSavingsBalance = account.getBalance();
                double newCheckingBalance = EconomyManager.getBalance(player.getUUID());

                // Erfolgs-Nachricht
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("💰 ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("ABHEBUNG ERFOLGREICH")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.literal("Betrag: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("-%.2f€", amount))
                        .withStyle(ChatFormatting.RED)));
                player.sendSystemMessage(Component.literal("Neues Sparkonto: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", newSavingsBalance))
                        .withStyle(ChatFormatting.LIGHT_PURPLE)));
                player.sendSystemMessage(Component.literal("Neues Girokonto: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", newCheckingBalance))
                        .withStyle(ChatFormatting.AQUA)));
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
            } else {
                // Prüfe ob Sperrfrist noch aktiv
                long currentDay = player.getServer().overworld().getDayTime() / 24000L;
                if (!account.isUnlocked(currentDay)) {
                    int daysLeft = account.getDaysUntilUnlock(currentDay);
                    player.sendSystemMessage(Component.literal("⚠ Sparkonto ist noch gesperrt!")
                        .withStyle(ChatFormatting.RED));
                    player.sendSystemMessage(Component.literal("Verbleibende Tage: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%d", daysLeft))
                            .withStyle(ChatFormatting.YELLOW)));
                    player.sendSystemMessage(Component.literal("Hinweis: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("Vorzeitige Abhebung: -10% Strafe")
                            .withStyle(ChatFormatting.RED)));
                } else if (account.getBalance() < amount) {
                    // Nicht genug Guthaben
                    player.sendSystemMessage(Component.literal("⚠ Nicht genug Guthaben auf Sparkonto!")
                        .withStyle(ChatFormatting.RED));
                    player.sendSystemMessage(Component.translatable("message.bank.available")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.2f€", account.getBalance()))
                            .withStyle(ChatFormatting.YELLOW)));
                } else {
                    player.sendSystemMessage(Component.literal("⚠ Fehler bei der Abhebung!")
                        .withStyle(ChatFormatting.RED));
                }
            }
        });
    }
}
