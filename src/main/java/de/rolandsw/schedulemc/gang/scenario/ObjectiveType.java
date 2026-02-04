package de.rolandsw.schedulemc.gang.scenario;

/**
 * Alle verfuegbaren Baustein-Typen fuer den Szenario-Editor.
 *
 * Jeder Typ hat: Anzeigename, Symbol, Farbe, Kategorie, Parameter-Definitionen.
 * Jeder Parameter hat einen Widget-Typ der bestimmt wie er im Editor dargestellt wird:
 * TEXT, NUMBER, COORD, DROPDOWN_NPC_NAME, DROPDOWN_NPC_TYPE, DROPDOWN_MERCHANT_CAT,
 * DROPDOWN_BANK_CAT, DROPDOWN_SERVICE_CAT, DROPDOWN_PLOT, DROPDOWN_PLOT_TYPE,
 * DROPDOWN_DIFFICULTY, DROPDOWN_ENTITY
 */
public enum ObjectiveType {

    // ═══════════════════════════════════════════════════════════
    // BEWEGUNG
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

    // ═══════════════════════════════════════════════════════════
    // INTERAKTION
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

    // ═══════════════════════════════════════════════════════════
    // UEBERFALL
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

    // ═══════════════════════════════════════════════════════════
    // UEBERLEBEN
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

    // ═══════════════════════════════════════════════════════════
    // KAMPF
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

    // ═══════════════════════════════════════════════════════════
    // WIRTSCHAFT
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

    // ═══════════════════════════════════════════════════════════
    // SONSTIGES
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
        DROPDOWN_ENTITY          // Gegner-Typ
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
