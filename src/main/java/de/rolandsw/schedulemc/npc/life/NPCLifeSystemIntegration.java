package de.rolandsw.schedulemc.npc.life;

import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.companion.CompanionData;
import de.rolandsw.schedulemc.npc.life.companion.CompanionManager;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.dialogue.DialogueManager;
import de.rolandsw.schedulemc.npc.life.dialogue.NPCDialogueProvider;
import de.rolandsw.schedulemc.npc.life.economy.DynamicPriceManager;
import de.rolandsw.schedulemc.npc.life.quest.Quest;
import de.rolandsw.schedulemc.npc.life.quest.QuestManager;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import de.rolandsw.schedulemc.npc.life.social.NPCInteractionManager;
import de.rolandsw.schedulemc.npc.life.social.Rumor;
import de.rolandsw.schedulemc.npc.life.social.RumorNetwork;
import de.rolandsw.schedulemc.npc.life.witness.CrimeType;
import de.rolandsw.schedulemc.npc.life.witness.WitnessManager;
import de.rolandsw.schedulemc.npc.life.world.WorldEventManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

import java.util.HashMap;
import java.util.Map;

/**
 * NPCLifeSystemIntegration - Zentraler Integrationspunkt für das NPC Life System
 *
 * Diese Klasse:
 * - Initialisiert alle Subsysteme
 * - Koordiniert deren Zusammenarbeit
 * - Bietet eine zentrale API für externe Zugriffe
 * - Verwaltet Persistenz aller Systeme
 */
public class NPCLifeSystemIntegration {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON-LIKE PER LEVEL
    // ═══════════════════════════════════════════════════════════

    private static final Map<ServerLevel, NPCLifeSystemIntegration> INTEGRATIONS = new HashMap<>();

    public static NPCLifeSystemIntegration get(ServerLevel level) {
        return INTEGRATIONS.computeIfAbsent(level, NPCLifeSystemIntegration::new);
    }

    public static void remove(ServerLevel level) {
        INTEGRATIONS.remove(level);

        // Alle Manager entfernen
        FactionManager.removeManager(level);
        RumorNetwork.removeNetwork(level);
        NPCInteractionManager.removeManager(level);
        WitnessManager.removeManager(level);
        DynamicPriceManager.removeManager(level);
        DialogueManager.removeManager(level);
        QuestManager.removeManager(level);
        CompanionManager.removeManager(level);
        WorldEventManager.removeManager(level);
    }

    // ═══════════════════════════════════════════════════════════
    // SUBSYSTEM REFERENCES
    // ═══════════════════════════════════════════════════════════

    private final ServerLevel level;

    private final FactionManager factionManager;
    private final RumorNetwork rumorNetwork;
    private final NPCInteractionManager interactionManager;
    private final WitnessManager witnessManager;
    private final DynamicPriceManager priceManager;
    private final DialogueManager dialogueManager;
    private final QuestManager questManager;
    private final CompanionManager companionManager;
    private final WorldEventManager worldEventManager;

    /** System aktiviert? */
    private boolean enabled = true;

    /** Tick-Zähler für periodische Updates */
    private int tickCounter = 0;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private NPCLifeSystemIntegration(ServerLevel level) {
        this.level = level;

        // Alle Manager initialisieren
        this.factionManager = FactionManager.getManager(level);
        this.rumorNetwork = RumorNetwork.getNetwork(level);
        this.interactionManager = NPCInteractionManager.getManager(level);
        this.witnessManager = WitnessManager.getManager(level);
        this.priceManager = DynamicPriceManager.getManager(level);
        this.dialogueManager = DialogueManager.getManager(level);
        this.questManager = QuestManager.getManager(level);
        this.companionManager = CompanionManager.getManager(level);
        this.worldEventManager = WorldEventManager.getManager(level);

        // Dialoge registrieren
        NPCDialogueProvider.setupForLevel(level);
    }

    // ═══════════════════════════════════════════════════════════
    // MAIN TICK
    // ═══════════════════════════════════════════════════════════

