package de.rolandsw.schedulemc.npc.life.quest;

import de.rolandsw.schedulemc.npc.life.social.Faction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Quest - Repräsentiert eine einzelne Quest
 *
 * Enthält alle Informationen über eine Quest: Ziele, Belohnungen, Status.
 */
public class Quest {

    // ═══════════════════════════════════════════════════════════
    // QUEST STATUS
    // ═══════════════════════════════════════════════════════════

    public enum QuestStatus {
        /** Quest ist verfügbar aber nicht angenommen */
        AVAILABLE,
        /** Quest wurde angenommen und läuft */
        ACTIVE,
        /** Alle Ziele erfüllt, Belohnung ausstehend */
        READY_TO_COMPLETE,
        /** Quest abgeschlossen */
        COMPLETED,
        /** Quest fehlgeschlagen */
        FAILED,
        /** Quest abgebrochen */
        ABANDONED
    }

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final String id;
    private final String title;
    private final String description;
    private final QuestType type;

    /** NPC der die Quest gegeben hat */
    private final UUID questGiverNPC;

    /** Zugehörige Fraktion */
    @Nullable
    private final Faction faction;

    /** Quest-Ziele */
    private final List<QuestObjective> objectives = new ArrayList<>();

    /** Belohnung */
    private QuestReward reward;

    /** Status */
    private QuestStatus status = QuestStatus.AVAILABLE;

    /** Zeitlimit in Minecraft-Tagen (0 = kein Limit) */
    private int timeLimitDays;

    /** Starttag der Quest */
    private long startDay = -1;

    /** Schwierigkeitsgrad (1-5) */
    private int difficulty = 1;

    /** Ist die Quest wiederholbar? */
    private boolean repeatable = false;

    /** Minimale Fraktionsreputation */
    private int minFactionRep = 0;

    /** Voraussetzungs-Quest */
    @Nullable
    private String prerequisiteQuestId;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════

