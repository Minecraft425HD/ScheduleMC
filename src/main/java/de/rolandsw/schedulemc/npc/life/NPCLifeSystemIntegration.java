package de.rolandsw.schedulemc.npc.life;

import de.rolandsw.schedulemc.npc.life.companion.CompanionManager;
import de.rolandsw.schedulemc.npc.life.dialogue.DialogueManager;
import de.rolandsw.schedulemc.npc.life.dialogue.NPCDialogueProvider;
import de.rolandsw.schedulemc.npc.life.economy.DynamicPriceManager;
import de.rolandsw.schedulemc.npc.life.quest.QuestManager;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import de.rolandsw.schedulemc.npc.life.social.NPCInteractionManager;
import de.rolandsw.schedulemc.npc.life.social.RumorNetwork;
import de.rolandsw.schedulemc.npc.life.witness.WitnessManager;
import de.rolandsw.schedulemc.npc.life.world.WorldEventManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

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
