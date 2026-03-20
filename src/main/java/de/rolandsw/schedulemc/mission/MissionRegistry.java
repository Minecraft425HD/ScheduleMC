package de.rolandsw.schedulemc.mission;

import java.util.*;

/**
 * Statische Registry aller verfügbaren Missions-Definitionen.
 *
 * Hauptmissionen: Narrative Story-Aufgaben mit hoher Belohnung.
 * Nebenmissionen: Wiederholbare Aktivitäten mit kleinen Belohnungen.
 */
public class MissionRegistry {

    private static final Map<String, MissionDefinition> REGISTRY = new LinkedHashMap<>();  // NOPMD

    /** IDs der dynamisch registrierten Missionen (aus Szenarien geladen). */
    private static final java.util.Set<String> DYNAMIC_IDS = new java.util.HashSet<>();  // NOPMD

    static {
        // ── Hauptmissionen ──────────────────────────────────────────────

        register(new MissionDefinition(
            "haupt_erster_kontakt",
            "Erster Kontakt",
            "Triff dich mit dem lokalen Händler und baue eine Geschäftsbeziehung auf.",
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
            "Kontrolliere 3 Bereiche der Stadt für mindestens eine Stunde.",
            MissionCategory.HAUPT,
            1200, 8000,
            3, "territory_captured"
        ));

        register(new MissionDefinition(
            "haupt_grossauftrag",
            "Der große Auftrag",
            "Schließe 10 erfolgreiche Transaktionen ab, um das Vertrauen der Bosse zu gewinnen.",
            MissionCategory.HAUPT,
            2000, 15000,
            10, "transaction_completed",
            Collections.singletonList("haupt_lieferung_01"),
            null, ""
        ));

        register(new MissionDefinition(
            "haupt_unterwelt",
            "Tief in der Unterwelt",
            "Verkaufe 10 Waren an Unterwelt-Kontakte und beweise deinen Wert.",
            MissionCategory.HAUPT,
            1500, 10000,
            10, "item_sold_to_npc",
            Collections.singletonList("haupt_erster_kontakt"),
            null, ""
        ));

        register(new MissionDefinition(
            "haupt_der_coup",
            "Der Coup",
            "Schließe 5 Raubüberfälle erfolgreich ab und werde zum gefürchteten Kriminellen.",
            MissionCategory.HAUPT,
            2500, 20000,
            5, "robbery_completed",
            Collections.singletonList("haupt_grossauftrag"),
            null, ""
        ));

        register(new MissionDefinition(
            "haupt_stadtboss",
            "Stadtboss",
            "Kontrolliere 5 Stadtteile gleichzeitig und festige deine Herrschaft.",
            MissionCategory.HAUPT,
            3000, 30000,
            5, "territory_captured",
            Arrays.asList("haupt_territorium", "haupt_der_coup"),
            null, ""
        ));

        register(new MissionDefinition(
            "haupt_geldkoenig",
            "Der Geldkönig",
            "Verdiene insgesamt 50.000 Coins und beweise deinen wirtschaftlichen Einfluss.",
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
            "Kleinhändler",
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
            "Sparsamer Bürger",
            "Zahle 3-mal Geld auf dein Bankkonto ein.",
            MissionCategory.NEBEN,
            250, 600,
            3, "bank_deposit"
        ));

        register(new MissionDefinition(
            "neben_killer",
            "Straßenkämpfer",
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
            "Schließe 5 Gang-Missionen erfolgreich ab.",
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
            "Stelle 10 Gegenstände her.",
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
            "Entkomme 5-mal erfolgreich der Polizei.",
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
            "Geldwäscher",
            "Zahle 5-mal Geld auf dein Bankkonto ein.",
            MissionCategory.NEBEN,
            280, 700,
            5, "bank_deposit"
        ));
    }

    private static void register(MissionDefinition definition) {
        REGISTRY.put(definition.getId(), definition);
    }

    /**
     * Registriert eine dynamisch geladene Mission (aus Szenario-Editor).
     * Dynamische Missionen koennen mit {@link #clearDynamic()} entfernt werden.
     */
    public static synchronized void registerDynamic(MissionDefinition definition) {
        REGISTRY.put(definition.getId(), definition);
        DYNAMIC_IDS.add(definition.getId());
    }

    /**
     * Entfernt alle dynamisch registrierten Missionen.
     * Statisch (im static-Block) registrierte Missionen bleiben erhalten.
     */
    public static synchronized void clearDynamic() {
        for (String id : DYNAMIC_IDS) {
            REGISTRY.remove(id);
        }
        DYNAMIC_IDS.clear();
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
