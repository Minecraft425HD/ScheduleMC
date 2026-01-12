package de.rolandsw.schedulemc.npc.crime.prison.network;

import de.rolandsw.schedulemc.npc.crime.prison.client.PrisonScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-only utility class to handle PrisonScreen operations
 * This class is only loaded on the client to avoid server-side class loading errors
 */
@OnlyIn(Dist.CLIENT)
public class ClientPrisonScreenHandler {

    /**
     * Opens the PrisonScreen with the given parameters
     * IMPORTANT: This method must ONLY be called from client-side code
     */
    public static void openPrisonScreen(int cellNumber, long totalSentenceTicks, long releaseTime,
                                        double bailAmount, double playerBalance, long bailAvailableAtTick) {
        PrisonScreen.open(
            cellNumber,
            totalSentenceTicks,
            releaseTime,
            bailAmount,
            playerBalance,
            bailAvailableAtTick
        );
    }

    /**
     * Closes the PrisonScreen if currently open and shows release message
     * IMPORTANT: This method must ONLY be called from client-side code
     */
    public static void closePrisonScreen(String reason) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.screen instanceof PrisonScreen prisonScreen) {
            prisonScreen.allowClose();
        }

        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.translatable("message.prison.released", reason),
                false
            );
        }
    }

    /**
     * Updates the balance displayed in the PrisonScreen if currently open
     * IMPORTANT: This method must ONLY be called from client-side code
     */
    public static void updatePrisonBalance(double newBalance) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof PrisonScreen prisonScreen) {
            prisonScreen.updateBalance(newBalance);
        }
    }
}