    public Quest(String id, String title, String description, QuestType type, UUID questGiverNPC) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.questGiverNPC = questGiverNPC;
        this.faction = null;
        this.reward = QuestReward.create();
    }

    public Quest(String id, String title, String description, QuestType type, UUID questGiverNPC, Faction faction) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.questGiverNPC = questGiverNPC;
        this.faction = faction;
        this.reward = QuestReward.create();
    }

    // ═══════════════════════════════════════════════════════════
    // BUILDER METHODS
    // ═══════════════════════════════════════════════════════════

    public Quest addObjective(QuestObjective objective) {
        objectives.add(objective);
        return this;
    }

    public Quest setReward(QuestReward reward) {
        this.reward = reward;
        return this;
    }

    public Quest setTimeLimit(int days) {
        this.timeLimitDays = days;
        return this;
    }

    public Quest setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(5, difficulty));
        return this;
    }

    public Quest setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
        return this;
    }

    public Quest setMinFactionRep(int minRep) {
        this.minFactionRep = minRep;
        return this;
    }

    public Quest setPrerequisite(String questId) {
        this.prerequisiteQuestId = questId;
        return this;
    }

    // ═══════════════════════════════════════════════════════════
    // QUEST LIFECYCLE
    // ═══════════════════════════════════════════════════════════

    /**
     * Startet die Quest
     */
    public boolean start(long currentDay) {
        if (status != QuestStatus.AVAILABLE) {
            return false;
        }

        status = QuestStatus.ACTIVE;
        startDay = currentDay;
        return true;
    }

    /**
     * Prüft und aktualisiert den Quest-Status
     */
    public void updateStatus(long currentDay) {
        if (status != QuestStatus.ACTIVE) return;

        // Zeitlimit prüfen
        if (timeLimitDays > 0 && currentDay - startDay > timeLimitDays) {
            fail();
            return;
        }

        // Prüfe ob alle Ziele erfüllt
        boolean allCompleted = true;
        boolean anyFailed = false;

        for (QuestObjective obj : objectives) {
            if (obj.isFailed()) {
                anyFailed = true;
                break;
            }
            if (!obj.isCompleted()) {
                allCompleted = false;
            }
        }

        if (anyFailed) {
            fail();
        } else if (allCompleted) {
            status = QuestStatus.READY_TO_COMPLETE;
        }
    }

    /**
     * Schließt die Quest ab
     */
    public boolean complete() {
        if (status != QuestStatus.READY_TO_COMPLETE) {
            return false;
        }

        status = QuestStatus.COMPLETED;
        return true;
    }

    /**
     * Lässt die Quest fehlschlagen
     */
    public void fail() {
        if (status == QuestStatus.ACTIVE || status == QuestStatus.READY_TO_COMPLETE) {
            status = QuestStatus.FAILED;
        }
    }

    /**
     * Bricht die Quest ab
     */
    public void abandon() {
        if (status == QuestStatus.ACTIVE) {
            status = QuestStatus.ABANDONED;
        }
    }

    /**
     * Setzt die Quest zurück (für wiederholbare Quests)
     */
    public void reset() {
        if (!repeatable) return;

        status = QuestStatus.AVAILABLE;
        startDay = -1;

        for (QuestObjective obj : objectives) {
            // Objectives müssen neu erstellt werden
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public QuestType getType() { return type; }
    public UUID getQuestGiverNPC() { return questGiverNPC; }
    @Nullable public Faction getFaction() { return faction; }
    public List<QuestObjective> getObjectives() { return new ArrayList<>(objectives); }
    public QuestReward getReward() { return reward; }
    public QuestStatus getStatus() { return status; }
    public int getTimeLimitDays() { return timeLimitDays; }
    public long getStartDay() { return startDay; }
    public int getDifficulty() { return difficulty; }
    public boolean isRepeatable() { return repeatable; }
    public int getMinFactionRep() { return minFactionRep; }
    @Nullable public String getPrerequisiteQuestId() { return prerequisiteQuestId; }

    /**
     * Prüft ob die Quest aktiv ist
     */
    public boolean isActive() {
        return status == QuestStatus.ACTIVE;
    }

    /**
     * Prüft ob die Quest abgeschlossen werden kann
     */
    public boolean isReadyToComplete() {
        return status == QuestStatus.READY_TO_COMPLETE;
    }

    /**
     * Prüft ob die Quest beendet ist (completed, failed, oder abandoned)
     */
    public boolean isFinished() {
        return status == QuestStatus.COMPLETED ||
               status == QuestStatus.FAILED ||
               status == QuestStatus.ABANDONED;
    }

    /**
     * Berechnet den Gesamtfortschritt (0-1)
     */
    public float getTotalProgress() {
        if (objectives.isEmpty()) return 0;

        float total = 0;
        for (QuestObjective obj : objectives) {
            total += obj.getProgressPercent();
        }
        return total / objectives.size();
    }

    /**
     * Verbleibende Zeit in Tagen
     */
    public int getRemainingDays(long currentDay) {
        if (timeLimitDays <= 0 || startDay < 0) return -1;
        return Math.max(0, timeLimitDays - (int)(currentDay - startDay));
    }

    /**
     * Holt ein spezifisches Objective
     */
    @Nullable
    public QuestObjective getObjective(String objectiveId) {
        return objectives.stream()
            .filter(o -> o.getId().equals(objectiveId))
            .findFirst()
            .orElse(null);
    }

    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putString("id", id);
        tag.putString("title", title);
        tag.putString("description", description);
        tag.putString("type", type.name());
        tag.putUUID("questGiverNPC", questGiverNPC);
        tag.putString("status", status.name());
        tag.putInt("timeLimitDays", timeLimitDays);
        tag.putLong("startDay", startDay);
        tag.putInt("difficulty", difficulty);
        tag.putBoolean("repeatable", repeatable);
        tag.putInt("minFactionRep", minFactionRep);

        if (faction != null) {
            tag.putString("faction", faction.name());
        }

        if (prerequisiteQuestId != null) {
            tag.putString("prerequisiteQuestId", prerequisiteQuestId);
        }

        // Objectives
        ListTag objectivesTag = new ListTag();
        for (QuestObjective obj : objectives) {
            objectivesTag.add(obj.save());
        }
        tag.put("objectives", objectivesTag);

        // Reward
        tag.put("reward", reward.save());

        return tag;
    }

    public static Quest load(CompoundTag tag) {
        String id = tag.getString("id");
        String title = tag.getString("title");
        String description = tag.getString("description");
        QuestType type = QuestType.valueOf(tag.getString("type"));
        UUID questGiverNPC = tag.getUUID("questGiverNPC");

        Faction faction = null;
        if (tag.contains("faction")) {
            faction = Faction.valueOf(tag.getString("faction"));
        }

        Quest quest = new Quest(id, title, description, type, questGiverNPC, faction);

        quest.status = QuestStatus.valueOf(tag.getString("status"));
        quest.timeLimitDays = tag.getInt("timeLimitDays");
        quest.startDay = tag.getLong("startDay");
        quest.difficulty = tag.getInt("difficulty");
        quest.repeatable = tag.getBoolean("repeatable");
        quest.minFactionRep = tag.getInt("minFactionRep");

        if (tag.contains("prerequisiteQuestId")) {
            quest.prerequisiteQuestId = tag.getString("prerequisiteQuestId");
        }

        // Objectives
        ListTag objectivesTag = tag.getList("objectives", Tag.TAG_COMPOUND);
        for (int i = 0; i < objectivesTag.size(); i++) {
            quest.objectives.add(QuestObjective.load(objectivesTag.getCompound(i)));
        }

        // Reward
        if (tag.contains("reward")) {
            quest.reward = QuestReward.load(tag.getCompound("reward"));
        }

        return quest;
    }

    @Override
    public String toString() {
        return String.format("Quest{id='%s', title='%s', type=%s, status=%s, progress=%.0f%%}",
            id, title, type, status, getTotalProgress() * 100);
    }
}
