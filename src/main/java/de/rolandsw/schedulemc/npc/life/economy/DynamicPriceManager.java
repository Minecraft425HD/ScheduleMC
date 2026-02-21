package de.rolandsw.schedulemc.npc.life.economy;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import de.rolandsw.schedulemc.market.SeasonalPriceModifier;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * DynamicPriceManager - Verwaltet dynamische Preise und Marktbedingungen mit JSON-Persistenz
 *
 * Features:
 * - Globale Marktbedingungen
 * - Kategorie-spezifische Bedingungen
 * - Zeitbasierte Schwankungen
 * - Event-basierte Änderungen
 */
public class DynamicPriceManager extends AbstractPersistenceManager<DynamicPriceManager.DynamicPriceManagerData> {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private static volatile DynamicPriceManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    @Nullable
    public static DynamicPriceManager getInstance() {
        return instance;
    }

    public static DynamicPriceManager getInstance(MinecraftServer server) {
        DynamicPriceManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new DynamicPriceManager(server);
                }
            }
        }
        return result;
    }

    /**
     * Gets manager instance for a specific level (convenience method).
     * Note: Manager is server-wide, not per-level.
     */
    public static DynamicPriceManager getManager(ServerLevel level) {
        return getInstance(level.getServer());
    }

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Wie oft wird der Markt aktualisiert (in Ticks) */
    public static final int UPDATE_INTERVAL = 24000; // Einmal pro Spieltag

    /** Basis-Chance für Marktveränderungen */
    public static final float MARKET_CHANGE_CHANCE = 0.3f;

    /** Untere Grenze der zufälligen Preisschwankung (ergibt ±5%-Bereich: 0.95–1.05) */
    static final float PRICE_VARIANCE_MIN = 0.95f;

    /** Bandbreite der zufälligen Preisschwankung (10%) */
    static final float PRICE_VARIANCE_RANGE = 0.1f;

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private MinecraftServer server;

    /** Globale Marktbedingung */
    private MarketCondition globalCondition = MarketCondition.NORMAL;

    /** Kategorie-spezifische Bedingungen */
    private final Map<String, MarketCondition> categoryConditions = new ConcurrentHashMap<>();

    /** Zusätzliche temporäre Modifikatoren */
    private final Map<String, TemporaryModifier> temporaryModifiers = new ConcurrentHashMap<>();

    /** Preis-History für Analysen */
    private final Deque<PriceSnapshot> priceHistory = new ArrayDeque<>();
    private static final int MAX_HISTORY = 30; // 30 Tage

    /** Letzter bekannter Tag */
    private long lastKnownDay = -1;

    /** Tick-Counter (TRANSIENT - nicht persistiert) */
    private int tickCounter = 0;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private DynamicPriceManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("npc_life_prices.json").toFile(),
            GsonHelper.get()
        );
        this.server = server;
        load();
    }

    // ═══════════════════════════════════════════════════════════
    // TICK / UPDATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird jeden Tick aufgerufen
     */
    public void tick(ServerLevel level) {
        tickCounter++;

        // Temporäre Modifikatoren aktualisieren
        temporaryModifiers.entrySet().removeIf(e -> {
            e.getValue().ticksRemaining--;
            if (e.getValue().ticksRemaining <= 0) {
                markDirty();
                return true;
            }
            return false;
        });

        // Tageswechsel prüfen
        long currentDay = level.getDayTime() / 24000;
        if (lastKnownDay != -1 && currentDay > lastKnownDay) {
            onDayChange(currentDay);
        }
        lastKnownDay = currentDay;
    }

    /**
     * Wird bei Tageswechsel aufgerufen
     */
    private void onDayChange(long currentDay) {
        // Saison aktualisieren
        SeasonalPriceModifier.getInstance().updateSeason(currentDay);

        // Markt-Update durchführen
        updateMarketConditions();

        // Snapshot speichern
        savePriceSnapshot(currentDay);
        markDirty();
    }

    /**
     * Aktualisiert die Marktbedingungen
     */
    private void updateMarketConditions() {
        // Globale Bedingung möglicherweise ändern
        if (ThreadLocalRandom.current().nextDouble() < MARKET_CHANGE_CHANCE) {
            MarketCondition[] possible = globalCondition.getPossibleTransitions();
            if (possible.length > 0) {
                // Gewichtete Auswahl
                float roll = (float) ThreadLocalRandom.current().nextDouble();
                float cumulative = 0;
                for (MarketCondition condition : possible) {
                    cumulative += globalCondition.getTransitionChance(condition);
                    if (roll < cumulative) {
                        globalCondition = condition;
                        markDirty();
                        break;
                    }
                }
            }
        }

        // Kategorie-Bedingungen ähnlich aktualisieren
        List<String> categoriesToUpdate = new ArrayList<>(categoryConditions.keySet());
        for (String category : categoriesToUpdate) {
            if (ThreadLocalRandom.current().nextDouble() < MARKET_CHANGE_CHANCE * 0.5) {
                MarketCondition current = categoryConditions.get(category);
                MarketCondition[] possible = current.getPossibleTransitions();
                if (possible.length > 0 && ThreadLocalRandom.current().nextDouble() < 0.5) {
                    categoryConditions.put(category, possible[ThreadLocalRandom.current().nextInt(possible.length)]);
                    markDirty();
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MARKET CONDITIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt die globale Marktbedingung zurück
     */
    public MarketCondition getGlobalMarketCondition() {
        return globalCondition;
    }

    /**
     * Setzt die globale Marktbedingung
     */
    public void setGlobalMarketCondition(MarketCondition condition) {
        this.globalCondition = condition;
        markDirty();
    }

    /**
     * Alias für setGlobalMarketCondition - setzt die Marktbedingung
     */
    public void setMarketCondition(MarketCondition condition) {
        setGlobalMarketCondition(condition);
    }

    /**
     * Gibt die Marktbedingung für eine Kategorie zurück
     */
    public MarketCondition getCategoryCondition(String category) {
        return categoryConditions.getOrDefault(category, globalCondition);
    }

    /**
     * Setzt die Marktbedingung für eine Kategorie
     */
    public void setCategoryCondition(String category, MarketCondition condition) {
        categoryConditions.put(category, condition);
        markDirty();
    }

    /**
     * Entfernt die Kategorie-spezifische Bedingung (fällt auf global zurück)
     */
    public void resetCategoryCondition(String category) {
        categoryConditions.remove(category);
        markDirty();
    }

    // ═══════════════════════════════════════════════════════════
    // TEMPORARY MODIFIERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt einen temporären Modifikator hinzu
     */
    public void addTemporaryModifier(String id, float modifier, int durationTicks, String reason) {
        temporaryModifiers.put(id, new TemporaryModifier(modifier, durationTicks, reason));
        markDirty();
    }

    /**
     * Entfernt einen temporären Modifikator
     */
    public void removeTemporaryModifier(String id) {
        temporaryModifiers.remove(id);
        markDirty();
    }

    /**
     * Berechnet den kombinierten temporären Modifikator
     */
    public float getCombinedTemporaryModifier() {
        if (temporaryModifiers.isEmpty()) return 1.0f;

        float combined = 1.0f;
        for (TemporaryModifier mod : temporaryModifiers.values()) {
            combined *= mod.modifier;
        }
        return combined;
    }

    // ═══════════════════════════════════════════════════════════
    // PRICE CALCULATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den finalen Preis für ein Item
     *
     * @param basePrice Basispreis des Items
     * @param category Kategorie des Items (z.B. "FOOD", "WEAPONS")
     * @return Angepasster Preis
     */
    public int calculatePrice(int basePrice, String category) {
        float modifier = 1.0f;

        // Kategorie-Bedingung oder global
        MarketCondition condition = getCategoryCondition(category);
        modifier *= condition.getPriceMultiplier();

        // Saisonaler Modifikator
        modifier *= SeasonalPriceModifier.getInstance().getModifier(category);

        // Temporäre Modifikatoren
        modifier *= getCombinedTemporaryModifier();

        // Zufällige kleine Schwankung (±5%)
        modifier *= PRICE_VARIANCE_MIN + (float) ThreadLocalRandom.current().nextDouble() * PRICE_VARIANCE_RANGE;

        return Math.max(1, Math.round(basePrice * modifier));
    }

    /**
     * Berechnet den Preis mit NPC-Modifikatoren
     */
    public int calculatePriceWithNPC(int basePrice, String category, float npcModifier) {
        int marketPrice = calculatePrice(basePrice, category);
        return Math.max(1, Math.round(marketPrice * npcModifier));
    }

    // ═══════════════════════════════════════════════════════════
    // EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird bei einem Marktkrise-Event aufgerufen
     */
    public void triggerMarketCrisis(String category, int durationDays) {
        if (category == null || category.isEmpty()) {
            globalCondition = MarketCondition.CRISIS;
        } else {
            categoryConditions.put(category, MarketCondition.CRISIS);
        }

        addTemporaryModifier(
            "crisis_" + (category != null ? category : "global"),
            1.5f,
            durationDays * 24000,
            "Marktkrise"
        );
    }

    /**
     * Wird bei einem Überfluss-Event aufgerufen
     */
    public void triggerSurplus(String category, int durationDays) {
        categoryConditions.put(category, MarketCondition.SURPLUS);

        addTemporaryModifier(
            "surplus_" + category,
            0.6f,
            durationDays * 24000,
            "Warenüberfluss: " + category
        );
    }

    /**
     * Wird bei einer Knappheit aufgerufen
     */
    public void triggerShortage(String category, int durationDays) {
        categoryConditions.put(category, MarketCondition.SHORTAGE);

        addTemporaryModifier(
            "shortage_" + category,
            1.8f,
            durationDays * 24000,
            "Warenknappheit: " + category
        );
    }

    // ═══════════════════════════════════════════════════════════
    // HISTORY
    // ═══════════════════════════════════════════════════════════

    private void savePriceSnapshot(long day) {
        if (priceHistory.size() >= MAX_HISTORY) {
            priceHistory.removeLast();
        }
        priceHistory.addFirst(new PriceSnapshot(day, globalCondition, getCombinedTemporaryModifier()));
    }

    public List<PriceSnapshot> getPriceHistory() {
        return new ArrayList<>(priceHistory);
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<DynamicPriceManagerData>(){}.getType();
    }

    @Override
    protected void onDataLoaded(DynamicPriceManagerData data) {
        int invalidCount = 0;
        int correctedCount = 0;

        // VALIDATE GLOBAL CONDITION
        if (data.globalCondition != null) {
            try {
                globalCondition = MarketCondition.valueOf(data.globalCondition);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid global market condition {}, resetting to NORMAL", data.globalCondition);
                globalCondition = MarketCondition.NORMAL;
                correctedCount++;
            }
        } else {
            globalCondition = MarketCondition.NORMAL;
        }

        // VALIDATE LAST KNOWN DAY
        if (data.lastKnownDay < 0) {
            LOGGER.warn("Invalid lastKnownDay {}, resetting to 0", data.lastKnownDay);
            lastKnownDay = 0;
            correctedCount++;
        } else {
            lastKnownDay = data.lastKnownDay;
        }

        // Validate and load categoryConditions
        categoryConditions.clear();
        if (data.categoryConditions != null) {
            // Check collection size
            if (data.categoryConditions.size() > 1000) {
                LOGGER.warn("Category conditions map size ({}) exceeds limit, potential corruption",
                    data.categoryConditions.size());
                correctedCount++;
            }

            for (Map.Entry<String, MarketCondition> entry : data.categoryConditions.entrySet()) {
                try {
                    String category = entry.getKey();
                    MarketCondition condition = entry.getValue();

                    // NULL CHECK
                    if (category == null || category.isEmpty()) {
                        LOGGER.warn("Null/empty category key, skipping");
                        invalidCount++;
                        continue;
                    }
                    if (condition == null) {
                        LOGGER.warn("Null market condition for category {}, skipping", category);
                        invalidCount++;
                        continue;
                    }

                    // VALIDATE CATEGORY LENGTH
                    if (category.length() > 100) {
                        LOGGER.warn("Category name too long ({} chars), skipping", category.length());
                        invalidCount++;
                        continue;
                    }

                    categoryConditions.put(category, condition);
                } catch (Exception e) {
                    LOGGER.error("Error loading category condition for {}", entry.getKey(), e);
                    invalidCount++;
                }
            }
        }

        // Validate and load temporaryModifiers
        temporaryModifiers.clear();
        if (data.temporaryModifiers != null) {
            // Check collection size
            if (data.temporaryModifiers.size() > 1000) {
                LOGGER.warn("Temporary modifiers map size ({}) exceeds limit, potential corruption",
                    data.temporaryModifiers.size());
                correctedCount++;
            }

            for (Map.Entry<String, TemporaryModifier> entry : data.temporaryModifiers.entrySet()) {
                try {
                    String key = entry.getKey();
                    TemporaryModifier modifier = entry.getValue();

                    // NULL CHECK
                    if (key == null || key.isEmpty()) {
                        LOGGER.warn("Null/empty modifier key, skipping");
                        invalidCount++;
                        continue;
                    }
                    if (modifier == null) {
                        LOGGER.warn("Null price modifier for key {}, skipping", key);
                        invalidCount++;
                        continue;
                    }

                    // VALIDATE KEY LENGTH
                    if (key.length() > 100) {
                        LOGGER.warn("Modifier key too long ({} chars), skipping", key.length());
                        invalidCount++;
                        continue;
                    }

                    temporaryModifiers.put(key, modifier);
                } catch (Exception e) {
                    LOGGER.error("Error loading temporary modifier for {}", entry.getKey(), e);
                    invalidCount++;
                }
            }
        }

        // Validate and load priceHistory
        priceHistory.clear();
        if (data.priceHistory != null) {
            // Check collection size
            if (data.priceHistory.size() > MAX_HISTORY * 2) {
                LOGGER.warn("Price history size ({}) exceeds limit, potential corruption",
                    data.priceHistory.size());
                correctedCount++;
            }

            for (PriceSnapshot snapshot : data.priceHistory) {
                try {
                    // NULL CHECK
                    if (snapshot == null) {
                        LOGGER.warn("Null price snapshot in history, skipping");
                        invalidCount++;
                        continue;
                    }

                    priceHistory.add(snapshot);
                } catch (Exception e) {
                    LOGGER.error("Error loading price snapshot", e);
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
    protected DynamicPriceManagerData getCurrentData() {
        DynamicPriceManagerData data = new DynamicPriceManagerData();
        data.globalCondition = globalCondition.name();
        data.lastKnownDay = lastKnownDay;
        data.categoryConditions = new HashMap<>(categoryConditions);
        data.temporaryModifiers = new HashMap<>(temporaryModifiers);
        data.priceHistory = new ArrayList<>(priceHistory);
        return data;
    }

    @Override
    protected String getComponentName() {
        return "DynamicPriceManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("%s market, %d categories, %d modifiers",
            globalCondition.name(), categoryConditions.size(), temporaryModifiers.size());
    }

    @Override
    protected void onCriticalLoadFailure() {
        globalCondition = MarketCondition.NORMAL;
        categoryConditions.clear();
        temporaryModifiers.clear();
        priceHistory.clear();
        lastKnownDay = -1;
    }

    // ═══════════════════════════════════════════════════════════
    // DATA CLASSES FOR JSON SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public static class DynamicPriceManagerData {
        public String globalCondition;
        public long lastKnownDay;
        public Map<String, MarketCondition> categoryConditions;
        public Map<String, TemporaryModifier> temporaryModifiers;
        public List<PriceSnapshot> priceHistory;
    }

    public static class TemporaryModifier {
        public final float modifier;
        public int ticksRemaining;
        public final String reason;

        public TemporaryModifier(float modifier, int ticksRemaining, String reason) {
            this.modifier = modifier;
            this.ticksRemaining = ticksRemaining;
            this.reason = reason;
        }
    }

    public static class PriceSnapshot {
        public final long day;
        public final MarketCondition condition;
        public final float modifier;

        public PriceSnapshot(long day, MarketCondition condition, float modifier) {
            this.day = day;
            this.condition = condition;
            this.modifier = modifier;
        }
    }

    @Override
    public String toString() {
        return String.format("DynamicPriceManager{global=%s, categories=%d, tempMods=%d}",
            globalCondition.name(), categoryConditions.size(), temporaryModifiers.size());
    }

    public String getMarketReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Marktbericht ===\n");
        sb.append(String.format("Globaler Markt: %s (×%.2f)\n",
            globalCondition.getDisplayName(), globalCondition.getPriceMultiplier()));

        if (!categoryConditions.isEmpty()) {
            sb.append("\nKategorien:\n");
            for (Map.Entry<String, MarketCondition> entry : categoryConditions.entrySet()) {
                sb.append(String.format("  %s: %s (×%.2f)\n",
                    entry.getKey(), entry.getValue().getDisplayName(),
                    entry.getValue().getPriceMultiplier()));
            }
        }

        if (!temporaryModifiers.isEmpty()) {
            sb.append("\nAktive Events:\n");
            for (Map.Entry<String, TemporaryModifier> entry : temporaryModifiers.entrySet()) {
                sb.append(String.format("  %s: ×%.2f (%s)\n",
                    entry.getValue().reason, entry.getValue().modifier,
                    formatTicksRemaining(entry.getValue().ticksRemaining)));
            }
        }

        return sb.toString();
    }

    private String formatTicksRemaining(int ticks) {
        int days = ticks / 24000;
        if (days > 0) return days + " Tage";
        int hours = ticks / 1000;
        return hours + " Stunden";
    }
}
