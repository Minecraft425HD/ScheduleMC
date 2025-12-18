package de.rolandsw.schedulemc.economy;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Shop-Konto mit 7-Tage-Tracking und Aktien-System
 *
 * Vorschlag 2: Aktien-basiertes Investment
 * - 100 Aktien total
 * - Max 2 Aktionäre
 * - Gewinnausschüttung basierend auf 7-Tage-Nettoumsatz
 */
public class ShopAccount {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final String shopId;

    // === AKTIEN-SYSTEM ===
    private static final int TOTAL_SHARES = 100;
    private static final int MAX_SHAREHOLDERS = 2;
    // Optimierung: Initial capacity = MAX_SHAREHOLDERS (2)
    private final List<ShareHolder> shareholders = new ArrayList<>(MAX_SHAREHOLDERS);

    // === 7-TAGE-TRACKING ===
    // Optimierung: ArrayDeque statt LinkedList (schneller für 7-Tage-Historie)
    private final Deque<DailyRevenueRecord> revenueHistory = new ArrayDeque<>(7);
    private long currentDay = -1;
    @Nullable
    private DailyRevenueRecord todayRecord = null;

    // === PAYOUT ===
    private long lastPayoutDay = 0;
    private static final int PAYOUT_INTERVAL_DAYS = 7;

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
     * Registriert Einnahmen (Verkaufserlös)
     */
    public void addRevenue(Level level, int amount, String source) {
        updateDayIfNeeded(level);
        todayRecord.addRevenue(amount);
        LOGGER.debug("Shop {}: +{}€ Einnahmen ({})", shopId, amount, source);
    }

    /**
     * Registriert Ausgaben (Lieferkosten)
     */
    public void addExpense(Level level, int amount, String reason) {
        updateDayIfNeeded(level);
        todayRecord.addExpense(amount);
        LOGGER.debug("Shop {}: -{}€ Ausgaben ({})", shopId, amount, reason);
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
        // Optimierung: Keine Kopie, nutzt unmodifiable view
        return Collections.unmodifiableList(new ArrayList<>(revenueHistory));
    }

    @Nullable
    public DailyRevenueRecord getTodayRecord() {
        return todayRecord;
    }

    // ═══════════════════════════════════════════════════════════
    // AKTIEN-VERWALTUNG
    // ═══════════════════════════════════════════════════════════

    public int getAvailableShares() {
        int owned = shareholders.stream()
            .mapToInt(ShareHolder::getSharesOwned)
            .sum();
        return TOTAL_SHARES - owned;
    }

    public boolean canAddShareholder() {
        return shareholders.size() < MAX_SHAREHOLDERS;
    }

    public boolean hasShareholder(UUID playerUUID) {
        return shareholders.stream()
            .anyMatch(sh -> sh.getPlayerUUID().equals(playerUUID));
    }

    @Nullable
    public ShareHolder getShareholder(UUID playerUUID) {
        return shareholders.stream()
            .filter(sh -> sh.getPlayerUUID().equals(playerUUID))
            .findFirst()
            .orElse(null);
    }

    /**
     * Spieler kauft Aktien
     */
    public boolean purchaseShares(UUID playerUUID, String playerName, int shares, int totalCost) {
        if (shares <= 0 || shares > getAvailableShares()) {
            return false; // Nicht genug Aktien verfügbar
        }

        ShareHolder existing = getShareholder(playerUUID);

        if (existing != null) {
            // Spieler kauft mehr Aktien dazu
            existing.addShares(shares, totalCost);
            LOGGER.info("Aktionär {} kauft {} weitere Aktien (Gesamt: {})",
                playerName, shares, existing.getSharesOwned());
        } else {
            // Neuer Aktionär
            if (!canAddShareholder()) {
                return false; // Max 2 Aktionäre erreicht
            }

            ShareHolder newHolder = new ShareHolder(playerUUID, playerName, shares, totalCost);
            shareholders.add(newHolder);
            LOGGER.info("Neuer Aktionär: {} kauft {} Aktien ({}%)",
                playerName, shares, newHolder.getOwnershipPercentage());
        }

        return true;
    }

    /**
     * Spieler verkauft Aktien zurück
     */
    public int sellShares(UUID playerUUID, int shares) {
        ShareHolder holder = getShareholder(playerUUID);
        if (holder == null || holder.getSharesOwned() < shares) {
            return 0;
        }

        // Berechne Rückerstattung (75% des Kaufpreises)
        float pricePerShare = holder.getPurchasePrice() / (float)holder.getSharesOwned();
        int refund = (int)(pricePerShare * shares * 0.75f);

        holder.removeShares(shares);

        // Entferne Aktionär wenn keine Aktien mehr
        if (holder.getSharesOwned() == 0) {
            shareholders.remove(holder);
            LOGGER.info("Aktionär {} verkauft alle Aktien und verlässt Shop", holder.getPlayerName());
        } else {
            LOGGER.info("Aktionär {} verkauft {} Aktien (Verbleibend: {})",
                holder.getPlayerName(), shares, holder.getSharesOwned());
        }

        return refund;
    }

