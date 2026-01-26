package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.tobacco.screen.TobaccoNegotiationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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

    /**
     * Handles the negotiation response from the server
     * Updates the GUI with mood, round, and NPC response
     */
    public static void handleNegotiationResponse(NegotiationResponsePacket packet) {
        Minecraft mc = Minecraft.getInstance();

        if (packet.isAccepted()) {
            // Deal akzeptiert - GUI schließen und Erfolgsmeldung zeigen
            if (mc.screen instanceof TobaccoNegotiationScreen) {
                mc.setScreen(null);  // GUI schließen
            }
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal("§a" + packet.getMessage()));
            }
        } else {
            // Deal abgelehnt - GUI aktualisieren
            if (mc.screen instanceof TobaccoNegotiationScreen screen) {
                screen.updateNegotiationResponse(
                    packet.getMessage(),
                    packet.getCounterOffer(),
                    packet.getMood(),
                    packet.getRound(),
                    packet.getMaxRounds()
                );
            } else if (mc.player != null) {
                // GUI ist nicht mehr offen - zeige Nachricht im Chat
                mc.player.sendSystemMessage(Component.literal("§e" + packet.getMessage()));
                if (packet.getCounterOffer() > 0) {
                    mc.player.sendSystemMessage(Component.translatable(
                        "message.tobacco.counteroffer",
                        String.format("%.2f", packet.getCounterOffer())
                    ));
                }
            }
        }
    }
}
