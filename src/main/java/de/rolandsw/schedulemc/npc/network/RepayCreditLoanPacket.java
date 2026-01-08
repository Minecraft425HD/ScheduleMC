package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.CreditLoan;
import de.rolandsw.schedulemc.economy.CreditLoanManager;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum vorzeitigen Zurückzahlen eines Kredits
 * Client → Server
 */
public class RepayCreditLoanPacket {

    public RepayCreditLoanPacket() {
    }

    public void encode(FriendlyByteBuf buf) {
        // Keine Daten nötig - Spieler-UUID kommt aus Context
    }

    public static RepayCreditLoanPacket decode(FriendlyByteBuf buf) {
        return new RepayCreditLoanPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            CreditLoanManager loanManager = CreditLoanManager.getInstance(player.getServer());

            // Prüfe ob aktiver Kredit existiert
            CreditLoan loan = loanManager.getLoan(player.getUUID());
            if (loan == null) {
                player.sendSystemMessage(Component.translatable("network.credit.no_active_loan"));
                return;
            }

            double remaining = loan.getRemaining();
            double balance = EconomyManager.getBalance(player.getUUID());

            // Prüfe ob genug Geld vorhanden
            if (balance < remaining) {
                player.sendSystemMessage(Component.translatable("network.credit.insufficient_funds_repay"));
                player.sendSystemMessage(Component.translatable("network.credit.required_label")
                    .append(Component.literal(String.format("%.2f€", remaining))));
                player.sendSystemMessage(Component.translatable("network.credit.balance_label")
                    .append(Component.literal(String.format("%.2f€", balance))));
                return;
            }

            // Führe Rückzahlung durch
            if (loanManager.repayLoan(player.getUUID())) {
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("🏦 ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.translatable("message.bank.loan_paid_off")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.translatable("gui.common.amount_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("-%.2f€", remaining))
                        .withStyle(ChatFormatting.RED)));
                player.sendSystemMessage(Component.translatable("message.bank.new_balance_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID())))
                        .withStyle(ChatFormatting.GOLD)));
                player.sendSystemMessage(Component.translatable("network.credit.debt_free"));
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
            } else {
                player.sendSystemMessage(Component.translatable("network.credit.repayment_error"));
            }
        });
    }
}
