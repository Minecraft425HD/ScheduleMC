package de.rolandsw.schedulemc.npc.life.dialogue;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.core.NPCMemory;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dialog-Konsequenz-System: Gespraeche haben echte Auswirkungen.
 *
 * Konsequenz-Typen:
 * - REPUTATION: Aendert Fraktions-Reputation
 * - DISCOUNT: Gibt temporaeren Rabatt bei diesem NPC
 * - QUEST: Startet eine Quest
 * - INFORMATION: Gibt Hinweis/Information weiter
 * - RELATIONSHIP: Aendert NPC-Spieler Beziehung
 * - ECONOMY: Geld-Transfer (Bestechung, Bezahlung)
 *
 * NPCs erinnern sich an vergangene Gespraeche und reagieren darauf.
 */
public class DialogueConsequenceSystem {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile DialogueConsequenceSystem instance;

    // NPC-Erinnerungen an Gespraeche: NPC-UUID -> Spieler-UUID -> Letzte Themen
    private final Map<UUID, Map<UUID, ConversationMemory>> conversationHistory = new ConcurrentHashMap<>();

    public enum ConsequenceType {
        REPUTATION,
        DISCOUNT,
        QUEST,
        INFORMATION,
        RELATIONSHIP,
        ECONOMY
    }

    /**
     * Eine einzelne Dialog-Konsequenz.
     */
    public static class DialogueConsequence {
        private final ConsequenceType type;
        private final String parameter;
        private final double value;
        private final String message;

        public DialogueConsequence(ConsequenceType type, String parameter, double value, @Nullable String message) {
            this.type = type;
            this.parameter = parameter;
            this.value = value;
            this.message = message;
        }

        public ConsequenceType getType() { return type; }
        public String getParameter() { return parameter; }
        public double getValue() { return value; }
        @Nullable public String getMessage() { return message; }
    }

    /**
     * Speichert Gespraeche zwischen NPC und Spieler.
     */
    static class ConversationMemory {
        final List<String> topics = new ArrayList<>();
        int conversationCount = 0;
        long lastConversation = 0;
        int helpCount = 0; // Wie oft der Spieler geholfen hat
        int lieCount = 0; // Wie oft der Spieler gelogen hat
        boolean hasDiscount = false;
        long discountExpiry = 0;

