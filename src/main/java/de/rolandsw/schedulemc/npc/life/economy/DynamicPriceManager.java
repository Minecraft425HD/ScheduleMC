package de.rolandsw.schedulemc.npc.life.economy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

/**
 * DynamicPriceManager - Verwaltet dynamische Preise und Marktbedingungen
 *
 * Features:
 * - Globale Marktbedingungen
 * - Kategorie-spezifische Bedingungen
 * - Zeitbasierte Schwankungen
 * - Event-basierte Änderungen
 */
public class DynamicPriceManager {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON-LIKE PER LEVEL
    // ═══════════════════════════════════════════════════════════

    private static final Map<ServerLevel, DynamicPriceManager> MANAGERS = new HashMap<>();

    public static DynamicPriceManager getManager(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level, l -> new DynamicPriceManager());
    }

    public static void removeManager(ServerLevel level) {
        MANAGERS.remove(level);
    }

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Wie oft wird der Markt aktualisiert (in Ticks) */
    public static final int UPDATE_INTERVAL = 24000; // Einmal pro Spieltag

    /** Basis-Chance für Marktveränderungen */
    public static final float MARKET_CHANGE_CHANCE = 0.3f;

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    /** Globale Marktbedingung */
    private MarketCondition globalCondition = MarketCondition.NORMAL;

    /** Kategorie-spezifische Bedingungen */
    private final Map<String, MarketCondition> categoryConditions = new HashMap<>();

    /** Zusätzliche temporäre Modifikatoren */
    private final Map<String, TemporaryModifier> temporaryModifiers = new HashMap<>();

    /** Preis-History für Analysen */
    private final Deque<PriceSnapshot> priceHistory = new ArrayDeque<>();
    private static final int MAX_HISTORY = 30; // 30 Tage

    /** Letzter bekannter Tag */
    private long lastKnownDay = -1;

    /** Tick-Counter */
    private int tickCounter = 0;

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
            return e.getValue().ticksRemaining <= 0;
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
        // Markt-Update durchführen
        updateMarketConditions();

        // Snapshot speichern
        savePriceSnapshot(currentDay);
    }

    /**
     * Aktualisiert die Marktbedingungen
     */
    private void updateMarketConditions() {
        // Globale Bedingung möglicherweise ändern
        if (Math.random() < MARKET_CHANGE_CHANCE) {
            MarketCondition[] possible = globalCondition.getPossibleTransitions();
            if (possible.length > 0) {
                // Gewichtete Auswahl
                float roll = (float) Math.random();
                float cumulative = 0;
                for (MarketCondition condition : possible) {
                    cumulative += globalCondition.getTransitionChance(condition);
                    if (roll < cumulative) {
                        globalCondition = condition;
                        break;
                    }
                }
            }
        }

        // Kategorie-Bedingungen ähnlich aktualisieren
        List<String> categoriesToUpdate = new ArrayList<>(categoryConditions.keySet());
        for (String category : categoriesToUpdate) {
            if (Math.random() < MARKET_CHANGE_CHANCE * 0.5) {
                MarketCondition current = categoryConditions.get(category);
                MarketCondition[] possible = current.getPossibleTransitions();
                if (possible.length > 0 && Math.random() < 0.5) {
                    categoryConditions.put(category, possible[(int) (Math.random() * possible.length)]);
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
    }

    /**
     * Entfernt die Kategorie-spezifische Bedingung (fällt auf global zurück)
     */
    public void resetCategoryCondition(String category) {
        categoryConditions.remove(category);
    }

    // ═══════════════════════════════════════════════════════════
    // TEMPORARY MODIFIERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt einen temporären Modifikator hinzu
     */
    public void addTemporaryModifier(String id, float modifier, int durationTicks, String reason) {
        temporaryModifiers.put(id, new TemporaryModifier(modifier, durationTicks, reason));
    }

    /**
     * Entfernt einen temporären Modifikator
     */
    public void removeTemporaryModifier(String id) {
        temporaryModifiers.remove(id);
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

        // Temporäre Modifikatoren
        modifier *= getCombinedTemporaryModifier();

        // Zufällige kleine Schwankung (±5%)
        modifier *= 0.95f + (float) Math.random() * 0.1f;

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
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putString("GlobalCondition", globalCondition.name());
        tag.putLong("LastKnownDay", lastKnownDay);

        // Kategorie-Bedingungen
        CompoundTag categoriesTag = new CompoundTag();
        for (Map.Entry<String, MarketCondition> entry : categoryConditions.entrySet()) {
            categoriesTag.putString(entry.getKey(), entry.getValue().name());
        }
        tag.put("CategoryConditions", categoriesTag);

        // Temporäre Modifikatoren
        ListTag modifiersList = new ListTag();
        for (Map.Entry<String, TemporaryModifier> entry : temporaryModifiers.entrySet()) {
            CompoundTag modTag = new CompoundTag();
            modTag.putString("Id", entry.getKey());
            modTag.putFloat("Modifier", entry.getValue().modifier);
            modTag.putInt("TicksRemaining", entry.getValue().ticksRemaining);
            modTag.putString("Reason", entry.getValue().reason);
            modifiersList.add(modTag);
        }
        tag.put("TemporaryModifiers", modifiersList);

        return tag;
    }

    public void load(CompoundTag tag) {
        globalCondition = MarketCondition.fromName(tag.getString("GlobalCondition"));
        lastKnownDay = tag.getLong("LastKnownDay");

        categoryConditions.clear();
        CompoundTag categoriesTag = tag.getCompound("CategoryConditions");
        for (String key : categoriesTag.getAllKeys()) {
            categoryConditions.put(key, MarketCondition.fromName(categoriesTag.getString(key)));
        }

        temporaryModifiers.clear();
        ListTag modifiersList = tag.getList("TemporaryModifiers", Tag.TAG_COMPOUND);
        for (int i = 0; i < modifiersList.size(); i++) {
            CompoundTag modTag = modifiersList.getCompound(i);
            temporaryModifiers.put(
                modTag.getString("Id"),
                new TemporaryModifier(
                    modTag.getFloat("Modifier"),
                    modTag.getInt("TicksRemaining"),
                    modTag.getString("Reason")
                )
            );
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════════════
    // INNER CLASSES
    // ═══════════════════════════════════════════════════════════

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
}
