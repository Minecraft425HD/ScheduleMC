package de.rolandsw.schedulemc.economy;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * Steuerdienst für Grundstückstransaktionen.
 *
 * Regeln:
 * - 19% MwSt. auf jeden Kaufvorgang (Käufer zahlt Kaufpreis + 19% obendrauf)
 * - 40% Spekulationssteuer wenn Weiterverkauf innerhalb von 7 Tagen nach Kauf
 * - Beim Abandon innerhalb von 7 Tagen: 40% des Spieler-Rückerstattungsanteils
 *   gehen zusätzlich an den Staat
 */
public class PlotTaxService {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Spekulationsfenster: 7 Tage in Millisekunden */
    public static final long SPECULATION_WINDOW_MS = 7L * 24 * 60 * 60 * 1000;

    /** Spekulationssteuersatz: 40% des vollen Verkaufspreises */
    public static final double SPECULATION_RATE = 0.40;

    private PlotTaxService() {}

    // ═══════════════════════════════════════════════════════════
    // KAUF-STEUERN
    // ═══════════════════════════════════════════════════════════

    /**
     * Ergebnis der Käufer-Kostenkalkulation.
     */
    public static final class BuyerCostResult {
        public final boolean success;
        public final double vatAmount;
        public final double totalPaid;

        private BuyerCostResult(boolean success, double vatAmount, double totalPaid) {
            this.success = success;
            this.vatAmount = vatAmount;
            this.totalPaid = totalPaid;
        }
    }

    /**
     * Zieht Kaufpreis + MwSt. vom Käufer ab und leitet die MwSt. an die Staatskasse weiter.
     * Der Netto-Kaufpreis (ohne MwSt.) bleibt dem Aufrufer zur Weiterleitung an Verkäufer/Staat.
     *
     * @param buyerUUID  UUID des Käufers
     * @param salePrice  Netto-Kaufpreis (ohne MwSt.)
     * @param plotName   Grundstücksname (für Buchungstext)
     * @return BuyerCostResult; success=false wenn Guthaben unzureichend (kein Geld wurde abgebucht)
     */
    public static BuyerCostResult applyBuyerCosts(UUID buyerUUID, double salePrice, String plotName) {
        double vatRate = ModConfigHandler.COMMON.TAX_SALES_RATE.get();
        double vatAmount = salePrice * vatRate;
        double totalCost = salePrice + vatAmount;

        int vatPercent = (int) Math.round(vatRate * 100);

        if (!EconomyManager.withdraw(buyerUUID, totalCost, TransactionType.PLOT_PURCHASE,
                "Grundstückskauf inkl. " + vatPercent + "% MwSt: " + plotName)) {
            return new BuyerCostResult(false, vatAmount, totalCost);
        }

        StateAccount.deposit((int) Math.round(vatAmount),
                "MwSt " + vatPercent + "% Grundstück: " + plotName);

        LOGGER.info("Plot VAT: {}€ ({}%) collected for '{}'", String.format("%.2f", vatAmount), vatPercent, plotName);
        return new BuyerCostResult(true, vatAmount, totalCost);
    }

    // ═══════════════════════════════════════════════════════════
    // SPEKULATIONSSTEUER (VERKAUF)
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob Spekulationssteuer anfällt und bucht sie ggf. vom Verkäufer.
     * Greift wenn der Verkäufer das Grundstück innerhalb von 7 Tagen weiterverkauft.
     *
     * @param sellerUUID   UUID des Verkäufers
     * @param purchaseTime Zeitpunkt des ursprünglichen Kaufs (Epoch-ms); 0 = unbekannt
     * @param salePrice    Verkaufspreis (Grundlage für 40%-Berechnung)
     * @param plotName     Grundstücksname (für Buchungstext)
     * @return Abgebuchter Steuerbetrag; 0 wenn keine Spekulationssteuer anfiel
     */
    public static double applySellerSpeculationTax(UUID sellerUUID, long purchaseTime,
                                                    double salePrice, String plotName) {
        if (!isInSpeculationWindow(purchaseTime)) return 0.0;

        double specTax = salePrice * SPECULATION_RATE;
        EconomyManager.withdraw(sellerUUID, specTax, TransactionType.TAX_SPECULATION,
                "Spekulationssteuer (40%): " + plotName);
        StateAccount.deposit((int) Math.round(specTax),
                "Spekulationssteuer 40%: " + plotName);

        LOGGER.info("Speculation tax: {}€ (40%) collected from {} for '{}'",
                String.format("%.2f", specTax), sellerUUID, plotName);
        return specTax;
    }

    // ═══════════════════════════════════════════════════════════
    // ABANDON-AUFTEILUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet die Geldaufteilung beim Abandon (Grundstück aufgeben).
     * Basis: 50% Spieler, 50% Staat.
     * Innerhalb von 7 Tagen: zusätzlich 40% des Spieleranteils an den Staat.
     *
     * @param purchaseTime  Zeitpunkt des Kaufs (Epoch-ms); 0 = unbekannt
     * @param originalPrice Basispreis des Grundstücks
     * @return double[]{playerShare, stateShare}
     */
    public static double[] calculateAbandonSplit(long purchaseTime, double originalPrice) {
        double playerShare = originalPrice * 0.5;
        double stateShare = originalPrice - playerShare;

        if (isInSpeculationWindow(purchaseTime)) {
            double specTax = playerShare * SPECULATION_RATE;
            playerShare -= specTax;
            stateShare += specTax;
        }

        return new double[]{playerShare, stateShare};
    }

    // ═══════════════════════════════════════════════════════════
    // HILFSMETHODEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt zurück ob ein Kaufzeitpunkt innerhalb des Spekulationsfensters (7 Tage) liegt.
     * Bei purchaseTime=0 (unbekannt/staatseigen) gilt das Fenster als nicht aktiv.
     */
    public static boolean isInSpeculationWindow(long purchaseTime) {
        return purchaseTime > 0
                && (System.currentTimeMillis() - purchaseTime) < SPECULATION_WINDOW_MS;
    }
}
