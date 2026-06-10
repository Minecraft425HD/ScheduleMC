package de.rolandsw.schedulemc.mission;

import java.util.*;

/**
 * Statische Registry aller verfügbaren Missions-Definitionen.
 *
 * Hauptmissionen: Narrative Story-Aufgaben mit hoher Belohnung.
 * Nebenmissionen: Wiederholbare Aktivitäten mit kleinen Belohnungen.
 */
public class MissionRegistry {

    private static final Map<String, MissionDefinition> REGISTRY = new LinkedHashMap<>();

    /** IDs der dynamisch registrierten Missionen (aus Szenarien geladen). */
    private static final java.util.Set<String> DYNAMIC_IDS = new java.util.HashSet<>();

    static {
        // ── Hauptmissionen ──────────────────────────────────────────────

        register(new MissionDefinition(
            "haupt_erster_kontakt",
            "Erster Kontakt",
            "Meet the local merchant and build a business relationship.",
            MissionCategory.HAUPT,
            500, 2000,
            1, "npc_interaction_dealer"
        ));

        register(new MissionDefinition(
            "haupt_lieferung_01",
            "Die erste Lieferung",
            "Liefere 5 Pakete an die angegebenen Adressen. Sei diskret.",
            MissionCategory.HAUPT,
            800, 5000,
            5, "package_delivered"
        ));

        register(new MissionDefinition(
            "haupt_territorium",
            "Territorium sichern",
            "Control 3 areas of the city for at least one hour.",
            MissionCategory.HAUPT,
            1200, 8000,
            3, "territory_captured"
        ));

        register(new MissionDefinition(
            "haupt_grossauftrag",
            "The Big Job",
            "Complete 10 successful transactions to win the bosses' trust.",
            MissionCategory.HAUPT,
            2000, 15000,
            10, "transaction_completed",
            Collections.singletonList("haupt_lieferung_01"),
            null, ""
        ));

        register(new MissionDefinition(
            "haupt_unterwelt",
            "Tief in der Unterwelt",
            "Sell 10 goods to underworld contacts and prove your worth.",
            MissionCategory.HAUPT,
            1500, 10000,
            10, "item_sold_to_npc",
            Collections.singletonList("haupt_erster_kontakt"),
            null, ""
        ));

        register(new MissionDefinition(
            "haupt_der_coup",
            "Der Coup",
            "Successfully complete 5 robberies and become a feared criminal.",
            MissionCategory.HAUPT,
            2500, 20000,
            5, "robbery_completed",
            Collections.singletonList("haupt_grossauftrag"),
            null, ""
        ));

        register(new MissionDefinition(
            "haupt_stadtboss",
            "Stadtboss",
            "Control 5 districts simultaneously and consolidate your rule.",
            MissionCategory.HAUPT,
            3000, 30000,
            5, "territory_captured",
            Arrays.asList("haupt_territorium", "haupt_der_coup"),
            null, ""
        ));

        register(new MissionDefinition(
            "haupt_geldkoenig",
            "The Money King",
            "Earn a total of 50,000 coins and prove your economic influence.",
            MissionCategory.HAUPT,
            2200, 25000,
            50000, "money_earned",
            Collections.singletonList("haupt_unterwelt"),
            null, ""
        ));

        // ── Nebenmissionen ───────────────────────────────────────────────

        register(new MissionDefinition(
            "neben_fahrzeuge_01",
            "Stadtfahrer",
            "Fahre 10 km in Fahrzeugen durch die Stadt.",
            MissionCategory.NEBEN,
            200, 500,
            10, "km_driven"
        ));

        register(new MissionDefinition(
            "neben_handel_01",
            "Small-time Dealer",
            "Verkaufe 20 Waren an NPCs.",
            MissionCategory.NEBEN,
            300, 800,
            20, "item_sold_to_npc"
        ));

        register(new MissionDefinition(
            "neben_erkunder",
            "Stadterkunder",
            "Besuche 5 verschiedene Stadtteile.",
            MissionCategory.NEBEN,
            150, 400,
            5, "district_visited"
        ));

        register(new MissionDefinition(
            "neben_banker",
            "Thrifty Citizen",
            "Zahle 3-mal Geld auf dein Bankkonto ein.",
            MissionCategory.NEBEN,
            250, 600,
            3, "bank_deposit"
        ));

        register(new MissionDefinition(
            "neben_killer",
            "Street Fighter",
            "Besiege 25 Gegner in der Stadt.",
            MissionCategory.NEBEN,
            400, 1000,
            25, "enemy_killed"
        ));

        register(new MissionDefinition(
            "neben_schmuggler",
            "Kurierdienst",
            "Liefere 10 Pakete an verschiedene Adressen.",
            MissionCategory.NEBEN,
            350, 900,
            10, "package_delivered"
        ));

        register(new MissionDefinition(
            "neben_gangmissionar",
            "Gangmissionar",
            "Successfully complete 5 gang missions.",
            MissionCategory.NEBEN,
            500, 1500,
            5, "gang_mission_completed"
        ));

        register(new MissionDefinition(
            "neben_millionaer",
            "Auf dem Weg nach oben",
            "Verdiene insgesamt 10.000 Coins.",
            MissionCategory.NEBEN,
            300, 1000,
            10000, "money_earned"
        ));

        register(new MissionDefinition(
            "neben_handwerker",
            "Handwerker",
            "Craft 10 items.",
            MissionCategory.NEBEN,
            200, 600,
            10, "item_crafted"
        ));

        register(new MissionDefinition(
            "neben_vielbereist",
            "Vielgereister",
            "Fahre insgesamt 50 km in Fahrzeugen.",
            MissionCategory.NEBEN,
            450, 1200,
            50, "km_driven"
        ));

        register(new MissionDefinition(
            "neben_polizeiflucht",
            "Entkommen!",
            "Successfully escape the police 5 times.",
            MissionCategory.NEBEN,
            380, 1100,
            5, "mission_completed"
        ));

        register(new MissionDefinition(
            "neben_sozial",
            "Stadtbekannter",
            "Sprich mit 20 verschiedenen NPCs.",
            MissionCategory.NEBEN,
            180, 500,
            20, "npc_talked"
        ));

        register(new MissionDefinition(
            "neben_geldwaescher",
            "Money Launderer",
            "Zahle 5-mal Geld auf dein Bankkonto ein.",
            MissionCategory.NEBEN,
            280, 700,
            5, "bank_deposit"
        ));

        // ── Weitere Hauptmissionen (Story-Arc II) ──────────────────────
        register(new MissionDefinition(
            "haupt_zeugenschutz",
            "Protect the witness",
            "An important witness is in danger. Protect him and get him away safely.",
            MissionCategory.HAUPT,
            1800, 12000,
            5, "enemy_killed",
            Collections.singletonList("haupt_erster_kontakt"),
            null, ""
        ));

        register(new MissionDefinition(
            "haupt_korruption",
            "Korruption aufdecken",
            "Collect 5 pieces of evidence against corrupt officials and hand them to your contact.",
            MissionCategory.HAUPT,
            2400, 18000,
            5, "item_collected",
            Collections.singletonList("haupt_unterwelt"),
            null, ""
        ));

        register(new MissionDefinition(
            "haupt_kronzeuge",
            "Kronzeuge",
            "Cooperate with 4 different informants and secure immunity.",
            MissionCategory.HAUPT,
            2800, 22000,
            4, "npc_talked",
            Arrays.asList("haupt_korruption", "haupt_der_coup"),
            null, ""
        ));

        register(new MissionDefinition(
            "haupt_rache",
            "Die Abrechnung",
            "Take out your archenemy and end the feud once and for all.",
            MissionCategory.HAUPT,
            3500, 35000,
            1, "enemy_killed",
            Arrays.asList("haupt_stadtboss", "haupt_kronzeuge"),
            null, ""
        ));

        register(new MissionDefinition(
            "haupt_mafia_koenig",
            "King of the Mafia",
            "Collect tribute from 10 NPCs and secure your rule over the city.",
            MissionCategory.HAUPT,
            4000, 50000,
            10, "transaction_completed",
            Collections.singletonList("haupt_rache"),
            null, ""
        ));

        // ── Weitere Nebenmissionen ───────────────────────────────────────
        register(new MissionDefinition(
            "neben_safeknacker",
            "Safeknacker",
            "Successfully crack 3 safes.",
            MissionCategory.NEBEN,
            420, 1200,
            3, "robbery_completed"
        ));

        register(new MissionDefinition(
            "neben_spion",
            "Informant",
            "Obtain 8 secret pieces of information through NPC conversations.",
            MissionCategory.NEBEN,
            320, 900,
            8, "npc_talked"
        ));

        register(new MissionDefinition(
            "neben_gebietskontrolle",
            "Gebietskontrolle",
            "Sichere 8 Territorien.",
            MissionCategory.NEBEN,
            550, 1600,
            8, "territory_captured"
        ));

        register(new MissionDefinition(
            "neben_traders",
            "Wholesaler",
            "Verkaufe 50 Waren an NPCs.",
            MissionCategory.NEBEN,
            480, 1400,
            50, "item_sold_to_npc"
        ));

        register(new MissionDefinition(
            "neben_gangchef",
            "Gangchef",
            "Successfully complete 15 gang missions.",
            MissionCategory.NEBEN,
            700, 2200,
            15, "gang_mission_completed"
        ));

        register(new MissionDefinition(
            "neben_strassenrennen",
            "Street Racer",
            "Travel 100 km in vehicles.",
            MissionCategory.NEBEN,
            600, 1800,
            100, "km_driven"
        ));

        register(new MissionDefinition(
            "neben_bankier",
            "Millionaire",
            "Verdiene insgesamt 100.000 Coins.",
            MissionCategory.NEBEN,
            900, 3000,
            100000, "money_earned"
        ));

        register(new MissionDefinition(
            "neben_legende",
            "Lebende Legende",
            "Complete 25 missions of any kind.",
            MissionCategory.NEBEN,
            1000, 5000,
            25, "mission_completed"
        ));
    }

