package de.rolandsw.schedulemc.npc.life.quest;

import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.NPCLifeConstants;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import org.slf4j.Logger;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * QuestManager - Verwaltet alle Quests im System mit JSON-Persistenz
 *
 * Verantwortlich für:
 * - Quest-Vorlagen registrieren
 * - Dynamische Quest-Generierung
 * - Spieler-Fortschritt verwalten
 * - Quest-Belohnungen verteilen
 */
public class QuestManager extends AbstractPersistenceManager<QuestManager.QuestManagerData> {

    private static final Logger LOGGER = LogUtils.getLogger();

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private static volatile QuestManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    @Nullable
    public static QuestManager getInstance() {
        return instance;
    }

    public static QuestManager getInstance(MinecraftServer server) {
        QuestManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new QuestManager(server);
                }
            }
        }
        return result;
    }

    /**
     * Gets manager instance for a specific level (convenience method).
     * Note: Manager is server-wide, not per-level.
     */
    public static QuestManager getManager(ServerLevel level) {
        return getInstance(level.getServer());
    }

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private MinecraftServer server;

    /** Registrierte Quest-Vorlagen: Template ID -> Template (TRANSIENT - nicht persistiert) */
    private final Map<String, QuestTemplate> questTemplates = new ConcurrentHashMap<>();

    /** Spieler-Fortschritt: Player UUID -> Progress */
    private final Map<UUID, QuestProgress> playerProgress = new ConcurrentHashMap<>();

    /** NPC-Quest-Angebote: NPC UUID -> List of Quest IDs currently offered */
    private final Map<UUID, List<String>> npcQuestOffers = new ConcurrentHashMap<>();

    /** Quest-ID-Zähler für eindeutige IDs */
    private int questIdCounter = 0;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private QuestManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("npc_life_quests.json").toFile(),
            GsonHelper.get()
        );
        this.server = server;
        load();
        registerDefaultTemplates();
    }

    // ═══════════════════════════════════════════════════════════
    // TEMPLATE REGISTRATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert die Standard-Quest-Vorlagen
     */
    private void registerDefaultTemplates() {
        // Lieferquests
        registerTemplate(QuestTemplate.builder("delivery_basic")
            .type(QuestType.DELIVERY)
            .title("Einfache Lieferung")
            .description("Liefern Sie ein Paket an den Empfänger.")
            .difficulty(1)
            .baseReward(QuestReward.create().money(50).factionRep(Faction.HAENDLER, 2))
            .build());

        registerTemplate(QuestTemplate.builder("delivery_urgent")
            .type(QuestType.DELIVERY)
            .title("Dringende Lieferung")
            .description("Eine zeitkritische Lieferung muss schnell zugestellt werden!")
            .difficulty(2)
            .timeLimit(1)
            .baseReward(QuestReward.create().money(150).factionRep(Faction.HAENDLER, 5))
            .build());

        // Sammelquests
        registerTemplate(QuestTemplate.builder("collect_materials")
            .type(QuestType.COLLECTION)
            .title("Materialsammlung")
            .description("Sammeln Sie die benötigten Materialien.")
            .difficulty(1)
            .baseReward(QuestReward.create().money(75).experience(50))
            .build());

        // Eskort-Quests
        registerTemplate(QuestTemplate.builder("escort_citizen")
            .type(QuestType.ESCORT)
            .title("Sicheres Geleit")
            .description("Begleiten Sie den NPC sicher zu seinem Ziel.")
            .difficulty(3)
            .minFactionRep(10)
            .baseReward(QuestReward.create().money(200).factionRep(Faction.BUERGER, 10))
            .build());

        // Eliminierungs-Quests
        registerTemplate(QuestTemplate.builder("eliminate_threat")
            .type(QuestType.ELIMINATION)
            .title("Bedrohung beseitigen")
            .description("Eliminieren Sie die Bedrohung in der Gegend.")
            .difficulty(3)
            .minFactionRep(20)
            .baseReward(QuestReward.create().money(300).factionRep(Faction.ORDNUNG, 15))
            .build());

        // Ermittlungs-Quests
        registerTemplate(QuestTemplate.builder("investigate_crime")
            .type(QuestType.INVESTIGATION)
            .title("Ermittlung")
            .description("Untersuchen Sie den Vorfall und finden Sie Hinweise.")
            .difficulty(2)
            .minFactionRep(15)
            .baseReward(QuestReward.create().money(150).factionRep(Faction.ORDNUNG, 8))
            .build());

        // Verhandlungs-Quests
        registerTemplate(QuestTemplate.builder("negotiate_deal")
            .type(QuestType.NEGOTIATION)
            .title("Vermittlung")
            .description("Verhandeln Sie einen Deal zwischen den Parteien.")
            .difficulty(4)
            .minFactionRep(25)
            .baseReward(QuestReward.create().money(250).factionRep(Faction.HAENDLER, 12))
            .build());

        // Untergrund-Quests
        registerTemplate(QuestTemplate.builder("underground_delivery")
            .type(QuestType.DELIVERY)
            .title("Diskrete Lieferung")
            .description("Eine Lieferung, über die niemand etwas erfahren sollte...")
            .difficulty(2)
            .faction(Faction.UNTERGRUND)
            .minFactionRep(10)
            .baseReward(QuestReward.create().money(200).factionRep(Faction.UNTERGRUND, 8))
            .build());
    }

    /**
     * Registriert eine Quest-Vorlage
     */
    public void registerTemplate(QuestTemplate template) {
        questTemplates.put(template.getId(), template);
    }

    // ═══════════════════════════════════════════════════════════
    // QUEST GENERATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Generiert eine Quest basierend auf einer Vorlage
     */
    @Nullable
    public Quest generateQuest(String templateId, CustomNPCEntity questGiver, ServerPlayer player) {
        QuestTemplate template = questTemplates.get(templateId);
        if (template == null) return null;

        // Prüfe ob Spieler die Quest annehmen kann
        FactionManager factionManager = FactionManager.getInstance();
        if (factionManager != null && template.getFaction() != null) {
            int playerRep = factionManager.getReputation(player.getUUID(), template.getFaction());
            if (playerRep < template.getMinFactionRep()) {
                return null;
            }
        }

        // Einzigartige Quest-ID generieren
        String questId = templateId + "_" + (++questIdCounter);
        markDirty();

        // Quest erstellen
        Quest quest = new Quest(
            questId,
            template.getTitle(),
            template.getDescription(),
            template.getType(),
            questGiver.getNpcData().getNpcUUID(),
            template.getFaction()
        );

        // Eigenschaften setzen
        quest.setDifficulty(template.getDifficulty());
        quest.setTimeLimit(template.getTimeLimit());
        quest.setMinFactionRep(template.getMinFactionRep());
        quest.setRepeatable(template.isRepeatable());

        // Ziele generieren
        generateObjectives(quest, template, questGiver, player);

        // Belohnung setzen (skaliert nach Schwierigkeit)
        QuestReward reward = template.getBaseReward().scale(1.0f + (template.getDifficulty() - 1) * 0.25f);
        quest.setReward(reward);

        return quest;
    }

    /**
     * Generiert Ziele für eine Quest
     */
    private void generateObjectives(Quest quest, QuestTemplate template, CustomNPCEntity questGiver, ServerPlayer player) {
        switch (template.getType()) {
            case DELIVERY -> {
                // Lieferung: Item an einen anderen NPC
                quest.addObjective(QuestObjective.collectItems(
                    "collect_package",
                    Items.PAPER, // Placeholder
                    1,
                    "Paket abholen"
                ));
                // Hier würde man dynamisch einen Ziel-NPC finden
                quest.addObjective(QuestObjective.deliverToNPC(
                    "deliver_package",
                    Items.PAPER,
                    1,
                    questGiver.getNpcData().getNpcUUID(), // Placeholder - sollte anderer NPC sein
                    "Paket abliefern"
                ));
            }

            case COLLECTION -> {
                // Sammlung: Bestimmte Anzahl Items
                int amount = 5 + template.getDifficulty() * 5;
                quest.addObjective(QuestObjective.collectItems(
                    "collect_materials",
                    Items.COAL, // Placeholder
                    amount,
                    String.format("Sammle %d Materialien", amount)
                ));
            }

            case ESCORT -> {
                // Eskorte: NPC zu einem Ort bringen
                BlockPos destination = questGiver.blockPosition().offset(50, 0, 50); // Placeholder
                quest.addObjective(QuestObjective.escortNPC(
                    "escort_npc",
                    questGiver.getNpcData().getNpcUUID(),
                    destination,
                    "Begleite den NPC sicher zum Ziel"
                ));
            }

            case ELIMINATION -> {
                // Eliminierung: Feinde besiegen
                int kills = 3 + template.getDifficulty() * 2;
                quest.addObjective(QuestObjective.killEntities(
                    "eliminate_threats",
                    "zombie", // Placeholder
                    kills,
                    String.format("Beseitige %d Bedrohungen", kills)
                ));
            }

            case INVESTIGATION -> {
                // Ermittlung: Mehrere NPCs befragen
                quest.addObjective(QuestObjective.talkToNPC(
                    "investigate_1",
                    questGiver.getNpcData().getNpcUUID(), // Placeholder
                    "Befrage Zeugen"
                ));
                quest.addObjective(QuestObjective.visitLocation(
                    "investigate_2",
                    questGiver.blockPosition().offset(20, 0, 20),
                    5,
                    "Untersuche den Tatort"
                ));
            }

            case NEGOTIATION -> {
                // Verhandlung: Mit mehreren NPCs sprechen
                quest.addObjective(QuestObjective.talkToNPC(
                    "negotiate_party1",
                    questGiver.getNpcData().getNpcUUID(), // Placeholder
                    "Sprich mit der ersten Partei"
                ));
                quest.addObjective(QuestObjective.negotiateDeal(
                    "negotiate_deal",
                    questGiver.getNpcData().getNpcUUID(), // Placeholder
                    "Schließe den Deal ab"
                ));
            }
        }
    }

    /**
     * Generiert eine zufällige Quest für einen NPC
     */
    @Nullable
    public Quest generateRandomQuest(CustomNPCEntity npc, ServerPlayer player) {
        NPCType npcType = npc.getNpcType();

        // Passende Templates finden
        List<QuestTemplate> suitable = new ArrayList<>();
        for (QuestTemplate template : questTemplates.values()) {
            if (template.getType().canBeGivenBy(npcType)) {
                // Fraktions-Check
                if (template.getFaction() != null) {
                    Faction npcFaction = Faction.forNPCType(npcType);
                    if (template.getFaction() != npcFaction) continue;
                }
                suitable.add(template);
            }
        }

        if (suitable.isEmpty()) return null;

        // Zufällig auswählen
        QuestTemplate selected = suitable.get(ThreadLocalRandom.current().nextInt(suitable.size()));
        return generateQuest(selected.getId(), npc, player);
    }

    // ═══════════════════════════════════════════════════════════
    // PLAYER PROGRESS
    // ═══════════════════════════════════════════════════════════

    /**
     * Holt den Fortschritt eines Spielers
     */
    public QuestProgress getProgress(ServerPlayer player) {
        return playerProgress.computeIfAbsent(player.getUUID(), QuestProgress::new);
    }

    /**
     * Lässt einen Spieler eine Quest annehmen
     */
    public boolean acceptQuest(ServerPlayer player, Quest quest) {
        QuestProgress progress = getProgress(player);
        ServerLevel level = (ServerLevel) player.level();
        long currentDay = level.getDayTime() / 24000;

        boolean result = progress.acceptQuest(quest, currentDay);
        if (result) {
            markDirty();
        }
        return result;
    }

    /**
     * Schließt eine Quest ab und gibt Belohnung
     */
    public boolean completeQuest(ServerPlayer player, String questId) {
        QuestProgress progress = getProgress(player);
        Quest quest = progress.getActiveQuest(questId);

        if (quest == null || !quest.isReadyToComplete()) {
            return false;
        }

        // Quest abschließen
        if (!progress.completeQuest(questId)) {
            return false;
        }

        ServerLevel level = (ServerLevel) player.level();

        // Belohnung geben
        quest.getReward().grant(player, level);

        // NPC-Reaktion
        CustomNPCEntity questGiver = findNPC(level, quest.getQuestGiverNPC());
        if (questGiver != null) {
            NPCLifeData lifeData = questGiver.getLifeData();
            if (lifeData != null) {
                lifeData.getEmotions().trigger(EmotionState.HAPPY, 30.0f, 1200);
                lifeData.getMemory().addMemory(
                    player.getUUID(),
                    MemoryType.QUEST_COMPLETED,
                    "Quest abgeschlossen: " + quest.getTitle(),
                    6
                );
                lifeData.getMemory().addPlayerTag(player.getUUID(), "QuestErfüller");
            }
        }

        // Bei wiederholbarer Quest: Cooldown setzen
        if (quest.isRepeatable()) {
            long currentDay = level.getDayTime() / 24000;
            progress.setCooldown(questId, currentDay + NPCLifeConstants.Timing.QUEST_REPEAT_COOLDOWN_DAYS);
        }

        markDirty();
        return true;
    }

    /**
     * Aktualisiert alle Spieler-Fortschritte
     */
    public void tick(ServerLevel level) {
        long currentDay = level.getDayTime() / 24000;
        for (QuestProgress progress : playerProgress.values()) {
            progress.tick(currentDay);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // NPC QUEST OFFERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Aktualisiert die Quest-Angebote eines NPCs
     */
    public void refreshNPCQuests(CustomNPCEntity npc) {
        UUID npcUUID = npc.getNpcData().getNpcUUID();
        List<String> offers = new ArrayList<>();

        // Generiere 1-3 Quest-Angebote
        int numQuests = 1 + ThreadLocalRandom.current().nextInt(3);
        // TODO: Quest-Generierungslogik ist noch nicht implementiert.
        // Verwende stattdessen getQuestOffers(), die dynamisch via generateRandomQuest() arbeitet.
        LOGGER.debug("refreshNPCQuests called for NPC {} — generation not yet implemented (numQuests={})",
            npcUUID, numQuests);

        npcQuestOffers.put(npcUUID, offers);
        markDirty();
    }

    /**
     * Holt die Quest-Angebote eines NPCs
     */
    public List<Quest> getQuestOffers(CustomNPCEntity npc, ServerPlayer player) {
        List<Quest> quests = new ArrayList<>();

        // Generiere dynamisch eine Quest wenn keine vorhanden
        Quest randomQuest = generateRandomQuest(npc, player);
        if (randomQuest != null) {
            quests.add(randomQuest);
        }

        return quests;
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    /**
     * Findet einen NPC anhand seiner UUID
     */
    @Nullable
    private CustomNPCEntity findNPC(ServerLevel level, UUID npcUUID) {
        for (var entity : level.getAllEntities()) {
            if (entity instanceof CustomNPCEntity npc) {
                if (npc.getNpcData().getNpcUUID().equals(npcUUID)) {
                    return npc;
                }
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<QuestManagerData>(){}.getType();
    }

    @Override
    protected void onDataLoaded(QuestManagerData data) {
        playerProgress.clear();
        npcQuestOffers.clear();

        int invalidCount = 0;
        int correctedCount = 0;

        // VALIDATE QUEST ID COUNTER
        if (data.questIdCounter < 0) {
            LOGGER.warn("Invalid quest ID counter {}, resetting to 0", data.questIdCounter);
            questIdCounter = 0;
            correctedCount++;
        } else {
            questIdCounter = data.questIdCounter;
        }

        // Validate and load playerProgress
        if (data.playerProgress != null) {
            // Check collection size
            if (data.playerProgress.size() > 10000) {
                LOGGER.warn("Player progress map size ({}) exceeds limit, potential corruption",
                    data.playerProgress.size());
                correctedCount++;
            }

            for (Map.Entry<UUID, QuestProgress> entry : data.playerProgress.entrySet()) {
                try {
                    UUID playerUUID = entry.getKey();
                    QuestProgress progress = entry.getValue();

                    // NULL CHECK
                    if (progress == null) {
                        LOGGER.warn("Null QuestProgress for player {}, skipping", playerUUID);
                        invalidCount++;
                        continue;
                    }

                    boolean progressCorrected = false;

                    // VALIDATE ACTIVE QUESTS SIZE
                    if (progress.getActiveQuests().size() > QuestProgress.MAX_ACTIVE_QUESTS) {
                        LOGGER.warn("Player {} has too many active quests ({}), will be limited",
                            playerUUID, progress.getActiveQuests().size());
                        progressCorrected = true;
                    }

                    // VALIDATE ACTIVE QUESTS - check for null quests
                    for (Map.Entry<String, Quest> questEntry : new HashMap<>(progress.getActiveQuests()).entrySet()) {
                        if (questEntry.getValue() == null) {
                            LOGGER.warn("Player {} has null quest with ID {}, removing",
                                playerUUID, questEntry.getKey());
                            progress.getActiveQuests().remove(questEntry.getKey());
                            invalidCount++;
                            progressCorrected = true;
                        }
                    }

                    // VALIDATE COMPLETED QUESTS SIZE
                    if (progress.getCompletedQuestIds().size() > QuestProgress.MAX_COMPLETED_HISTORY * 2) {
                        LOGGER.warn("Player {} has too many completed quests ({}), potential corruption",
                            playerUUID, progress.getCompletedQuestIds().size());
                        progressCorrected = true;
                    }

                    // VALIDATE QUEST STATISTICS (>= 0)
                    if (progress.getTotalQuestsCompleted() < 0) {
                        LOGGER.warn("Player {} has negative completed quests count {}, resetting to 0",
                            playerUUID, progress.getTotalQuestsCompleted());
                        progressCorrected = true;
                    }
                    if (progress.getTotalQuestsFailed() < 0) {
                        LOGGER.warn("Player {} has negative failed quests count {}, resetting to 0",
                            playerUUID, progress.getTotalQuestsFailed());
                        progressCorrected = true;
                    }
                    if (progress.getTotalQuestsAbandoned() < 0) {
                        LOGGER.warn("Player {} has negative abandoned quests count {}, resetting to 0",
                            playerUUID, progress.getTotalQuestsAbandoned());
                        progressCorrected = true;
                    }

                    // VALIDATE COOLDOWNS - check for null keys/values and negative values
                    for (Map.Entry<String, Long> cooldownEntry : new HashMap<>(progress.getQuestCooldowns()).entrySet()) {
                        if (cooldownEntry.getKey() == null || cooldownEntry.getValue() == null) {
                            LOGGER.warn("Player {} has null cooldown entry, removing", playerUUID);
                            progress.getQuestCooldowns().remove(cooldownEntry.getKey());
                            invalidCount++;
                            progressCorrected = true;
                        } else if (cooldownEntry.getValue() < 0) {
                            LOGGER.warn("Player {} has negative cooldown {} for quest {}, resetting to 0",
                                playerUUID, cooldownEntry.getValue(), cooldownEntry.getKey());
                            progress.getQuestCooldowns().put(cooldownEntry.getKey(), 0L);
                            progressCorrected = true;
                        }
                    }

                    if (progressCorrected) {
                        correctedCount++;
                    }

                    playerProgress.put(playerUUID, progress);
                } catch (Exception e) {
                    LOGGER.error("Error loading quest progress for player {}", entry.getKey(), e);
                    invalidCount++;
                }
            }
        }

        // Validate and load npcQuestOffers
        if (data.npcQuestOffers != null) {
            // Check collection size
            if (data.npcQuestOffers.size() > 10000) {
                LOGGER.warn("NPC quest offers map size ({}) exceeds limit, potential corruption",
                    data.npcQuestOffers.size());
                correctedCount++;
            }

            for (Map.Entry<UUID, List<String>> entry : data.npcQuestOffers.entrySet()) {
                try {
                    UUID npcUUID = entry.getKey();
                    List<String> offers = entry.getValue();

                    // NULL CHECK
                    if (offers == null) {
                        LOGGER.warn("Null quest offers list for NPC {}, skipping", npcUUID);
                        invalidCount++;
                        continue;
                    }

                    // VALIDATE LIST SIZE
                    if (offers.size() > 100) {
                        LOGGER.warn("NPC {} has too many quest offers ({}), truncating to 100",
                            npcUUID, offers.size());
                        offers = new ArrayList<>(offers.subList(0, 100));
                        correctedCount++;
                    }

                    // VALIDATE QUEST IDS - check for null or empty strings
                    List<String> validOffers = new ArrayList<>();
                    for (String questId : offers) {
                        if (questId == null || questId.isEmpty()) {
                            LOGGER.warn("NPC {} has null/empty quest ID in offers, skipping", npcUUID);
                            invalidCount++;
                            continue;
                        }
                        if (questId.length() > 200) {
                            LOGGER.warn("NPC {} has too long quest ID ({}), skipping", npcUUID, questId.length());
                            invalidCount++;
                            continue;
                        }
                        validOffers.add(questId);
                    }

                    if (validOffers.size() != offers.size()) {
                        correctedCount++;
                    }

                    npcQuestOffers.put(npcUUID, validOffers);
                } catch (Exception e) {
                    LOGGER.error("Error loading NPC quest offers for {}", entry.getKey(), e);
                    invalidCount++;
                }
            }
        }

        // SUMMARY
        if (invalidCount > 0 || correctedCount > 0) {
            LOGGER.warn("Data validation: {} invalid entries, {} corrected entries",
                invalidCount, correctedCount);
            if (correctedCount > 0) {
                markDirty(); // Re-save corrected data
            }
        }
    }

    @Override
    protected QuestManagerData getCurrentData() {
        QuestManagerData data = new QuestManagerData();
        data.questIdCounter = questIdCounter;
        data.playerProgress = new HashMap<>(playerProgress);
        data.npcQuestOffers = new HashMap<>(npcQuestOffers);
        return data;
    }

    @Override
    protected String getComponentName() {
        return "QuestManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("%d players, %d templates",
            playerProgress.size(), questTemplates.size());
    }

    @Override
    protected void onCriticalLoadFailure() {
        playerProgress.clear();
        npcQuestOffers.clear();
        questIdCounter = 0;
    }

    // ═══════════════════════════════════════════════════════════
    // DATA CLASS FOR JSON SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public static class QuestManagerData {
        public int questIdCounter;
        public Map<UUID, QuestProgress> playerProgress;
        public Map<UUID, List<String>> npcQuestOffers;
    }

    // ═══════════════════════════════════════════════════════════
    // INNER CLASS: QUEST TEMPLATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Quest-Vorlage für dynamische Quest-Generierung
     */
    public static class QuestTemplate {
        private final String id;
        private final QuestType type;
        private final String title;
        private final String description;
        private final int difficulty;
        private final int timeLimit;
        private final Faction faction;
        private final int minFactionRep;
        private final boolean repeatable;
        private final QuestReward baseReward;

        private QuestTemplate(Builder builder) {
            this.id = builder.id;
            this.type = builder.type;
            this.title = builder.title;
            this.description = builder.description;
            this.difficulty = builder.difficulty;
            this.timeLimit = builder.timeLimit;
            this.faction = builder.faction;
            this.minFactionRep = builder.minFactionRep;
            this.repeatable = builder.repeatable;
            this.baseReward = builder.baseReward;
        }

        public static Builder builder(String id) {
            return new Builder(id);
        }

        // Getters
        public String getId() { return id; }
        public QuestType getType() { return type; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public int getDifficulty() { return difficulty; }
        public int getTimeLimit() { return timeLimit; }
        public Faction getFaction() { return faction; }
        public int getMinFactionRep() { return minFactionRep; }
        public boolean isRepeatable() { return repeatable; }
        public QuestReward getBaseReward() { return baseReward; }

        public static class Builder {
            private final String id;
            private QuestType type = QuestType.DELIVERY;
            private String title = "Quest";
            private String description = "";
            private int difficulty = 1;
            private int timeLimit = 0;
            private Faction faction = null;
            private int minFactionRep = 0;
            private boolean repeatable = true;
            private QuestReward baseReward = QuestReward.create();

            public Builder(String id) {
                this.id = id;
            }

            public Builder type(QuestType type) { this.type = type; return this; }
            public Builder title(String title) { this.title = title; return this; }
            public Builder description(String desc) { this.description = desc; return this; }
            public Builder difficulty(int diff) { this.difficulty = diff; return this; }
            public Builder timeLimit(int days) { this.timeLimit = days; return this; }
            public Builder faction(Faction faction) { this.faction = faction; return this; }
            public Builder minFactionRep(int rep) { this.minFactionRep = rep; return this; }
            public Builder repeatable(boolean rep) { this.repeatable = rep; return this; }
            public Builder baseReward(QuestReward reward) { this.baseReward = reward; return this; }

            public QuestTemplate build() {
                return new QuestTemplate(this);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("QuestManager{templates=%d, players=%d}",
            questTemplates.size(), playerProgress.size());
    }
}
