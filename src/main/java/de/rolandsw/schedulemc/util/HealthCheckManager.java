package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.managers.DailyRewardManager;
import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.region.PlotManager;
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
            return String.format("§7%s: %s §7- %s",
                componentName, status.getDisplayName(), message);
        }
    }

    /**
     * Prüft den Health-Status aller Systeme
     */
    public static Map<String, ComponentHealth> checkAllSystems() {
        Map<String, ComponentHealth> results = new LinkedHashMap<>();

        // Economy System
        results.put("economy", checkEconomySystem());

        // Plot System
        results.put("plot", checkPlotSystem());

        // Wallet System
        results.put("wallet", checkWalletSystem());

        // Message System
        results.put("message", checkMessageSystem());

        // Daily Reward System
        results.put("daily", checkDailyRewardSystem());

        // Weitere Systeme können hier hinzugefügt werden
        // results.put("warehouse", checkWarehouseSystem());
        // results.put("vehicle", checkVehicleSystem());

        return results;
    }

    /**
     * Prüft Economy-System
     */
    private static ComponentHealth checkEconomySystem() {
        try {
            if (EconomyManager.isHealthy()) {
                return new ComponentHealth(
                    "Economy System",
                    SystemHealth.HEALTHY,
                    EconomyManager.getHealthInfo()
                );
            } else {
                String error = EconomyManager.getLastError();
                SystemHealth status = error != null && error.contains("backup")
                    ? SystemHealth.DEGRADED
                    : SystemHealth.UNHEALTHY;

                return new ComponentHealth(
                    "Economy System",
                    status,
                    EconomyManager.getHealthInfo()
                );
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei Economy Health-Check", e);
            return new ComponentHealth(
                "Economy System",
                SystemHealth.UNHEALTHY,
                "Health-Check fehlgeschlagen: " + e.getMessage()
            );
        }
    }

    /**
     * Prüft Plot-System
     */
    private static ComponentHealth checkPlotSystem() {
        try {
            if (PlotManager.isHealthy()) {
                return new ComponentHealth(
                    "Plot System",
                    SystemHealth.HEALTHY,
                    PlotManager.getHealthInfo()
                );
            } else {
                String error = PlotManager.getLastError();
                SystemHealth status = error != null && error.contains("backup")
                    ? SystemHealth.DEGRADED
                    : SystemHealth.UNHEALTHY;

                return new ComponentHealth(
                    "Plot System",
                    status,
                    PlotManager.getHealthInfo()
                );
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei Plot Health-Check", e);
            return new ComponentHealth(
                "Plot System",
                SystemHealth.UNHEALTHY,
                "Health-Check fehlgeschlagen: " + e.getMessage()
            );
        }
    }

    /**
     * Prüft Wallet-System
     */
    private static ComponentHealth checkWalletSystem() {
        try {
            if (WalletManager.isHealthy()) {
                return new ComponentHealth(
                    "Wallet System",
                    SystemHealth.HEALTHY,
                    WalletManager.getHealthInfo()
                );
            } else {
                String error = WalletManager.getLastError();
                SystemHealth status = error != null && error.contains("backup")
                    ? SystemHealth.DEGRADED
                    : SystemHealth.UNHEALTHY;

                return new ComponentHealth(
                    "Wallet System",
                    status,
                    WalletManager.getHealthInfo()
                );
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei Wallet Health-Check", e);
            return new ComponentHealth(
                "Wallet System",
                SystemHealth.UNHEALTHY,
                "Health-Check fehlgeschlagen: " + e.getMessage()
            );
        }
    }

    /**
     * Prüft Message-System
     */
    private static ComponentHealth checkMessageSystem() {
        try {
            if (MessageManager.isHealthy()) {
                return new ComponentHealth(
                    "Message System",
                    SystemHealth.HEALTHY,
                    MessageManager.getHealthInfo()
                );
            } else {
                String error = MessageManager.getLastError();
                SystemHealth status = error != null && error.contains("backup")
                    ? SystemHealth.DEGRADED
                    : SystemHealth.UNHEALTHY;

                return new ComponentHealth(
                    "Message System",
                    status,
                    MessageManager.getHealthInfo()
                );
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei Message Health-Check", e);
            return new ComponentHealth(
                "Message System",
                SystemHealth.UNHEALTHY,
                "Health-Check fehlgeschlagen: " + e.getMessage()
            );
        }
    }

    /**
     * Prüft Daily Reward-System
     */
    private static ComponentHealth checkDailyRewardSystem() {
        try {
            if (DailyRewardManager.isHealthy()) {
                return new ComponentHealth(
                    "Daily Reward System",
                    SystemHealth.HEALTHY,
                    DailyRewardManager.getHealthInfo()
                );
            } else {
                String error = DailyRewardManager.getLastError();
                SystemHealth status = error != null && error.contains("backup")
                    ? SystemHealth.DEGRADED
                    : SystemHealth.UNHEALTHY;

                return new ComponentHealth(
                    "Daily Reward System",
                    status,
                    DailyRewardManager.getHealthInfo()
                );
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei Daily Reward Health-Check", e);
            return new ComponentHealth(
                "Daily Reward System",
                SystemHealth.UNHEALTHY,
                "Health-Check fehlgeschlagen: " + e.getMessage()
            );
        }
    }

    /**
     * Gibt einen formatierten Health-Report zurück
     */
    public static String getHealthReport() {
        Map<String, ComponentHealth> results = checkAllSystems();

        StringBuilder report = new StringBuilder();
        report.append("§e§l═══════════════════════════════§r\n");
        report.append("§6§lSYSTEM HEALTH REPORT§r\n");
        report.append("§e§l═══════════════════════════════§r\n\n");

        int healthy = 0;
        int degraded = 0;
        int unhealthy = 0;

        for (ComponentHealth health : results.values()) {
            report.append(health.toString()).append("\n");

            switch (health.getStatus()) {
                case HEALTHY -> healthy++;
                case DEGRADED -> degraded++;
                case UNHEALTHY -> unhealthy++;
            }
        }

        report.append("\n§e§l═══════════════════════════════§r\n");
        report.append(String.format("§aGesund: %d §7| §eDegradiert: %d §7| §cUngesund: %d\n",
            healthy, degraded, unhealthy));
        report.append("§e§l═══════════════════════════════§r");

        return report.toString();
    }

    /**
     * Gibt den Overall-Status zurück
     */
    public static SystemHealth getOverallHealth() {
        Map<String, ComponentHealth> results = checkAllSystems();

        boolean hasUnhealthy = results.values().stream()
            .anyMatch(h -> h.getStatus() == SystemHealth.UNHEALTHY);

        if (hasUnhealthy) {
            return SystemHealth.UNHEALTHY;
        }

        boolean hasDegraded = results.values().stream()
            .anyMatch(h -> h.getStatus() == SystemHealth.DEGRADED);

        if (hasDegraded) {
            return SystemHealth.DEGRADED;
        }

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

        LOGGER.info("=== SYSTEM HEALTH CHECK ===");
        for (ComponentHealth health : results.values()) {
            switch (health.getStatus()) {
                case HEALTHY ->
                    LOGGER.info("{}: GESUND - {}", health.getComponentName(), health.getMessage());
                case DEGRADED ->
                    LOGGER.warn("{}: DEGRADED - {}", health.getComponentName(), health.getMessage());
                case UNHEALTHY ->
                    LOGGER.error("{}: UNGESUND - {}", health.getComponentName(), health.getMessage());
            }
        }
        LOGGER.info("Overall Status: {}", getOverallHealth());
        LOGGER.info("===========================");
    }

    /**
     * Gibt eine kurze Status-Zusammenfassung zurück
     */
    public static String getQuickStatus() {
        SystemHealth overall = getOverallHealth();
        return String.format("§7System Status: %s", overall.getDisplayName());
    }
}
