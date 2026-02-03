package de.rolandsw.schedulemc.economy;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import org.slf4j.Logger;

/**
 * EINHEITLICHE Preisberechnung für ALLE Produkte im gesamten Mod.
 *
 * Ersetzt die bisherigen zwei inkonsistenten Formeln:
 * - getBasePrice() = seedPrice / 10-20 (viel zu niedrig)
 * - calculatePrice() = seedPrice × 2.0-3.5 × quality × amount (zu hoch)
 *
 * NEUE EINHEITLICHE FORMEL:
 * finalPrice = referencePrice × qualityMultiplier × supplyDemandMultiplier
 *              × riskPremium × economyCycleMultiplier × eventMultiplier
 *
 * Der referencePrice wird aus dem Rohstoff-Invest + Arbeit + Marge berechnet
 * und ist so kalibriert, dass:
 * - Anfänger (Level 1-5) mit einfachen Produkten 50-80€/Tag verdienen
 * - Fortgeschrittene (Level 10-20) 150-300€/Tag
 * - Experten (Level 25-30) 400-800€/Tag
 * - Passive Einnahmen (Daily Reward, Gehalt) NICHT dominieren
 */
public class UnifiedPriceCalculator {

    private static final Logger LOGGER = LogUtils.getLogger();

    private UnifiedPriceCalculator() {
        // Utility class
    }

    // ═══════════════════════════════════════════════════════════
    // REFERENZ-PREISE (kalibriert auf Tagesgehalt-Basis)
    // ═══════════════════════════════════════════════════════════

    /**
     * Tages-Referenz-Gehalt: Das Einkommen das ein durchschnittlicher
     * Spieler pro Tag durch Arbeit verdienen sollte.
     * Alle Produktpreise sind relativ dazu kalibriert.
     */
    public static final double DAILY_REFERENCE_INCOME = 150.0;

    /**
     * Tägliche Grundkosten (Essen auf Hard-Difficulty).
     * Ein Spieler muss mindestens dies verdienen um zu überleben.
     */
    public static final double DAILY_FOOD_COST = 20.0;

    // ═══════════════════════════════════════════════════════════
    // HAUPT-PREISBERECHNUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den vereinheitlichten Verkaufspreis für ein Produkt.
     *
     * @param referencePrice          Basis-Referenzpreis des Produkts (aus Typ-Enum)
     * @param qualityMultiplier       Qualitäts-Multiplikator (aus Quality-Enum)
     * @param supplyDemandMultiplier  Angebot/Nachfrage-Multiplikator (aus MarketData)
     * @param riskMultiplier          Risiko-Aufschlag (aus RiskPremium, 1.0 für legal)
     * @param cycleMultiplier         Wirtschaftszyklus-Multiplikator (aus EconomyCycle)
     * @param eventMultiplier         Event-Multiplikator (aus PriceManager)
     * @param category                Item-Kategorie (für Preis-Grenzen)
     * @param amount                  Anzahl der Items
     * @return Finaler Preis (begrenzt durch PriceBounds)
     */
    public static double calculatePrice(double referencePrice, double qualityMultiplier,
                                         double supplyDemandMultiplier, double riskMultiplier,
                                         double cycleMultiplier, double eventMultiplier,
                                         ItemCategory category, int amount) {

        // Schritt 1: Basis-Preis mit Qualität
        double baseWithQuality = referencePrice * qualityMultiplier;

        // Schritt 2: Dynamische Multiplikatoren anwenden
        double dynamicPrice = baseWithQuality * supplyDemandMultiplier * cycleMultiplier * eventMultiplier;

        // Schritt 3: Risiko-Aufschlag (nur für illegale Produkte)
        double withRisk = dynamicPrice * riskMultiplier;

        // Schritt 4: Preis-Grenzen anwenden (pro Einheit)
        double clampedUnitPrice = PriceBounds.clamp(withRisk, referencePrice, category);

        // Schritt 5: Auf Menge multiplizieren
        double totalPrice = clampedUnitPrice * amount;

        LOGGER.debug("Price calc: ref={:.2f} × q={:.2f} × sd={:.2f} × risk={:.2f} × cycle={:.2f} × event={:.2f} = {:.2f} (×{} = {:.2f})",
                referencePrice, qualityMultiplier, supplyDemandMultiplier,
                riskMultiplier, cycleMultiplier, eventMultiplier,
                clampedUnitPrice, amount, totalPrice);

        return Math.max(ABSOLUTE_MIN_TOTAL, totalPrice);
    }

