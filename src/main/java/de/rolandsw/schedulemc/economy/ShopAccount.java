package de.rolandsw.schedulemc.economy;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Shop-Konto mit 7-Tage-Umsatz-Tracking und Kontostand.
 *
 * - Akkumuliert Netto-Einnahmen (nach 19% MwSt) im Kontostand
 * - Zahlt Warehouse-Lieferkosten aus dem Kontostand
 * - Trackt 7-Tage-Einnahmen und -Ausgaben für Statistiken
 */
public class ShopAccount {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final String shopId;

    // === 7-TAGE-TRACKING ===
    // Optimierung: ArrayDeque statt LinkedList (schneller für 7-Tage-Historie)
    private final Deque<DailyRevenueRecord> revenueHistory = new ArrayDeque<>(7);
    private long currentDay = -1;
    @Nullable
    private DailyRevenueRecord todayRecord = null;

    // === SHOP-KONTOSTAND ===
    // Realer akkumulierter Netto-Umsatz.
    // Einnahmen (nach MwSt) fließen hier rein, Lieferkosten werden abgezogen.
    private int shopBalance = 0;

    public ShopAccount(String shopId) {
        this.shopId = shopId;
    }

    // ═══════════════════════════════════════════════════════════
    // TAGES-TRACKING
    // ═══════════════════════════════════════════════════════════

