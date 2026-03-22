package de.rolandsw.schedulemc.npc.life.quest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.*;

/**
 * QuestProgress - Verfolgt den Quest-Fortschritt eines Spielers
 *
 * Speichert aktive, abgeschlossene und fehlgeschlagene Quests.
 */
public class QuestProgress {

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Maximale Anzahl aktiver Quests */
    public static final int MAX_ACTIVE_QUESTS = 10;

    /** Maximale gespeicherte abgeschlossene Quests */
    public static final int MAX_COMPLETED_HISTORY = 50;

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final UUID playerUUID;

    /** Aktive Quests: Quest ID -> Quest */
    private final Map<String, Quest> activeQuests = new LinkedHashMap<>();

    /** Abgeschlossene Quest-IDs (für Prerequisite-Check) */
    private final Set<String> completedQuestIds = new LinkedHashSet<>();

    /** Fehlgeschlagene Quest-IDs */
    private final Set<String> failedQuestIds = new HashSet<>();

    /** Quest-Statistiken */
    private int totalQuestsCompleted = 0;
    private int totalQuestsFailed = 0;
    private int totalQuestsAbandoned = 0;

    /** Cooldowns für wiederholbare Quests: Quest ID -> Tag wann wieder verfügbar */
    private final Map<String, Long> questCooldowns = new HashMap<>();

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public QuestProgress(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    // ═══════════════════════════════════════════════════════════
    // QUEST MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Akzeptiert eine neue Quest
     */
    public boolean acceptQuest(Quest quest, long currentDay) {
        // Prüfe Limit
        if (activeQuests.size() >= MAX_ACTIVE_QUESTS) {
            return false;
        }

        // Prüfe ob bereits aktiv
        if (activeQuests.containsKey(quest.getId())) {
            return false;
        }

        // Prüfe Prerequisite
        String prereq = quest.getPrerequisiteQuestId();
        if (prereq != null && !completedQuestIds.contains(prereq)) {
            return false;
        }

        // Prüfe Cooldown für wiederholbare Quests
        if (questCooldowns.containsKey(quest.getId())) {
            if (currentDay < questCooldowns.get(quest.getId())) {
                return false;
            }
            questCooldowns.remove(quest.getId());
        }

        // Quest starten
        if (!quest.start(currentDay)) {
            return false;
        }

        activeQuests.put(quest.getId(), quest);
        return true;
    }

    /**
     * Schließt eine Quest ab
     */
    public boolean completeQuest(String questId) {
        Quest quest = activeQuests.get(questId);
        if (quest == null || !quest.isReadyToComplete()) {
            return false;
        }

        if (!quest.complete()) {
            return false;
        }

        // Aus aktiven entfernen
        activeQuests.remove(questId);

        // Zu abgeschlossen hinzufügen
        completedQuestIds.add(questId);
        totalQuestsCompleted++;

        // History begrenzen
        while (completedQuestIds.size() > MAX_COMPLETED_HISTORY) {
            Iterator<String> it = completedQuestIds.iterator();
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
        }

        return true;
    }

    /**
     * Lässt eine Quest fehlschlagen
     */
    public void failQuest(String questId) {
        Quest quest = activeQuests.remove(questId);
        if (quest != null) {
            quest.fail();
            failedQuestIds.add(questId);
            totalQuestsFailed++;
        }
    }

    /**
     * Bricht eine Quest ab
     */
    public void abandonQuest(String questId) {
        Quest quest = activeQuests.remove(questId);
        if (quest != null) {
            quest.abandon();
            totalQuestsAbandoned++;
        }
    }

    /**
     * Aktualisiert alle aktiven Quests
     */
    public void tick(long currentDay) {
        List<String> toFail = new ArrayList<>();

        for (Quest quest : activeQuests.values()) {
            quest.updateStatus(currentDay);

            if (quest.getStatus() == Quest.QuestStatus.FAILED) {
                toFail.add(quest.getId());
            }
        }

        // Fehlgeschlagene entfernen
        for (String questId : toFail) {
            Quest quest = activeQuests.remove(questId);
            if (quest != null) {
                failedQuestIds.add(questId);
                totalQuestsFailed++;
            }
        }
    }

    /**
     * Setzt Cooldown für wiederholbare Quest
     */
    public void setCooldown(String questId, long availableDay) {
        questCooldowns.put(questId, availableDay);
    }

    // ═══════════════════════════════════════════════════════════
    // OBJECTIVE PROGRESS
    // ═══════════════════════════════════════════════════════════

    /**
     * Meldet Item-Fortschritt für alle aktiven Quests
     */
    public void reportItemProgress(net.minecraft.world.item.ItemStack stack) {
        for (Quest quest : activeQuests.values()) {
            for (QuestObjective obj : quest.getObjectives()) {
                obj.checkItemProgress(stack);
            }
        }
    }

    /**
     * Meldet Positions-Fortschritt
     */
    public void reportLocationProgress(net.minecraft.core.BlockPos pos) {
        for (Quest quest : activeQuests.values()) {
            for (QuestObjective obj : quest.getObjectives()) {
                obj.checkLocationProgress(pos);
            }
        }
    }

    /**
     * Meldet NPC-Interaktions-Fortschritt
     */
    public void reportNPCInteraction(UUID npcUUID) {
        for (Quest quest : activeQuests.values()) {
            for (QuestObjective obj : quest.getObjectives()) {
                obj.checkNPCInteraction(npcUUID);
            }
        }
    }

    /**
     * Meldet Kill-Fortschritt
     */
    public void reportKill(String entityType) {
        for (Quest quest : activeQuests.values()) {
            for (QuestObjective obj : quest.getObjectives()) {
                obj.checkKillProgress(entityType);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public UUID getPlayerUUID() { return playerUUID; }

    public Map<String, Quest> getActiveQuests() {
        return new LinkedHashMap<>(activeQuests);
    }

    public Quest getActiveQuest(String questId) {
        return activeQuests.get(questId);
    }

    public boolean hasActiveQuest(String questId) {
        return activeQuests.containsKey(questId);
    }

    public boolean hasCompletedQuest(String questId) {
        return completedQuestIds.contains(questId);
    }

    public boolean hasFailedQuest(String questId) {
        return failedQuestIds.contains(questId);
    }

    public int getActiveQuestCount() {
        return activeQuests.size();
    }

    public Set<String> getCompletedQuestIds() { return completedQuestIds; }
    public Map<String, Long> getQuestCooldowns() { return questCooldowns; }
    public int getTotalQuestsCompleted() { return totalQuestsCompleted; }
    public int getTotalQuestsFailed() { return totalQuestsFailed; }
    public int getTotalQuestsAbandoned() { return totalQuestsAbandoned; }

    /**
     * Prüft ob die Quest vom Spieler angenommen werden kann
     */
    public boolean canAcceptQuest(Quest quest, long currentDay) {
        if (activeQuests.size() >= MAX_ACTIVE_QUESTS) return false;
        if (activeQuests.containsKey(quest.getId())) return false;

        // Prerequisite
        String prereq = quest.getPrerequisiteQuestId();
        if (prereq != null && !completedQuestIds.contains(prereq)) return false;

        // Cooldown
        if (questCooldowns.containsKey(quest.getId())) {
            if (currentDay < questCooldowns.get(quest.getId())) return false;
        }

        return true;
    }

    /**
     * Gibt alle Quests zurück, die beim angegebenen NPC abgeholt werden können
     */
    public List<Quest> getQuestsReadyToCompleteAt(UUID npcUUID) {
        List<Quest> ready = new ArrayList<>();
        for (Quest quest : activeQuests.values()) {
            if (quest.isReadyToComplete() && quest.getQuestGiverNPC().equals(npcUUID)) {
                ready.add(quest);
            }
        }
        return ready;
    }

    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("playerUUID", playerUUID);
        tag.putInt("totalCompleted", totalQuestsCompleted);
        tag.putInt("totalFailed", totalQuestsFailed);
        tag.putInt("totalAbandoned", totalQuestsAbandoned);

        // Active Quests
        ListTag activeTag = new ListTag();
        for (Quest quest : activeQuests.values()) {
            activeTag.add(quest.save());
        }
        tag.put("activeQuests", activeTag);

        // Completed IDs
        ListTag completedTag = new ListTag();
        for (String id : completedQuestIds) {
            completedTag.add(StringTag.valueOf(id));
        }
        tag.put("completedIds", completedTag);

        // Failed IDs
        ListTag failedTag = new ListTag();
        for (String id : failedQuestIds) {
            failedTag.add(StringTag.valueOf(id));
        }
        tag.put("failedIds", failedTag);

        // Cooldowns
        CompoundTag cooldownsTag = new CompoundTag();
        for (Map.Entry<String, Long> entry : questCooldowns.entrySet()) {
            cooldownsTag.putLong(entry.getKey(), entry.getValue());
        }
        tag.put("cooldowns", cooldownsTag);

        return tag;
    }

    public static QuestProgress load(CompoundTag tag) {
        UUID playerUUID = tag.getUUID("playerUUID");
        QuestProgress progress = new QuestProgress(playerUUID);

        progress.totalQuestsCompleted = tag.getInt("totalCompleted");
        progress.totalQuestsFailed = tag.getInt("totalFailed");
        progress.totalQuestsAbandoned = tag.getInt("totalAbandoned");

        // Active Quests
        ListTag activeTag = tag.getList("activeQuests", Tag.TAG_COMPOUND);
        for (int i = 0; i < activeTag.size(); i++) {
            Quest quest = Quest.load(activeTag.getCompound(i));
            progress.activeQuests.put(quest.getId(), quest);
        }

        // Completed IDs
        ListTag completedTag = tag.getList("completedIds", Tag.TAG_STRING);
        for (int i = 0; i < completedTag.size(); i++) {
            progress.completedQuestIds.add(completedTag.getString(i));
        }

        // Failed IDs
        ListTag failedTag = tag.getList("failedIds", Tag.TAG_STRING);
        for (int i = 0; i < failedTag.size(); i++) {
            progress.failedQuestIds.add(failedTag.getString(i));
        }

        // Cooldowns
        CompoundTag cooldownsTag = tag.getCompound("cooldowns");
        for (String key : cooldownsTag.getAllKeys()) {
            progress.questCooldowns.put(key, cooldownsTag.getLong(key));
        }

        return progress;
    }

    @Override
    public String toString() {
        return String.format("QuestProgress{player=%s, active=%d, completed=%d, failed=%d}",
            playerUUID, activeQuests.size(), totalQuestsCompleted, totalQuestsFailed);
    }
}
