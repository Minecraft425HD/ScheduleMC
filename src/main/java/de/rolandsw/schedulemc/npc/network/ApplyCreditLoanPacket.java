package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.CreditLoan;
import de.rolandsw.schedulemc.economy.CreditLoanManager;
import de.rolandsw.schedulemc.economy.CreditScore;
import de.rolandsw.schedulemc.economy.CreditScoreManager;
import de.rolandsw.schedulemc.economy.RecurringPaymentManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum Beantragen eines Kredits
 * Client → Server
 */
public class ApplyCreditLoanPacket {
    private final int loanTypeOrdinal;

    public ApplyCreditLoanPacket(int loanTypeOrdinal) {
        this.loanTypeOrdinal = loanTypeOrdinal;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(loanTypeOrdinal);
    }

    public static ApplyCreditLoanPacket decode(FriendlyByteBuf buf) {
        return new ApplyCreditLoanPacket(buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Validiere Kredittyp
            CreditLoan.CreditLoanType[] types = CreditLoan.CreditLoanType.values();
            if (loanTypeOrdinal < 0 || loanTypeOrdinal >= types.length) {
                player.sendSystemMessage(Component.translatable("network.credit.invalid_loan_type"));
                return;
            }

            CreditLoan.CreditLoanType loanType = types[loanTypeOrdinal];
            CreditLoanManager loanManager = CreditLoanManager.getInstance(player.getServer());
            CreditScoreManager scoreManager = CreditScoreManager.getInstance(player.getServer());

            // Prüfe ob bereits aktiver Kredit
            if (loanManager.hasActiveLoan(player.getUUID())) {
                player.sendSystemMessage(Component.translatable("network.credit.already_active_loan"));
                player.sendSystemMessage(Component.translatable("network.credit.repay_first"));
                return;
            }

            // Prüfe 10er-Limit für Daueraufträge (Kredit zählt als Dauerauftrag)
            RecurringPaymentManager recurringManager = RecurringPaymentManager.getInstance(player.getServer());
            int currentPayments = recurringManager.getPayments(player.getUUID()).size();
            int maxPerPlayer = ModConfigHandler.COMMON.RECURRING_MAX_PER_PLAYER.get();

            if (currentPayments >= maxPerPlayer) {
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable("message.bank.loan_rejected")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                player.sendSystemMessage(Component.translatable("message.common.already_have_prefix")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(currentPayments + "/" + maxPerPlayer)
                        .withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.bank.active_orders_suffix")
                        .withStyle(ChatFormatting.GRAY)));
                player.sendSystemMessage(Component.translatable("message.bank.loan_counts_as_order")
                    .withStyle(ChatFormatting.GOLD));
                player.sendSystemMessage(Component.translatable("message.bank.delete_existing_first")
                    .withStyle(ChatFormatting.YELLOW));
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Prüfe Bonität
            if (!scoreManager.canTakeLoan(player.getUUID(), loanType)) {
                CreditScore score = scoreManager.getOrCreateScore(player.getUUID());
                CreditScore.CreditRating currentRating = score.getRating(scoreManager.getCurrentDay());

                player.sendSystemMessage(Component.translatable("network.credit.loan_rejected_insufficient"));
                player.sendSystemMessage(Component.translatable("network.credit.insufficient_credit_score"));
                player.sendSystemMessage(Component.translatable("network.credit.required_rating")
                    .append(Component.literal(loanType.getRequiredRating().getDisplayName())));
                player.sendSystemMessage(Component.translatable("network.credit.current_rating")
                    .append(Component.literal(currentRating.getDisplayName())));
                return;
            }

            // Beantrage Kredit
            if (loanManager.applyForLoan(player.getUUID(), loanType)) {
                CreditLoan loan = loanManager.getLoan(player.getUUID());
                if (loan != null) {
                    // Erfolgs-Nachricht
                    player.sendSystemMessage(Component.literal("═══════════════════════════════")
                        .withStyle(ChatFormatting.GREEN));
                    player.sendSystemMessage(Component.literal("🏦 ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.translatable("message.bank.loan_approved")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                    player.sendSystemMessage(Component.translatable("network.credit.type_label")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(loanType.getDisplayNameDE())
                            .withStyle(ChatFormatting.GOLD)));
                    player.sendSystemMessage(Component.translatable("gui.common.amount_label")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("+%.2f€", loanType.getBaseAmount()))
                            .withStyle(ChatFormatting.GREEN)));
                    player.sendSystemMessage(Component.translatable("network.credit.effective_interest")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.1f%%", loan.getEffectiveInterestRate() * 100))
                            .withStyle(ChatFormatting.RED)));
                    player.sendSystemMessage(Component.translatable("network.credit.duration_label")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(loanType.getDurationDays()))
                            .withStyle(ChatFormatting.AQUA))
                        .append(Component.translatable("network.credit.days_suffix")
                            .withStyle(ChatFormatting.AQUA)));
                    player.sendSystemMessage(Component.translatable("message.bank.daily_rate")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("-%.2f€", loan.getDailyPayment()))
                            .withStyle(ChatFormatting.RED)));
                    player.sendSystemMessage(Component.literal("═══════════════════════════════")
                        .withStyle(ChatFormatting.GREEN));
                }
            } else {
                player.sendSystemMessage(Component.translatable("network.credit.loan_rejected_unknown"));
                player.sendSystemMessage(Component.translatable("network.credit.unknown_error"));
            }
        });
    }
}
