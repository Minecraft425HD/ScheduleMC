package de.rolandsw.schedulemc.economy;

/**
 * Die 6 Phasen des Wirtschaftszyklus.
 *
 * Zyklus-Ablauf:
 * NORMAL → BOOM → UEBERHITZUNG → REZESSION → DEPRESSION → ERHOLUNG → NORMAL
 *
 * Jede Phase beeinflusst:
 * - Preis-Multiplikator (Verkaufspreise)
 * - Einkaufs-Multiplikator (Rohstoff/Maschinen-Kosten)
 * - Event-Wahrscheinlichkeit
 * - Spieler-Einkommen-Multiplikator (Gehälter, Daily Rewards)
 */
public enum EconomyCyclePhase {

    /**
     * Normalzustand - Alle Multiplikatoren bei 1.0
     * Dauer: 5-10 MC-Tage
     */
    NORMAL(
            "Normal", "§f", "→",
            1.0,   // Verkaufspreise
            1.0,   // Einkaufspreise
            1.0,   // Gehälter
            0.10,  // 10% Event-Chance
            5, 10  // Dauer: 5-10 MC-Tage
    ),

    /**
     * Boom - Preise steigen, Nachfrage hoch, gute Verdienstmöglichkeiten
     * Dauer: 3-7 MC-Tage
     */
    BOOM(
            "Boom", "§a", "↗",
            1.20,  // Verkaufspreise +20%
            1.10,  // Einkaufspreise +10%
            1.10,  // Gehälter +10%
            0.15,  // 15% Event-Chance (mehr Events)
            3, 7
    ),

    /**
     * Überhitzung - Preise extrem hoch, aber instabil
     * Hohe Gewinne, aber Crash-Risiko
     * Dauer: 2-4 MC-Tage
     */
    UEBERHITZUNG(
            "Überhitzung", "§6", "⚠",
            1.40,  // Verkaufspreise +40%
            1.25,  // Einkaufspreise +25%
            1.15,  // Gehälter +15%
            0.25,  // 25% Event-Chance (viele Events)
            2, 4
    ),

    /**
     * Rezession - Preise fallen, Nachfrage sinkt
     * Dauer: 3-6 MC-Tage
     */
    REZESSION(
            "Rezession", "§c", "↘",
            0.80,  // Verkaufspreise -20%
            0.90,  // Einkaufspreise -10%
            0.90,  // Gehälter -10%
            0.15,  // 15% Event-Chance
            3, 6
    ),

    /**
     * Depression - Tiefpunkt, Preise am niedrigsten
     * Guter Zeitpunkt zum Einkaufen/Investieren
     * Dauer: 2-5 MC-Tage
     */
    DEPRESSION(
            "Depression", "§4", "↓",
            0.60,  // Verkaufspreise -40%
            0.75,  // Einkaufspreise -25% (auch günstiger einkaufen!)
            0.80,  // Gehälter -20%
            0.20,  // 20% Event-Chance
            2, 5
    ),

    /**
     * Erholung - Preise steigen langsam wieder
     * Dauer: 3-6 MC-Tage
     */
    ERHOLUNG(
            "Erholung", "§e", "↗",
            0.90,  // Verkaufspreise -10% (steigend)
            0.95,  // Einkaufspreise -5%
            0.95,  // Gehälter -5%
            0.10,  // 10% Event-Chance
            3, 6
    );

    private final String displayName;
    private final String colorCode;
    private final String symbol;
    private final double sellPriceMultiplier;
    private final double buyPriceMultiplier;
    private final double salaryMultiplier;
    private final double eventChance;
    private final int minDurationDays;
    private final int maxDurationDays;

    EconomyCyclePhase(String displayName, String colorCode, String symbol,
                      double sellPriceMultiplier, double buyPriceMultiplier,
                      double salaryMultiplier, double eventChance,
                      int minDurationDays, int maxDurationDays) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.symbol = symbol;
        this.sellPriceMultiplier = sellPriceMultiplier;
        this.buyPriceMultiplier = buyPriceMultiplier;
        this.salaryMultiplier = salaryMultiplier;
        this.eventChance = eventChance;
        this.minDurationDays = minDurationDays;
        this.maxDurationDays = maxDurationDays;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getSellPriceMultiplier() {
        return sellPriceMultiplier;
    }

    public double getBuyPriceMultiplier() {
        return buyPriceMultiplier;
    }

    public double getSalaryMultiplier() {
        return salaryMultiplier;
    }

    public double getEventChance() {
        return eventChance;
    }

    public int getMinDurationDays() {
        return minDurationDays;
    }

    public int getMaxDurationDays() {
        return maxDurationDays;
    }

    /**
     * @return Die nächste Phase im Zyklus
     */
    public EconomyCyclePhase getNextPhase() {
        return switch (this) {
            case NORMAL -> BOOM;
            case BOOM -> UEBERHITZUNG;
            case UEBERHITZUNG -> REZESSION;
            case REZESSION -> DEPRESSION;
            case DEPRESSION -> ERHOLUNG;
            case ERHOLUNG -> NORMAL;
        };
    }

    /**
     * @return Formatierte Anzeige mit Farbe und Symbol
     */
    public String getFormattedName() {
        return colorCode + symbol + " " + displayName + "§r";
    }

    /**
     * @return true wenn dies eine positive Phase ist (gut für Verkäufe)
     */
    public boolean isPositive() {
        return this == BOOM || this == UEBERHITZUNG;
    }

    /**
     * @return true wenn dies eine negative Phase ist (Preise fallen)
     */
    public boolean isNegative() {
        return this == REZESSION || this == DEPRESSION;
    }
}