    private static void register(MissionDefinition definition) {
        REGISTRY.put(definition.getId(), definition);
    }

    /**
     * Registriert eine dynamisch geladene Mission (aus Szenario-Editor).
     * Dynamische Missionen koennen mit {@link #clearDynamic()} entfernt werden.
     */
    public static void registerDynamic(MissionDefinition definition) {
        synchronized (MissionRegistry.class) {
            REGISTRY.put(definition.getId(), definition);
            DYNAMIC_IDS.add(definition.getId());
        }
    }

    /**
     * Entfernt alle dynamisch registrierten Missionen.
     * Statisch (im static-Block) registrierte Missionen bleiben erhalten.
     */
    public static void clearDynamic() {
        synchronized (MissionRegistry.class) {
            for (String id : DYNAMIC_IDS) {
                REGISTRY.remove(id);
            }
            DYNAMIC_IDS.clear();
        }
    }

    public static MissionDefinition getById(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<MissionDefinition> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static List<MissionDefinition> getByCategory(MissionCategory category) {
        List<MissionDefinition> result = new ArrayList<>();
        for (MissionDefinition def : REGISTRY.values()) {
            if (def.getCategory() == category) {
                result.add(def);
            }
        }
        return result;
    }

    public static boolean exists(String id) {
        return REGISTRY.containsKey(id);
    }
}
