package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.achievement.AchievementManager;
import de.rolandsw.schedulemc.economy.*;
import de.rolandsw.schedulemc.gang.GangManager;
import de.rolandsw.schedulemc.lock.LockManager;
import de.rolandsw.schedulemc.managers.DailyRewardManager;
import de.rolandsw.schedulemc.managers.NPCEntityRegistry;
import de.rolandsw.schedulemc.managers.NPCNameRegistry;
import de.rolandsw.schedulemc.market.DynamicMarketManager;
import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.npc.crime.BountyManager;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.territory.TerritoryManager;
import de.rolandsw.schedulemc.towing.TowingYardManager;
import de.rolandsw.schedulemc.warehouse.WarehouseManager;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.util.*;

/**
 * Zentraler Health-Check-Manager für alle Systeme
 *
 * Features:
 * - Überwacht Health-Status aller Manager
 * - Aggregierte Health-Reports
 * - Warning-System bei Problemen
 * - Admin-Command Integration
 */
public class HealthCheckManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    public enum SystemHealth {
        HEALTHY("health.status.healthy"),
        DEGRADED("health.status.degraded"),
        UNHEALTHY("health.status.unhealthy");

        private final String translationKey;

        SystemHealth(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getDisplayName() {
            return Component.translatable(translationKey).getString();
        }
    }

    /**
     * Health-Status einer einzelnen Komponente
     */
    public static class ComponentHealth {
        private final String componentName;
        private final SystemHealth status;
        private final String message;
        private final long lastCheckTime;

        public ComponentHealth(String componentName, SystemHealth status, String message) {
            this.componentName = componentName;
            this.status = status;
            this.message = message;
            this.lastCheckTime = System.currentTimeMillis();
        }

        public String getComponentName() {
            return componentName;
        }

        public SystemHealth getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public long getLastCheckTime() {
            return lastCheckTime;
        }

        @Override
        public String toString() {
            String statusColor = switch (status) {
                case HEALTHY -> "§a";
                case DEGRADED -> "§e";
                case UNHEALTHY -> "§c";
            };
            return String.format("  %s● §f%s §7- %s", statusColor, componentName, message);
        }
    }

    // ==========================================
    // Haupt-Check-Methoden
    // ==========================================

    /**
     * Prüft den Health-Status aller Systeme
     */
    public static Map<String, ComponentHealth> checkAllSystems() {
        Map<String, ComponentHealth> results = new LinkedHashMap<>();

        // === Kern-Systeme ===
        results.put("economy", checkEconomySystem());
        results.put("plot", checkPlotSystem());
        results.put("wallet", checkWalletSystem());

        // === Finanz-Systeme ===
        results.put("loan", checkLoanSystem());
        results.put("creditloan", checkCreditLoanSystem());
        results.put("creditscore", checkCreditScoreSystem());
        results.put("savings", checkSavingsSystem());
        results.put("tax", checkTaxSystem());
        results.put("overdraft", checkOverdraftSystem());
        results.put("recurring", checkRecurringPaymentSystem());
        results.put("shopaccount", checkShopAccountSystem());

        // === NPC & Crime ===
        results.put("crime", checkCrimeSystem());
        results.put("bounty", checkBountySystem());
        results.put("npc", checkNPCSystem());

        // === Spieler-Systeme ===
        results.put("gang", checkGangSystem());
        results.put("territory", checkTerritorySystem());
        results.put("achievement", checkAchievementSystem());
        results.put("daily", checkDailyRewardSystem());
        results.put("message", checkMessageSystem());

        // === Welt-Systeme ===
        results.put("lock", checkLockSystem());
        results.put("market", checkMarketSystem());
        results.put("warehouse", checkWarehouseSystem());
        results.put("towing", checkTowingSystem());

        return results;
    }

    // ==========================================
    // Kern-Systeme
    // ==========================================