        void recordConversation(String topic) {
            topics.add(topic);
            if (topics.size() > 20) topics.remove(0); // Max 20 Themen
            conversationCount++;
            lastConversation = System.currentTimeMillis();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private DialogueConsequenceSystem() {}

    public static DialogueConsequenceSystem getInstance() {
        if (instance == null) {
            synchronized (DialogueConsequenceSystem.class) {
                if (instance == null) {
                    instance = new DialogueConsequenceSystem();
                }
            }
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // CONSEQUENCE APPLICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Wendet eine Dialog-Konsequenz an.
     */
    public void applyConsequence(ServerPlayer player, CustomNPCEntity npc, DialogueConsequence consequence) {
        UUID playerUUID = player.getUUID();

        switch (consequence.getType()) {
            case REPUTATION -> applyReputation(playerUUID, consequence);
            case DISCOUNT -> applyDiscount(playerUUID, npc, consequence);
            case ECONOMY -> applyEconomy(playerUUID, consequence);
            case RELATIONSHIP -> applyRelationship(playerUUID, npc, consequence);
            case INFORMATION -> applyInformation(player, npc, consequence);
            case QUEST -> LOGGER.info("Quest consequence triggered for player {}", playerUUID);
        }

        // Gespraech protokollieren
        recordConversation(npc.getUUID(), playerUUID, consequence.getType().name());

        // Nachricht an Spieler
        if (consequence.getMessage() != null) {
            player.sendSystemMessage(Component.literal(
                "\u00A77[" + npc.getNpcName() + "] \u00A7f" + consequence.getMessage()
            ));
        }
    }

    private void applyReputation(UUID playerUUID, DialogueConsequence c) {
        FactionManager fm = FactionManager.getInstance();
        if (fm != null) {
            try {
                Faction faction = Faction.valueOf(c.getParameter().toUpperCase());
                fm.modifyReputation(playerUUID, faction, (int) c.getValue());
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Unknown faction in dialogue consequence: {}", c.getParameter());
            }
        }
    }

    private void applyDiscount(UUID playerUUID, CustomNPCEntity npc, DialogueConsequence c) {
        ConversationMemory memory = getOrCreateMemory(npc.getUUID(), playerUUID);
        memory.hasDiscount = true;
        // Rabatt fuer 1 Stunde (c.getValue() als Prozent)
        memory.discountExpiry = System.currentTimeMillis() + (long)(c.getValue() * 60 * 1000);
    }

    private void applyEconomy(UUID playerUUID, DialogueConsequence c) {
        double amount = c.getValue();
        if (amount > 0) {
            EconomyManager.deposit(playerUUID, amount);
        } else if (amount < 0) {
            EconomyManager.withdraw(playerUUID, -amount);
        }
    }

    private void applyRelationship(UUID playerUUID, CustomNPCEntity npc, DialogueConsequence c) {
        ConversationMemory memory = getOrCreateMemory(npc.getUUID(), playerUUID);
        if (c.getValue() > 0) {
            memory.helpCount += (int) c.getValue();
        } else {
            memory.lieCount += (int) -c.getValue();
        }
    }

    private void applyInformation(ServerPlayer player, CustomNPCEntity npc, DialogueConsequence c) {
        // NPC teilt Information basierend auf Beziehung
        ConversationMemory memory = getOrCreateMemory(npc.getUUID(), player.getUUID());
        if (memory.conversationCount >= 3) {
            // Nach 3+ Gespraechen gibt der NPC bessere Infos
            player.sendSystemMessage(Component.literal(
                "\u00A7e[Tipp] \u00A77" + c.getParameter()
            ));
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CONVERSATION MEMORY
    // ═══════════════════════════════════════════════════════════

    /**
     * Generiert kontext-abhaengige Begruessungen basierend auf Gespraechshistorie.
     */
    public String getContextualGreeting(UUID npcUUID, UUID playerUUID) {
        ConversationMemory memory = getMemory(npcUUID, playerUUID);
        if (memory == null) {
            return "Hallo! Ich glaube, wir haben uns noch nicht kennengelernt.";
        }

        if (memory.lieCount > 3) {
            return "Du schon wieder... Ich traue dir nicht.";
        }

        if (memory.helpCount > 5) {
            return "Mein Freund! Schoen dich zu sehen! Ich habe einen Rabatt fuer dich.";
        }

        if (memory.conversationCount > 10) {
            return "Hey, lange nicht gesehen! Was kann ich fuer dich tun?";
        }

        if (memory.conversationCount > 3) {
            return "Ah, du bist es wieder. Was brauchst du?";
        }

        return "Willkommen zurueck. Wie kann ich helfen?";
    }

    /**
     * Prueft ob ein NPC dem Spieler einen Rabatt gewaehrt.
     */
    public float getDiscountModifier(UUID npcUUID, UUID playerUUID) {
        ConversationMemory memory = getMemory(npcUUID, playerUUID);
        if (memory == null) return 1.0f;

        // Stammkunden-Rabatt: 5% nach 5 Gespraechen, 10% nach 10
        float discount = 1.0f;
        if (memory.conversationCount >= 10) {
            discount = 0.9f; // 10% Rabatt
        } else if (memory.conversationCount >= 5) {
            discount = 0.95f; // 5% Rabatt
        }

        // Temporaerer Dialog-Rabatt
        if (memory.hasDiscount && System.currentTimeMillis() < memory.discountExpiry) {
            discount *= 0.85f; // Zusaetzlich 15% Rabatt
        }

        // Luegner-Aufschlag
        if (memory.lieCount > 2) {
            discount *= 1.1f; // 10% teurer
        }

        return discount;
    }

    private void recordConversation(UUID npcUUID, UUID playerUUID, String topic) {
        getOrCreateMemory(npcUUID, playerUUID).recordConversation(topic);
    }

    private ConversationMemory getOrCreateMemory(UUID npcUUID, UUID playerUUID) {
        return conversationHistory
            .computeIfAbsent(npcUUID, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(playerUUID, k -> new ConversationMemory());
    }

    @Nullable
    private ConversationMemory getMemory(UUID npcUUID, UUID playerUUID) {
        Map<UUID, ConversationMemory> npcMemories = conversationHistory.get(npcUUID);
        return npcMemories != null ? npcMemories.get(playerUUID) : null;
    }
}
