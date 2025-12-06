package de.rolandsw.schedulemc.economy;

import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Verwaltet dynamische Preise für Shop-Items
 *
 * System: Multiplikatoren auf Shop-GUI-Preise
 * - Zeitbasierte Wellen (±15%)
 * - Event-System (Dürre, etc.)
 * - Finale Preis = Shop-GUI-Preis × Multiplikator
 */
public class PriceManager {

    private static final List<EconomicEvent> activeEvents = new ArrayList<>();

    /**
     * Gibt Preis-MULTIPLIKATOR zurück (nicht absoluten Preis!)
     *
     * 1.0 = Normalpreis
     * 0.85 = 15% günstiger
     * 1.15 = 15% teurer
     */
    public static float getPriceMultiplier(Item item) {
        // 1. Zeitbasierte Welle (±15%)
        long time = System.currentTimeMillis() / 1000; // Sekunden
        double wave = Math.sin(time * 0.0001); // Sehr langsame Welle
        double baseMultiplier = 1.0 + (wave * 0.15); // 0.85 - 1.15

        // 2. Event-Multiplikatoren
        float eventMultiplier = 1.0f;
        for (EconomicEvent event : activeEvents) {
            if (event.affectsItem(item)) {
                eventMultiplier *= event.getMultiplier(item);
            }
        }

        return (float)(baseMultiplier * eventMultiplier);
    }

    /**
     * Berechnet finalen Verkaufspreis
     * @param basePrice Preis aus Shop-GUI
     * @param item Das Item
     * @return Finaler Preis
     */
    public static int getFinalPrice(int basePrice, Item item) {
        float multiplier = getPriceMultiplier(item);
        return Math.max(1, (int)(basePrice * multiplier)); // Mindestens 1€
    }

    /**
     * Gibt aktuellen Multiplikator als Prozentsatz zurück
     * z.B. 0.85 → "-15%", 1.15 → "+15%"
     */
    public static String getMultiplierAsPercentage(Item item) {
        float multiplier = getPriceMultiplier(item);
        int percentage = (int)((multiplier - 1.0f) * 100);
        if (percentage > 0) {
            return "+" + percentage + "%";
        } else if (percentage < 0) {
            return percentage + "%";
        } else {
            return "±0%";
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EVENT-SYSTEM
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt Wirtschafts-Event hinzu
     */
    public static void addEvent(EconomicEvent event) {
        activeEvents.add(event);
    }

    /**
     * Entfernt Event
     */
    public static void removeEvent(EconomicEvent event) {
        activeEvents.remove(event);
    }

    /**
     * Entfernt abgelaufene Events
     */
    public static void removeExpiredEvents() {
        activeEvents.removeIf(EconomicEvent::isExpired);
    }

    /**
     * Gibt alle aktiven Events zurück
     */
    public static List<EconomicEvent> getActiveEvents() {
        return new ArrayList<>(activeEvents);
    }

    /**
     * Prüft tägliche Events (10% Chance)
     */
    public static void checkDailyEvents() {
        // Entferne abgelaufene Events
        removeExpiredEvents();

        // 10% Chance auf neues Event
        if (Math.random() < 0.1) {
            triggerRandomEvent();
        }
    }

    /**
     * Triggert zufälliges Event
     */
    private static void triggerRandomEvent() {
        // TODO: Implementiere Event-Pool
        // Beispiele siehe Vorschlag 2 Dokumentation
    }
}
