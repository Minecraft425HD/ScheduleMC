package de.rolandsw.schedulemc.gang.scenario;

/**
 * Alle verfuegbaren Baustein-Typen fuer den Szenario-Editor.
 *
 * 147 Bloecke in 14 Kategorien.
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
    LEAVE_BUILDING("Gebaeude verlassen", "L", 0xFF1A6FAF, Category.BEWEGUNG,
            new ParamDef[]{
                    new ParamDef("plot_id", "Gebaeude", "", ParamWidget.DROPDOWN_PLOT)
            }),
    MEET_AT_VEHICLE("Zum Fahrzeug", "V", 0xFF1F618D, Category.BEWEGUNG,
            new ParamDef[]{
                    new ParamDef("vehicle_type", "Fahrzeug", "AUTO", ParamWidget.DROPDOWN_VEHICLE),
                    new ParamDef("plot_id", "Standort", "", ParamWidget.DROPDOWN_PLOT)
            }),
    SWIM_TO("Zum Unterwasserort schwimmen", "~", 0xFF117A65, Category.BEWEGUNG,
            new ParamDef[]{
                    new ParamDef("x", "X", "0", ParamWidget.COORD),
                    new ParamDef("y", "Y", "40", ParamWidget.COORD),
                    new ParamDef("z", "Z", "0", ParamWidget.COORD),
                    new ParamDef("radius", "Radius", "5", ParamWidget.NUMBER)
            }),
    REACH_HEIGHT("Hoehe erreichen", "^", 0xFF1A5276, Category.BEWEGUNG,
            new ParamDef[]{
                    new ParamDef("min_y", "Min Y", "80", ParamWidget.NUMBER),
                    new ParamDef("plot_id", "Gebiet (optional)", "", ParamWidget.DROPDOWN_PLOT)
            }),
    SPRINT_TO("Zum Ziel sprinten", "S", 0xFF154360, Category.BEWEGUNG,
            new ParamDef[]{
                    new ParamDef("x", "X", "0", ParamWidget.COORD),
                    new ParamDef("y", "Y", "64", ParamWidget.COORD),
                    new ParamDef("z", "Z", "0", ParamWidget.COORD),
                    new ParamDef("radius", "Radius", "5", ParamWidget.NUMBER),
                    new ParamDef("time_limit", "Zeitlimit (s)", "30", ParamWidget.NUMBER)
            }),

    // ═══════════════════════════════════════════════════════════
    // INTERAKTION (12)
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
    RECEIVE_KEY("Schluessel erhalten", "\uD83D\uDD11", 0xFF8E44AD, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("lock_id", "Schloss", "", ParamWidget.DROPDOWN_LOCK),
                    new ParamDef("npc_name", "Von NPC", "", ParamWidget.DROPDOWN_NPC_NAME)
            }),
    RECEIVE_CODE("Code erhalten", "\uD83D\uDD22", 0xFF9B59B6, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("lock_id", "Schloss", "", ParamWidget.DROPDOWN_LOCK),
                    new ParamDef("npc_name", "Von NPC", "", ParamWidget.DROPDOWN_NPC_NAME)
            }),
    SEARCH_AREA("Bereich durchsuchen", "?", 0xFFCA8A00, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("plot_id", "Bereich", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("clue_count", "Hinweise finden", "3", ParamWidget.NUMBER),
                    new ParamDef("time_limit", "Zeitlimit (s)", "120", ParamWidget.NUMBER)
            }),
    PICK_POCKET("Taschendiebstahl", "\uD83D\uDC5C", 0xFFC07000, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("npc_name", "Ziel-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    CALL_NPC("NPC anrufen", "\uD83D\uDCF1", 0xFFE5A800, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("dialog_id", "Dialog-ID", "call_default", ParamWidget.TEXT)
            }),
    REPAIR_ITEM("Item reparieren", "\uD83D\uDD27", 0xFFB35A00, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("item_id", "Item", "minecraft:iron_sword", ParamWidget.TEXT),
                    new ParamDef("plot_id", "Werkstatt", "", ParamWidget.DROPDOWN_PLOT)
            }),
    INSTALL_DEVICE("Geraet installieren", "\uD83D\uDCE1", 0xFF9E4B00, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("device_type", "Typ (WANZE/KAMERA/BOMBE)", "WANZE", ParamWidget.TEXT),
                    new ParamDef("plot_id", "Zielort", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
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
    ROB_CASINO("Kasino ausrauben", "\u265E", 0xFF7B241C, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("plot_id", "Kasino", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("difficulty", "Schwierigkeit", "4", ParamWidget.DROPDOWN_DIFFICULTY),
                    new ParamDef("time_limit", "Zeitlimit (s)", "300", ParamWidget.NUMBER)
            }),
    ROB_ARMORED_CAR("Geldtransporter ueberfallen", "\u26A0", 0xFF6E2C0A, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("vehicle_type", "Transporter-Typ", "LKW", ParamWidget.DROPDOWN_VEHICLE),
                    new ParamDef("difficulty", "Schwierigkeit", "4", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    ROB_PHARMACY("Apotheke ausrauben", "+", 0xFF78281F, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("plot_id", "Apotheke", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY),
                    new ParamDef("time_limit", "Zeitlimit (s)", "90", ParamWidget.NUMBER)
            }),
    TAKE_HOSTAGE("Geisel nehmen", "\u2620", 0xFF641E16, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("npc_name", "Geisel-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("ransom", "Loesegeld", "10000", ParamWidget.NUMBER)
            }),
    BLOW_WALL("Wand sprengen", "\uD83D\uDCA5", 0xFF4A235A, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("plot_id", "Ziel", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("explosive_amount", "Sprengstoff-Menge", "1", ParamWidget.NUMBER)
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
    HEAL_TO_FULL("Vollstaendig heilen", "+", 0xFF8E44AD, Category.UEBERLEBEN,
            new ParamDef[]{
                    new ParamDef("min_hearts", "Mind. Herzen", "10", ParamWidget.NUMBER)
            }),
    AVOID_DETECTION("Unentdeckt bleiben", "\uD83D\uDC41", 0xFF6C3483, Category.UEBERLEBEN,
            new ParamDef[]{
                    new ParamDef("duration", "Dauer (s)", "60", ParamWidget.NUMBER),
                    new ParamDef("plot_id", "Gebiet", "", ParamWidget.DROPDOWN_PLOT)
            }),
    SURVIVE_WITH_HP("Mit HP ueberleben", "\u2665", 0xFF5B2C6F, Category.UEBERLEBEN,
            new ParamDef[]{
                    new ParamDef("min_hp_percent", "Mind. HP (%)", "50", ParamWidget.NUMBER),
                    new ParamDef("duration", "Dauer (s)", "120", ParamWidget.NUMBER)
            }),
    TAKE_COVER("Deckung nehmen", "\u26F4", 0xFF4A235A, Category.UEBERLEBEN,
            new ParamDef[]{
                    new ParamDef("plot_id", "Deckungsort", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("duration", "Dauer (s)", "30", ParamWidget.NUMBER)
            }),
    OUTRUN_VEHICLE("Verfolger abschuetteln", "\uD83C\uDFC3", 0xFF76448A, Category.UEBERLEBEN,
            new ParamDef[]{
                    new ParamDef("vehicle_type", "Verfolger-Typ", "AUTO", ParamWidget.DROPDOWN_VEHICLE),
                    new ParamDef("time_limit", "Zeitlimit (s)", "90", ParamWidget.NUMBER)
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
    KILL_WITH_WEAPON("Mit Waffe toeten", "W", 0xFF273746, Category.KAMPF,
            new ParamDef[]{
                    new ParamDef("weapon_id", "Waffe (Item-ID)", "schedulemc:pistol", ParamWidget.TEXT),
                    new ParamDef("count", "Anzahl", "5", ParamWidget.NUMBER)
            }),
    HEADSHOT("Kopfschuesse erzielen", "\uD83C\uDFAF", 0xFF212F3D, Category.KAMPF,
            new ParamDef[]{
                    new ParamDef("count", "Anzahl", "3", ParamWidget.NUMBER)
            }),
    MELEE_FIGHT("Nahkampf gewinnen", "\u2694", 0xFF1C2833, Category.KAMPF,
            new ParamDef[]{
                    new ParamDef("entity_type", "Gegner-Typ", "zombie", ParamWidget.DROPDOWN_ENTITY),
                    new ParamDef("count", "Anzahl", "5", ParamWidget.NUMBER)
            }),
    SUPPRESS_AREA("Gebiet niederhaltend beschiessen", "~", 0xFF17202A, Category.KAMPF,
            new ParamDef[]{
                    new ParamDef("plot_id", "Gebiet", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("duration", "Dauer (s)", "60", ParamWidget.NUMBER)
            }),
    CAPTURE_ENEMY("Gegner gefangen nehmen", "\u26D3", 0xFF1A252F, Category.KAMPF,
            new ParamDef[]{
                    new ParamDef("npc_name", "Ziel-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
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
    INVEST_MONEY("Geld investieren", "\u25B2", 0xFFB7950B, Category.WIRTSCHAFT,
            new ParamDef[]{
                    new ParamDef("plot_id", "Unternehmen", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("amount", "Investition", "5000", ParamWidget.NUMBER)
            }),
    COLLECT_RENT("Miete kassieren", "\uD83C\uDFE0", 0xFFA0840A, Category.WIRTSCHAFT,
            new ParamDef[]{
                    new ParamDef("plot_id", "Immobilie", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("expected_amount", "Erwarteter Betrag", "1000", ParamWidget.NUMBER)
            }),
    BRIBE_NPC("NPC bestechen", "\uD83D\uDCB0", 0xFF8A7308, Category.WIRTSCHAFT,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("bribe_amount", "Bestechungsgeld", "2000", ParamWidget.NUMBER)
            }),
    NEGOTIATE("Verhandeln", "\uD83E\uDD1D", 0xFF756207, Category.WIRTSCHAFT,
            new ParamDef[]{
                    new ParamDef("npc_name", "Verhandlungspartner", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("target_price", "Zielpreis", "5000", ParamWidget.NUMBER),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    EXTORT_BUSINESS("Betrieb erpressen", "\u2762", 0xFF5E5005, Category.WIRTSCHAFT,
            new ParamDef[]{
                    new ParamDef("plot_id", "Betrieb", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("demand_amount", "Forderung", "3000", ParamWidget.NUMBER),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
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
    SET_FLAG("Missions-Flag setzen", "\u2691", 0xFF7B7D7D, Category.SONSTIGES,
            new ParamDef[]{
                    new ParamDef("flag_key", "Flag-Name", "custom_flag", ParamWidget.TEXT),
                    new ParamDef("flag_value", "Wert", "1", ParamWidget.TEXT)
            }),
    RADIO_CALL("Funk-Anruf", "\uD83D\uDCFB", 0xFF717D7E, Category.SONSTIGES,
            new ParamDef[]{
                    new ParamDef("caller", "Anrufer-Name", "Boss", ParamWidget.TEXT),
                    new ParamDef("message", "Nachricht", "Gut gemacht.", ParamWidget.TEXT)
            }),
    TAKE_PHOTO("Foto aufnehmen", "\uD83D\uDCF7", 0xFF616A6B, Category.SONSTIGES,
            new ParamDef[]{
                    new ParamDef("target", "Ziel (NPC/Plot/Text)", "", ParamWidget.TEXT),
                    new ParamDef("radius", "Maximale Entfernung", "10", ParamWidget.NUMBER)
            }),
    TRIGGER_ALARM("Alarm ausloesen", "\uD83D\uDEA8", 0xFF515A5A, Category.SONSTIGES,
            new ParamDef[]{
                    new ParamDef("plot_id", "Gebaeude", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("alarm_type", "Typ (POLIZEI/PRIVAT/BRAND)", "POLIZEI", ParamWidget.TEXT)
            }),
    UNLOCK_DOOR("Tuer oeffnen", "\uD83D\uDEAA", 0xFF424949, Category.SONSTIGES,
            new ParamDef[]{
                    new ParamDef("lock_id", "Schloss", "", ParamWidget.DROPDOWN_LOCK),
                    new ParamDef("method", "Methode (CODE/SCHLUESSEL/HACK)", "CODE", ParamWidget.TEXT)
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
    ESCAPE_IN_VEHICLE("Im Fahrzeug fliehen", "\uD83D\uDE97", 0xFF1565C0, Category.FAHRZEUG,
            new ParamDef[]{
                    new ParamDef("vehicle_type", "Fahrzeug", "AUTO", ParamWidget.DROPDOWN_VEHICLE),
                    new ParamDef("plot_id", "Fluchtort", "", ParamWidget.DROPDOWN_PLOT)
            }),
    PARK_VEHICLE("Fahrzeug parken", "P", 0xFF1451A8, Category.FAHRZEUG,
            new ParamDef[]{
                    new ParamDef("vehicle_type", "Fahrzeug", "AUTO", ParamWidget.DROPDOWN_VEHICLE),
                    new ParamDef("plot_id", "Parkplatz", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("radius", "Radius", "5", ParamWidget.NUMBER)
            }),
    HIJACK_VEHICLE("Fahrzeug kapern", "H", 0xFF123E88, Category.FAHRZEUG,
            new ParamDef[]{
                    new ParamDef("vehicle_type", "Fahrzeug", "AUTO", ParamWidget.DROPDOWN_VEHICLE),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    REPAIR_VEHICLE("Fahrzeug reparieren", "\uD83D\uDD27", 0xFF0F3470, Category.FAHRZEUG,
            new ParamDef[]{
                    new ParamDef("vehicle_type", "Fahrzeug", "AUTO", ParamWidget.DROPDOWN_VEHICLE),
                    new ParamDef("plot_id", "Werkstatt", "", ParamWidget.DROPDOWN_PLOT)
            }),
    FUEL_VEHICLE("Fahrzeug betanken", "\u26FD", 0xFF0D2A58, Category.FAHRZEUG,
            new ParamDef[]{
                    new ParamDef("vehicle_type", "Fahrzeug", "AUTO", ParamWidget.DROPDOWN_VEHICLE),
                    new ParamDef("plot_id", "Tankstelle", "", ParamWidget.DROPDOWN_PLOT)
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
    TAIL_NPC("NPC beschatten", "\uD83D\uDC64", 0xFF4D6570, Category.STEALTH,
            new ParamDef[]{
                    new ParamDef("npc_name", "Ziel-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("duration", "Dauer (s)", "120", ParamWidget.NUMBER),
                    new ParamDef("max_distance", "Max. Entfernung", "15", ParamWidget.NUMBER)
            }),
    HACK_CAMERA("Sicherheitskamera hacken", "\uD83D\uDCF9", 0xFF415A65, Category.STEALTH,
            new ParamDef[]{
                    new ParamDef("plot_id", "Gebaeude", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    PLANT_BUG("Wanze platzieren", "\uD83D\uDC1B", 0xFF364F58, Category.STEALTH,
            new ParamDef[]{
                    new ParamDef("plot_id", "Zielraum", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    AVOID_GUARDS("Waechter umgehen", "\uD83D\uDEAB", 0xFF293F4A, Category.STEALTH,
            new ParamDef[]{
                    new ParamDef("plot_id", "Gebiet", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("guard_count", "Waechteranzahl", "3", ParamWidget.NUMBER)
            }),
    SILENTLY_ELIMINATE("Lautlos ausschalten", "\uD83E\uDD35", 0xFF1C2A33, Category.STEALTH,
            new ParamDef[]{
                    new ParamDef("npc_name", "Ziel-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
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
            }),

    // ═══════════════════════════════════════════════════════════
    // STORY / SPIELER-MISSIONEN (7 Bloecke)
    // Konfigurationsbausteine fuer Spieler-Missionen (HAUPT/NEBEN).
    // Werden im Editor als normale Bloecke verwendet; beim Speichern
    // extrahiert der Server die Parameter in eine MissionDefinition.
    // ═══════════════════════════════════════════════════════════
    MISSION_INFO("Mission-Info", "\u24D8", 0xFF00BCD4, Category.STORY,
            new ParamDef[]{
                    new ParamDef("description", "Beschreibung", "Beschreibung hier...", ParamWidget.TEXT),
                    new ParamDef("npc_giver", "NPC-Geber", "", ParamWidget.DROPDOWN_NPC_NAME)
            }),
    MISSION_PREREQ("Voraussetzung", "\u2934", 0xFF009688, Category.STORY,
            new ParamDef[]{
                    new ParamDef("prereq_id", "Mission-ID", "", ParamWidget.TEXT)
            }),
    MISSION_TRACKING("Tracking-Event", "\u2197", 0xFF4CAF50, Category.STORY,
            new ParamDef[]{
                    new ParamDef("tracking_key", "Tracking-Key", "item_collected", ParamWidget.DROPDOWN_TRACKING_KEY),
                    new ParamDef("target_amount", "Ziel-Menge", "1", ParamWidget.NUMBER)
            }),
    NPC_GIVE_MISSION("Auftrag annehmen", "\uD83D\uDCCB", 0xFF26C6DA, Category.STORY,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("dialog_id", "Dialog-ID", "give_mission", ParamWidget.TEXT)
            }),
    NPC_COMPLETE_MISSION("Auftrag abgeben", "\u2714", 0xFF00ACC1, Category.STORY,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("dialog_id", "Dialog-ID", "complete_mission", ParamWidget.TEXT)
            }),
    MISSION_FAIL_COND("Abbruch-Bedingung", "\u2715", 0xFFEF5350, Category.STORY,
            new ParamDef[]{
                    new ParamDef("condition_key", "Bedingung", "player_died", ParamWidget.DROPDOWN_TRACKING_KEY),
                    new ParamDef("threshold", "Schwellenwert", "1", ParamWidget.NUMBER)
            }),
    PLAYER_NOTIFY("Hinweis anzeigen", "\uD83D\uDCAC", 0xFF80DEEA, Category.STORY,
            new ParamDef[]{
                    new ParamDef("text", "Text", "Hinweis...", ParamWidget.TEXT),
                    new ParamDef("color", "Farbe", "GELB", ParamWidget.DROPDOWN_COLOR),
                    new ParamDef("persistent", "Dauerhaft (0/1)", "0", ParamWidget.NUMBER)
            }),
    MISSION_TIMER("Zeitmessung", "\u23F1", 0xFF006064, Category.STORY,
            new ParamDef[]{
                    new ParamDef("time_limit", "Zeitlimit (s)", "300", ParamWidget.NUMBER),
                    new ParamDef("fail_message", "Fehlermeldung", "Zeit abgelaufen!", ParamWidget.TEXT)
            }),
    DIALOG_CHOICE("Dialog-Entscheidung", "\uD83D\uDCAC", 0xFF00838F, Category.STORY,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("question", "Frage", "Was moechtest du tun?", ParamWidget.TEXT),
                    new ParamDef("choice_a", "Option A", "Annehmen", ParamWidget.TEXT),
                    new ParamDef("choice_b", "Option B", "Ablehnen", ParamWidget.TEXT)
            }),
    MISSION_STAGE("Missionsabschnitt", "\u25B6\u25B6", 0xFF0097A7, Category.STORY,
            new ParamDef[]{
                    new ParamDef("stage_name", "Abschnittsname", "Teil 2", ParamWidget.TEXT),
                    new ParamDef("notify_player", "Spieler benachrichtigen (0/1)", "1", ParamWidget.NUMBER)
            }),
    GRANT_ITEM("Item vergeben", "\uD83C\uDF81", 0xFF00ACC1, Category.STORY,
            new ParamDef[]{
                    new ParamDef("item_id", "Item-ID", "minecraft:diamond", ParamWidget.TEXT),
                    new ParamDef("amount", "Menge", "1", ParamWidget.NUMBER),
                    new ParamDef("message", "Meldung", "Du hast etwas erhalten!", ParamWidget.TEXT)
            }),
    SET_OBJECTIVE_TEXT("Zieltext setzen", "\u270F", 0xFF26C6DA, Category.STORY,
            new ParamDef[]{
                    new ParamDef("objective_text", "Aufgabentext", "Gehe zum naechsten Ziel.", ParamWidget.TEXT)
            }),

    // ═══════════════════════════════════════════════════════════
    // MAFIA (8) – Unterwelt-Hierarchie & Organisationsaufgaben
    // ═══════════════════════════════════════════════════════════
    CONTACT_BOSS("Boss kontaktieren", "\uD83D\uDC51", 0xFFAD1457, Category.MAFIA,
            new ParamDef[]{
                    new ParamDef("npc_name", "Boss-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("dialog_id", "Dialog-ID", "orders", ParamWidget.TEXT)
            }),
    GANG_MEETING("Gang-Treffen", "\uD83E\uDD1C", 0xFF9C1151, Category.MAFIA,
            new ParamDef[]{
                    new ParamDef("plot_id", "Treffpunkt", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("attendees", "Mind. Teilnehmer", "3", ParamWidget.NUMBER)
            }),
    TERRITORY_CLAIM("Territorium beanspruchen", "\uD83C\uDFF4", 0xFF880E4F, Category.MAFIA,
            new ParamDef[]{
                    new ParamDef("plot_id", "Gebiet", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("hold_time", "Haltezeit (s)", "300", ParamWidget.NUMBER)
            }),
    SEND_SOLDIER("Soldaten entsenden", "\uD83D\uDC82", 0xFF6A1140, Category.MAFIA,
            new ParamDef[]{
                    new ParamDef("npc_name", "Soldat-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("target_plot", "Ziel", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("task", "Aufgabe", "Bewachen", ParamWidget.TEXT)
            }),
    ELIMINATE_RIVAL("Rivalen ausschalten", "\u2620", 0xFF4A0E2E, Category.MAFIA,
            new ParamDef[]{
                    new ParamDef("npc_name", "Ziel-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("difficulty", "Schwierigkeit", "3", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    BRIBE_OFFICIAL("Beamten bestechen", "\uD83D\uDD25", 0xFF880E4F, Category.MAFIA,
            new ParamDef[]{
                    new ParamDef("npc_name", "Beamter-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("bribe_amount", "Bestechungsgeld", "5000", ParamWidget.NUMBER),
                    new ParamDef("favor", "Gegenleistung", "wanted_reduce", ParamWidget.TEXT)
            }),
    COLLECT_TRIBUTE("Tribut kassieren", "\uD83D\uDCB4", 0xFF9C1151, Category.MAFIA,
            new ParamDef[]{
                    new ParamDef("npc_name", "Tributaer-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("amount", "Betrag", "3000", ParamWidget.NUMBER)
            }),
    MAFIA_TRIAL("Mafia-Tribunal", "\u2696", 0xFFAD1457, Category.MAFIA,
            new ParamDef[]{
                    new ParamDef("plot_id", "Tribunalort", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("verdict", "Urteil (FREISPRUCH/STRAFE/TOD)", "STRAFE", ParamWidget.TEXT)
            }),

    // ═══════════════════════════════════════════════════════════
    // TECHNOLOGIE (8) – Digital-Kriminalitaet & Ueberwachung
    // ═══════════════════════════════════════════════════════════
    HACK_NETWORK("Netzwerk hacken", "\uD83D\uDD78", 0xFF006064, Category.TECHNOLOGIE,
            new ParamDef[]{
                    new ParamDef("target_system", "Zielsystem", "polizei_db", ParamWidget.TEXT),
                    new ParamDef("difficulty", "Schwierigkeit", "3", ParamWidget.DROPDOWN_DIFFICULTY),
                    new ParamDef("time_limit", "Zeitlimit (s)", "120", ParamWidget.NUMBER)
            }),
    CLONE_PHONE("Handy klonen", "\uD83D\uDCF2", 0xFF00575B, Category.TECHNOLOGIE,
            new ParamDef[]{
                    new ParamDef("npc_name", "Ziel-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    TRACK_DEVICE("Ortung einrichten", "\uD83D\uDCE1", 0xFF004D55, Category.TECHNOLOGIE,
            new ParamDef[]{
                    new ParamDef("target", "Ziel (NPC/Fahrzeug)", "", ParamWidget.TEXT),
                    new ParamDef("duration", "Tracking-Dauer (s)", "600", ParamWidget.NUMBER)
            }),
    INTERCEPT_CALL("Kommunikation abhoeren", "\uD83D\uDCDE", 0xFF00434E, Category.TECHNOLOGIE,
            new ParamDef[]{
                    new ParamDef("npc_name", "Ziel-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("info_type", "Gesuchte Info", "meeting_location", ParamWidget.TEXT)
            }),
    DECRYPT_FILE("Datei entschluesseln", "\uD83D\uDD13", 0xFF00575B, Category.TECHNOLOGIE,
            new ParamDef[]{
                    new ParamDef("file_id", "Datei-ID", "encrypted_data", ParamWidget.TEXT),
                    new ParamDef("difficulty", "Schwierigkeit", "4", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    INSTALL_MALWARE("Schadsoftware installieren", "\uD83D\uDDA5", 0xFF006064, Category.TECHNOLOGIE,
            new ParamDef[]{
                    new ParamDef("plot_id", "Zielsystem-Standort", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("malware_type", "Typ (SPYWARE/RANSOMWARE/BACKDOOR)", "BACKDOOR", ParamWidget.TEXT)
            }),
    DISABLE_SECURITY("Sicherheitssystem deaktivieren", "\uD83D\uDEAB", 0xFF004D55, Category.TECHNOLOGIE,
            new ParamDef[]{
                    new ParamDef("plot_id", "Gebaeude", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("duration", "Deaktivierungsdauer (s)", "120", ParamWidget.NUMBER)
            }),
    DRONE_RECON("Drohnen-Aufklaerung", "\uD83D\uDE81", 0xFF00696E, Category.TECHNOLOGIE,
            new ParamDef[]{
                    new ParamDef("plot_id", "Aufklaerungsgebiet", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("scan_time", "Scann-Dauer (s)", "60", ParamWidget.NUMBER)
            }),

    // ═══════════════════════════════════════════════════════════
    // SOZIALES (8) – Manipulation, Ruf & Beziehungen
    // ═══════════════════════════════════════════════════════════
    INTIMIDATE_NPC("NPC einschuechtern", "\uD83D\uDCAA", 0xFF4527A0, Category.SOZIALES,
            new ParamDef[]{
                    new ParamDef("npc_name", "Ziel-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("method", "Methode", "ABLENKUNG", ParamWidget.DROPDOWN_METHOD),
                    new ParamDef("goal", "Ziel (SCHWEIGEN/KOOPERATION/FLUCHT)", "KOOPERATION", ParamWidget.TEXT)
            }),
    BEFRIEND_NPC("NPC anfreunden", "\uD83E\uDD1D", 0xFF3D1A91, Category.SOZIALES,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("interactions", "Benoet. Interaktionen", "5", ParamWidget.NUMBER)
            }),
    SPREAD_RUMOR("Geruecht verbreiten", "\uD83D\uDDE3", 0xFF311782, Category.SOZIALES,
            new ParamDef[]{
                    new ParamDef("target_npc", "Ziel (ueber wen)", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("rumor_type", "Typ (VERRAT/SCHWAECHE/DIEBSTAHL)", "VERRAT", ParamWidget.TEXT),
                    new ParamDef("spread_count", "Anzahl NPCs informieren", "3", ParamWidget.NUMBER)
            }),
    BUILD_REPUTATION("Ruf aufbauen", "\u2B50", 0xFF4527A0, Category.SOZIALES,
            new ParamDef[]{
                    new ParamDef("plot_id", "Gebiet", "", ParamWidget.DROPDOWN_PLOT),
                    new ParamDef("rep_type", "Reputationsart (RESPEKT/ANGST/BELIEBT)", "RESPEKT", ParamWidget.TEXT),
                    new ParamDef("target_points", "Zielpunkte", "100", ParamWidget.NUMBER)
            }),
    BLACKMAIL_NPC("NPC erpressen", "\uD83D\uDD0D", 0xFF37219A, Category.SOZIALES,
            new ParamDef[]{
                    new ParamDef("npc_name", "Ziel-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("leverage", "Druckmittel (INFO/FOTO/ZEUGE)", "INFO", ParamWidget.TEXT),
                    new ParamDef("demand", "Forderung", "5000", ParamWidget.TEXT)
            }),
    RECRUIT_NPC("NPC rekrutieren", "\uD83D\uDC65", 0xFF3D1A91, Category.SOZIALES,
            new ParamDef[]{
                    new ParamDef("npc_name", "Kandidat-NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("offer", "Angebot (Geld/Schutz/Status)", "Geld", ParamWidget.TEXT)
            }),
    CONVINCE_NPC("NPC ueberzeugen", "\uD83D\uDCAC", 0xFF311782, Category.SOZIALES,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("argument", "Argument", "Zusammenarbeit lohnt sich.", ParamWidget.TEXT),
                    new ParamDef("difficulty", "Schwierigkeit", "2", ParamWidget.DROPDOWN_DIFFICULTY)
            }),
    WIN_TRUST("Vertrauen gewinnen", "\uD83E\uDD1C", 0xFF4527A0, Category.SOZIALES,
            new ParamDef[]{
                    new ParamDef("npc_name", "NPC/Gruppe", "", ParamWidget.DROPDOWN_NPC_NAME),
                    new ParamDef("tasks_needed", "Benoet. Aufgaben", "3", ParamWidget.NUMBER)
            }),

    // ═══════════════════════════════════════════════════════════
    // LOGIK (5) – Editor-Steuerung & Ablauf-Kontrolle
    // ═══════════════════════════════════════════════════════════
    COMMENT("Kommentar", "#", 0xFF546E7A, Category.LOGIK,
            new ParamDef[]{
                    new ParamDef("note", "Notiz", "Hier passiert...", ParamWidget.TEXT)
            }),
    CONDITION_BRANCH("Bedingungsverzweigung", "\u2194", 0xFF37474F, Category.LOGIK,
            new ParamDef[]{
                    new ParamDef("condition_key", "Bedingung", "player_died", ParamWidget.DROPDOWN_TRACKING_KEY),
                    new ParamDef("threshold", "Schwellenwert", "1", ParamWidget.NUMBER),
                    new ParamDef("next_if_true", "Naechstes-ID (wahr)", "", ParamWidget.TEXT),
                    new ParamDef("next_if_false", "Naechstes-ID (falsch)", "", ParamWidget.TEXT)
            }),
    LOOP_REPEAT("Schleife wiederholen", "\u21BA", 0xFF2E3F4A, Category.LOGIK,
            new ParamDef[]{
                    new ParamDef("target_id", "Wiederhole ab Block-ID", "", ParamWidget.TEXT),
                    new ParamDef("count", "Wiederholungen", "3", ParamWidget.NUMBER)
            }),
    PARALLEL_HINT("Parallel-Aufgabe", "\u2551", 0xFF263238, Category.LOGIK,
            new ParamDef[]{
                    new ParamDef("parallel_ids", "Block-IDs (kommagetrennt)", "", ParamWidget.TEXT),
                    new ParamDef("wait_all", "Alle abwarten (0/1)", "1", ParamWidget.NUMBER)
            }),
    SAVE_CHECKPOINT("Speicherpunkt", "\uD83D\uDCBE", 0xFF455A64, Category.LOGIK,
            new ParamDef[]{
                    new ParamDef("checkpoint_id", "Checkpoint-ID", "cp1", ParamWidget.TEXT)
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
    public ParamDef[] getParamDefs() { return paramDefs != null ? paramDefs.clone() : null; }

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
        DROPDOWN_COLOR,          // Nachrichtenfarbe
        DROPDOWN_LOCK,           // Tuer-Schloss aus Server-Daten (Lock-ID + Position)
        DROPDOWN_TRACKING_KEY    // Tracking-Schluessel fuer Spieler-Missionen
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
        SPEZIAL("Spezial", 0xFF2ECC71),
        STORY("Story / Spieler-Missionen", 0xFF00BCD4),
        MAFIA("Mafia / Organisation", 0xFFAD1457),
        TECHNOLOGIE("Technologie", 0xFF006064),
        SOZIALES("Soziales & Manipulation", 0xFF4527A0),
        LOGIK("Logik / Editor-Steuerung", 0xFF37474F);

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
