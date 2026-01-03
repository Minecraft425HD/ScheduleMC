package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.CreditLoan;
import de.rolandsw.schedulemc.economy.CreditLoanManager;
import de.rolandsw.schedulemc.economy.CreditScore;
import de.rolandsw.schedulemc.economy.CreditScoreManager;
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
                player.sendSystemMessage(Component.literal(
                    "§c§lFehler: §7Ungültiger Kredittyp!"
                ));
                return;
            }

            CreditLoan.CreditLoanType loanType = types[loanTypeOrdinal];
            CreditLoanManager loanManager = CreditLoanManager.getInstance(player.getServer());
            CreditScoreManager scoreManager = CreditScoreManager.getInstance(player.getServer());

            // Prüfe ob bereits aktiver Kredit
            if (loanManager.hasActiveLoan(player.getUUID())) {
                player.sendSystemMessage(Component.literal(
                    "§c§lFehler: §7Du hast bereits einen aktiven Kredit!\n" +
                    "§7Bezahle diesen zuerst zurück."
                ));
                return;
            }

            // Prüfe Bonität
            if (!scoreManager.canTakeLoan(player.getUUID(), loanType)) {
                CreditScore score = scoreManager.getOrCreateScore(player.getUUID());
                CreditScore.CreditRating currentRating = score.getRating(scoreManager.getCurrentDay());

                player.sendSystemMessage(Component.literal(
                    "§c§lKredit abgelehnt!\n" +
                    "§7Deine Bonität reicht nicht aus.\n" +
                    "§7Benötigt: §e" + loanType.getRequiredRating().getDisplayName() + "\n" +
                    "§7Aktuell: §c" + currentRating.getDisplayName()
                ));
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
                        .append(Component.literal("KREDIT BEWILLIGT!")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                    player.sendSystemMessage(Component.literal("Typ: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(loanType.getDisplayNameDE())
                            .withStyle(ChatFormatting.GOLD)));
                    player.sendSystemMessage(Component.literal("Betrag: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("+%.2f€", loanType.getBaseAmount()))
                            .withStyle(ChatFormatting.GREEN)));
                    player.sendSystemMessage(Component.literal("Effektiver Zinssatz: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.1f%%", loan.getEffectiveInterestRate() * 100))
                            .withStyle(ChatFormatting.RED)));
                    player.sendSystemMessage(Component.literal("Laufzeit: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(loanType.getDurationDays() + " Tage")
                            .withStyle(ChatFormatting.AQUA)));
                    player.sendSystemMessage(Component.literal("Tägliche Rate: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("-%.2f€", loan.getDailyPayment()))
                            .withStyle(ChatFormatting.RED)));
                    player.sendSystemMessage(Component.literal("═══════════════════════════════")
                        .withStyle(ChatFormatting.GREEN));
                }
            } else {
                player.sendSystemMessage(Component.literal(
                    "§c§lKredit abgelehnt!\n" +
                    "§7Unbekannter Fehler bei der Kreditvergabe."
                ));
            }
        });
    }
}
