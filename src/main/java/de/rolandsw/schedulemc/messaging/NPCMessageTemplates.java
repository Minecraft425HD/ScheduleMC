package de.rolandsw.schedulemc.messaging;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;

/**
 * Provides reputation-based message templates for NPCs
 */
public class NPCMessageTemplates {

    // Low reputation (0-33): Unfriendly messages
    private static final List<String> LOW_REPUTATION_MESSAGES = Arrays.asList(
        "Lass mich in Ruhe.",
        "Was willst du von mir?",
        "Ich habe keine Zeit für dich."
    );

    // Medium reputation (34-66): Neutral messages
    private static final List<String> MEDIUM_REPUTATION_MESSAGES = Arrays.asList(
        "Hallo, wie geht's?",
        "Brauchst du etwas?",
        "Schönen Tag noch!"
    );

    // High reputation (67-100): Friendly messages
    private static final List<String> HIGH_REPUTATION_MESSAGES = Arrays.asList(
        "Hey! Schön dich zu sehen!",
        "Wie läuft's bei dir?",
        "Lass uns bald was unternehmen!"
    );

    /**
     * Gets three message options based on reputation level
     * @param reputation Reputation value (0-100)
     * @return List of 3 message options
     */
    public static List<String> getMessagesForReputation(int reputation) {
        if (reputation < 34) {
            return new ArrayList<>(LOW_REPUTATION_MESSAGES);
        } else if (reputation < 67) {
            return new ArrayList<>(MEDIUM_REPUTATION_MESSAGES);
        } else {
            return new ArrayList<>(HIGH_REPUTATION_MESSAGES);
        }
    }

    /**
     * Gets reputation level description
     */
    public static String getReputationLevel(int reputation) {
        if (reputation < 34) {
            return "§cNiedrig";
        } else if (reputation < 67) {
            return "§eNeutral";
        } else {
            return "§aHoch";
        }
    }
}