    public List<ShareHolder> getShareholders() {
        // Optimierung: Keine Kopie, nutzt unmodifiable view
        return Collections.unmodifiableList(shareholders);
    }

    // ═══════════════════════════════════════════════════════════
    // GEWINNAUSSCHÜTTUNG
    // ═══════════════════════════════════════════════════════════

    /**
     * Tick-Methode (wird von ShopAccountManager aufgerufen)
     */
    public void tick(Level level) {
        updateDayIfNeeded(level);

        long day = level.getDayTime() / 24000L;

        if (day - lastPayoutDay >= PAYOUT_INTERVAL_DAYS) {
            performPayout(level);
            lastPayoutDay = day;
        }
    }

    /**
     * Führt Gewinnausschüttung durch
     */
    private void performPayout(Level level) {
        if (shareholders.isEmpty()) {
            LOGGER.debug("Shop {}: Keine Aktionäre für Auszahlung", shopId);
            return;
        }

        int netRevenue = get7DayNetRevenue();

        if (netRevenue <= 0) {
            LOGGER.info("Shop {}: Kein Gewinn (7-Tage-Nettoumsatz: {}€)", shopId, netRevenue);

            // Benachrichtige Aktionäre
            for (ShareHolder holder : shareholders) {
                ServerPlayer player = level.getServer().getPlayerList()
                    .getPlayer(holder.getPlayerUUID());
                if (player != null) {
                    player.sendSystemMessage(
                        Component.literal("§c[Shop " + shopId + "] Kein Gewinn diese Woche. " +
                            "Nettoumsatz: " + netRevenue + "€")
                            .withStyle(ChatFormatting.RED)
                    );
                }
            }
            return;
        }

        LOGGER.info("Shop {}: Gewinnausschüttung! 7-Tage-Nettoumsatz: {}€", shopId, netRevenue);

        // Zahle jeden Aktionär basierend auf Shares
        for (ShareHolder holder : shareholders) {
            int payout = holder.calculatePayout(netRevenue);

            ServerPlayer player = level.getServer().getPlayerList()
                .getPlayer(holder.getPlayerUUID());

            if (player != null) {
                // Geld zum Konto hinzufügen
                EconomyManager.getInstance().deposit(holder.getPlayerUUID(), payout);

                player.sendSystemMessage(
                    Component.literal("§a§l[Shop Investment]§r\n" +
                        "§aGewinnausschüttung von Shop §e" + shopId + "§a!\n" +
                        "§7Deine Aktien: §e" + holder.getSharesOwned() + " §7(" +
                        String.format("%.1f", holder.getOwnershipPercentage()) + "%)\n" +
                        "§77-Tage-Nettoumsatz: §6" + netRevenue + "€\n" +
                        "§aDeine Auszahlung: §2§l" + payout + "€")
                        .withStyle(ChatFormatting.GREEN)
                );

                LOGGER.info("Aktionär {}: {}€ ausgezahlt ({}% = {} Aktien)",
                    holder.getPlayerName(), payout,
                    String.format("%.1f", holder.getOwnershipPercentage()),
                    holder.getSharesOwned());
            }
        }
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
        sb.append("§77-Tage-Einnahmen: §a").append(get7DayRevenue()).append("€\n");
        sb.append("§77-Tage-Ausgaben: §c").append(get7DayExpenses()).append("€\n");
        sb.append("§7Nettoumsatz: §6§l").append(get7DayNetRevenue()).append("€\n\n");

        sb.append("§e§lAktien:\n");
        sb.append("§7Verfügbar: §e").append(getAvailableShares()).append(" §7/ §e100\n");
        sb.append("§7Aktionäre: §e").append(shareholders.size()).append(" §7/ §e2\n\n");

        if (!shareholders.isEmpty()) {
            sb.append("§e§lAktionäre:\n");
            for (ShareHolder holder : shareholders) {
                sb.append("§7- §e").append(holder.getPlayerName())
                    .append("§7: §a").append(holder.getSharesOwned())
                    .append(" Aktien §7(")
                    .append(String.format("%.1f", holder.getOwnershipPercentage()))
                    .append("%)\n");
            }
        }

        sb.append("\n§7Nächste Auszahlung in: §e")
            .append(PAYOUT_INTERVAL_DAYS - (currentDay - lastPayoutDay))
            .append(" Tagen");

        return sb.toString();
    }
}
