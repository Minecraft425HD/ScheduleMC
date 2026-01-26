package de.rolandsw.schedulemc.npc.life.world;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.economy.DynamicPriceManager;
import de.rolandsw.schedulemc.npc.life.economy.MarketCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.*;

/**
 * WorldEventManager - Verwaltet alle Welt-Events
 *
 * Verantwortlich für:
 * - Zufällige Event-Generierung
 * - Aktive Events verwalten
 * - Event-Effekte auf die Welt anwenden
 */
public class WorldEventManager {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON-LIKE PER LEVEL
    // ═══════════════════════════════════════════════════════════

    private static final Map<ServerLevel, WorldEventManager> MANAGERS = new HashMap<>();

    public static WorldEventManager getManager(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level, l -> new WorldEventManager(l));
    }

    public static void removeManager(ServerLevel level) {
        MANAGERS.remove(level);
    }

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Maximale gleichzeitige Events */
    private static final int MAX_ACTIVE_EVENTS = 3;

    /** Ticks zwischen Event-Checks */
    private static final int EVENT_CHECK_INTERVAL = 2400; // 2 Minuten

    /** Minimale Tage zwischen Events gleichen Typs */
    private static final int EVENT_COOLDOWN_DAYS = 5;

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final ServerLevel level;

    /** Aktive Events */
    private final List<WorldEvent> activeEvents = new ArrayList<>();

    /** Event-History: Event Type -> letzter Tag */
    private final Map<WorldEventType, Long> eventHistory = new HashMap<>();

    /** Tick-Zähler für Event-Checks */
    private int tickCounter = 0;

    /** Letzter geprüfter Tag */
    private long lastCheckedDay = -1;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private WorldEventManager(ServerLevel level) {
        this.level = level;
    }

    // ═══════════════════════════════════════════════════════════
    // MAIN TICK
    // ═══════════════════════════════════════════════════════════

    /**
     * Haupttick - prüft auf neue Events und aktualisiert aktive
     */
    public void tick() {
        tickCounter++;

        // Nur periodisch prüfen
        if (tickCounter < EVENT_CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;

        long currentDay = level.getDayTime() / 24000;

        // Abgelaufene Events entfernen
        removeExpiredEvents(currentDay);

        // Neue Events generieren (täglich prüfen)
        if (currentDay != lastCheckedDay) {
            lastCheckedDay = currentDay;
            tryGenerateEvent(currentDay);
        }

        // Event-Effekte anwenden
        applyEventEffects(currentDay);
    }

    /**
     * Entfernt abgelaufene Events
     */
    private void removeExpiredEvents(long currentDay) {
        activeEvents.removeIf(event -> {
            if (event.isExpired(currentDay) || !event.isActive()) {
                onEventEnd(event);
                return true;
            }
            return false;
        });
    }

    /**
     * Versucht ein neues Event zu generieren
     */
    private void tryGenerateEvent(long currentDay) {
        if (activeEvents.size() >= MAX_ACTIVE_EVENTS) {
            return;
        }

        // Zufälligen Event-Typ wählen
        List<WorldEventType> candidates = getEligibleEventTypes(currentDay);
        if (candidates.isEmpty()) {
            return;
        }

        // Gewichtete Auswahl basierend auf Wahrscheinlichkeit
        double totalProbability = candidates.stream()
            .mapToDouble(WorldEventType::getProbability)
            .sum();

        double random = Math.random() * totalProbability;
        double cumulative = 0;

        for (WorldEventType type : candidates) {
            cumulative += type.getProbability();
            if (random <= cumulative) {
                // Dieses Event starten
                startEvent(type, currentDay);
                return;
            }
        }
    }

    /**
     * Gibt Event-Typen zurück, die gestartet werden können
     */
    private List<WorldEventType> getEligibleEventTypes(long currentDay) {
        List<WorldEventType> eligible = new ArrayList<>();

        for (WorldEventType type : WorldEventType.values()) {
            // Cooldown prüfen
            Long lastOccurrence = eventHistory.get(type);
            if (lastOccurrence != null && currentDay - lastOccurrence < EVENT_COOLDOWN_DAYS) {
                continue;
            }

            // Koexistenz prüfen
            boolean canCoexist = true;
            for (WorldEvent active : activeEvents) {
                if (!type.canCoexistWith(active.getType())) {
                    canCoexist = false;
                    break;
                }
            }

            if (canCoexist) {
                eligible.add(type);
            }
        }

        return eligible;
    }

    // ═══════════════════════════════════════════════════════════
    // EVENT MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Startet ein Event
     */
    public WorldEvent startEvent(WorldEventType type, long currentDay) {
        WorldEvent event = WorldEvent.global(type, currentDay);
        activeEvents.add(event);
        eventHistory.put(type, currentDay);

        onEventStart(event);

        return event;
    }

    /**
     * Startet ein lokales Event
     */
    public WorldEvent startLocalEvent(WorldEventType type, long currentDay, BlockPos epicenter, int radius) {
        WorldEvent event = WorldEvent.local(type, currentDay, epicenter, radius);
        activeEvents.add(event);
        eventHistory.put(type, currentDay);

        onEventStart(event);

        return event;
    }

    /**
     * Beendet ein Event vorzeitig
     */
    public void endEvent(UUID eventId) {
        for (WorldEvent event : activeEvents) {
            if (event.getEventId().equals(eventId)) {
                event.end();
                return;
            }
        }
    }

    /**
     * Wird aufgerufen wenn ein Event startet
     */
    private void onEventStart(WorldEvent event) {
        // Market-Condition aktualisieren
        MarketCondition condition = event.getType().getMarketEffect();
        DynamicPriceManager.getManager(level).setMarketCondition(condition);

        // Event ankündigen
        if (!event.isAnnounced()) {
            announceEvent(event);
            event.setAnnounced(true);
        }
    }

    /**
     * Wird aufgerufen wenn ein Event endet
     */
    private void onEventEnd(WorldEvent event) {
        // Market-Condition zurücksetzen wenn keine anderen Events aktiv
        boolean hasOtherMarketEvents = activeEvents.stream()
            .filter(e -> e != event)
            .anyMatch(e -> e.getType().getCategory() == WorldEventType.EventCategory.MARKET);

        if (!hasOtherMarketEvents) {
            DynamicPriceManager.getManager(level).setMarketCondition(MarketCondition.NORMAL);
        }
    }

    /**
     * Kündigt ein Event an (z.B. via Chat)
     */
    private void announceEvent(WorldEvent event) {
        // Hier könnte eine Server-weite Nachricht gesendet werden
        // level.getServer().getPlayerList().broadcastSystemMessage(
        //     Component.literal("[Event] " + event.getType().getDisplayName() + ": " +
        //         event.getType().getDescription()),
        //     false
        // );
    }

    // ═══════════════════════════════════════════════════════════
    // EFFECT APPLICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Wendet Event-Effekte auf die Welt an
     */
    private void applyEventEffects(long currentDay) {
        // NPCs beeinflussen
        for (var entity : level.getAllEntities()) {
            if (entity instanceof CustomNPCEntity npc) {
                applyEffectsToNPC(npc, currentDay);
            }
        }
    }

    /**
     * Wendet Event-Effekte auf einen NPC an
     */
    private void applyEffectsToNPC(CustomNPCEntity npc, long currentDay) {
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return;

        BlockPos npcPos = npc.blockPosition();

        for (WorldEvent event : activeEvents) {
            if (!event.affectsPosition(npcPos)) continue;

            float intensity = event.getIntensityAt(npcPos);

            // Stimmung beeinflussen
            float moodEffect = event.getCurrentMoodEffect(currentDay) * intensity;
            if (moodEffect != 0) {
                EmotionState emotion = moodEffect > 0 ? EmotionState.HAPPY : EmotionState.FEARFUL;
                lifeData.getEmotions().trigger(emotion, Math.abs(moodEffect) * 0.5f);
            }

            // Sicherheitsgefühl beeinflussen
            float safetyEffect = event.getCurrentSafetyEffect(currentDay) * intensity;
            if (safetyEffect != 0) {
                lifeData.getNeeds().modifySafety((int) safetyEffect);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt alle aktiven Events zurück
     */
    public List<WorldEvent> getActiveEvents() {
        return new ArrayList<>(activeEvents);
    }

    /**
     * Gibt aktive Events eines bestimmten Typs zurück
     */
    public List<WorldEvent> getActiveEvents(WorldEventType type) {
        return activeEvents.stream()
            .filter(e -> e.getType() == type)
            .toList();
    }

    /**
     * Gibt aktive Events einer Kategorie zurück
     */
    public List<WorldEvent> getActiveEvents(WorldEventType.EventCategory category) {
        return activeEvents.stream()
            .filter(e -> e.getType().getCategory() == category)
            .toList();
    }

    /**
     * Prüft ob ein Event-Typ aktiv ist
     */
    public boolean isEventActive(WorldEventType type) {
        return activeEvents.stream().anyMatch(e -> e.getType() == type);
    }

    /**
     * Holt ein Event anhand ID
     */
    @Nullable
    public WorldEvent getEvent(UUID eventId) {
        return activeEvents.stream()
            .filter(e -> e.getEventId().equals(eventId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Berechnet den kombinierten Preismodifikator aller Events
     */
    public float getCombinedPriceModifier(BlockPos pos) {
        long currentDay = level.getDayTime() / 24000;
        float modifier = 1.0f;

        for (WorldEvent event : activeEvents) {
            if (event.affectsPosition(pos)) {
                float eventMod = event.getCurrentPriceModifier(currentDay);
                modifier *= eventMod;
            }
        }

        return modifier;
    }

    /**
     * Berechnet den kombinierten Stimmungseffekt
     */
    public float getCombinedMoodEffect(BlockPos pos) {
        long currentDay = level.getDayTime() / 24000;
        float effect = 0;

        for (WorldEvent event : activeEvents) {
            if (event.affectsPosition(pos)) {
                effect += event.getCurrentMoodEffect(currentDay) * event.getIntensityAt(pos);
            }
        }

        return effect;
    }

    /**
     * Prüft ob aktuell eine Krise herrscht
     */
    public boolean isCrisisActive() {
        return activeEvents.stream()
            .anyMatch(e -> e.getType().getCategory() == WorldEventType.EventCategory.CRISIS);
    }

    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // Aktive Events
        ListTag eventsTag = new ListTag();
        for (WorldEvent event : activeEvents) {
            eventsTag.add(event.save());
        }
        tag.put("activeEvents", eventsTag);

        // Event-History
        CompoundTag historyTag = new CompoundTag();
        for (Map.Entry<WorldEventType, Long> entry : eventHistory.entrySet()) {
            historyTag.putLong(entry.getKey().name(), entry.getValue());
        }
        tag.put("eventHistory", historyTag);

        tag.putLong("lastCheckedDay", lastCheckedDay);

        return tag;
    }

    public void load(CompoundTag tag) {
        activeEvents.clear();
        eventHistory.clear();

        // Aktive Events
        ListTag eventsTag = tag.getList("activeEvents", Tag.TAG_COMPOUND);
        for (int i = 0; i < eventsTag.size(); i++) {
            WorldEvent event = WorldEvent.load(eventsTag.getCompound(i));
            activeEvents.add(event);
        }

        // Event-History
        CompoundTag historyTag = tag.getCompound("eventHistory");
        for (String key : historyTag.getAllKeys()) {
            WorldEventType type = WorldEventType.valueOf(key);
            long day = historyTag.getLong(key);
            eventHistory.put(type, day);
        }

        lastCheckedDay = tag.getLong("lastCheckedDay");
    }

    @Override
    public String toString() {
        return String.format("WorldEventManager{activeEvents=%d}", activeEvents.size());
    }
}
