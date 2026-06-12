package de.rolandsw.schedulemc.messaging;

import net.minecraft.network.chat.Component;
import java.util.*;

/**
 * Provides reputation-based message templates for NPCs
 */
public class NPCMessageTemplates {

    private static final int MESSAGES_PER_MOOD = 3;

    /**
     * Gets an unfriendly message Component based on variant
     * @param variant Variant index (0-2)
     * @return Translatable Component
     */
    /**
     * Smartphone-Nachricht für eine Warenanfrage (Fern-Benachrichtigung).
     */
    public static String getSupplyRequestMessage(String itemName, int amount, int payment,
                                                 int x, int y, int z, long deadlineDay) {
        return "Hey! Ich brauche " + amount + "x " + itemName + ". "
            + "Treffpunkt: " + x + ", " + y + ", " + z + ". "
            + "Ich zahle dir " + payment + "€. Sprich mich an, wenn du dabei bist — "
            + "das Angebot gilt bis Tag " + deadlineDay + ".";
    }

    /** Smartphone-Nachricht, wenn eine angenommene Besorgung scheitert. */
    public static String getSupplyRequestDisappointedMessage() {
        return "Ich habe vergeblich auf die Waren gewartet... Ich bin enttäuscht von dir.";
    }

    public static Component getUnfriendlyMessage(int variant) {
        return Component.translatable("npc.message.unfriendly." + variant);
    }

    /**
     * Gets a neutral message Component based on variant
     * @param variant Variant index (0-2)
     * @return Translatable Component
     */
    public static Component getNeutralMessage(int variant) {
        return Component.translatable("npc.message.neutral." + variant);
    }

    /**
     * Gets a friendly message Component based on variant
     * @param variant Variant index (0-2)
     * @return Translatable Component
     */
    public static Component getFriendlyMessage(int variant) {
        return Component.translatable("npc.message.friendly." + variant);
    }

    /**
     * Gets all message Components based on reputation level
     * @param reputation Reputation value (0-100)
     * @return List of 3 message Components
     */
    public static List<Component> getMessagesForReputation(int reputation) {
        List<Component> messages = new ArrayList<>();

        if (reputation < 34) {
            // Unfriendly messages
            for (int i = 0; i < MESSAGES_PER_MOOD; i++) {
                messages.add(getUnfriendlyMessage(i));
            }
        } else if (reputation < 67) {
            // Neutral messages
            for (int i = 0; i < MESSAGES_PER_MOOD; i++) {
                messages.add(getNeutralMessage(i));
            }
        } else {
            // Friendly messages
            for (int i = 0; i < MESSAGES_PER_MOOD; i++) {
                messages.add(getFriendlyMessage(i));
            }
        }

        return messages;
    }

    /**
     * Gets reputation level description Component
     * @param reputation Reputation value (0-100)
     * @return Translatable Component with color formatting
     */
    public static Component getReputationLevel(int reputation) {
        if (reputation < 34) {
            return Component.translatable("npc.reputation.low");
        } else if (reputation < 67) {
            return Component.translatable("npc.reputation.neutral");
        } else {
            return Component.translatable("npc.reputation.high");
        }
    }
}
