package de.rolandsw.schedulemc.mission;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Vorlage für eine Spieler-Mission.
 * Statische Konfiguration — Instanzen werden in {@link MissionRegistry} verwaltet.
 */
public class MissionDefinition {

    private final String id;
    private final String title;
    private final String description;
    private final MissionCategory category;
    private final int xpReward;
    private final int moneyReward;
    private final int targetAmount;
    private final String trackingKey;
    private final List<String> prerequisiteIds;
    @Nullable
    private final UUID npcGiverUUID;
    private final String npcGiverName;

    public MissionDefinition(String id, String title, String description,
                             MissionCategory category, int xpReward, int moneyReward,
                             int targetAmount, String trackingKey,
                             List<String> prerequisiteIds,
                             @Nullable UUID npcGiverUUID, String npcGiverName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.xpReward = xpReward;
        this.moneyReward = moneyReward;
        this.targetAmount = targetAmount;
        this.trackingKey = trackingKey;
        this.prerequisiteIds = prerequisiteIds != null ? prerequisiteIds : Collections.emptyList();
        this.npcGiverUUID = npcGiverUUID;
        this.npcGiverName = npcGiverName != null ? npcGiverName : "";
    }

    /** Vereinfachter Konstruktor ohne NPC-Geber und Voraussetzungen */
    public MissionDefinition(String id, String title, String description,
                             MissionCategory category, int xpReward, int moneyReward,
                             int targetAmount, String trackingKey) {
        this(id, title, description, category, xpReward, moneyReward,
             targetAmount, trackingKey, Collections.emptyList(), null, "");
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public MissionCategory getCategory() { return category; }
    public int getXpReward() { return xpReward; }
    public int getMoneyReward() { return moneyReward; }
    public int getTargetAmount() { return targetAmount; }
    public String getTrackingKey() { return trackingKey; }
    public List<String> getPrerequisiteIds() { return prerequisiteIds; }
    @Nullable public UUID getNpcGiverUUID() { return npcGiverUUID; }
    public String getNpcGiverName() { return npcGiverName; }
}
