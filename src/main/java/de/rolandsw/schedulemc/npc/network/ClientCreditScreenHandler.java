package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.client.screen.CreditAdvisorScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-only utility class to handle CreditAdvisorScreen operations
 * This class is only loaded on the client to avoid server-side class loading errors
 */
@OnlyIn(Dist.CLIENT)
public class ClientCreditScreenHandler {

    /**
     * Updates credit data in the CreditAdvisorScreen if currently open
     * IMPORTANT: This method must ONLY be called from client-side code
     */
    public static void updateCreditData(int creditScore, int ratingOrdinal, boolean hasActiveLoan,
                                        String loanType, double remaining, double daily,
                                        int progress, int remainingDays) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof CreditAdvisorScreen screen) {
            screen.updateCreditData(
                creditScore,
                ratingOrdinal,
                hasActiveLoan,
                loanType,
                remaining,
                daily,
                progress,
                remainingDays
            );
        }
    }
}
