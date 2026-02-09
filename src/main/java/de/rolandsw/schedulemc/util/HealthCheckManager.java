package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.achievement.AchievementManager;
import de.rolandsw.schedulemc.economy.*;
import de.rolandsw.schedulemc.gang.GangManager;
import de.rolandsw.schedulemc.gang.mission.GangMissionManager;
import de.rolandsw.schedulemc.gang.scenario.ScenarioManager;
import de.rolandsw.schedulemc.lock.LockManager;
import de.rolandsw.schedulemc.managers.DailyRewardManager;
import de.rolandsw.schedulemc.market.DynamicMarketManager;
import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.npc.crime.BountyManager;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.crime.prison.PrisonManager;
import de.rolandsw.schedulemc.npc.life.companion.CompanionManager;
import de.rolandsw.schedulemc.npc.life.dialogue.DialogueManager;
import de.rolandsw.schedulemc.npc.life.economy.DynamicPriceManager;
import de.rolandsw.schedulemc.npc.life.quest.QuestManager;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import de.rolandsw.schedulemc.npc.life.social.NPCInteractionManager;
import de.rolandsw.schedulemc.npc.life.witness.WitnessManager;
import de.rolandsw.schedulemc.npc.life.world.WorldEventManager;
import de.rolandsw.schedulemc.npc.personality.NPCRelationshipManager;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.territory.TerritoryManager;
import de.rolandsw.schedulemc.towing.TowingYardManager;
import de.rolandsw.schedulemc.warehouse.WarehouseManager;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.util.*;

