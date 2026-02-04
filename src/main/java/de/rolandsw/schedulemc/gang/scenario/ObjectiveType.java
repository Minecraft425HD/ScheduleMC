package de.rolandsw.schedulemc.gang.scenario;

/**
 * Alle verfuegbaren Baustein-Typen fuer den Szenario-Editor.
 *
 * Jeder Typ hat: Anzeigename, Symbol, Farbe, Kategorie, Parameter-Definitionen.
 * Die Parameter werden im Properties-Panel des Editors angezeigt.
 */
public enum ObjectiveType {

    // ═══════════════════════════════════════════════════════════
    // BEWEGUNG
    // ═══════════════════════════════════════════════════════════
    GOTO_LOCATION("Gehe zu Ort", "G", 0xFF3498DB, Category.BEWEGUNG,
            new ParamDef[]{
                    new ParamDef("x", "X", "0"),
                    new ParamDef("y", "Y", "64"),
                    new ParamDef("z", "Z", "0"),
                    new ParamDef("radius", "Radius", "5")
            }),
    GOTO_NPC("Gehe zu NPC", "N", 0xFF27AE60, Category.BEWEGUNG,
            new ParamDef[]{
                    new ParamDef("npc_type", "NPC-Typ", "SHOPKEEPER")
            }),

    // ═══════════════════════════════════════════════════════════
    // INTERAKTION
    // ═══════════════════════════════════════════════════════════
    INTERACT_NPC("NPC Gespraech", "I", 0xFFF1C40F, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("npc_type", "NPC-Typ", "SHOPKEEPER"),
                    new ParamDef("dialog_id", "Dialog-ID", "default")
            }),
    COLLECT_ITEMS("Items sammeln", "C", 0xFFE67E22, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("item_id", "Item", "minecraft:diamond"),
                    new ParamDef("amount", "Menge", "1")
            }),
    DELIVER_ITEM("Item liefern", "D", 0xFF8B6914, Category.INTERAKTION,
            new ParamDef[]{
                    new ParamDef("item_id", "Item", "minecraft:paper"),
                    new ParamDef("target_x", "Ziel-X", "0"),
                    new ParamDef("target_y", "Ziel-Y", "64"),
                    new ParamDef("target_z", "Ziel-Z", "0")
            }),

    // ═══════════════════════════════════════════════════════════
    // UEBERFALL
    // ═══════════════════════════════════════════════════════════
    ROB_STORE("Laden ueberfallen", "!", 0xFFE74C3C, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("difficulty", "Schwierigkeit", "1"),
                    new ParamDef("time_limit", "Zeitlimit (s)", "120")
            }),
    ROB_BANK("Bank ueberfallen", "B", 0xFFC0392B, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("difficulty", "Schwierigkeit", "3"),
                    new ParamDef("time_limit", "Zeitlimit (s)", "180")
            }),
    ROB_JEWELRY("Juwelier ueberfallen", "J", 0xFFAF7AC5, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("difficulty", "Schwierigkeit", "2"),
                    new ParamDef("time_limit", "Zeitlimit (s)", "150")
            }),
    ROB_GAS_STATION("Tankstelle ueberfallen", "T", 0xFFD35400, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("difficulty", "Schwierigkeit", "1"),
                    new ParamDef("time_limit", "Zeitlimit (s)", "90")
            }),
    CRACK_SAFE("Tresor knacken", "K", 0xFF7F8C8D, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("difficulty", "Schwierigkeit", "2"),
                    new ParamDef("time_limit", "Zeitlimit (s)", "60")
            }),
    HACK_SYSTEM("System hacken", "H", 0xFF2C3E50, Category.UEBERFALL,
            new ParamDef[]{
                    new ParamDef("difficulty", "Schwierigkeit", "2")
            }),

    // ═══════════════════════════════════════════════════════════
    // UEBERLEBEN
    // ═══════════════════════════════════════════════════════════
    SURVIVE_TIME("Zeit ueberleben", "Z", 0xFF9B59B6, Category.UEBERLEBEN,
            new ParamDef[]{
                    new ParamDef("duration", "Dauer (s)", "60")
            }),
    ESCAPE_ZONE("Fluchtzone", "F", 0xFF8E44AD, Category.UEBERLEBEN,
            new ParamDef[]{
                    new ParamDef("center_x", "Zentrum-X", "0"),
                    new ParamDef("center_y", "Zentrum-Y", "64"),
                    new ParamDef("center_z", "Zentrum-Z", "0"),
                    new ParamDef("radius", "Radius", "50")
            }),
    EVADE_POLICE("Polizei entkommen", "P", 0xFF1ABC9C, Category.UEBERLEBEN,
            new ParamDef[]{
                    new ParamDef("wanted_level", "Fahndungslevel", "2"),
                    new ParamDef("duration", "Dauer (s)", "120")
            }),

    // ═══════════════════════════════════════════════════════════
    // KAMPF
    // ═══════════════════════════════════════════════════════════
    KILL_MOBS("Gegner besiegen", "X", 0xFF34495E, Category.KAMPF,
            new ParamDef[]{
                    new ParamDef("entity_type", "Gegner-Typ", "zombie"),
                    new ParamDef("count", "Anzahl", "5")
            }),
    DEFEND_AREA("Gebiet verteidigen", "V", 0xFF196F3D, Category.KAMPF,
            new ParamDef[]{
                    new ParamDef("center_x", "Zentrum-X", "0"),
                    new ParamDef("center_y", "Zentrum-Y", "64"),
                    new ParamDef("center_z", "Zentrum-Z", "0"),
                    new ParamDef("duration", "Dauer (s)", "180")
            }),

    // ═══════════════════════════════════════════════════════════
    // WIRTSCHAFT
    // ═══════════════════════════════════════════════════════════
    EARN_MONEY("Geld verdienen", "$", 0xFFD4AC0D, Category.WIRTSCHAFT,
            new ParamDef[]{
                    new ParamDef("amount", "Betrag", "1000")
            }),
    SELL_ITEMS("Items verkaufen", "W", 0xFF17A589, Category.WIRTSCHAFT,
            new ParamDef[]{
                    new ParamDef("item_id", "Item", "minecraft:diamond"),
                    new ParamDef("amount", "Menge", "10")
            }),

    // ═══════════════════════════════════════════════════════════
    // SONSTIGES
    // ═══════════════════════════════════════════════════════════
    WAIT_TIME("Warten", ".", 0xFF95A5A6, Category.SONSTIGES,
            new ParamDef[]{
                    new ParamDef("duration", "Dauer (s)", "30")
            }),

    // ═══════════════════════════════════════════════════════════
    // SPEZIAL (nicht in Palette, automatisch im Editor)
    // ═══════════════════════════════════════════════════════════
    START("Start", "\u25B6", 0xFF2ECC71, Category.SPEZIAL, new ParamDef[]{}),
    REWARD("Belohnung", "\u2605", 0xFFFFD700, Category.SPEZIAL,
            new ParamDef[]{
                    new ParamDef("xp", "XP", "100"),
                    new ParamDef("money", "Geld", "5000")
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

    /**
     * Gibt den dunkleren Rand-Farbton zurueck.
     */
    public int getDarkerColor() {
        int r = Math.max(0, ((color >> 16) & 0xFF) - 40);
        int g = Math.max(0, ((color >> 8) & 0xFF) - 40);
        int b = Math.max(0, (color & 0xFF) - 40);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
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

    public record ParamDef(String key, String label, String defaultValue) {}
}
