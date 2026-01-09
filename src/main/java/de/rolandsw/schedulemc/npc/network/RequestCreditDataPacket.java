package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.CreditLoan;
import de.rolandsw.schedulemc.economy.CreditLoanManager;
import de.rolandsw.schedulemc.economy.CreditScore;
import de.rolandsw.schedulemc.economy.CreditScoreManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum Anfordern von Kredit-Daten
 * Client → Server
 *
 * Server antwortet mit SyncCreditDataPacket
 */
public class RequestCreditDataPacket {

    public RequestCreditDataPacket() {
    }

    public void encode(FriendlyByteBuf buf) {
        // Keine Daten nötig
    }

    public static RequestCreditDataPacket decode(FriendlyByteBuf buf) {
        return new RequestCreditDataPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            CreditScoreManager scoreManager = CreditScoreManager.getInstance(player.getServer());
            CreditLoanManager loanManager = CreditLoanManager.getInstance(player.getServer());

            // Hole Kredit-Score
            CreditScore creditScore = scoreManager.getOrCreateScore(player.getUUID());
            long currentDay = scoreManager.getCurrentDay();
            int score = creditScore.calculateScore(currentDay);
            CreditScore.CreditRating rating = creditScore.getRating(currentDay);

            // Hole aktiven Kredit (falls vorhanden)
            CreditLoan activeLoan = loanManager.getLoan(player.getUUID());

            boolean hasLoan = activeLoan != null;
            String loanType = hasLoan ? activeLoan.getType().getDisplayName().getString() : "";
            double remaining = hasLoan ? activeLoan.getRemaining() : 0;
            double daily = hasLoan ? activeLoan.getDailyPayment() : 0;
            int progress = hasLoan ? activeLoan.getProgressPercent() : 0;
            int remainingDays = hasLoan ? activeLoan.getRemainingDays(currentDay) : 0;

            // Sende Daten an Client
            NPCNetworkHandler.sendToPlayer(new SyncCreditDataPacket(
                score,
                rating.ordinal(),
                hasLoan,
                loanType,
                remaining,
                daily,
                progress,
                remainingDays
            ), player);
        });
    }
}