    /**
     * Haupt-Tick - wird jeden Gametick aufgerufen
     */
    public void tick() {
        if (!enabled) return;

        tickCounter++;

        // Jeden Tick
        interactionManager.tick();

        // Alle 20 Ticks (1 Sekunde)
        if (tickCounter % 20 == 0) {
            rumorNetwork.tick();
            witnessManager.tick();
            priceManager.tick();
            companionManager.tick();
        }

        // Alle 100 Ticks (5 Sekunden)
        if (tickCounter % 100 == 0) {
            questManager.tick();
        }

        // Alle 2400 Ticks (2 Minuten)
        if (tickCounter % 2400 == 0) {
            worldEventManager.tick();
        }

        // Counter zurücksetzen
        if (tickCounter >= 24000) {
            tickCounter = 0;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SUBSYSTEM ACCESS
    // ═══════════════════════════════════════════════════════════

    public FactionManager getFactionManager() { return factionManager; }
    public RumorNetwork getRumorNetwork() { return rumorNetwork; }
    public NPCInteractionManager getInteractionManager() { return interactionManager; }
    public WitnessManager getWitnessManager() { return witnessManager; }
    public DynamicPriceManager getPriceManager() { return priceManager; }
    public DialogueManager getDialogueManager() { return dialogueManager; }
    public QuestManager getQuestManager() { return questManager; }
    public CompanionManager getCompanionManager() { return companionManager; }
    public WorldEventManager getWorldEventManager() { return worldEventManager; }

    // ═══════════════════════════════════════════════════════════
    // CONFIGURATION
    // ═══════════════════════════════════════════════════════════

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ServerLevel getLevel() { return level; }

    // ═══════════════════════════════════════════════════════════
    // CROSS-SYSTEM EVENT COORDINATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Koordiniert Event: Spieler rekrutiert einen Begleiter
     * - Aktualisiert Fraktions-Reputation
     * - Verbreitet Gerüchte über die Rekrutierung
     * - Aktualisiert NPC-Gedächtnis
     */
    public void onCompanionRecruited(ServerPlayer player, CompanionData companion, CustomNPCEntity sourceNPC) {
        UUID playerUUID = player.getUUID();
        Faction npcFaction = Faction.forNPCType(sourceNPC.getNpcType());

        // Reputation erhöhen (Vertrauen gezeigt)
        factionManager.modifyReputation(playerUUID, npcFaction, 5);

        // Gerücht über Rekrutierung verbreiten
        Rumor rumor = Rumor.createNPC(
            sourceNPC.getNpcData().getNpcUUID(),
            "hat einen neuen Begleiter rekrutiert: " + companion.getName(),
            3, // importance
            5  // duration days
        );
        rumorNetwork.spreadRumor(rumor, sourceNPC.blockPosition());

        // NPC-Gedächtnis aktualisieren
        NPCLifeData lifeData = sourceNPC.getLifeData();
        if (lifeData != null) {
            lifeData.getMemory().addMemory(
                playerUUID,
                MemoryType.HELPED, // Rekrutierung = Vertrauen gezeigt
                "Begleiter rekrutiert: " + companion.getName(),
                5
            );
            lifeData.getMemory().addPlayerTag(playerUUID, "Begleiter-Besitzer");
            lifeData.getEmotions().trigger(EmotionState.HAPPY, 25.0f, 600);
        }
    }

    /**
     * Koordiniert Event: Begleiter wird entlassen
     * - Kann Reputation beeinflussen
     * - Verbreitet Gerüchte
     */
    public void onCompanionReleased(ServerPlayer player, CompanionData companion) {
        UUID playerUUID = player.getUUID();

        // Bei niedriger Loyalität: negative Reputation
        if (companion.getLoyalty() < 30) {
            factionManager.modifyReputation(playerUUID, Faction.BUERGER, -3);

            Rumor rumor = Rumor.createPlayer(
                playerUUID,
                "hat seinen unglücklichen Begleiter entlassen",
                2,
                3
            );
            rumorNetwork.spreadRumor(rumor, player.blockPosition());
        }
    }

    /**
     * Koordiniert Event: Quest abgeschlossen
     * - Reputation bereits durch QuestReward behandelt
     * - Verbreitet positive Gerüchte
     * - Aktualisiert NPC-Gedächtnis
     */
    public void onQuestCompleted(ServerPlayer player, Quest quest, CustomNPCEntity questGiver) {
        UUID playerUUID = player.getUUID();

        // Positive Gerüchte über den Spieler
        Rumor rumor = Rumor.createPlayer(
            playerUUID,
            "hat die Aufgabe '" + quest.getTitle() + "' erfolgreich abgeschlossen",
            4, // importance
            7  // duration days
        );
        rumorNetwork.spreadRumor(rumor, questGiver.blockPosition());

        // Bei schwierigen Quests: Extra Reputation
        if (quest.getDifficulty() >= 3) {
            Faction questFaction = quest.getFaction();
            if (questFaction != null) {
                factionManager.modifyReputation(playerUUID, questFaction, 3);
            }
        }
    }

    /**
     * Koordiniert Event: Quest fehlgeschlagen
     * - Reputation verringern
     * - Negative Gerüchte verbreiten
     */
    public void onQuestFailed(ServerPlayer player, Quest quest, CustomNPCEntity questGiver) {
        UUID playerUUID = player.getUUID();
        Faction questFaction = quest.getFaction();

        // Reputation verringern
        if (questFaction != null) {
            factionManager.modifyReputation(playerUUID, questFaction, -5);
        }

        // Negative Gerüchte
        Rumor rumor = Rumor.createPlayer(
            playerUUID,
            "hat bei der Aufgabe '" + quest.getTitle() + "' versagt",
            3,
            5
        );
        rumorNetwork.spreadRumor(rumor, questGiver.blockPosition());

        // NPC-Reaktion
        NPCLifeData lifeData = questGiver.getLifeData();
        if (lifeData != null) {
            lifeData.getEmotions().trigger(EmotionState.SAD, 20.0f, 600);
            lifeData.getMemory().addMemory(
                playerUUID,
                MemoryType.CONVERSATION,
                "Quest fehlgeschlagen: " + quest.getTitle(),
                4
            );
            lifeData.getMemory().addPlayerTag(playerUUID, "Unzuverlässig");
        }
    }

    /**
     * Koordiniert Event: Handel abgeschlossen
     * - Aktualisiert Preismanager mit Handelsdaten
     * - Bei großen Käufen: Gerüchte und Reputation
     */
    public void onTradeCompleted(ServerPlayer player, CustomNPCEntity merchant, int totalAmount) {
        UUID playerUUID = player.getUUID();
        Faction merchantFaction = Faction.forNPCType(merchant.getNpcType());

        // Handelsstatistik für dynamische Preise aktualisieren
        // (wird bereits im PurchaseItemPacket gemacht)

        // Bei großen Käufen (> 500): Gerüchte und Reputation
        if (totalAmount > 500) {
            factionManager.modifyReputation(playerUUID, merchantFaction, 1);

            if (totalAmount > 2000) {
                Rumor rumor = Rumor.createPlayer(
                    playerUUID,
                    "ist ein großzügiger Kunde mit tiefen Taschen",
                    2,
                    3
                );
                rumorNetwork.spreadRumor(rumor, merchant.blockPosition());
            }
        }
    }

    /**
     * Koordiniert Event: Verbrechen beobachtet
     * - WitnessManager bereits informiert durch reactToCrime
     * - Verbreitet Gerüchte über den Täter
     * - Aktualisiert Fraktions-Reputation
     */
    public void onCrimeWitnessed(ServerPlayer criminal, CrimeType crimeType, CustomNPCEntity witness) {
        UUID criminalUUID = criminal.getUUID();

        // Reputation bei relevanten Fraktionen verringern
        int reputationPenalty = crimeType.getSeverity() * 3;
        factionManager.modifyReputation(criminalUUID, Faction.ORDNUNG, -reputationPenalty);
        factionManager.modifyReputation(criminalUUID, Faction.BUERGER, -reputationPenalty / 2);

        // Gerücht über das Verbrechen verbreiten
        Rumor rumor = Rumor.createPlayer(
            criminalUUID,
            "wurde bei " + getCrimeDescription(crimeType) + " beobachtet",
            crimeType.getSeverity() + 2,
            crimeType.getSeverity() * 3
        );
        rumorNetwork.spreadRumor(rumor, witness.blockPosition());

        // Bei schwerem Verbrechen: Auch Untergrund informieren
        if (crimeType.getSeverity() >= 4) {
            factionManager.modifyReputation(criminalUUID, Faction.UNTERGRUND, 2);
        }
    }

    /**
     * Gibt eine lesbare Beschreibung für einen Verbrechenstyp zurück
     */
    private String getCrimeDescription(CrimeType crimeType) {
        return switch (crimeType) {
            case PETTY_THEFT, SHOPLIFTING -> "einem Diebstahl";
            case ASSAULT, AGGRAVATED_ASSAULT -> "einem Angriff";
            case ARMED_VIOLENCE -> "bewaffneter Gewalt";
            case BURGLARY -> "einem Einbruch";
            case ROBBERY -> "einem Raub";
            case VANDALISM -> "Vandalismus";
            case TRESPASSING -> "Hausfriedensbruch";
            case DRUG_USE, DRUG_DEALING_SMALL, DRUG_DEALING_LARGE -> "Drogenhandel";
            case BRIBERY -> "Bestechung";
            case FRAUD -> "Betrug";
            case THREAT -> "einer Bedrohung";
            case EVADING_POLICE -> "Flucht vor der Polizei";
        };
    }

    /**
     * Koordiniert Event: Bestechung angeboten
     * - Aktualisiert Zeugensystem
     * - Verbreitet ggf. Gerüchte
     */
    public void onBribeOffered(ServerPlayer player, CustomNPCEntity witness, boolean accepted) {
        UUID playerUUID = player.getUUID();

        NPCLifeData lifeData = witness.getLifeData();
        if (lifeData != null) {
            if (accepted) {
                // Bestechung angenommen: Zeuge schweigt, aber negative Reputation
                factionManager.modifyReputation(playerUUID, Faction.ORDNUNG, -2);

                // Zeuge erinnert sich an die Bestechung
                lifeData.getMemory().addMemory(
                    playerUUID,
                    MemoryType.BRIBE_OFFERED,
                    "Hat mich bestochen",
                    8
                );
                lifeData.getMemory().addPlayerTag(playerUUID, "Bestechlich");
            } else {
                // Bestechung abgelehnt: Gerüchte verbreiten
                Rumor rumor = Rumor.createPlayer(
                    playerUUID,
                    "hat versucht Zeugen zu bestechen",
                    5,
                    7
                );
                rumorNetwork.spreadRumor(rumor, witness.blockPosition());

                // Schwere Reputation-Strafe
                factionManager.modifyReputation(playerUUID, Faction.ORDNUNG, -10);
                factionManager.modifyReputation(playerUUID, Faction.BUERGER, -5);

                // Zeuge ist empört
                lifeData.getEmotions().trigger(EmotionState.ANGRY, 40.0f, 1200);
            }
        }
    }

    /**
     * Koordiniert Event: NPC stirbt
     * - Entfernt aus allen relevanten Systemen
     * - Verbreitet Gerüchte über den Tod
     */
    public void onNPCDeath(CustomNPCEntity npc, ServerPlayer killer) {
        UUID npcUUID = npc.getNpcData().getNpcUUID();

        // Gerücht über den Tod verbreiten
        String npcName = npc.getNpcData().getName();
        Rumor rumor = Rumor.createNPC(
            npcUUID,
            npcName + " wurde getötet",
            5,
            10
        );
        rumorNetwork.spreadRumor(rumor, npc.blockPosition());

        // Killer erhält schwere Reputation-Strafen
        if (killer != null) {
            factionManager.modifyReputation(killer.getUUID(), Faction.ORDNUNG, -20);
            factionManager.modifyReputation(killer.getUUID(), Faction.BUERGER, -15);

            // Bei Händler auch bei Händlerfraktion
            if (npc.getNpcType() == NPCType.VERKAEUFER) {
                factionManager.modifyReputation(killer.getUUID(), Faction.HAENDLER, -25);
            }
        }

        // Aus Companion-System entfernen falls Begleiter
        companionManager.releaseCompanion(npcUUID, npcUUID); // Dies ist ein Fallback

        // Aktive Quests dieses NPCs fehlschlagen lassen
        // (würde normalerweise im QuestManager behandelt)
    }

    /**
     * Koordiniert Event: Welt-Event startet
     * - Informiert alle relevanten Manager
     * - Passt Preise an
     */
    public void onWorldEventStarted(de.rolandsw.schedulemc.npc.life.world.WorldEventType eventType) {
        // Preise werden bereits im WorldEventManager angepasst

        // Gerücht über das Event verbreiten
        Rumor rumor = Rumor.createWorld(
            eventType.getDisplayName() + ": " + eventType.getDescription(),
            eventType.isNegative() ? 5 : 3,
            eventType.getMaxDuration()
        );
        rumorNetwork.broadcastRumor(rumor);
    }

    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Speichert alle Systeme
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putBoolean("enabled", enabled);
        tag.putInt("tickCounter", tickCounter);

        // Jedes Subsystem speichern
        tag.put("factionManager", factionManager.save());
        tag.put("rumorNetwork", rumorNetwork.save());
        tag.put("witnessManager", witnessManager.save());
        tag.put("priceManager", priceManager.save());
        tag.put("companionManager", companionManager.save());
        tag.put("worldEventManager", worldEventManager.save());
        tag.put("questManager", questManager.save());

        return tag;
    }

    /**
     * Lädt alle Systeme
     */
    public void load(CompoundTag tag) {
        enabled = tag.getBoolean("enabled");
        tickCounter = tag.getInt("tickCounter");

        // Jedes Subsystem laden
        if (tag.contains("factionManager")) {
            factionManager.load(tag.getCompound("factionManager"));
        }
        if (tag.contains("rumorNetwork")) {
            rumorNetwork.load(tag.getCompound("rumorNetwork"));
        }
        if (tag.contains("witnessManager")) {
            witnessManager.load(tag.getCompound("witnessManager"));
        }
        if (tag.contains("priceManager")) {
            priceManager.load(tag.getCompound("priceManager"));
        }
        if (tag.contains("companionManager")) {
            companionManager.load(tag.getCompound("companionManager"));
        }
        if (tag.contains("worldEventManager")) {
            worldEventManager.load(tag.getCompound("worldEventManager"));
        }
        if (tag.contains("questManager")) {
            questManager.load(tag.getCompound("questManager"));
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt Debug-Informationen zurück
     */
    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== NPC Life System ===\n");
        sb.append("Enabled: ").append(enabled).append("\n");
        sb.append("Tick: ").append(tickCounter).append("\n\n");

        sb.append("Subsystems:\n");
        sb.append("- ").append(factionManager).append("\n");
        sb.append("- ").append(rumorNetwork).append("\n");
        sb.append("- ").append(interactionManager).append("\n");
        sb.append("- ").append(witnessManager).append("\n");
        sb.append("- ").append(priceManager).append("\n");
        sb.append("- ").append(dialogueManager).append("\n");
        sb.append("- ").append(questManager).append("\n");
        sb.append("- ").append(companionManager).append("\n");
        sb.append("- ").append(worldEventManager).append("\n");

        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("NPCLifeSystemIntegration{level=%s, enabled=%s}",
            level.dimension().location(), enabled);
    }
}
