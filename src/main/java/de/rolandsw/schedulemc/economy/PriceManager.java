package de.rolandsw.schedulemc.economy;

import de.rolandsw.schedulemc.cannabis.items.CannabisItems;
import de.rolandsw.schedulemc.coca.items.CocaItems;
import de.rolandsw.schedulemc.lsd.items.LSDItems;
import de.rolandsw.schedulemc.mdma.items.MDMAItems;
import de.rolandsw.schedulemc.meth.items.MethItems;
import de.rolandsw.schedulemc.mushroom.items.MushroomItems;
import de.rolandsw.schedulemc.poppy.items.PoppyItems;
import de.rolandsw.schedulemc.tobacco.items.TobaccoItems;
import de.rolandsw.schedulemc.util.SecureRandomUtil;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Verwaltet dynamische Preise für Shop-Items
 *
 * System: Multiplikatoren auf Shop-GUI-Preise
 * - Zeitbasierte Wellen (±15%)
 * - Event-System (Dürre, etc.)
 * - Finale Preis = Shop-GUI-Preis × Multiplikator
 * SICHERHEIT: Thread-safe List für concurrent access
 */
public class PriceManager {

    // SICHERHEIT: CopyOnWriteArrayList für Thread-safe Iteration ohne ConcurrentModificationException
    private static final List<EconomicEvent> activeEvents = new CopyOnWriteArrayList<>();

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

        // 10% Chance auf neues Event (SICHERHEIT: SecureRandom statt Math.random())
        if (SecureRandomUtil.chance(0.1)) {
            triggerRandomEvent();
        }
    }

    /**
     * Triggert zufälliges Event aus dem Event-Pool
     * SICHERHEIT: Verwendet SecureRandom für unvorhersagbare Event-Auswahl
     */
    private static void triggerRandomEvent() {
        int eventType = SecureRandomUtil.nextInt(12);

        EconomicEvent event = switch (eventType) {
            // ═══════════════════════════════════════════════════════════
            // PREIS-BOOM EVENTS (Preise steigen)
            // ═══════════════════════════════════════════════════════════
            case 0 -> createEvent("Polizei-Razzia: Cannabis",
                    "Cannabis knapp - Preise steigen!",
                    getCannabisItems(), 1.5f, 3);

            case 1 -> createEvent("Polizei-Razzia: Kokain",
                    "Kokain knapp - Preise steigen!",
                    getCocaItems(), 1.6f, 3);

            case 2 -> createEvent("Festival-Saison",
                    "Party-Drogen sehr gefragt!",
                    getPartyDrugs(), 1.4f, 5);

            case 3 -> createEvent("Chemikalien-Knappheit",
                    "Synthetische Drogen teurer!",
                    getSyntheticDrugs(), 1.45f, 4);

            case 4 -> createEvent("Dürre",
                    "Pflanzen-basierte Produkte knapp!",
                    getPlantBasedDrugs(), 1.35f, 4);

            // ═══════════════════════════════════════════════════════════
            // PREIS-CRASH EVENTS (Preise fallen)
            // ═══════════════════════════════════════════════════════════
            case 5 -> createEvent("Überproduktion: Cannabis",
                    "Markt überschwemmt - Preise fallen!",
                    getCannabisItems(), 0.7f, 3);

            case 6 -> createEvent("Überproduktion: Meth",
                    "Zu viel Meth auf dem Markt!",
                    getMethItems(), 0.65f, 3);

            case 7 -> createEvent("Neue Konkurrenz",
                    "Alle Preise unter Druck!",
                    getAllDrugs(), 0.8f, 2);

            // ═══════════════════════════════════════════════════════════
            // SPEZIAL-EVENTS
            // ═══════════════════════════════════════════════════════════
            case 8 -> createEvent("VIP-Nachfrage",
                    "Reiche Kunden zahlen mehr für Kokain!",
                    getCocaItems(), 1.8f, 2);

            case 9 -> createEvent("Uni-Prüfungen",
                    "Studenten brauchen Aufputschmittel!",
                    getStimulants(), 1.3f, 3);

            case 10 -> createEvent("Techno-Festival",
                    "MDMA & LSD extrem gefragt!",
                    List.of(
                            MDMAItems.ECSTASY_PILL.get(),
                            MDMAItems.MDMA_KRISTALL.get(),
                            LSDItems.BLOTTER.get()
                    ), 1.7f, 2);

            case 11 -> createEvent("Grenzkontrollen",
                    "Import schwierig - lokale Ware teurer!",
                    getAllDrugs(), 1.25f, 5);

            default -> null;
        };

        if (event != null) {
            addEvent(event);
        }
    }

    /**
     * Erstellt ein Event mit einheitlichem Multiplikator für alle Items
     */
    private static EconomicEvent createEvent(String name, String description, List<Item> items, float multiplier, int days) {
        Map<Item, Float> multipliers = new HashMap<>();
        for (Item item : items) {
            if (item != null) {
                multipliers.put(item, multiplier);
            }
        }
        return new EconomicEvent(name, multipliers, days);
    }

    // ═══════════════════════════════════════════════════════════
    // ITEM-GRUPPEN
    // ═══════════════════════════════════════════════════════════

    private static List<Item> getCannabisItems() {
        return List.of(
                CannabisItems.CURED_BUD.get(),
                CannabisItems.HASH.get(),
                CannabisItems.CANNABIS_OIL.get(),
                CannabisItems.DRIED_BUD.get()
        );
    }

    private static List<Item> getCocaItems() {
        return List.of(
                CocaItems.COCAINE.get(),
                CocaItems.CRACK_ROCK.get(),
                CocaItems.COCA_PASTE.get()
        );
    }

    private static List<Item> getMethItems() {
        return List.of(
                MethItems.METH.get(),
                MethItems.KRISTALL_METH.get()
        );
    }

    private static List<Item> getPartyDrugs() {
        List<Item> items = new ArrayList<>();
        items.add(MDMAItems.ECSTASY_PILL.get());
        items.add(MDMAItems.MDMA_KRISTALL.get());
        items.add(LSDItems.BLOTTER.get());
        items.add(CocaItems.COCAINE.get());
        items.addAll(getCannabisItems());
        return items;
    }

    private static List<Item> getSyntheticDrugs() {
        List<Item> items = new ArrayList<>();
        items.addAll(getMethItems());
        items.add(MDMAItems.ECSTASY_PILL.get());
        items.add(MDMAItems.MDMA_KRISTALL.get());
        items.add(LSDItems.BLOTTER.get());
        items.add(LSDItems.LSD_LOESUNG.get());
        return items;
    }

    private static List<Item> getPlantBasedDrugs() {
        List<Item> items = new ArrayList<>();
        items.addAll(getCannabisItems());
        items.addAll(getCocaItems());
        return items;
    }

    private static List<Item> getStimulants() {
        List<Item> items = new ArrayList<>();
        items.addAll(getMethItems());
        items.addAll(getCocaItems());
        return items;
    }

    private static List<Item> getAllDrugs() {
        List<Item> items = new ArrayList<>();
        items.addAll(getCannabisItems());
        items.addAll(getCocaItems());
        items.addAll(getMethItems());
        items.add(MDMAItems.ECSTASY_PILL.get());
        items.add(MDMAItems.MDMA_KRISTALL.get());
        items.add(LSDItems.BLOTTER.get());
        return items;
    }

    /**
     * Manuell ein Event triggern (für Admin-Commands)
     */
    public static void triggerEventManually() {
        triggerRandomEvent();
    }
}