    private void updateDayIfNeeded(Level level) {
        long day = level.getDayTime() / 24000L;

        if (day != currentDay) {
            // Neuer Tag: Speichere alten Record
            if (todayRecord != null) {
                revenueHistory.add(todayRecord);

                // Nur letzten 7 Tage behalten
                while (revenueHistory.size() > 7) {
                    revenueHistory.removeFirst();
                }
            }

            // Neuer Record für heute
            currentDay = day;
            todayRecord = new DailyRevenueRecord(day, 0, 0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EINNAHMEN/AUSGABEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert Einnahmen (Verkaufserlös).
     * - 19% MwSt werden automatisch an die Staatskasse abgeführt
     * - Netto-Erlös fließt in den Shop-Kontostand
     */
    public void addRevenue(Level level, int amount, String source) {
        updateDayIfNeeded(level);

        // Berechne und ziehe MwSt ab
        double salesTaxRate = ModConfigHandler.COMMON.TAX_SALES_RATE.get();
        int salesTax = (int) (amount * salesTaxRate);
        int netRevenue = amount - salesTax;

        // Registriere Netto-Umsatz im Tracking
        todayRecord.addRevenue(netRevenue);

        // Netto-Erlös zum Shop-Kontostand addieren
        shopBalance += netRevenue;

        // Führe MwSt an Staatskasse ab
        if (salesTax > 0 && level.getServer() != null) {
            StateAccount.getInstance(level.getServer()).deposit(salesTax, "MwSt Shop " + shopId);
        }

        LOGGER.debug("Shop {}: +{}€ revenue ({}) - Sales tax: {}€, Net: {}€, Balance: {}€",
            shopId, amount, source, salesTax, netRevenue, shopBalance);
    }

    /**
     * Registriert Ausgaben im 7-Tage-Tracking (ohne den Kontostand zu ändern).
     * Für direkte Kontostand-Abzüge nutze {@link #payDeliveryCost(Level, int, String)}.
     */
    public void addExpense(Level level, int amount, String reason) {
        updateDayIfNeeded(level);
        todayRecord.addExpense(amount);
        LOGGER.debug("Shop {}: -{}€ expenses ({})", shopId, amount, reason);
    }

    /**
     * Zahlt Lieferkosten aus dem Shop-Kontostand.
     * Der Shop bezahlt so viel wie möglich, der Rest muss extern gedeckt werden.
     *
     * @param level  Welt-Instanz
     * @param amount Gesamtbetrag der Lieferkosten
     * @param reason Beschreibung der Ausgabe
     * @return Betrag der tatsächlich vom Shop bezahlt wurde (0 bis amount)
     */
    public int payDeliveryCost(Level level, int amount, String reason) {
        updateDayIfNeeded(level);

        int shopPays = Math.min(shopBalance, amount);

        if (shopPays > 0) {
            shopBalance -= shopPays;
            todayRecord.addExpense(shopPays);
            LOGGER.info("Shop {}: -{}€ Lieferkosten ({}) vom Kontostand, Rest-Balance: {}€",
                shopId, shopPays, reason, shopBalance);
        }

        return shopPays;
    }

    /**
     * Erstattet einen Betrag direkt auf den Shop-Kontostand zurück (ohne MwSt-Abzug).
     * Wird bei fehlgeschlagenen Lieferungen verwendet.
     *
     * @param level  Welt-Instanz
     * @param amount Erstattungsbetrag
     * @param reason Beschreibung
     */
    public void refundDeliveryCost(Level level, int amount, String reason) {
        updateDayIfNeeded(level);
        shopBalance += amount;
        // Expense-Tracking rückgängig machen
        todayRecord.addRevenue(amount);
        LOGGER.info("Shop {}: +{}€ Rückerstattung ({}), Balance: {}€", shopId, amount, reason, shopBalance);
    }

    /**
     * Gibt den aktuellen Shop-Kontostand zurück.
     */
    public int getShopBalance() {
        return shopBalance;
    }

    // ═══════════════════════════════════════════════════════════
    // 7-TAGE-NETTOUMSATZ
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet Nettoumsatz der letzten 7 Tage
     */
    public int get7DayNetRevenue() {
        int totalRevenue = 0;
        int totalExpenses = 0;

        // Summiere alle Records der letzten 7 Tage
        for (DailyRevenueRecord record : revenueHistory) {
            totalRevenue += record.getRevenue();
            totalExpenses += record.getExpenses();
        }

        // Inkludiere heute
        if (todayRecord != null) {
            totalRevenue += todayRecord.getRevenue();
            totalExpenses += todayRecord.getExpenses();
        }

        return Math.max(0, totalRevenue - totalExpenses);
    }

    public int get7DayRevenue() {
        int total = 0;
        for (DailyRevenueRecord record : revenueHistory) {
            total += record.getRevenue();
        }
        if (todayRecord != null) total += todayRecord.getRevenue();
        return total;
    }

    public int get7DayExpenses() {
        int total = 0;
        for (DailyRevenueRecord record : revenueHistory) {
            total += record.getExpenses();
        }
        if (todayRecord != null) total += todayRecord.getExpenses();
        return total;
    }

    public List<DailyRevenueRecord> getRevenueHistory() {
        return Collections.unmodifiableList(new ArrayList<>(revenueHistory));
    }

    @Nullable
    public DailyRevenueRecord getTodayRecord() {
        return todayRecord;
    }

    // ═══════════════════════════════════════════════════════════
    // TICK
    // ═══════════════════════════════════════════════════════════

    /**
     * Tick-Methode (wird von ShopAccountManager aufgerufen)
     */
    public void tick(Level level) {
        updateDayIfNeeded(level);
    }

    // ═══════════════════════════════════════════════════════════
    // INFO & UTILITY
    // ═══════════════════════════════════════════════════════════

    public String getShopId() {
        return shopId;
    }

    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("§e§l=== Shop: ").append(shopId).append(" ===§r\n");
        sb.append("§7Kontostand: §6§l").append(shopBalance).append("€§r\n");
        sb.append("§77-Tage-Einnahmen: §a").append(get7DayRevenue()).append("€\n");
        sb.append("§77-Tage-Ausgaben: §c").append(get7DayExpenses()).append("€\n");
        sb.append("§7Nettoumsatz: §6§l").append(get7DayNetRevenue()).append("€");

        return sb.toString();
    }
}