    /**
     * Vereinfachte Berechnung wenn nicht alle Multiplikatoren bekannt sind.
     * Nutzt Standardwerte für fehlende Multiplikatoren.
     */
    public static double calculateSimplePrice(double referencePrice, double qualityMultiplier,
                                               ItemCategory category, int amount) {
        return calculatePrice(referencePrice, qualityMultiplier, 1.0, 1.0, 1.0, 1.0, category, amount);
    }

    // ═══════════════════════════════════════════════════════════
    // REFERENZ-PREIS-BERECHNUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den Referenz-Preis für ein Produkt basierend auf:
     * - Rohstoff-Kosten (Samen, Chemikalien)
     * - Produktionszeit
     * - Erwartete Marge
     *
     * @param inputCostPerUnit  Kosten für Rohstoffe pro Einheit Endprodukt
     * @param productionTimeTicks Produktionszeit in Ticks
     * @param targetMarginPercent Ziel-Gewinnmarge in Prozent (z.B. 100 = 100% Gewinn)
     * @return Referenz-Preis pro Einheit
     */
    public static double calculateReferencePrice(double inputCostPerUnit, int productionTimeTicks,
                                                  double targetMarginPercent) {
        // Zeitwert: Längere Produktion = höherer Preis
        // 1 MC-Tag = 24000 Ticks = ~20 Min Echtzeit
        double dayFraction = productionTimeTicks / 24000.0;
        double timeValue = dayFraction * DAILY_REFERENCE_INCOME * 0.1; // 10% des Tageseinkommens pro Tag Produktionszeit

        // Rohstoff-Kosten + Zeitwert + Marge
        double costBasis = inputCostPerUnit + timeValue;
        double withMargin = costBasis * (1.0 + targetMarginPercent / 100.0);

        return Math.max(ABSOLUTE_MIN_TOTAL, withMargin);
    }

    /**
     * Berechnet den Referenz-Preis für Drogen mit Potenz-System.
     * Berücksichtigt die zusätzliche Potenz als Preisfaktor.
     *
     * @param inputCostPerUnit    Rohstoff-Kosten pro Einheit
     * @param productionTimeTicks Produktionszeit
     * @param potencyMultiplier   Potenz-Multiplikator (1.0 = Standard)
     * @param targetMarginPercent Ziel-Gewinnmarge
     * @return Referenz-Preis pro Einheit
     */
    public static double calculateDrugReferencePrice(double inputCostPerUnit, int productionTimeTicks,
                                                      double potencyMultiplier, double targetMarginPercent) {
        double baseRef = calculateReferencePrice(inputCostPerUnit, productionTimeTicks, targetMarginPercent);
        return baseRef * potencyMultiplier;
    }

    /**
     * Berechnet den Referenz-Preis für gealterte Produkte (Wein, Käse, etc.).
     * Alterung erhöht den Wert.
     *
     * @param baseReferencePrice  Basis-Referenzpreis
     * @param ageMultiplier       Alterungs-Multiplikator (aus AgeLevel-Enum)
     * @param processingMultiplier Verarbeitungs-Multiplikator (aus ProcessingMethod-Enum)
     * @return Referenz-Preis mit Alterung und Verarbeitung
     */
    public static double calculateAgedProductPrice(double baseReferencePrice, double ageMultiplier,
                                                    double processingMultiplier) {
        return baseReferencePrice * ageMultiplier * processingMultiplier;
    }