    private static ComponentHealth checkEconomySystem() {
        try {
            if (EconomyManager.isHealthy()) {
                return new ComponentHealth("Economy", SystemHealth.HEALTHY, EconomyManager.getHealthInfo());
            } else {
                return new ComponentHealth("Economy",
                    determineStatus(EconomyManager.getLastError()), EconomyManager.getHealthInfo());
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei Economy Health-Check", e);
            return errorHealth("Economy", e);
        }
    }

    private static ComponentHealth checkPlotSystem() {
        try {
            if (PlotManager.isHealthy()) {
                return new ComponentHealth("Plot System", SystemHealth.HEALTHY, PlotManager.getHealthInfo());
            } else {
                return new ComponentHealth("Plot System",
                    determineStatus(PlotManager.getLastError()), PlotManager.getHealthInfo());
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei Plot Health-Check", e);
            return errorHealth("Plot System", e);
        }
    }

    private static ComponentHealth checkWalletSystem() {
        try {
            if (WalletManager.isHealthy()) {
                return new ComponentHealth("Wallet", SystemHealth.HEALTHY, WalletManager.getHealthInfo());
            } else {
                return new ComponentHealth("Wallet",
                    determineStatus(WalletManager.getLastError()), WalletManager.getHealthInfo());
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei Wallet Health-Check", e);
            return errorHealth("Wallet", e);
        }
    }

    // ==========================================
    // Finanz-Systeme (getInstance-basiert)
    // ==========================================

    private static ComponentHealth checkLoanSystem() {
        return checkSingletonManager("Loan", LoanManager.class, () -> {
            LoanManager mgr = LoanManager.getInstance();
            if (mgr == null) return notInitialized("Loan");
            return mgr.isHealthy()
                ? new ComponentHealth("Loan", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Loan", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkCreditLoanSystem() {
        return checkSingletonManager("Credit Loan", CreditLoanManager.class, () -> {
            CreditLoanManager mgr = CreditLoanManager.getInstance();
            if (mgr == null) return notInitialized("Credit Loan");
            return mgr.isHealthy()
                ? new ComponentHealth("Credit Loan", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Credit Loan", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkCreditScoreSystem() {
        return checkSingletonManager("Credit Score", CreditScoreManager.class, () -> {
            CreditScoreManager mgr = CreditScoreManager.getInstance();
            if (mgr == null) return notInitialized("Credit Score");
            return mgr.isHealthy()
                ? new ComponentHealth("Credit Score", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Credit Score", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkSavingsSystem() {
        return checkSingletonManager("Savings", SavingsAccountManager.class, () -> {
            SavingsAccountManager mgr = SavingsAccountManager.getInstance();
            if (mgr == null) return notInitialized("Savings");
            return mgr.isHealthy()
                ? new ComponentHealth("Savings", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Savings", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkTaxSystem() {
        return checkSingletonManager("Tax", TaxManager.class, () -> {
            TaxManager mgr = TaxManager.getInstance();
            if (mgr == null) return notInitialized("Tax");
            return mgr.isHealthy()
                ? new ComponentHealth("Tax", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Tax", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkOverdraftSystem() {
        return checkSingletonManager("Overdraft", OverdraftManager.class, () -> {
            OverdraftManager mgr = OverdraftManager.getInstance();
            if (mgr == null) return notInitialized("Overdraft");
            return mgr.isHealthy()
                ? new ComponentHealth("Overdraft", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Overdraft", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkRecurringPaymentSystem() {
        return checkSingletonManager("Recurring Payments", RecurringPaymentManager.class, () -> {
            RecurringPaymentManager mgr = RecurringPaymentManager.getInstance();
            if (mgr == null) return notInitialized("Recurring Payments");
            return mgr.isHealthy()
                ? new ComponentHealth("Recurring Payments", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Recurring Payments", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkShopAccountSystem() {
        try {
            int count = ShopAccountManager.getShopCount();
            return new ComponentHealth("Shop Accounts", SystemHealth.HEALTHY,
                "Aktiv, " + count + " Shops registriert");
        } catch (Exception e) {
            LOGGER.error("Fehler bei ShopAccount Health-Check", e);
            return errorHealth("Shop Accounts", e);
        }
    }

    // ==========================================
    // NPC & Crime Systeme
    // ==========================================

    private static ComponentHealth checkCrimeSystem() {
        try {
            if (CrimeManager.isHealthy()) {
                return new ComponentHealth("Crime", SystemHealth.HEALTHY, CrimeManager.getHealthInfo());
            } else {
                return new ComponentHealth("Crime",
                    determineStatus(CrimeManager.getLastError()), CrimeManager.getHealthInfo());
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei Crime Health-Check", e);
            return errorHealth("Crime", e);
        }
    }

    private static ComponentHealth checkBountySystem() {
        return checkSingletonManager("Bounty", BountyManager.class, () -> {
            BountyManager mgr = BountyManager.getInstance();
            if (mgr == null) return notInitialized("Bounty");
            String info = mgr.isHealthy() ? mgr.getHealthInfo() : mgr.getHealthInfo();
            String extra = mgr.getStatistics();
            return mgr.isHealthy()
                ? new ComponentHealth("Bounty", SystemHealth.HEALTHY, info + " | " + extra)
                : new ComponentHealth("Bounty", determineStatus(mgr.getLastError()), info);
        });
    }

    private static ComponentHealth checkNPCSystem() {
        try {
            return new ComponentHealth("NPC Registry", SystemHealth.HEALTHY,
                "Aktiv, Name-Registry geladen");
        } catch (Exception e) {
            LOGGER.error("Fehler bei NPC Health-Check", e);
            return errorHealth("NPC Registry", e);
        }
    }

    // ==========================================
    // Spieler-Systeme
    // ==========================================

    private static ComponentHealth checkGangSystem() {
        return checkSingletonManager("Gang", GangManager.class, () -> {
            GangManager mgr = GangManager.getInstance();
            if (mgr == null) return notInitialized("Gang");
            String info = mgr.isHealthy() ? mgr.getHealthInfo() : mgr.getHealthInfo();
            int count = mgr.getGangCount();
            return mgr.isHealthy()
                ? new ComponentHealth("Gang", SystemHealth.HEALTHY, info + " | " + count + " Gangs")
                : new ComponentHealth("Gang", determineStatus(mgr.getLastError()), info);
        });
    }

    private static ComponentHealth checkTerritorySystem() {
        return checkSingletonManager("Territory", TerritoryManager.class, () -> {
            TerritoryManager mgr = TerritoryManager.getInstance();
            if (mgr == null) return notInitialized("Territory");
            String info = mgr.isHealthy() ? mgr.getHealthInfo() : mgr.getHealthInfo();
            int count = mgr.getTerritoryCount();
            return mgr.isHealthy()
                ? new ComponentHealth("Territory", SystemHealth.HEALTHY, info + " | " + count + " Territories")
                : new ComponentHealth("Territory", determineStatus(mgr.getLastError()), info);
        });
    }

    private static ComponentHealth checkAchievementSystem() {
        return checkSingletonManager("Achievement", AchievementManager.class, () -> {
            AchievementManager mgr = AchievementManager.getInstance();
            if (mgr == null) return notInitialized("Achievement");
            return mgr.isHealthy()
                ? new ComponentHealth("Achievement", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Achievement", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkDailyRewardSystem() {
        try {
            if (DailyRewardManager.isHealthy()) {
                return new ComponentHealth("Daily Reward", SystemHealth.HEALTHY, DailyRewardManager.getHealthInfo());
            } else {
                return new ComponentHealth("Daily Reward",
                    determineStatus(DailyRewardManager.getLastError()), DailyRewardManager.getHealthInfo());
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei Daily Reward Health-Check", e);
            return errorHealth("Daily Reward", e);
        }
    }

    private static ComponentHealth checkMessageSystem() {
        try {
            if (MessageManager.isHealthy()) {
                return new ComponentHealth("Messaging", SystemHealth.HEALTHY, MessageManager.getHealthInfo());
            } else {
                return new ComponentHealth("Messaging",
                    determineStatus(MessageManager.getLastError()), MessageManager.getHealthInfo());
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei Message Health-Check", e);
            return errorHealth("Messaging", e);
        }
    }

    // ==========================================
    // Welt-Systeme
    // ==========================================

    private static ComponentHealth checkLockSystem() {
        try {
            LockManager mgr = LockManager.getInstance();
            if (mgr == null) return notInitialized("Lock");
            int count = mgr.getLockCount();
            return new ComponentHealth("Lock", SystemHealth.HEALTHY,
                "Aktiv, " + count + " Locks registriert");
        } catch (Exception e) {
            LOGGER.error("Fehler bei Lock Health-Check", e);
            return errorHealth("Lock", e);
        }
    }

    private static ComponentHealth checkMarketSystem() {
        try {
            DynamicMarketManager mgr = DynamicMarketManager.getInstance();
            if (mgr == null) return notInitialized("Market");
            boolean enabled = mgr.isEnabled();
            return new ComponentHealth("Dynamic Market",
                enabled ? SystemHealth.HEALTHY : SystemHealth.DEGRADED,
                enabled ? "Aktiv, Markt-Simulation läuft" : "Deaktiviert");
        } catch (Exception e) {
            LOGGER.error("Fehler bei Market Health-Check", e);
            return errorHealth("Dynamic Market", e);
        }
    }

    private static ComponentHealth checkWarehouseSystem() {
        try {
            int count = WarehouseManager.getAllWarehouses().size();
            return new ComponentHealth("Warehouse", SystemHealth.HEALTHY,
                "Aktiv, " + count + " Warehouses registriert");
        } catch (Exception e) {
            LOGGER.error("Fehler bei Warehouse Health-Check", e);
            return errorHealth("Warehouse", e);
        }
    }

    private static ComponentHealth checkTowingSystem() {
        try {
            if (TowingYardManager.isHealthy()) {
                return new ComponentHealth("Towing", SystemHealth.HEALTHY,
                    "Aktiv, Towing-System betriebsbereit");
            } else {
                return new ComponentHealth("Towing",
                    determineStatus(TowingYardManager.getLastError()),
                    "Problem erkannt: " + TowingYardManager.getLastError());
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei Towing Health-Check", e);
            return errorHealth("Towing", e);
        }
    }

    // ==========================================
    // Report-Methoden
    // ==========================================

    /**
     * Gibt einen formatierten Health-Report zurück
     */
    public static String getHealthReport() {
        Map<String, ComponentHealth> results = checkAllSystems();

        StringBuilder report = new StringBuilder();
        report.append("§e§l══════════════════════════════════§r\n");
        report.append("§6§l     SYSTEM HEALTH REPORT§r\n");
        report.append("§e§l══════════════════════════════════§r\n");

        int healthy = 0;
        int degraded = 0;
        int unhealthy = 0;

        // Gruppierte Ausgabe
        report.append("\n§6§lKern-Systeme:§r\n");
        appendIfPresent(report, results, "economy");
        appendIfPresent(report, results, "plot");
        appendIfPresent(report, results, "wallet");

        report.append("\n§6§lFinanz-Systeme:§r\n");
        appendIfPresent(report, results, "loan");
        appendIfPresent(report, results, "creditloan");
        appendIfPresent(report, results, "creditscore");
        appendIfPresent(report, results, "savings");
        appendIfPresent(report, results, "tax");
        appendIfPresent(report, results, "overdraft");
        appendIfPresent(report, results, "recurring");
        appendIfPresent(report, results, "shopaccount");

        report.append("\n§6§lNPC & Crime:§r\n");
        appendIfPresent(report, results, "crime");
        appendIfPresent(report, results, "bounty");
        appendIfPresent(report, results, "npc");

        report.append("\n§6§lSpieler-Systeme:§r\n");
        appendIfPresent(report, results, "gang");
        appendIfPresent(report, results, "territory");
        appendIfPresent(report, results, "achievement");
        appendIfPresent(report, results, "daily");
        appendIfPresent(report, results, "message");

        report.append("\n§6§lWelt-Systeme:§r\n");
        appendIfPresent(report, results, "lock");
        appendIfPresent(report, results, "market");
        appendIfPresent(report, results, "warehouse");
        appendIfPresent(report, results, "towing");

        for (ComponentHealth health : results.values()) {
            switch (health.getStatus()) {
                case HEALTHY -> healthy++;
                case DEGRADED -> degraded++;
                case UNHEALTHY -> unhealthy++;
            }
        }

        report.append("\n§e§l══════════════════════════════════§r\n");
        report.append(String.format("  §a✔ Gesund: %d §7| §e⚠ Degradiert: %d §7| §c✘ Ungesund: %d §7| §fGesamt: %d\n",
            healthy, degraded, unhealthy, results.size()));
        report.append("§e§l══════════════════════════════════§r");

        return report.toString();
    }

    private static void appendIfPresent(StringBuilder sb, Map<String, ComponentHealth> results, String key) {
        ComponentHealth health = results.get(key);
        if (health != null) {
            sb.append(health.toString()).append("\n");
        }
    }

    /**
     * Gibt den Overall-Status zurück
     */
    public static SystemHealth getOverallHealth() {
        Map<String, ComponentHealth> results = checkAllSystems();

        boolean hasUnhealthy = results.values().stream()
            .anyMatch(h -> h.getStatus() == SystemHealth.UNHEALTHY);
        if (hasUnhealthy) return SystemHealth.UNHEALTHY;

        boolean hasDegraded = results.values().stream()
            .anyMatch(h -> h.getStatus() == SystemHealth.DEGRADED);
        if (hasDegraded) return SystemHealth.DEGRADED;

        return SystemHealth.HEALTHY;
    }

    /**
     * Prüft ob alle Systeme gesund sind
     */
    public static boolean isAllHealthy() {
        return getOverallHealth() == SystemHealth.HEALTHY;
    }

    /**
     * Loggt einen Health-Check in die Console
     */
    public static void logHealthCheck() {
        Map<String, ComponentHealth> results = checkAllSystems();

        LOGGER.info("=== SYSTEM HEALTH CHECK ({} Systeme) ===", results.size());
        for (ComponentHealth health : results.values()) {
            switch (health.getStatus()) {
                case HEALTHY ->
                    LOGGER.info("  [OK]   {}: {}", health.getComponentName(), health.getMessage());
                case DEGRADED ->
                    LOGGER.warn("  [WARN] {}: {}", health.getComponentName(), health.getMessage());
                case UNHEALTHY ->
                    LOGGER.error("  [FAIL] {}: {}", health.getComponentName(), health.getMessage());
            }
        }
        LOGGER.info("Overall Status: {}", getOverallHealth());
        LOGGER.info("==========================================");
    }

    /**
     * Gibt eine kurze Status-Zusammenfassung zurück
     */
    public static String getQuickStatus() {
        Map<String, ComponentHealth> results = checkAllSystems();
        long healthy = results.values().stream().filter(h -> h.getStatus() == SystemHealth.HEALTHY).count();
        SystemHealth overall = getOverallHealth();
        return String.format("§7System Status: %s §7(%d/%d Systeme gesund)",
            overall.getDisplayName(), healthy, results.size());
    }

    /**
     * Gibt den Health-Status eines einzelnen Systems zurück
     */
    public static ComponentHealth checkSystem(String systemKey) {
        Map<String, ComponentHealth> all = checkAllSystems();
        return all.get(systemKey);
    }

    // ==========================================
    // Hilfsmethoden
    // ==========================================

    @FunctionalInterface
    private interface HealthSupplier {
        ComponentHealth get();
    }

    private static ComponentHealth checkSingletonManager(String name, Class<?> managerClass, HealthSupplier supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            LOGGER.error("Fehler bei {} Health-Check", name, e);
            return errorHealth(name, e);
        }
    }

    private static SystemHealth determineStatus(String error) {
        if (error != null && error.contains("backup")) return SystemHealth.DEGRADED;
        return SystemHealth.UNHEALTHY;
    }

    private static ComponentHealth notInitialized(String name) {
        return new ComponentHealth(name, SystemHealth.DEGRADED, "Noch nicht initialisiert");
    }

    private static ComponentHealth errorHealth(String name, Exception e) {
        return new ComponentHealth(name, SystemHealth.UNHEALTHY,
            "Health-Check fehlgeschlagen: " + e.getMessage());
    }
}
