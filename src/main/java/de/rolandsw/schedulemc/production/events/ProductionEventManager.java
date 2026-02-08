package de.rolandsw.schedulemc.production.events;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.market.SeasonalPriceModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Produktions-Event-System: Zufaellige Ereignisse die Produktion beeinflussen.
 *
 * Event-Typen:
 * - POLICE_RAID: Polizeirazzia, illegale Produktion pausiert 10min
 * - BUMPER_HARVEST: Rekordernte, Ertrag +50% fuer 1 Tag
 * - CHEMICAL_SPILL: Chemieunfall, Qualitaet -1 Stufe fuer 1 Tag
 * - SUPPLY_SHORTAGE: Rohstoffknappheit, Produktionszeit +30%
 * - DEMAND_SURGE: Nachfrageboom, Verkaufspreis +40%
 * - EQUIPMENT_FAILURE: Geraeteausfall, eine zufaellige Produktion pausiert
 * - PERFECT_CONDITIONS: Perfekte Bedingungen, Qualitaet +1 Stufe
 * - BLACK_MARKET_OPENING: Schwarzmarkt oeffnet, illegale Preise +60%
 *
 * Events werden taeglich mit 15% Chance ausgeloest.
 * Maximal 3 gleichzeitige Events.
 */
public class ProductionEventManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile ProductionEventManager instance;

    private static final double EVENT_CHANCE = 0.15; // 15% pro Tag
    private static final int MAX_ACTIVE_EVENTS = 3;

    // Aktive Events
    private final Map<String, ProductionEvent> activeEvents = new ConcurrentHashMap<>();

    // Event-Definitionen
    private final List<ProductionEventTemplate> eventTemplates = new ArrayList<>();

    public enum EventSeverity {
        POSITIVE("\u00A7a", "+"),
        NEUTRAL("\u00A7e", "~"),
        NEGATIVE("\u00A7c", "!");

        private final String color;
        private final String symbol;

        EventSeverity(String color, String symbol) {
            this.color = color;
            this.symbol = symbol;
        }

        public String getColor() { return color; }
        public String getSymbol() { return symbol; }
    }

    public enum EventCategory {
        LEGAL("Legale Produktion"),
        ILLEGAL("Illegale Produktion"),
        ALL("Alle Produktionen"),
        MARKET("Markt");

        private final String displayName;

        EventCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    // ═══════════════════════════════════════════════════════════
    // DATA CLASSES
    // ═══════════════════════════════════════════════════════════

    public static class ProductionEventTemplate {
        private final String id;
        private final String name;
        private final String description;
        private final EventSeverity severity;
        private final EventCategory category;
        private final int minDuration; // in Ticks
        private final int maxDuration;
        private final float yieldModifier;    // Multiplikator fuer Ertrag
        private final float speedModifier;    // Multiplikator fuer Geschwindigkeit
        private final float priceModifier;    // Multiplikator fuer Preise
        private final int qualityChange;      // +/- Qualitaetsstufen
        private final float seasonalWeight;   // Extra-Chance in bestimmter Saison (1.0 = normal)

        public ProductionEventTemplate(String id, String name, String description,
                EventSeverity severity, EventCategory category,
                int minDuration, int maxDuration,
                float yieldModifier, float speedModifier, float priceModifier,
                int qualityChange, float seasonalWeight) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.severity = severity;
            this.category = category;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
            this.yieldModifier = yieldModifier;
            this.speedModifier = speedModifier;
            this.priceModifier = priceModifier;
            this.qualityChange = qualityChange;
            this.seasonalWeight = seasonalWeight;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public EventSeverity getSeverity() { return severity; }
    }

    public static class ProductionEvent {
        private final ProductionEventTemplate template;
        private final long startTick;
        private final int duration;

        public ProductionEvent(ProductionEventTemplate template, long startTick, int duration) {
            this.template = template;
            this.startTick = startTick;
            this.duration = duration;
        }

        public ProductionEventTemplate getTemplate() { return template; }
        public long getStartTick() { return startTick; }
        public int getDuration() { return duration; }
        public boolean isExpired(long currentTick) { return currentTick - startTick >= duration; }

        public float getYieldModifier() { return template.yieldModifier; }
        public float getSpeedModifier() { return template.speedModifier; }
        public float getPriceModifier() { return template.priceModifier; }
        public int getQualityChange() { return template.qualityChange; }
    }

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private ProductionEventManager() {
        registerDefaultEvents();
    }

    public static ProductionEventManager getInstance() {
        if (instance == null) {
            synchronized (ProductionEventManager.class) {
                if (instance == null) {
                    instance = new ProductionEventManager();
                }
            }
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // DEFAULT EVENTS
    // ═══════════════════════════════════════════════════════════

    private void registerDefaultEvents() {
        // Positive Events
        eventTemplates.add(new ProductionEventTemplate(
            "bumper_harvest", "Rekordernte",
            "Perfekte Wachstumsbedingungen erhoehen den Ertrag aller Pflanzen!",
            EventSeverity.POSITIVE, EventCategory.LEGAL,
            24000, 48000,     // 1-2 Tage
            1.5f, 1.0f, 1.0f, 0, 1.0f
        ));

        eventTemplates.add(new ProductionEventTemplate(
            "perfect_conditions", "Perfekte Bedingungen",
            "Ideale Temperatur und Feuchtigkeit - Qualitaet steigt!",
            EventSeverity.POSITIVE, EventCategory.ALL,
            24000, 36000,
            1.0f, 1.0f, 1.0f, 1, 1.0f
        ));

        eventTemplates.add(new ProductionEventTemplate(
            "demand_surge", "Nachfrageboom",
            "Ploetzlich starke Nachfrage - Verkaufspreise steigen!",
            EventSeverity.POSITIVE, EventCategory.MARKET,
            12000, 36000,
            1.0f, 1.0f, 1.4f, 0, 1.0f
        ));

        eventTemplates.add(new ProductionEventTemplate(
            "black_market_opening", "Schwarzmarkt oeffnet",
            "Ein Schmuggler-Ring eroeffnet neue Handelsrouten!",
            EventSeverity.POSITIVE, EventCategory.ILLEGAL,
            12000, 24000,
            1.0f, 1.0f, 1.6f, 0, 1.0f
        ));

        eventTemplates.add(new ProductionEventTemplate(
            "fast_growth", "Wachstumsschub",
            "Beschleunigte Wachstumsphase - Produktion 30% schneller!",
            EventSeverity.POSITIVE, EventCategory.LEGAL,
            24000, 48000,
            1.0f, 1.3f, 1.0f, 0, 1.0f
        ));

        // Negative Events
        eventTemplates.add(new ProductionEventTemplate(
            "police_raid", "Polizeirazzia",
            "Die Polizei durchsucht illegale Produktionsstaetten!",
            EventSeverity.NEGATIVE, EventCategory.ILLEGAL,
            12000, 24000,     // 0.5-1 Tage
            0.0f, 0.0f, 1.0f, 0, 1.0f  // Produktion stoppt komplett
        ));

        eventTemplates.add(new ProductionEventTemplate(
            "chemical_spill", "Chemieunfall",
            "Eine verunreinigte Charge senkt die Produktqualitaet.",
            EventSeverity.NEGATIVE, EventCategory.ALL,
            24000, 24000,
            1.0f, 1.0f, 1.0f, -1, 1.0f
        ));

        eventTemplates.add(new ProductionEventTemplate(
            "supply_shortage", "Rohstoffknappheit",
            "Engpass bei Grundstoffen - Produktion verlangsamt sich.",
            EventSeverity.NEGATIVE, EventCategory.ALL,
            24000, 48000,
            0.8f, 0.7f, 1.2f, 0, 1.0f
        ));

        eventTemplates.add(new ProductionEventTemplate(
            "equipment_failure", "Geraeteausfall",
            "Ein wichtiges Produktionsgeraet ist defekt.",
            EventSeverity.NEGATIVE, EventCategory.ALL,
            6000, 18000,
            0.5f, 0.5f, 1.0f, 0, 1.0f
        ));

        // Neutral Events
        eventTemplates.add(new ProductionEventTemplate(
            "market_shift", "Marktverschiebung",
            "Aenderung der Nachfragestruktur - einige Preise steigen, andere fallen.",
            EventSeverity.NEUTRAL, EventCategory.MARKET,
            24000, 48000,
            1.0f, 1.0f, 1.0f, 0, 1.0f
        ));

        eventTemplates.add(new ProductionEventTemplate(
            "inspection", "Qualitaetskontrolle",
            "Behoerden pruefen Produktionsstandards.",
            EventSeverity.NEUTRAL, EventCategory.LEGAL,
            12000, 24000,
            1.0f, 0.85f, 1.1f, 0, 1.0f // Langsamer aber bessere Preise
        ));

        LOGGER.info("ProductionEventManager: {} Event-Vorlagen registriert", eventTemplates.size());
    }

    // ═══════════════════════════════════════════════════════════
    // EVENT MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird taeglich aufgerufen um neue Events auszuloesen.
     */
    public void onDayChange(long currentTick, @Nullable MinecraftServer server) {
        // Abgelaufene Events entfernen
        activeEvents.entrySet().removeIf(e -> e.getValue().isExpired(currentTick));

        // Neues Event ausloesen?
        if (activeEvents.size() >= MAX_ACTIVE_EVENTS) return;

        if (ThreadLocalRandom.current().nextDouble() < EVENT_CHANCE) {
            ProductionEvent event = generateRandomEvent(currentTick);
            if (event != null) {
                activeEvents.put(event.getTemplate().getId(), event);
                LOGGER.info("Produktions-Event ausgeloest: {} (Dauer: {} Ticks)",
                    event.getTemplate().getName(), event.getDuration());

                // Spieler benachrichtigen
                if (server != null) {
                    broadcastEvent(server, event);
                }
            }
        }
    }

    @Nullable
    private ProductionEvent generateRandomEvent(long currentTick) {
        // Bereits aktive Event-IDs ausschliessen
        List<ProductionEventTemplate> available = new ArrayList<>();
        for (ProductionEventTemplate template : eventTemplates) {
            if (!activeEvents.containsKey(template.id)) {
                available.add(template);
            }
        }

        if (available.isEmpty()) return null;

        // Gewichtete Auswahl (saisonale Events bevorzugen)
        ProductionEventTemplate chosen = available.get(
            ThreadLocalRandom.current().nextInt(available.size())
        );

        int duration = ThreadLocalRandom.current().nextInt(
            chosen.minDuration, chosen.maxDuration + 1
        );

        return new ProductionEvent(chosen, currentTick, duration);
    }

    private void broadcastEvent(MinecraftServer server, ProductionEvent event) {
        ProductionEventTemplate t = event.getTemplate();
        String msg = t.severity.getColor() + "\u00A7l[" + t.severity.getSymbol() + "] " + t.name +
            "\u00A7r\n" + "\u00A77" + t.description;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(Component.literal(msg));
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MODIFIER QUERIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt den kombinierten Ertrags-Modifikator aller aktiven Events zurueck.
     */
    public float getCombinedYieldModifier(EventCategory category) {
        float modifier = 1.0f;
        for (ProductionEvent event : activeEvents.values()) {
            if (event.getTemplate().category == category || event.getTemplate().category == EventCategory.ALL) {
                modifier *= event.getYieldModifier();
            }
        }
        return modifier;
    }

    /**
     * Gibt den kombinierten Geschwindigkeits-Modifikator zurueck.
     */
    public float getCombinedSpeedModifier(EventCategory category) {
        float modifier = 1.0f;
        for (ProductionEvent event : activeEvents.values()) {
            if (event.getTemplate().category == category || event.getTemplate().category == EventCategory.ALL) {
                modifier *= event.getSpeedModifier();
            }
        }
        return modifier;
    }

    /**
     * Gibt den kombinierten Preis-Modifikator zurueck.
     */
    public float getCombinedPriceModifier(EventCategory category) {
        float modifier = 1.0f;
        for (ProductionEvent event : activeEvents.values()) {
            if (event.getTemplate().category == category
                || event.getTemplate().category == EventCategory.ALL
                || event.getTemplate().category == EventCategory.MARKET) {
                modifier *= event.getPriceModifier();
            }
        }
        return modifier;
    }

    /**
     * Gibt den kombinierten Qualitaetsaenderung zurueck.
     */
    public int getCombinedQualityChange() {
        int change = 0;
        for (ProductionEvent event : activeEvents.values()) {
            change += event.getQualityChange();
        }
        return change;
    }

    /**
     * Gibt alle aktiven Events zurueck.
     */
    public Collection<ProductionEvent> getActiveEvents() {
        return new ArrayList<>(activeEvents.values());
    }

    /**
     * Prueft ob ein bestimmter Event-Typ aktiv ist.
     */
    public boolean isEventActive(String eventId) {
        return activeEvents.containsKey(eventId);
    }

    /**
     * Generiert einen Event-Bericht fuer Spieler.
     */
    public String getEventReport() {
        if (activeEvents.isEmpty()) {
            return "\u00A77Keine aktiven Produktions-Events.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\u00A76\u00A7l=== Aktive Events ===\u00A7r\n");

        for (ProductionEvent event : activeEvents.values()) {
            ProductionEventTemplate t = event.getTemplate();
            sb.append(t.severity.getColor()).append(t.severity.getSymbol())
              .append(" \u00A7f").append(t.name)
              .append(" \u00A77(").append(t.category.getDisplayName()).append(")\n");
            sb.append("  \u00A77").append(t.description).append("\n");

            // Modifikatoren anzeigen
            if (t.yieldModifier != 1.0f) {
                sb.append("  \u00A77Ertrag: ").append(formatModifier(t.yieldModifier)).append("\n");
            }
            if (t.speedModifier != 1.0f) {
                sb.append("  \u00A77Geschwindigkeit: ").append(formatModifier(t.speedModifier)).append("\n");
            }
            if (t.priceModifier != 1.0f) {
                sb.append("  \u00A77Preise: ").append(formatModifier(t.priceModifier)).append("\n");
            }
            if (t.qualityChange != 0) {
                String sign = t.qualityChange > 0 ? "\u00A7a+" : "\u00A7c";
                sb.append("  \u00A77Qualitaet: ").append(sign).append(t.qualityChange).append(" Stufen\n");
            }
        }

        return sb.toString();
    }

    private String formatModifier(float mod) {
        int percent = Math.round((mod - 1.0f) * 100);
        if (percent > 0) return "\u00A7a+" + percent + "%";
        if (percent < 0) return "\u00A7c" + percent + "%";
        return "\u00A7f0%";
    }
}
