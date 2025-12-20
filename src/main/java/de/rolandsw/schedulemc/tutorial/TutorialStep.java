package de.rolandsw.schedulemc.tutorial;

/**
 * Tutorial-Schritte für neue Spieler
 *
 * Jeder Schritt hat:
 * - Titel & Beschreibung
 * - Aufgaben zum Abschließen
 * - Belohnung bei Abschluss
 */
public enum TutorialStep {

    /**
     * Schritt 1: Willkommen
     * - Begrüßung des Spielers
     * - Übersicht über den Mod
     */
    WELCOME(
        "Willkommen",
        "§6Willkommen bei ScheduleMC!§r\n\n" +
        "§7Dieser Mod fügt ein umfassendes Wirtschafts-,\n" +
        "§7Produktions- und NPC-System hinzu.\n\n" +
        "§eDrücke §6T§e, um das Tutorial fortzusetzen.",
        50.0,
        new String[] {
            "Öffne das Tutorial-Menü"
        }
    ),

    /**
     * Schritt 2: Economy
     * - Geld-System verstehen
     * - Kontostand prüfen
     */
    ECONOMY(
        "Wirtschafts-System",
        "§6Geld verdienen & verwalten§r\n\n" +
        "§7• Nutze §6/money§7 um deinen Kontostand zu sehen\n" +
        "§7• Verdiene Geld durch Handel mit NPCs\n" +
        "§7• Verwalte dein Konto mit der Bank-App\n\n" +
        "§eAufgabe: Prüfe deinen Kontostand mit §6/money",
        100.0,
        new String[] {
            "Führe /money aus",
            "Öffne die Bank-App im Smartphone"
        }
    ),

    /**
     * Schritt 3: Plots
     * - Grundstücks-System verstehen
     * - Eigenen Plot kaufen
     */
    PLOTS(
        "Grundstücks-System",
        "§6Immobilien kaufen & verwalten§r\n\n" +
        "§7• Kaufe Plots mit §6/plot buy\n" +
        "§7• Verwalte Plots mit §6/plot info\n" +
        "§7• Plots schützen dein Eigentum\n\n" +
        "§eAufgabe: Kaufe deinen ersten Plot",
        500.0,
        new String[] {
            "Kaufe einen Plot",
            "Öffne die Plot-App im Smartphone"
        }
    ),

    /**
     * Schritt 4: Production
     * - Produktions-System verstehen
     * - Erste Pflanze anbauen
     */
    PRODUCTION(
        "Produktions-System",
        "§6Pflanzen anbauen & verarbeiten§r\n\n" +
        "§7• Baue Tabak, Cannabis, etc. an\n" +
        "§7• Verarbeite Rohstoffe zu Produkten\n" +
        "§7• Verkaufe Produkte an NPCs\n\n" +
        "§eAufgabe: Pflanze deine erste Pflanze",
        250.0,
        new String[] {
            "Pflanze eine Pflanze",
            "Warte bis sie gewachsen ist",
            "Ernte die Pflanze"
        }
    ),

    /**
     * Schritt 5: NPCs
     * - NPC-System verstehen
     * - Mit NPC handeln
     */
    NPC(
        "NPC-System",
        "§6NPCs & Handel§r\n\n" +
        "§7• NPCs haben Persönlichkeiten\n" +
        "§7• Preise variieren je nach Beziehung\n" +
        "§7• Baue Beziehungen auf durch Handel\n\n" +
        "§eAufgabe: Handle mit einem NPC",
        200.0,
        new String[] {
            "Finde einen NPC-Shop",
            "Kaufe oder verkaufe ein Item"
        }
    ),

    /**
     * Schritt 6: Smartphone
     * - Smartphone-System verstehen
     * - Apps nutzen
     */
    SMARTPHONE(
        "Smartphone-System",
        "§6Smartphone & Apps§r\n\n" +
        "§7• Drücke §6P§7 um das Smartphone zu öffnen\n" +
        "§7• Nutze Apps für Banking, Maps, etc.\n" +
        "§7• Bleib über Markt-Trends informiert\n\n" +
        "§eAufgabe: Öffne das Smartphone",
        150.0,
        new String[] {
            "Öffne das Smartphone (P)",
            "Öffne 3 verschiedene Apps"
        }
    ),

    /**
     * Schritt 7: Abschluss
     * - Tutorial abgeschlossen
     * - Große Belohnung
     */
    COMPLETION(
        "Tutorial abgeschlossen!",
        "§6§lGlückwunsch!§r\n\n" +
        "§7Du hast das Tutorial abgeschlossen!\n" +
        "§7Hier ist deine Belohnung:\n\n" +
        "§6+10,000€ Startgeld§r\n\n" +
        "§eViel Erfolg in ScheduleMC!",
        10000.0,
        new String[] {
            "Tutorial abgeschlossen"
        }
    );

    // ═══════════════════════════════════════════════════════════
    // PROPERTIES
    // ═══════════════════════════════════════════════════════════

    private final String title;
    private final String description;
    private final double reward;
    private final String[] tasks;

    TutorialStep(String title, String description, double reward, String[] tasks) {
        this.title = title;
        this.description = description;
        this.reward = reward;
        this.tasks = tasks;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getReward() {
        return reward;
    }

    public String[] getTasks() {
        return tasks;
    }

    /**
     * Nächster Schritt (null wenn letzter)
     */
    public TutorialStep getNext() {
        int nextOrdinal = this.ordinal() + 1;
        if (nextOrdinal < values().length) {
            return values()[nextOrdinal];
        }
        return null;
    }

    /**
     * Vorheriger Schritt (null wenn erster)
     */
    public TutorialStep getPrevious() {
        int prevOrdinal = this.ordinal() - 1;
        if (prevOrdinal >= 0) {
            return values()[prevOrdinal];
        }
        return null;
    }

    /**
     * Ist dies der erste Schritt?
     */
    public boolean isFirst() {
        return this == WELCOME;
    }

    /**
     * Ist dies der letzte Schritt?
     */
    public boolean isLast() {
        return this == COMPLETION;
    }

    /**
     * Fortschritt in Prozent
     */
    public int getProgressPercent() {
        return (int) ((this.ordinal() / (float) (values().length - 1)) * 100);
    }

    /**
     * Formatierte Anzeige
     */
    public String getFormattedTitle() {
        return String.format("§6§l[%d/%d] %s", this.ordinal() + 1, values().length, title);
    }

    @Override
    public String toString() {
        return String.format("TutorialStep[%s, reward=%.0f€]", title, reward);
    }
}
