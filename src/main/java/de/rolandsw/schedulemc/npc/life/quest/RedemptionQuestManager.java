package de.rolandsw.schedulemc.npc.life.quest;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vergebungs-Quest-System: Erlaubt Spielern, verlorene Reputation wiederherzustellen.
 *
 * Quests werden automatisch verfuegbar wenn Reputation unter -20 faellt.
 * Abschluss einer Quest gibt +15 Reputation bei der betroffenen Fraktion.
 *
 * Quest-Typen:
 * - Community Service: Hilf der Stadt (ORDNUNG/BUERGER)
 * - Spende: Zahle eine Geldstrafe (alle Fraktionen)
 * - Kurierdienst: Liefere Waren (HAENDLER)
 * - Wachpatrouille: Patrouiliere ein Gebiet (ORDNUNG)
 */
public class RedemptionQuestManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile RedemptionQuestManager instance;

    // Aktive Vergebungs-Quests pro Spieler
    private final Map<UUID, RedemptionQuest> activeQuests = new ConcurrentHashMap<>();

    // Cooldown: Ein Quest pro Fraktion alle 30 Minuten
    private final Map<UUID, Map<Faction, Long>> cooldowns = new ConcurrentHashMap<>();

    private static final long COOLDOWN_MS = 30 * 60 * 1000L; // 30 Minuten
    private static final int REPUTATION_THRESHOLD = -20; // Ab wann Quests verfuegbar
    private static final int REPUTATION_REWARD = 15; // Belohnung pro Quest

    public enum RedemptionQuestType {
        COMMUNITY_SERVICE("Sozialdienst", "Hilf 5 NPCs bei einer Aufgabe", 5),
        DONATION("Spende", "Zahle eine Geldstrafe", 1),
        COURIER("Kurierdienst", "Liefere 3 Waren an NPCs", 3),
        PATROL("Wachpatrouille", "Patrouiliere 5 Minuten durch die Stadt", 1);

        private final String displayName;
        private final String description;
        private final int requiredProgress;

        RedemptionQuestType(String displayName, String description, int requiredProgress) {
            this.displayName = displayName;
            this.description = description;
            this.requiredProgress = requiredProgress;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public int getRequiredProgress() { return requiredProgress; }
    }

    public static class RedemptionQuest {
        private final UUID playerUUID;
        private final Faction targetFaction;
        private final RedemptionQuestType type;
        private int progress;
        private final long startTime;

        public RedemptionQuest(UUID playerUUID, Faction targetFaction, RedemptionQuestType type) {
            this.playerUUID = playerUUID;
            this.targetFaction = targetFaction;
            this.type = type;
            this.progress = 0;
            this.startTime = System.currentTimeMillis();
        }

        public UUID getPlayerUUID() { return playerUUID; }
        public Faction getTargetFaction() { return targetFaction; }
        public RedemptionQuestType getType() { return type; }
        public int getProgress() { return progress; }
        public boolean isComplete() { return progress >= type.getRequiredProgress(); }

        public void incrementProgress() { progress++; }
    }

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private RedemptionQuestManager() {}

    public static RedemptionQuestManager getInstance() {
        if (instance == null) {
            synchronized (RedemptionQuestManager.class) {
                if (instance == null) {
                    instance = new RedemptionQuestManager();
                }
            }
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // QUEST MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Prueft ob ein Spieler eine Vergebungs-Quest annehmen kann.
     */
    public boolean canAcceptQuest(UUID playerUUID, Faction faction) {
        // Schon eine aktive Quest?
        if (activeQuests.containsKey(playerUUID)) return false;

        // Cooldown pruefen
        Map<Faction, Long> playerCooldowns = cooldowns.get(playerUUID);
        if (playerCooldowns != null) {
            Long lastTime = playerCooldowns.get(faction);
            if (lastTime != null && System.currentTimeMillis() - lastTime < COOLDOWN_MS) {
                return false;
            }
        }

        // Reputation muss unter Schwellwert sein
        FactionManager fm = FactionManager.getInstance();
        if (fm == null) return false;

        return fm.getReputation(playerUUID, faction) <= REPUTATION_THRESHOLD;
    }

    /**
     * Startet eine Vergebungs-Quest.
     */
    @Nullable
    public RedemptionQuest startQuest(ServerPlayer player, Faction faction) {
        UUID uuid = player.getUUID();
        if (!canAcceptQuest(uuid, faction)) return null;

        // Zufaelligen Quest-Typ waehlen
        RedemptionQuestType[] types = RedemptionQuestType.values();
        RedemptionQuestType type = types[new Random().nextInt(types.length)];

        RedemptionQuest quest = new RedemptionQuest(uuid, faction, type);
        activeQuests.put(uuid, quest);

        player.sendSystemMessage(Component.literal(
            "\u00A76\u00A7l\u2605 Vergebungs-Quest angenommen! \u2605\u00A7r\n" +
            "\u00A7fFraktion: \u00A7e" + faction.getDisplayName() + "\n" +
            "\u00A7fAufgabe: \u00A7e" + type.getDisplayName() + "\n" +
            "\u00A7f" + type.getDescription() + "\n" +
            "\u00A77Belohnung: \u00A7a+" + REPUTATION_REWARD + " Reputation"
        ));

        LOGGER.info("Player {} started redemption quest for faction {} (type: {})",
            uuid, faction, type);
        return quest;
    }

    /**
     * Meldet Fortschritt fuer eine aktive Quest.
     */
    public void reportProgress(ServerPlayer player) {
        UUID uuid = player.getUUID();
        RedemptionQuest quest = activeQuests.get(uuid);
        if (quest == null) return;

        quest.incrementProgress();

        if (quest.isComplete()) {
            completeQuest(player, quest);
        } else {
            player.sendSystemMessage(Component.literal(
                "\u00A7a[Quest] \u00A77Fortschritt: " + quest.getProgress() +
                "/" + quest.getType().getRequiredProgress()
            ));
        }
    }

    private void completeQuest(ServerPlayer player, RedemptionQuest quest) {
        UUID uuid = player.getUUID();

        // Reputation wiederherstellen
        FactionManager fm = FactionManager.getInstance();
        if (fm != null) {
            fm.modifyReputation(uuid, quest.getTargetFaction(), REPUTATION_REWARD);
        }

        // Cooldown setzen
        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
            .put(quest.getTargetFaction(), System.currentTimeMillis());

        // Quest entfernen
        activeQuests.remove(uuid);

        player.sendSystemMessage(Component.literal(
            "\u00A7a\u00A7l\u2714 Vergebungs-Quest abgeschlossen! \u2714\u00A7r\n" +
            "\u00A7a+" + REPUTATION_REWARD + " Reputation bei " +
            quest.getTargetFaction().getDisplayName()
        ));

        LOGGER.info("Player {} completed redemption quest for faction {} (+{} rep)",
            uuid, quest.getTargetFaction(), REPUTATION_REWARD);
    }

    /**
     * Gibt die aktive Quest eines Spielers zurueck.
     */
    @Nullable
    public RedemptionQuest getActiveQuest(UUID playerUUID) {
        return activeQuests.get(playerUUID);
    }

    /**
     * Bricht eine aktive Quest ab.
     */
    public void abandonQuest(UUID playerUUID) {
        activeQuests.remove(playerUUID);
    }

    /**
     * Gibt verfuegbare Vergebungs-Fraktionen fuer einen Spieler zurueck.
     */
    public List<Faction> getAvailableFactions(UUID playerUUID) {
        List<Faction> available = new ArrayList<>();
        for (Faction faction : Faction.values()) {
            if (canAcceptQuest(playerUUID, faction)) {
                available.add(faction);
            }
        }
        return available;
    }
}
