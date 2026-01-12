package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.tobacco.screen.TobaccoNegotiationScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-only utility class to handle TobaccoNegotiationScreen operations
 * This class is only loaded on the client to avoid server-side class loading errors
 */
@OnlyIn(Dist.CLIENT)
public class ClientTobaccoScreenHandler {

    /**
     * Updates purchase decision data in TobaccoNegotiationScreen if currently open
     * IMPORTANT: This method must ONLY be called from client-side code
     */
    public static void updatePurchaseDecision(int totalScore, boolean willingToBuy, int desiredAmount, int npcWallet) {
        if (Minecraft.getInstance().screen instanceof TobaccoNegotiationScreen screen) {
            screen.updatePurchaseDecision(totalScore, willingToBuy, desiredAmount, npcWallet);
        }
    }
}