    // ═══════════════════════════════════════════════════════════
    // EINKAUFS-PREIS (was Spieler für Rohstoffe zahlen)
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den Einkaufspreis für Samen/Rohstoffe/Maschinen.
     * Einkaufspreise haben weniger Volatilität als Verkaufspreise.
     *
     * @param baseShopPrice          Basis-Shop-Preis
     * @param supplyDemandMultiplier S&D Multiplikator (gedämpft)
     * @param cycleMultiplier        Wirtschaftszyklus
     * @param category               Item-Kategorie
     * @return Einkaufspreis
     */
    public static double calculateBuyPrice(double baseShopPrice, double supplyDemandMultiplier,
                                            double cycleMultiplier, ItemCategory category) {
        // Einkaufspreise sind weniger volatil (50% der S&D-Auswirkung)
        double dampedSD = 1.0 + (supplyDemandMultiplier - 1.0) * 0.5;
        double dampedCycle = 1.0 + (cycleMultiplier - 1.0) * 0.5;

        double price = baseShopPrice * dampedSD * dampedCycle;

        // Konfiszierungs-Risiko auf illegale Maschinen
        price *= RiskPremium.getConfiscationRiskMultiplier(category);

        return PriceBounds.clamp(price, baseShopPrice, category);
    }

    // ═══════════════════════════════════════════════════════════
    // GEHALT-BERECHNUNG (Harmonisiert mit Produktpreisen)
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet das harmonisierte Gehalt für einen Job.
     * Gehälter skalieren mit der Wirtschaft, aber gedämpft.
     *
     * @param baseSalary      Basis-Gehalt des Jobs
     * @param cycleMultiplier Wirtschaftszyklus-Multiplikator
     * @return Angepasstes Gehalt
     */
    public static double calculateSalary(double baseSalary, double cycleMultiplier) {
        // Gehälter schwanken nur mit 30% der Zyklusstärke
        double dampedCycle = 1.0 + (cycleMultiplier - 1.0) * 0.3;
        return Math.max(1.0, baseSalary * dampedCycle);
    }

    /**
     * Berechnet den harmonisierten Daily Reward.
     * Passive Einkommen werden reduziert wenn Produktion profitabel ist.
     *
     * @param baseReward      Basis Daily Reward
     * @param cycleMultiplier Wirtschaftszyklus
     * @return Angepasster Daily Reward
     */
    public static double calculateDailyReward(double baseReward, double cycleMultiplier) {
        // Passive Belohnungen schwanken nur minimal (20% der Zyklusstärke)
        double dampedCycle = 1.0 + (cycleMultiplier - 1.0) * 0.2;
        return Math.max(1.0, baseReward * dampedCycle);
    }

    // ═══════════════════════════════════════════════════════════
    // PROFIT-BERECHNUNG (für Spieler-Info)
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den erwarteten Profit pro Einheit nach Abzug aller Kosten.
     *
     * @param sellPrice    Verkaufspreis pro Einheit
     * @param inputCost    Rohstoff-Kosten pro Einheit
     * @param taxRate      Steuersatz (0.0-1.0)
     * @return Netto-Profit pro Einheit
     */
    public static double calculateNetProfit(double sellPrice, double inputCost, double taxRate) {
        double afterTax = sellPrice * (1.0 - taxRate);
        return afterTax - inputCost;
    }

    /**
     * Berechnet den erwarteten täglichen Profit einer Produktionskette.
     *
     * @param profitPerUnit     Profit pro Einheit
     * @param unitsPerDay       Einheiten pro Tag
     * @param maintenanceCostPerDay Tägliche Unterhaltskosten
     * @return Täglicher Netto-Profit
     */
    public static double calculateDailyProfit(double profitPerUnit, double unitsPerDay,
                                               double maintenanceCostPerDay) {
        return (profitPerUnit * unitsPerDay) - maintenanceCostPerDay;
    }

    // ═══════════════════════════════════════════════════════════
    // KONSTANTEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Absoluter Mindestpreis für jede Transaktion
     */
    private static final double ABSOLUTE_MIN_TOTAL = 0.01;

    /**
     * Steuersatz für legale Verkäufe (MwSt)
     */
    public static final double DEFAULT_TAX_RATE = 0.19;

    /**
     * Steuersatz für illegale Verkäufe (0 - keine Steuern im Schwarzmarkt)
     */
    public static final double ILLEGAL_TAX_RATE = 0.0;

    /**
     * Gibt den passenden Steuersatz für eine Kategorie zurück.
     */
    public static double getTaxRate(ItemCategory category) {
        return category.getTaxRate();
    }
}
