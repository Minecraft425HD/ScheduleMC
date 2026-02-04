package de.rolandsw.schedulemc.gang.scenario;

/**
 * Alle verfuegbaren Baustein-Typen fuer den Szenario-Editor.
 *
 * 61 Bloecke in 10 Kategorien.
 * Jeder Typ hat: Anzeigename, Symbol, Farbe, Kategorie, Parameter-Definitionen.
 * Jeder Parameter hat einen Widget-Typ der bestimmt wie er im Editor dargestellt wird.
 */
public enum ObjectiveType {

    // ═══════════════════════════════════════════════════════════
    // BEWEGUNG (6)
    // ═══════════════════════════════════════════════════════════
    GOTO_LOCATION("Gehe zu Ort", "G", 0xFF3498DB, Category.BEWEGUNG,
            new ParamDef[]{
                    new ParamDef("x", "X", "0", ParamWidget.COORD),
                    new ParamDef("y", "Y", "64", ParamWidget.COORD),
                    new ParamDef("z", "Z", "0", ParamWidget.COORD),
                    new ParamDef("radius", "Radius", "5", ParamWidget.NUMBER)
            }),
    GOTO_NPC("Gehe zu NPC", "N", 0xFF27AE60, Category.BEWEGUNG,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("npc_type", "Beruf", "VERKAEUFER", ParamWidget.DROPDOWN_NPC_TYPE)
            }),
    GOTO_PLOT("Gehe zu Grundstueck", "P", 0xFF2980B9, Category.BEWEGUNG,
            new ParamDef[]{
                    new ParamDef("plot_id", "Grundstueck", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("radius", "Radius", "5", ParamWidget.NUMBER)
            }),
    FOLLOW_NPC("NPC folgen", "F", 0xFF1E8449, Category.BEWEGUNG,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("duration", "Dauer (s)", "60", ParamWidget.NUMBER)
            }),
    DRIVE_TO("Zum Ziel fahren", "A", 0xFF2E86C1, Category.BEWEGUNG,
            new ParamDef[]{
                    new ParamDef("plot_id", "Ziel", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("vehicle_type", "Fahrzeug", "AUTO", ParamWidget.DROPDOWN_VEHICLE)
            }),
    ENTER_BUILDING("Gebaeude betreten", "E", 0xFF2471A3, Category.BEWEGUNG,
            new ParamDef[]{
                    new ParamDef("plot_id", "Gebaeude", "", ParamWidget.DROPDOWN_PLOT)
            }),

    // ═══════════════════════════════════════════════════════════
    // INTERAKTION (10)
    // ═══════════════════════════════════════════════════════════
    TALK_TO_NPC("Mit NPC reden", "I", 0xFFF1C40F, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("npc_type", "Beruf", "VERKAEUFER", ParamWidget.DROPDOWN_NPC_TYPE),
                    new ParamDef("dialog_id", "Dialog-ID", "default", ParamWidget.TEXT)
            }),
    TRADE_NPC("Mit NPC handeln", "M", 0xFFE6A817, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("merchant_cat", "Kategorie", "LEBENSMITTEL", ParamWidget.DROPDOWN_MERCHANT_CAT)
            }),
    COLLECT_ITEMS("Items sammeln", "C", 0xFFE67E22, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("item_id", "Item", "minecraft:diamond", ParamWidget.TEXT),
                    new ParamDef("amount", "Menge", "1", ParamWidget.NUMBER)
            }),
    DELIVER_ITEM("Item liefern", "D", 0xFF8B6914, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("item_id", "Item", "minecraft:paper", ParamWidget.TEXT),
                    new ParamDef("plot_id", "Ziel-Grundstueck", "", ParamWidget.DROPDOWN_PLOT)
            }),
    PICK_LOCK("Schloss knacken", "L", 0xFFCCA300, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY),
                    new ParamDef("time_limit", "Zeitlimit (s)", "30", ParamWidget.NUMBER)
            }),
    PLANT_ITEM("Item platzieren", "P", 0xFFB7950B, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("item_id", "Item", "minecraft:tnt", ParamWidget.TEXT),
                    new ParamDef("plot_id", "Ort", "", ParamWidget.DROPDOWN_PLOT)
            }),
    USE_ITEM("Item benutzen", "U", 0xFFD4AC0D, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("item_id", "Item", "minecraft:ender_pearl", ParamWidget.TEXT)
            }),
    GIVE_ITEM_NPC("Item an NPC geben", "G", 0xFFD68910, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("item_id", "Item", "minecraft:emerald", ParamWidget.TEXT),
                    new ParamDef("amount", "Menge", "1", ParamWidget.NUMBER)
            }),
    STEAL_ITEM("Item stehlen", "S", 0xFFA04000, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("item_id", "Item", "minecraft:diamond", ParamWidget.TEXT),
                    new ParamDef("plot_id", "Ort", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    BUY_ITEM("Item kaufen", "K", 0xFFF0B27A, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("npc_name", "Haendler", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("merchant_cat", "Kategorie", "LEBENSMITTEL", ParamWidget.DROPDOWN_MERCHANT_CAT),
                    new ParamDef("item_id", "Item", "minecraft:bread", ParamWidget.TEXT)
            }),

    // ═══════════════════════════════════════════════════════════
    // UEBERFALL (11)
    // ═══════════════════════════════════════════════════════════
    ROB_STORE("Laden ueberfallen", "!", 0xFFE74C3C, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("plot_id", "Laden", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("difficulty", "Schwierigkeit", "1", ParamWidget.DROPDOWN_DIFFICULTY),
                    new ParamDef("time_limit", "Zeitlimit (s)", "120", ParamWidget.NUMBER)
            }),
    ROB_BANK("Bank ueberfallen", "B", 0xFFC0392B, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("plot_id", "Bank", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("difficulty", "Schwierigkeit", "3", ParamWidget.DROPDOWN_DIFFICULTY),
                    new ParamDef("time_limit", "Zeitlimit (s)", "180", ParamWidget.NUMBER)
            }),
    ROB_JEWELRY("Juwelier ueberfallen", "J", 0xFFAF7AC5, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("plot_id", "Juwelier", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY),
                    new ParamDef("time_limit", "Zeitlimit (s)", "150", ParamWidget.NUMBER)
            }),
    ROB_GAS_STATION("Tankstelle ueberfallen", "T", 0xFFD35400, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("plot_id", "Tankstelle", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("difficulty", "Schwierigkeit", "1", ParamWidget.DROPDOWN_DIFFICULTY),
                    new ParamDef("time_limit", "Zeitlimit (s)", "90", ParamWidget.NUMBER)
            }),
    ROB_PLOT("Grundstueck ueberfallen", "R", 0xFFB03A2E, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("plot_id", "Grundstueck", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY),
                    new ParamDef("time_limit", "Zeitlimit (s)", "120", ParamWidget.NUMBER)
            }),
    CRACK_SAFE("Tresor knacken", "K", 0xFF7F8C8D, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY),
                    new ParamDef("time_limit", "Zeitlimit (s)", "60", ParamWidget.NUMBER)
            }),
    HACK_SYSTEM("System hacken", "H", 0xFF2C3E50, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    ROB_NPC("NPC ausrauben", "N", 0xFFCB4335, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    PLANT_BOMB("Bombe platzieren", "Q", 0xFFB71C1C, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("plot_id", "Grundstueck", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("timer", "Timer (s)", "30", ParamWidget.NUMBER)
            }),
    KIDNAP_NPC("NPC entfuehren", "E", 0xFF922B21, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("plot_id", "Ziel-Versteck", "", ParamWidget.DROPDOWN_PLOT)
            }),
    SABOTAGE("Sabotage", "S", 0xFF943126, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("plot_id", "Grundstueck", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
            }),

    // ═══════════════════════════════════════════════════════════
    // UEBERLEBEN (5)
    // ═══════════════════════════════════════════════════════════
    SURVIVE_TIME("Zeit ueberleben", "Z", 0xFF9B59B6, Category.UEBERLEBEN,
            new ParamDef[]{
                    new ParamDef("duration", "Dauer (s)", "60", ParamWidget.NUMBER)
            }),
    ESCAPE_ZONE("Fluchtzone", "F", 0xFF8E44AD, Category.UEBERLEBEN,
            new ParamDef[]{
                    new ParamDef("plot_id", "Flucht-Gebiet", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("radius", "Radius", "50", ParamWidget.NUMBER)
            }),
    EVADE_POLICE("Polizei entkommen", "P", 0xFF1ABC9C, Category.UEBERLEBEN,
            new ParamDef[]{
                    new ParamDef("wanted_level", "Fahndungslevel", "2", ParamWidget.NUMBER),
                    new ParamDef("duration", "Dauer (s)", "120", ParamWidget.NUMBER)
            }),
    HIDE_FROM_POLICE("Vor Polizei verstecken", "H", 0xFF7D3C98, Category.UEBERLEBEN,
            new ParamDef[]{
                    new ParamDef("duration", "Dauer (s)", "90", ParamWidget.NUMBER),
                    new ParamDef("plot_id", "Versteck-Gebiet", "", ParamWidget.DROPDOWN_PLOT)
            }),
    LOSE_WANTED("Fahndung verlieren", "W", 0xFF6C3483, Category.UEBERLEBEN,
            new ParamDef[]{
                    new ParamDef("target_level", "Ziellevel", "0", ParamWidget.NUMBER)
            }),

    // ═══════════════════════════════════════════════════════════
    // KAMPF (6)
    // ═══════════════════════════════════════════════════════════
    KILL_MOBS("Gegner besiegen", "X", 0xFF34495E, Category.KAMPF,
            new ParamDef[]{
                    new ParamDef("entity_type", "Gegner-Typ", "zombie", ParamWidget.DROPDOWN_ENTITY),
                    new ParamDef("count", "Anzahl", "5", ParamWidget.NUMBER)
            }),
    DEFEND_AREA("Gebiet verteidigen", "V", 0xFF196F3D, Category.KAMPF,
            new ParamDef[]{
                    new ParamDef("plot_id", "Gebiet", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("duration", "Dauer (s)", "180", ParamWidget.NUMBER)
            }),
    FIGHT_NPC("NPC besiegen", "F", 0xFF2C3E50, Category.KAMPF,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    PROTECT_NPC("NPC beschuetzen", "B", 0xFF1A5276, Category.KAMPF,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("duration", "Dauer (s)", "120", ParamWidget.NUMBER)
            }),
    AMBUSH("Hinterhalt", "A", 0xFF283747, Category.KAMPF,
            new ParamDef[]{
                    new ParamDef("plot_id", "Ort", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("entity_type", "Gegner-Typ", "zombie", ParamWidget.DROPDOWN_ENTITY),
                    new ParamDef("count", "Anzahl", "3", ParamWidget.NUMBER)
            }),
    GANG_FIGHT("Gang-Kampf", "G", 0xFF1B2631, Category.KAMPF,
            new ParamDef[]{
                    new ParamDef("plot_id", "Gebiet", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("duration", "Dauer (s)", "300", ParamWidget.NUMBER),
                    new ParamDef("difficulty", "Schwierigkeit", "3", ParamWidget.DROPDOWN_DIFFICULTY)
            }),

    // ═══════════════════════════════════════════════════════════
    // WIRTSCHAFT (6)
    // ═══════════════════════════════════════════════════════════
    EARN_MONEY("Geld verdienen", "$", 0xFFD4AC0D, Category.WIRTSCHAFT,
            new ParamDef[]{
                    new ParamDef("amount", "Betrag", "1000", ParamWidget.NUMBER)
            }),
    SELL_ITEMS("Items verkaufen", "W", 0xFF17A589, Category.WIRTSCHAFT,
            new ParamDef[]{
                    new ParamDef("item_id", "Item", "minecraft:diamond", ParamWidget.TEXT),
                    new ParamDef("amount", "Menge", "10", ParamWidget.NUMBER)
            }),
    LAUNDER_MONEY("Geldwaesche", "L", 0xFFB8860B, Category.WIRTSCHAFT,
            new ParamDef[]{
                    new ParamDef("amount", "Betrag", "5000", ParamWidget.NUMBER),
                    new ParamDef("plot_id", "Ort", "", ParamWidget.DROPDOWN_PLOT)
            }),
    COLLECT_DEBT("Schulden eintreiben", "S", 0xFFAA8800, Category.WIRTSCHAFT,
            new ParamDef[]{
                    new ParamDef("npc_name", "Schuldner", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("amount", "Betrag", "2000", ParamWidget.NUMBER)
            }),
    BUY_PROPERTY("Grundstueck kaufen", "H", 0xFF9A7D0A, Category.WIRTSCHAFT,
            new ParamDef[]{
                    new ParamDef("plot_id", "Grundstueck", "", ParamWidget.DROPDOWN_PLOT)
            }),
    PAY_NPC("NPC bezahlen", "B", 0xFF7D6608, Category.WIRTSCHAFT,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("amount", "Betrag", "500", ParamWidget.NUMBER)
            }),

    // ═══════════════════════════════════════════════════════════
    // SONSTIGES (9)
    // ═══════════════════════════════════════════════════════════
    WAIT_TIME("Warten", ".", 0xFF95A5A6, Category.SONSTIGES,
            new ParamDef[]{
                    new ParamDef("duration", "Dauer (s)", "30", ParamWidget.NUMBER)
            }),
    PATROL_ROUTE("Patrouille", "L", 0xFF5D6D7E, Category.SONSTIGES,
            new ParamDef[]{
                    new ParamDef("plot_from", "Von", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("plot_to", "Nach", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("duration", "Dauer (s)", "120", ParamWidget.NUMBER)
            }),
    CHECKPOINT("Checkpoint", "C", 0xFF7F8C8D, Category.SONSTIGES,
            new ParamDef[]{}),
    RANDOM_EVENT("Zufallsereignis", "?", 0xFF839192, Category.SONSTIGES,
            new ParamDef[]{
                    new ParamDef("chance", "Chance (%)", "50", ParamWidget.NUMBER),
                    new ParamDef("event_type", "Ereignis", "POLIZEI_KONTROLLE", ParamWidget.DROPDOWN_EVENT)
            }),
    SEND_MESSAGE("Nachricht senden", "N", 0xFFAAB7B8, Category.SONSTIGES,
            new ParamDef[]{
                    new ParamDef("text", "Text", "Missionsziel!", ParamWidget.TEXT),
                    new ParamDef("color", "Farbe", "GELB", ParamWidget.DROPDOWN_COLOR)
            }),
    PLAY_SOUND("Sound abspielen", "S", 0xFF85929E, Category.SONSTIGES,
            new ParamDef[]{
                    new ParamDef("sound_id", "Sound", "minecraft:entity.experience_orb.pickup", ParamWidget.TEXT)
            }),
    SPAWN_ENTITY("Entity spawnen", "E", 0xFF717D7E, Category.SONSTIGES,
            new ParamDef[]{
                    new ParamDef("entity_type", "Entity-Typ", "zombie", ParamWidget.DROPDOWN_ENTITY),
                    new ParamDef("count", "Anzahl", "3", ParamWidget.NUMBER),
                    new ParamDef("plot_id", "Ort", "", ParamWidget.DROPDOWN_PLOT)
            }),
    TELEPORT("Teleportieren", "T", 0xFF566573, Category.SONSTIGES,
            new ParamDef[]{
                    new ParamDef("x", "X", "0", ParamWidget.COORD),
                    new ParamDef("y", "Y", "64", ParamWidget.COORD),
                    new ParamDef("z", "Z", "0", ParamWidget.COORD)
            }),
    SET_WAYPOINT("Wegpunkt setzen", "W", 0xFF626567, Category.SONSTIGES,
            new ParamDef[]{
                    new ParamDef("name", "Name", "Ziel", ParamWidget.TEXT),
                    new ParamDef("x", "X", "0", ParamWidget.COORD),
                    new ParamDef("y", "Y", "64", ParamWidget.COORD),
                    new ParamDef("z", "Z", "0", ParamWidget.COORD)
            }),

    // ═══════════════════════════════════════════════════════════
    // FAHRZEUG (3)
    // ═══════════════════════════════════════════════════════════
    CHASE_VEHICLE("Fahrzeug verfolgen", "V", 0xFF1565C0, Category.FAHRZEUG,
            new ParamDef[]{
                    new ParamDef("vehicle_type", "Fahrzeug", "AUTO", ParamWidget.DROPDOWN_VEHICLE),
                    new ParamDef("duration", "Dauer (s)", "120", ParamWidget.NUMBER)
            }),
    RACE("Rennen fahren", "R", 0xFF1976D2, Category.FAHRZEUG,
            new ParamDef[]{
                    new ParamDef("plot_from", "Start", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("plot_to", "Ziel", "", ParamWidget.DROPDOWN_PLOT)
            }),
    VEHICLE_DELIVERY("Fahrzeug abliefern", "D", 0xFF1E88E5, Category.FAHRZEUG,
            new ParamDef[]{
                    new ParamDef("vehicle_type", "Fahrzeug", "AUTO", ParamWidget.DROPDOWN_VEHICLE),
                    new ParamDef("plot_id", "Ziel", "", ParamWidget.DROPDOWN_PLOT)
            }),

    // ═══════════════════════════════════════════════════════════
    // STEALTH (3)
    // ═══════════════════════════════════════════════════════════
    SNEAK_PAST("Vorbeischleichen", "S", 0xFF546E7A, Category.STEALTH,
            new ParamDef[]{
                    new ParamDef("plot_id", "Gebiet", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    DISGUISE("Verkleidung", "V", 0xFF607D8B, Category.STEALTH,
            new ParamDef[]{
                    new ParamDef("outfit", "Outfit", "POLIZIST", ParamWidget.DROPDOWN_OUTFIT)
            }),
    DISTRACT_NPC("NPC ablenken", "A", 0xFF78909C, Category.STEALTH,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("method", "Methode", "ABLENKUNG", ParamWidget.DROPDOWN_METHOD)
            }),

    // ═══════════════════════════════════════════════════════════
    // SPEZIAL (nicht in Palette, automatisch im Editor)
    // ═══════════════════════════════════════════════════════════
    START("Start", "\u25B6", 0xFF2ECC71, Category.SPEZIAL, new ParamDef[]{}),
    REWARD("Belohnung", "\u2605", 0xFFFFD700, Category.SPEZIAL,
            new ParamDef[]{
                    new ParamDef("xp", "XP", "100", ParamWidget.NUMBER),
                    new ParamDef("money", "Geld", "5000", ParamWidget.NUMBER)
            });

    private final String displayName;
    private final String icon;
    private final int color;
    private final Category category;
    private final ParamDef[] paramDefs;

    ObjectiveType(String displayName, String icon, int color, Category category, ParamDef[] paramDefs) {
        this.displayName = displayName;
        this.icon = icon;
        this.color = color;
        this.category = category;
        this.paramDefs = paramDefs;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
    public int getColor() { return color; }
    public Category getCategory() { return category; }
    public ParamDef[] getParamDefs() { return paramDefs; }

    public int getDarkerColor() {
        int r = Math.max(0, ((color >> 16) & 0xFF) - 40);
        int g = Math.max(0, ((color >> 8) & 0xFF) - 40);
        int b = Math.max(0, (color & 0xFF) - 40);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    // ═══════════════════════════════════════════════════════════
    // WIDGET-TYP (bestimmt Darstellung im Properties-Panel)
    // ═══════════════════════════════════════════════════════════

    public enum ParamWidget {
        TEXT,                    // Freitextfeld
        NUMBER,                  // Zahlenfeld mit +/- Buttons
        COORD,                   // Koordinate (farbig: X=rot, Y=gruen, Z=blau)
        DROPDOWN_NPC_NAME,       // NPC-Name aus Server-Daten
        DROPDOWN_NPC_TYPE,       // NPCType Enum
        DROPDOWN_MERCHANT_CAT,   // MerchantCategory Enum
        DROPDOWN_BANK_CAT,       // BankCategory Enum
        DROPDOWN_SERVICE_CAT,    // ServiceCategory Enum
        DROPDOWN_PLOT,           // Grundstueck aus Server-Daten
        DROPDOWN_PLOT_TYPE,      // PlotType Enum
        DROPDOWN_DIFFICULTY,     // Schwierigkeit 1-5
        DROPDOWN_ENTITY,         // Gegner-/Entity-Typ
        DROPDOWN_VEHICLE,        // Fahrzeugtyp
        DROPDOWN_OUTFIT,         // Verkleidungs-Outfit
        DROPDOWN_METHOD,         // Ablenkungs-Methode
        DROPDOWN_EVENT,          // Zufallsereignis-Typ
        DROPDOWN_COLOR           // Nachrichtenfarbe
    }

    // ═══════════════════════════════════════════════════════════
    // KATEGORIE
    // ═══════════════════════════════════════════════════════════

    public enum Category {
        BEWEGUNG("Bewegung", 0xFF3498DB),
        INTERAKTION("Interaktion", 0xFFF39C12),
        UEBERFALL("Ueberfall", 0xFFE74C3C),
        UEBERLEBEN("Ueberleben", 0xFF9B59B6),
        KAMPF("Kampf", 0xFF34495E),
        WIRTSCHAFT("Wirtschaft", 0xFFD4AC0D),
        SONSTIGES("Sonstiges", 0xFF95A5A6),
        FAHRZEUG("Fahrzeug", 0xFF2196F3),
        STEALTH("Stealth", 0xFF607D8B),
        SPEZIAL("Spezial", 0xFF2ECC71);

        private final String displayName;
        private final int color;

        Category(String displayName, int color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() { return displayName; }
        public int getColor() { return color; }
    }

    // ═══════════════════════════════════════════════════════════
    // PARAMETER-DEFINITION
    // ═══════════════════════════════════════════════════════════

    public record ParamDef(String key, String label, String defaultValue, ParamWidget widget) {}
}
