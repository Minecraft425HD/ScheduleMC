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
    private final boolean forced;

    public SavingsWithdrawPacket(double amount, boolean forced) {
        this.amount = amount;
        this.forced = forced;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(amount);
        buf.writeBoolean(forced);
    }

    public static SavingsWithdrawPacket decode(FriendlyByteBuf buf) {
        return new SavingsWithdrawPacket(buf.readDouble(), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Prüfe ob Betrag positiv
            if (amount <= 0) {
                player.sendSystemMessage(Component.translatable("message.bank.amount_must_positive")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            SavingsAccountManager manager = SavingsAccountManager.getInstance(player.getServer());
            List<SavingsAccount> accounts = manager.getAccounts(player.getUUID());

            // Prüfe ob Sparkonto existiert
            if (accounts.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.bank.no_savings")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            SavingsAccount account = accounts.get(0);

            // HINWEIS: Balance-Prüfung erfolgt atomar in SavingsAccountManager.withdrawFromSavings()
            // Separate Prüfung entfernt wegen TOCTOU Race Condition

            // Versuche Abhebung (erzwingen = forced Parameter von Client)
            if (manager.withdrawFromSavings(player.getUUID(), account.getAccountId(), amount, forced)) {
                // OPTIMIERT: Hole aktuelle Werte NACH der Transaktion
                double newSavingsBalance = account.getBalance();
                double newCheckingBalance = EconomyManager.getBalance(player.getUUID());

                // Aktualisiere Client-Daten
                de.rolandsw.schedulemc.economy.network.RequestBankDataPacket requestPacket =
                    new de.rolandsw.schedulemc.economy.network.RequestBankDataPacket();
                requestPacket.handle(() -> ctx.get());

                // Erfolgs-Nachricht
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("💰 ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.translatable("message.bank.withdrawal_successful")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.translatable("message.bank.amount_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("-%.2f€", amount))
                        .withStyle(ChatFormatting.RED)));
                player.sendSystemMessage(Component.translatable("message.bank.new_savings_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", newSavingsBalance))
                        .withStyle(ChatFormatting.LIGHT_PURPLE)));
                player.sendSystemMessage(Component.translatable("message.bank.new_checking_label")
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
                    player.sendSystemMessage(Component.translatable("message.bank.savings_locked")
                        .withStyle(ChatFormatting.RED));
                    player.sendSystemMessage(Component.translatable("network.bank.remaining_days")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%d", daysLeft))
                            .withStyle(ChatFormatting.YELLOW)));
                    player.sendSystemMessage(Component.translatable("network.bank.hint_label")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("network.bank.early_withdrawal_penalty")
                            .withStyle(ChatFormatting.RED)));
                } else if (account.getBalance() < amount) {
                    // Nicht genug Guthaben
                    player.sendSystemMessage(Component.translatable("message.bank.insufficient_savings")
                        .withStyle(ChatFormatting.RED));
                    player.sendSystemMessage(Component.translatable("message.bank.available")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.2f€", account.getBalance()))
                            .withStyle(ChatFormatting.YELLOW)));
                } else {
                    player.sendSystemMessage(Component.translatable("network.bank.withdrawal_error")
                        .withStyle(ChatFormatting.RED));
                }
            }
        });
    }
}
