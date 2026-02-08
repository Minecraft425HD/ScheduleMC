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
    // Haupt-Check-Methoden
    // ==========================================

    /**
     * Prüft den Health-Status aller 38 Systeme
     */
    public static Map<String, ComponentHealth> checkAllSystems() {
        Map<String, ComponentHealth> results = new LinkedHashMap<>();

        // === Kern-Systeme (3) ===
        results.put("economy", checkEconomySystem());
        results.put("plot", checkPlotSystem());
        results.put("wallet", checkWalletSystem());

        // === Finanz-Systeme (9) ===
        results.put("loan", checkLoanSystem());
        results.put("creditloan", checkCreditLoanSystem());
        results.put("creditscore", checkCreditScoreSystem());
        results.put("savings", checkSavingsSystem());
        results.put("tax", checkTaxSystem());
        results.put("overdraft", checkOverdraftSystem());
        results.put("recurring", checkRecurringPaymentSystem());
        results.put("shopaccount", checkShopAccountSystem());
        results.put("interest", checkInterestSystem());

        // === NPC & Crime (5) ===
        results.put("crime", checkCrimeSystem());
        results.put("bounty", checkBountySystem());
        results.put("npc", checkNPCSystem());
        results.put("prison", checkPrisonSystem());
        results.put("witness", checkWitnessSystem());

        // === NPC Life (8) ===
        results.put("dialogue", checkDialogueSystem());
        results.put("quest", checkQuestSystem());
        results.put("companion", checkCompanionSystem());
        results.put("faction", checkFactionSystem());
        results.put("npcinteraction", checkNPCInteractionSystem());
        results.put("relationship", checkRelationshipSystem());
        results.put("worldevent", checkWorldEventSystem());
        results.put("dynamicprice", checkDynamicPriceSystem());

        // === Spieler-Systeme (7) ===
        results.put("gang", checkGangSystem());
        results.put("territory", checkTerritorySystem());
        results.put("achievement", checkAchievementSystem());
        results.put("daily", checkDailyRewardSystem());
        results.put("message", checkMessageSystem());
        results.put("gangmission", checkGangMissionSystem());
        results.put("scenario", checkScenarioSystem());

        // === Welt-Systeme (4) ===
        results.put("lock", checkLockSystem());
        results.put("market", checkMarketSystem());
        results.put("warehouse", checkWarehouseSystem());
        results.put("towing", checkTowingSystem());

        // === Infrastruktur (2) ===
        results.put("antiexploit", checkAntiExploitSystem());
        results.put("threadpool", checkThreadPoolSystem());

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
    // Finanz-Systeme
    // ==========================================

    private static ComponentHealth checkLoanSystem() {
        return checkSingletonManager("Loan", () -> {
            LoanManager mgr = LoanManager.getInstance();
            if (mgr == null) return notInitialized("Loan");
            return mgr.isHealthy()
                ? new ComponentHealth("Loan", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Loan", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkCreditLoanSystem() {
        return checkSingletonManager("Credit Loan", () -> {
            CreditLoanManager mgr = CreditLoanManager.getInstance();
            if (mgr == null) return notInitialized("Credit Loan");
            return mgr.isHealthy()
                ? new ComponentHealth("Credit Loan", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Credit Loan", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkCreditScoreSystem() {
        return checkSingletonManager("Credit Score", () -> {
            CreditScoreManager mgr = CreditScoreManager.getInstance();
            if (mgr == null) return notInitialized("Credit Score");
            return mgr.isHealthy()
                ? new ComponentHealth("Credit Score", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Credit Score", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkSavingsSystem() {
        return checkSingletonManager("Savings", () -> {
            SavingsAccountManager mgr = SavingsAccountManager.getInstance();
            if (mgr == null) return notInitialized("Savings");
            return mgr.isHealthy()
                ? new ComponentHealth("Savings", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Savings", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkTaxSystem() {
        return checkSingletonManager("Tax", () -> {
            TaxManager mgr = TaxManager.getInstance();
            if (mgr == null) return notInitialized("Tax");
            return mgr.isHealthy()
                ? new ComponentHealth("Tax", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Tax", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkOverdraftSystem() {
        return checkSingletonManager("Overdraft", () -> {
            OverdraftManager mgr = OverdraftManager.getInstance();
            if (mgr == null) return notInitialized("Overdraft");
            return mgr.isHealthy()
                ? new ComponentHealth("Overdraft", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Overdraft", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkRecurringPaymentSystem() {
        return checkSingletonManager("Recurring Payments", () -> {
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

    private static ComponentHealth checkInterestSystem() {
        return checkSingletonManager("Interest", () -> {
            InterestManager mgr = InterestManager.getInstance(null);
            if (mgr == null) return notInitialized("Interest");
            return mgr.isHealthy()
                ? new ComponentHealth("Interest", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Interest", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
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
        return checkSingletonManager("Bounty", () -> {
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

    private static ComponentHealth checkPrisonSystem() {
        try {
            PrisonManager mgr = PrisonManager.getInstance();
            if (mgr == null) return notInitialized("Prison");
            int count = mgr.getPrisonerCount();
            return new ComponentHealth("Prison", SystemHealth.HEALTHY,
                "Aktiv, " + count + " Gefangene");
        } catch (Exception e) {
            LOGGER.error("Fehler bei Prison Health-Check", e);
            return errorHealth("Prison", e);
        }
    }

    private static ComponentHealth checkWitnessSystem() {
        return checkSingletonManager("Witness", () -> {
            WitnessManager mgr = WitnessManager.getInstance();
            if (mgr == null) return notInitialized("Witness");
            return mgr.isHealthy()
                ? new ComponentHealth("Witness", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Witness", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    // ==========================================
    // NPC Life Systeme
    // ==========================================

    private static ComponentHealth checkDialogueSystem() {
        return checkSingletonManager("Dialogue", () -> {
            DialogueManager mgr = DialogueManager.getInstance();
            if (mgr == null) return notInitialized("Dialogue");
            return mgr.isHealthy()
                ? new ComponentHealth("Dialogue", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Dialogue", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkQuestSystem() {
        return checkSingletonManager("Quest", () -> {
            QuestManager mgr = QuestManager.getInstance();
            if (mgr == null) return notInitialized("Quest");
            return mgr.isHealthy()
                ? new ComponentHealth("Quest", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Quest", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkCompanionSystem() {
        return checkSingletonManager("Companion", () -> {
            CompanionManager mgr = CompanionManager.getInstance();
            if (mgr == null) return notInitialized("Companion");
            return mgr.isHealthy()
                ? new ComponentHealth("Companion", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Companion", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkFactionSystem() {
        return checkSingletonManager("Faction", () -> {
            FactionManager mgr = FactionManager.getInstance();
            if (mgr == null) return notInitialized("Faction");
            return mgr.isHealthy()
                ? new ComponentHealth("Faction", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("Faction", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkNPCInteractionSystem() {
        return checkSingletonManager("NPC Interaction", () -> {
            NPCInteractionManager mgr = NPCInteractionManager.getInstance();
            if (mgr == null) return notInitialized("NPC Interaction");
            return mgr.isHealthy()
                ? new ComponentHealth("NPC Interaction", SystemHealth.HEALTHY, mgr.getHealthInfo())
                : new ComponentHealth("NPC Interaction", determineStatus(mgr.getLastError()), mgr.getHealthInfo());
        });
    }

    private static ComponentHealth checkRelationshipSystem() {
        return checkSingletonManager("NPC Relationship", () -> {
            NPCRelationshipManager mgr = NPCRelationshipManager.getInstance();
            if (mgr == null) return notInitialized("NPC Relationship");
            String info = mgr.isHealthy() ? mgr.getHealthInfo() : mgr.getHealthInfo();
            int total = mgr.getTotalRelationships();
            int players = mgr.getPlayerCount();
            return mgr.isHealthy()
                ? new ComponentHealth("NPC Relationship", SystemHealth.HEALTHY,
                    info + " | " + total + " Beziehungen, " + players + " Spieler")
                : new ComponentHealth("NPC Relationship", determineStatus(mgr.getLastError()), info);
        });
    }

    private static ComponentHealth checkWorldEventSystem() {
        return checkSingletonManager("World Event", () -> {
            WorldEventManager mgr = WorldEventManager.getInstance();
            if (mgr == null) return notInitialized("World Event");
            String info = mgr.isHealthy() ? mgr.getHealthInfo() : mgr.getHealthInfo();
            int active = mgr.getActiveEvents().size();
            return mgr.isHealthy()
                ? new ComponentHealth("World Event", SystemHealth.HEALTHY,
                    info + " | " + active + " aktive Events")
                : new ComponentHealth("World Event", determineStatus(mgr.getLastError()), info);
        });
    }

    private static ComponentHealth checkDynamicPriceSystem() {
        return checkSingletonManager("Dynamic Price", () -> {
            DynamicPriceManager mgr = DynamicPriceManager.getInstance();
            if (mgr == null) return notInitialized("Dynamic Price");
            String info = mgr.isHealthy() ? mgr.getHealthInfo() : mgr.getHealthInfo();
            String condition = mgr.getGlobalMarketCondition().name();
            return mgr.isHealthy()
                ? new ComponentHealth("Dynamic Price", SystemHealth.HEALTHY,
                    info + " | Marktlage: " + condition)
                : new ComponentHealth("Dynamic Price", determineStatus(mgr.getLastError()), info);
        });
    }

    // ==========================================
    // Spieler-Systeme
    // ==========================================

    private static ComponentHealth checkGangSystem() {
        return checkSingletonManager("Gang", () -> {
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
        return checkSingletonManager("Territory", () -> {
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
        return checkSingletonManager("Achievement", () -> {
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

    private static ComponentHealth checkGangMissionSystem() {
        try {
            GangMissionManager mgr = GangMissionManager.getInstance();
            if (mgr == null) return notInitialized("Gang Mission");
            return new ComponentHealth("Gang Mission", SystemHealth.HEALTHY,
                "Aktiv, Missions-System betriebsbereit");
        } catch (Exception e) {
            LOGGER.error("Fehler bei GangMission Health-Check", e);
            return errorHealth("Gang Mission", e);
        }
    }

    private static ComponentHealth checkScenarioSystem() {
        try {
            ScenarioManager mgr = ScenarioManager.getInstance();
            if (mgr == null) return notInitialized("Scenario");
            int total = mgr.getScenarioCount();
            int active = mgr.getActiveCount();
            return new ComponentHealth("Scenario", SystemHealth.HEALTHY,
                "Aktiv, " + active + "/" + total + " Szenarien aktiv");
        } catch (Exception e) {
            LOGGER.error("Fehler bei Scenario Health-Check", e);
            return errorHealth("Scenario", e);
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
    // Infrastruktur-Systeme
    // ==========================================

    private static ComponentHealth checkAntiExploitSystem() {
        try {
            AntiExploitManager mgr = AntiExploitManager.getInstance();
            if (mgr == null) return notInitialized("AntiExploit");
            return new ComponentHealth("AntiExploit", SystemHealth.HEALTHY,
                "Aktiv, Exploit-Erkennung betriebsbereit");
        } catch (Exception e) {
            LOGGER.error("Fehler bei AntiExploit Health-Check", e);
            return errorHealth("AntiExploit", e);
        }
    }

    private static ComponentHealth checkThreadPoolSystem() {
        try {
            String stats = ThreadPoolManager.getStatistics();
            int ioQueue = ThreadPoolManager.getIOPoolQueueSize();
            int compActive = ThreadPoolManager.getComputationPoolActiveCount();
            return new ComponentHealth("ThreadPool",
                ioQueue > 100 ? SystemHealth.DEGRADED : SystemHealth.HEALTHY,
                stats + " | IO-Queue: " + ioQueue + ", Compute-Active: " + compActive);
        } catch (Exception e) {
            LOGGER.error("Fehler bei ThreadPool Health-Check", e);
            return errorHealth("ThreadPool", e);
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
        appendIfPresent(report, results, "interest");

        report.append("\n§6§lNPC & Crime:§r\n");
        appendIfPresent(report, results, "crime");
        appendIfPresent(report, results, "bounty");
        appendIfPresent(report, results, "npc");
        appendIfPresent(report, results, "prison");
        appendIfPresent(report, results, "witness");

        report.append("\n§6§lNPC Life:§r\n");
        appendIfPresent(report, results, "dialogue");
        appendIfPresent(report, results, "quest");
        appendIfPresent(report, results, "companion");
        appendIfPresent(report, results, "faction");
        appendIfPresent(report, results, "npcinteraction");
        appendIfPresent(report, results, "relationship");
        appendIfPresent(report, results, "worldevent");
        appendIfPresent(report, results, "dynamicprice");

        report.append("\n§6§lSpieler-Systeme:§r\n");
        appendIfPresent(report, results, "gang");
        appendIfPresent(report, results, "territory");
        appendIfPresent(report, results, "achievement");
        appendIfPresent(report, results, "daily");
        appendIfPresent(report, results, "message");
        appendIfPresent(report, results, "gangmission");
        appendIfPresent(report, results, "scenario");

        report.append("\n§6§lWelt-Systeme:§r\n");
        appendIfPresent(report, results, "lock");
        appendIfPresent(report, results, "market");
        appendIfPresent(report, results, "warehouse");
        appendIfPresent(report, results, "towing");

        report.append("\n§6§lInfrastruktur:§r\n");
        appendIfPresent(report, results, "antiexploit");
        appendIfPresent(report, results, "threadpool");

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