/**
 * Zentraler Health-Check-Manager für alle 38 Systeme
 *
 * Kategorien:
 * - Kern-Systeme (3): Economy, Plot, Wallet
 * - Finanz-Systeme (9): Loan, CreditLoan, CreditScore, Savings, Tax, Overdraft, Recurring, ShopAccount, Interest
 * - NPC & Crime (5): Crime, Bounty, NPC, Prison, Witness
 * - NPC Life (8): Dialogue, Quest, Companion, Faction, NPCInteraction, Relationship, WorldEvent, DynamicPrice
 * - Spieler-Systeme (7): Gang, Territory, Achievement, DailyReward, Message, GangMission, Scenario
 * - Welt-Systeme (4): Lock, Market, Warehouse, Towing
 * - Infrastruktur (2): AntiExploit, ThreadPool
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
    // Health Check Registry
    // ==========================================

    /**
     * Registry für Health-Checks. Jeder Check wird mit Key, Kategorie und
     * Supplier registriert. Eliminiert 30+ duplizierte Methoden.
     */
    private static final Map<String, HealthCheckEntry> checkRegistry = new LinkedHashMap<>();

    private static class HealthCheckEntry {
        final String category;
        final HealthSupplier supplier;

        HealthCheckEntry(String category, HealthSupplier supplier) {
            this.category = category;
            this.supplier = supplier;
        }
    }

    // Statische Initialisierung aller Health-Checks
    static {
        // === Kern-Systeme (3) ===
        registerStaticCheck("economy", "Kern", "Economy",
            () -> EconomyManager.isHealthy(), EconomyManager::getHealthInfo, EconomyManager::getLastError);
        registerStaticCheck("plot", "Kern", "Plot System",
            () -> PlotManager.isHealthy(), PlotManager::getHealthInfo, PlotManager::getLastError);
        registerStaticCheck("wallet", "Kern", "Wallet",
            () -> WalletManager.isHealthy(), WalletManager::getHealthInfo, WalletManager::getLastError);

        // === Finanz-Systeme (9) ===
        registerManagerCheck("loan", "Finanz", "Loan", LoanManager::getInstance);
        registerManagerCheck("creditloan", "Finanz", "Credit Loan", CreditLoanManager::getInstance);
        registerManagerCheck("creditscore", "Finanz", "Credit Score", CreditScoreManager::getInstance);
        registerManagerCheck("savings", "Finanz", "Savings", SavingsAccountManager::getInstance);
        registerManagerCheck("tax", "Finanz", "Tax", TaxManager::getInstance);
        registerManagerCheck("overdraft", "Finanz", "Overdraft", OverdraftManager::getInstance);
        registerManagerCheck("recurring", "Finanz", "Recurring Payments", RecurringPaymentManager::getInstance);
        registerCheck("shopaccount", "Finanz", () -> {
            int count = ShopAccountManager.getShopCount();
            return new ComponentHealth("Shop Accounts", SystemHealth.HEALTHY, "Aktiv, " + count + " Shops registriert");
        });
        registerManagerCheck("interest", "Finanz", "Interest", () -> InterestManager.getInstance(null));

        // === NPC & Crime (5) ===
        registerStaticCheck("crime", "NPC & Crime", "Crime",
            () -> CrimeManager.isHealthy(), CrimeManager::getHealthInfo, CrimeManager::getLastError);
        registerManagerCheck("bounty", "NPC & Crime", "Bounty", BountyManager::getInstance);
        registerCheck("npc", "NPC & Crime", () ->
            new ComponentHealth("NPC Registry", SystemHealth.HEALTHY, "Aktiv, Name-Registry geladen"));
        registerCheck("prison", "NPC & Crime", () -> {
            PrisonManager mgr = PrisonManager.getInstance();
            if (mgr == null) return notInitialized("Prison");
            return new ComponentHealth("Prison", SystemHealth.HEALTHY, "Aktiv, " + mgr.getPrisonerCount() + " Gefangene");
        });
        registerManagerCheck("witness", "NPC & Crime", "Witness", WitnessManager::getInstance);

        // === NPC Life (8) ===
        registerManagerCheck("dialogue", "NPC Life", "Dialogue", DialogueManager::getInstance);
        registerManagerCheck("quest", "NPC Life", "Quest", QuestManager::getInstance);
        registerManagerCheck("companion", "NPC Life", "Companion", CompanionManager::getInstance);
        registerManagerCheck("faction", "NPC Life", "Faction", FactionManager::getInstance);
        registerManagerCheck("npcinteraction", "NPC Life", "NPC Interaction", NPCInteractionManager::getInstance);
        registerManagerCheck("relationship", "NPC Life", "NPC Relationship", NPCRelationshipManager::getInstance);
        registerManagerCheck("worldevent", "NPC Life", "World Event", WorldEventManager::getInstance);
        registerManagerCheck("dynamicprice", "NPC Life", "Dynamic Price", DynamicPriceManager::getInstance);

        // === Spieler-Systeme (7) ===
        registerManagerCheck("gang", "Spieler", "Gang", GangManager::getInstance);
        registerManagerCheck("territory", "Spieler", "Territory", TerritoryManager::getInstance);
        registerManagerCheck("achievement", "Spieler", "Achievement", AchievementManager::getInstance);
        registerStaticCheck("daily", "Spieler", "Daily Reward",
            () -> DailyRewardManager.isHealthy(), DailyRewardManager::getHealthInfo, DailyRewardManager::getLastError);
        registerStaticCheck("message", "Spieler", "Messaging",
            () -> MessageManager.isHealthy(), MessageManager::getHealthInfo, MessageManager::getLastError);
        registerCheck("gangmission", "Spieler", () -> {
            GangMissionManager mgr = GangMissionManager.getInstance();
            if (mgr == null) return notInitialized("Gang Mission");
            return new ComponentHealth("Gang Mission", SystemHealth.HEALTHY, "Aktiv, Missions-System betriebsbereit");
        });
        registerCheck("scenario", "Spieler", () -> {
            ScenarioManager mgr = ScenarioManager.getInstance();
            if (mgr == null) return notInitialized("Scenario");
            return new ComponentHealth("Scenario", SystemHealth.HEALTHY,
                "Aktiv, " + mgr.getActiveCount() + "/" + mgr.getScenarioCount() + " Szenarien aktiv");
        });

        // === Welt-Systeme (4) ===
        registerCheck("lock", "Welt", () -> {
            LockManager mgr = LockManager.getInstance();
            if (mgr == null) return notInitialized("Lock");
            return new ComponentHealth("Lock", SystemHealth.HEALTHY, "Aktiv, " + mgr.getLockCount() + " Locks registriert");
        });
        registerCheck("market", "Welt", () -> {
            DynamicMarketManager mgr = DynamicMarketManager.getInstance();
            if (mgr == null) return notInitialized("Market");
            boolean enabled = mgr.isEnabled();
            return new ComponentHealth("Dynamic Market",
                enabled ? SystemHealth.HEALTHY : SystemHealth.DEGRADED,
                enabled ? "Aktiv, Markt-Simulation läuft" : "Deaktiviert");
        });
        registerCheck("warehouse", "Welt", () -> {
            int count = WarehouseManager.getAllWarehouses().size();
            return new ComponentHealth("Warehouse", SystemHealth.HEALTHY, "Aktiv, " + count + " Warehouses registriert");
        });
        registerStaticCheck("towing", "Welt", "Towing",
            () -> TowingYardManager.isHealthy(), () -> "Aktiv, Towing-System betriebsbereit", TowingYardManager::getLastError);

        // === Infrastruktur (2) ===
        registerCheck("antiexploit", "Infrastruktur", () -> {
            AntiExploitManager mgr = AntiExploitManager.getInstance();
            if (mgr == null) return notInitialized("AntiExploit");
            return new ComponentHealth("AntiExploit", SystemHealth.HEALTHY, "Aktiv, Exploit-Erkennung betriebsbereit");
        });
        registerCheck("threadpool", "Infrastruktur", () -> {
            String stats = ThreadPoolManager.getStatistics();
            int ioQueue = ThreadPoolManager.getIOPoolQueueSize();
            int compActive = ThreadPoolManager.getComputationPoolActiveCount();
            return new ComponentHealth("ThreadPool",
                ioQueue > 100 ? SystemHealth.DEGRADED : SystemHealth.HEALTHY,
                stats + " | IO-Queue: " + ioQueue + ", Compute-Active: " + compActive);
        });
    }

    // ==========================================
    // Registry Helper Methods
    // ==========================================

    private interface ManagerSupplier<T> {
        T get();
    }

    private interface HealthInfoSupplier {
        String get();
    }

    private interface HealthyCheck {
        boolean isHealthy();
    }

    private static void registerCheck(String key, String category, HealthSupplier supplier) {
        checkRegistry.put(key, new HealthCheckEntry(category, supplier));
    }

    /**
     * Registriert einen Check für Manager mit isHealthy()/getHealthInfo()/getLastError() Pattern.
     * Reduziert ~15 identische Methoden auf einen generischen Einzeiler.
     */
    private static <T extends AbstractPersistenceManager<?>> void registerManagerCheck(
            String key, String category, String name, ManagerSupplier<T> managerSupplier) {
        registerCheck(key, category, () -> {
            T mgr = managerSupplier.get();
            if (mgr == null) return notInitialized(name);
            return mgr.isHealthy()
                ? new ComponentHealth(name, SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth(name, determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    /**
     * Registriert einen Check für statische Manager (z.B. EconomyManager, CrimeManager).
     */
    private static void registerStaticCheck(String key, String category, String name,
            HealthyCheck healthyCheck, HealthInfoSupplier infoSupplier, HealthInfoSupplier errorSupplier) {
        registerCheck(key, category, () -> {
            if (healthyCheck.isHealthy()) {
                return new ComponentHealth(name, SystemHealth.HEALTHY, infoSupplier.get());
            } else {
                return new ComponentHealth(name, determineStatus(errorSupplier.get()), infoSupplier.get());
            }
        });
    }

    // ==========================================
    // Haupt-Check-Methoden
    // ==========================================

    /**
     * Prüft den Health-Status aller registrierten Systeme.
     * Nutzt die Registry statt 38 einzelner Methoden.
     */
    public static Map<String, ComponentHealth> checkAllSystems() {
        Map<String, ComponentHealth> results = new LinkedHashMap<>();

        for (Map.Entry<String, HealthCheckEntry> entry : checkRegistry.entrySet()) {
            results.put(entry.getKey(), checkSingletonManager(entry.getKey(), entry.getValue().supplier));
        }

        return results;
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

        // Gruppierte Ausgabe nach Registry-Kategorien
        String lastCategory = null;
        for (Map.Entry<String, HealthCheckEntry> regEntry : checkRegistry.entrySet()) {
            String category = regEntry.getValue().category;
            if (!category.equals(lastCategory)) {
                report.append("\n\u00A76\u00A7l").append(category).append(":\u00A7r\n");
                lastCategory = category;
            }
            appendIfPresent(report, results, regEntry.getKey());
        }

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
        return calculateOverallHealth(checkAllSystems());
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
     * OPTIMIERT: Berechnet Overall-Status aus vorhandenem Ergebnis statt doppeltem Check
     */
    public static String getQuickStatus() {
        Map<String, ComponentHealth> results = checkAllSystems();
        long healthy = results.values().stream().filter(h -> h.getStatus() == SystemHealth.HEALTHY).count();
        SystemHealth overall = calculateOverallHealth(results);
        return String.format("§7System Status: %s §7(%d/%d Systeme gesund)",
            overall.getDisplayName(), healthy, results.size());
    }

    /**
     * Berechnet den Overall-Status aus einem vorhandenen Ergebnis
     */
    private static SystemHealth calculateOverallHealth(Map<String, ComponentHealth> results) {
        boolean hasUnhealthy = results.values().stream()
            .anyMatch(h -> h.getStatus() == SystemHealth.UNHEALTHY);
        if (hasUnhealthy) return SystemHealth.UNHEALTHY;

        boolean hasDegraded = results.values().stream()
            .anyMatch(h -> h.getStatus() == SystemHealth.DEGRADED);
        if (hasDegraded) return SystemHealth.DEGRADED;

        return SystemHealth.HEALTHY;
    }

    /**
     * Gibt den Health-Status eines einzelnen Systems zurück
     * OPTIMIERT: Führt nur den einen relevanten Check aus statt alle 38
     */
    public static ComponentHealth checkSystem(String systemKey) {
        HealthCheckEntry entry = checkRegistry.get(systemKey);
        if (entry == null) return null;
        return checkSingletonManager(systemKey, entry.supplier);
    }

    // ==========================================
    // Hilfsmethoden
    // ==========================================

    @FunctionalInterface
    private interface HealthSupplier {
        ComponentHealth get();
    }

    private static ComponentHealth checkSingletonManager(String name, HealthSupplier supplier) {
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
