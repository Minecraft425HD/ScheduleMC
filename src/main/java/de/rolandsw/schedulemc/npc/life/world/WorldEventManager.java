package de.rolandsw.schedulemc.npc.life.world;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.economy.DynamicPriceManager;
import de.rolandsw.schedulemc.npc.life.economy.MarketCondition;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WorldEventManager - Verwaltet alle Welt-Events mit JSON-Persistenz
 *
 * Verantwortlich für:
 * - Zufällige Event-Generierung
 * - Aktive Events verwalten
 * - Event-Effekte auf die Welt anwenden
 */
public class WorldEventManager extends AbstractPersistenceManager<WorldEventManager.WorldEventManagerData> {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private static volatile WorldEventManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    @Nullable
    public static WorldEventManager getInstance() {
        return instance;
    }

    /**
     * Gets manager instance for a specific level (convenience method).
     * Note: Manager is server-wide, not per-level.
     */
    public static WorldEventManager getManager(ServerLevel level) {
        return getInstance(level.getServer());
    }

    public static WorldEventManager getInstance(MinecraftServer server) {
        WorldEventManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new WorldEventManager(server);
                }
            }
        }
        return result;
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

    private MinecraftServer server;

    /** Aktive Events */
    private final List<WorldEvent> activeEvents = new ArrayList<>();

    /** Event-History: Event Type -> letzter Tag */
    private final Map<WorldEventType, Long> eventHistory = new ConcurrentHashMap<>();

    /** Tick-Zähler für Event-Checks (TRANSIENT - nicht persistiert) */
    private int tickCounter = 0;

    /** Letzter geprüfter Tag */
    private long lastCheckedDay = -1;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private WorldEventManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("npc_life_world_events.json").toFile(),
            GsonHelper.get()
        );
        this.server = server;
        load();
    }

    // ═══════════════════════════════════════════════════════════
    // MAIN TICK
    // ═══════════════════════════════════════════════════════════

    /**
     * Haupttick - prüft auf neue Events und aktualisiert aktive
     */
    public void tick(ServerLevel level) {
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
            tryGenerateEvent(level, currentDay);
            markDirty();
        }

        // Event-Effekte anwenden
        applyEventEffects(level, currentDay);
    }

    /**
     * Entfernt abgelaufene Events
     */
    private void removeExpiredEvents(long currentDay) {
        activeEvents.removeIf(event -> {
            if (event.isExpired(currentDay) || !event.isActive()) {
                onEventEnd(event);
                markDirty();
                return true;
            }
            return false;
        });
    }

    /**
     * Versucht ein neues Event zu generieren
     */
    private void tryGenerateEvent(ServerLevel level, long currentDay) {
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
        markDirty();

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
        markDirty();

        return event;
    }

    /**
     * Beendet ein Event vorzeitig
     */
    public void endEvent(UUID eventId) {
        for (WorldEvent event : activeEvents) {
            if (event.getEventId().equals(eventId)) {
                event.end();
                markDirty();
                return;
            }
        }
    }

    /**
     * Wird aufgerufen wenn ein Event startet
     */
    private void onEventStart(WorldEvent event) {
        // Market-Condition aktualisieren
        DynamicPriceManager priceManager = DynamicPriceManager.getInstance();
        if (priceManager != null) {
            MarketCondition condition = event.getType().getMarketEffect();
            priceManager.setMarketCondition(condition);
        }

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
            DynamicPriceManager priceManager = DynamicPriceManager.getInstance();
            if (priceManager != null) {
                priceManager.setMarketCondition(MarketCondition.NORMAL);
            }
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
    private void applyEventEffects(ServerLevel level, long currentDay) {
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
     * Berechnet den kombinierten Preismodifikator aller Events (convenience method)
     */
    public float getCombinedPriceModifier(BlockPos pos) {
        if (server == null) return 1.0f;
        long currentDay = server.overworld().getDayTime() / 24000L;
        return getCombinedPriceModifier(pos, currentDay);
    }

    /**
     * Berechnet den kombinierten Preismodifikator aller Events
     */
    public float getCombinedPriceModifier(BlockPos pos, long currentDay) {
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
    public float getCombinedMoodEffect(BlockPos pos, long currentDay) {
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
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<WorldEventManagerData>(){}.getType();
    }

    @Override
    protected void onDataLoaded(WorldEventManagerData data) {
        activeEvents.clear();
        eventHistory.clear();

        if (data.activeEvents != null) {
            activeEvents.addAll(data.activeEvents);
        }
        if (data.eventHistory != null) {
            eventHistory.putAll(data.eventHistory);
        }
        lastCheckedDay = data.lastCheckedDay;
    }

    @Override
    protected WorldEventManagerData getCurrentData() {
        WorldEventManagerData data = new WorldEventManagerData();
        data.activeEvents = new ArrayList<>(activeEvents);
        data.eventHistory = new HashMap<>(eventHistory);
        data.lastCheckedDay = lastCheckedDay;
        return data;
    }

    @Override
    protected String getComponentName() {
        return "WorldEventManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("%d active events", activeEvents.size());
    }

    @Override
    protected void onCriticalLoadFailure() {
        activeEvents.clear();
        eventHistory.clear();
        lastCheckedDay = -1;
    }

    // ═══════════════════════════════════════════════════════════
    // DATA CLASS FOR JSON SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public static class WorldEventManagerData {
        public List<WorldEvent> activeEvents;
        public Map<WorldEventType, Long> eventHistory;
        public long lastCheckedDay;
    }

    @Override
    public String toString() {
        return String.format("WorldEventManager{activeEvents=%d}", activeEvents.size());
    }
}
