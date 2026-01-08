package de.rolandsw.schedulemc.util;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.managers.DailyRewardManager;
import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.region.PlotManager;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for {@link HealthCheckManager}.
 *
 * <p>Tests verify:
 * <ul>
 *   <li>SystemHealth enum values and display names</li>
 *   <li>ComponentHealth creation and properties</li>
 *   <li>Individual system health checks (Economy, Plot, Wallet, Message, DailyReward)</li>
 *   <li>Aggregate health checks (all systems)</li>
 *   <li>Overall health determination logic</li>
 *   <li>Health report formatting</li>
 *   <li>Quick status generation</li>
 * </ul>
 *
 * <p><b>Note:</b> Uses {@link MockedStatic} for static manager methods.
 */
@DisplayName("HealthCheckManager Tests")
class HealthCheckManagerTest {

    private MockedStatic<EconomyManager> economyMock;
    private MockedStatic<PlotManager> plotMock;
    private MockedStatic<WalletManager> walletMock;
    private MockedStatic<MessageManager> messageMock;
    private MockedStatic<DailyRewardManager> dailyRewardMock;

    @BeforeEach
    void setUp() {
        economyMock = mockStatic(EconomyManager.class);
        plotMock = mockStatic(PlotManager.class);
        walletMock = mockStatic(WalletManager.class);
        messageMock = mockStatic(MessageManager.class);
        dailyRewardMock = mockStatic(DailyRewardManager.class);

        // Default: All systems healthy
        setupHealthyDefaults();
    }

    @AfterEach
    void tearDown() {
        economyMock.close();
        plotMock.close();
        walletMock.close();
        messageMock.close();
        dailyRewardMock.close();
    }

    private void setupHealthyDefaults() {
        economyMock.when(EconomyManager::isHealthy).thenReturn(true);
        economyMock.when(EconomyManager::getHealthInfo).thenReturn("Economy OK");

        plotMock.when(PlotManager::isHealthy).thenReturn(true);
        plotMock.when(PlotManager::getHealthInfo).thenReturn("Plot OK");

        walletMock.when(WalletManager::isHealthy).thenReturn(true);
        walletMock.when(WalletManager::getHealthInfo).thenReturn("Wallet OK");

        messageMock.when(MessageManager::isHealthy).thenReturn(true);
        messageMock.when(MessageManager::getHealthInfo).thenReturn("Message OK");

        dailyRewardMock.when(DailyRewardManager::isHealthy).thenReturn(true);
        dailyRewardMock.when(DailyRewardManager::getHealthInfo).thenReturn("Daily Reward OK");
    }

    // ═══════════════════════════════════════════════════════════
    // SYSTEMHEALTH ENUM TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("SystemHealth Enum")
    class SystemHealthTests {

        @Test
        @DisplayName("HEALTHY should have green display name")
        void healthyShouldHaveGreenDisplayName() {
            assertThat(HealthCheckManager.SystemHealth.HEALTHY.getDisplayName())
                .contains("GESUND");
        }

        @Test
        @DisplayName("DEGRADED should have yellow display name")
        void degradedShouldHaveYellowDisplayName() {
            assertThat(HealthCheckManager.SystemHealth.DEGRADED.getDisplayName())
                .contains("EINGESCHRÄNKT");
        }

        @Test
        @DisplayName("UNHEALTHY should have red display name")
        void unhealthyShouldHaveRedDisplayName() {
            assertThat(HealthCheckManager.SystemHealth.UNHEALTHY.getDisplayName())
                .contains("UNGESUND");
        }

        @Test
        @DisplayName("should have all three values")
        void shouldHaveAllThreeValues() {
            HealthCheckManager.SystemHealth[] values = HealthCheckManager.SystemHealth.values();

            assertThat(values).hasSize(3);
            assertThat(values).containsExactly(
                HealthCheckManager.SystemHealth.HEALTHY,
                HealthCheckManager.SystemHealth.DEGRADED,
                HealthCheckManager.SystemHealth.UNHEALTHY
            );
        }
    }

    // ═══════════════════════════════════════════════════════════
    // COMPONENTHEALTH TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ComponentHealth")
    class ComponentHealthTests {

        @Test
        @DisplayName("should store component name, status, and message")
        void shouldStoreAllProperties() {
            HealthCheckManager.ComponentHealth health = new HealthCheckManager.ComponentHealth(
                "Test Component",
                HealthCheckManager.SystemHealth.HEALTHY,
                "All good"
            );

            assertThat(health.getComponentName()).isEqualTo("Test Component");
            assertThat(health.getStatus()).isEqualTo(HealthCheckManager.SystemHealth.HEALTHY);
            assertThat(health.getMessage()).isEqualTo("All good");
        }

        @Test
        @DisplayName("should record creation time")
        void shouldRecordCreationTime() {
            long before = System.currentTimeMillis();

            HealthCheckManager.ComponentHealth health = new HealthCheckManager.ComponentHealth(
                "Test",
                HealthCheckManager.SystemHealth.HEALTHY,
                "OK"
            );

            long after = System.currentTimeMillis();

            assertThat(health.getLastCheckTime()).isBetween(before, after);
        }

        @Test
        @DisplayName("toString() should format with component name, status, and message")
        void toStringShouldFormatCorrectly() {
            HealthCheckManager.ComponentHealth health = new HealthCheckManager.ComponentHealth(
                "Economy System",
                HealthCheckManager.SystemHealth.HEALTHY,
                "All OK"
            );

            String str = health.toString();

            assertThat(str).contains("Economy System");
            assertThat(str).contains("GESUND");
            assertThat(str).contains("All OK");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // INDIVIDUAL SYSTEM CHECKS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Individual System Health Checks")
    class IndividualSystemTests {

        @Test
        @DisplayName("checkAllSystems() should check all 5 systems")
        void checkAllSystemsShouldCheckAll() {
            Map<String, HealthCheckManager.ComponentHealth> results =
                HealthCheckManager.checkAllSystems();

            assertThat(results).hasSize(5);
            assertThat(results).containsKeys("economy", "plot", "wallet", "message", "daily");
        }

        @Test
        @DisplayName("economy system should be HEALTHY when isHealthy() returns true")
        void economyShouldBeHealthyWhenHealthy() {
            economyMock.when(EconomyManager::isHealthy).thenReturn(true);
            economyMock.when(EconomyManager::getHealthInfo).thenReturn("Economy OK");

            Map<String, HealthCheckManager.ComponentHealth> results =
                HealthCheckManager.checkAllSystems();

            assertThat(results.get("economy").getStatus())
                .isEqualTo(HealthCheckManager.SystemHealth.HEALTHY);
            assertThat(results.get("economy").getMessage()).isEqualTo("Economy OK");
        }

        @Test
        @DisplayName("economy system should be UNHEALTHY when isHealthy() returns false")
        void economyShouldBeUnhealthyWhenUnhealthy() {
            economyMock.when(EconomyManager::isHealthy).thenReturn(false);
            economyMock.when(EconomyManager::getHealthInfo).thenReturn("Economy Error");
            economyMock.when(EconomyManager::getLastError).thenReturn("Critical error");

            Map<String, HealthCheckManager.ComponentHealth> results =
                HealthCheckManager.checkAllSystems();

            assertThat(results.get("economy").getStatus())
                .isEqualTo(HealthCheckManager.SystemHealth.UNHEALTHY);
        }

        @Test
        @DisplayName("economy system should be DEGRADED when error contains 'backup'")
        void economyShouldBeDegradedWhenBackupError() {
            economyMock.when(EconomyManager::isHealthy).thenReturn(false);
            economyMock.when(EconomyManager::getHealthInfo).thenReturn("Using backup");
            economyMock.when(EconomyManager::getLastError).thenReturn("backup fallback");

            Map<String, HealthCheckManager.ComponentHealth> results =
                HealthCheckManager.checkAllSystems();

            assertThat(results.get("economy").getStatus())
                .isEqualTo(HealthCheckManager.SystemHealth.DEGRADED);
        }

        @Test
        @DisplayName("plot system should be HEALTHY when healthy")
        void plotShouldBeHealthyWhenHealthy() {
            plotMock.when(PlotManager::isHealthy).thenReturn(true);
            plotMock.when(PlotManager::getHealthInfo).thenReturn("Plot OK");

            Map<String, HealthCheckManager.ComponentHealth> results =
                HealthCheckManager.checkAllSystems();

            assertThat(results.get("plot").getStatus())
                .isEqualTo(HealthCheckManager.SystemHealth.HEALTHY);
        }

        @Test
        @DisplayName("wallet system should be HEALTHY when healthy")
        void walletShouldBeHealthyWhenHealthy() {
            walletMock.when(WalletManager::isHealthy).thenReturn(true);
            walletMock.when(WalletManager::getHealthInfo).thenReturn("Wallet OK");

            Map<String, HealthCheckManager.ComponentHealth> results =
                HealthCheckManager.checkAllSystems();

            assertThat(results.get("wallet").getStatus())
                .isEqualTo(HealthCheckManager.SystemHealth.HEALTHY);
        }

        @Test
        @DisplayName("message system should be HEALTHY when healthy")
        void messageShouldBeHealthyWhenHealthy() {
            messageMock.when(MessageManager::isHealthy).thenReturn(true);
            messageMock.when(MessageManager::getHealthInfo).thenReturn("Message OK");

            Map<String, HealthCheckManager.ComponentHealth> results =
                HealthCheckManager.checkAllSystems();

            assertThat(results.get("message").getStatus())
                .isEqualTo(HealthCheckManager.SystemHealth.HEALTHY);
        }

        @Test
        @DisplayName("daily reward system should be HEALTHY when healthy")
        void dailyShouldBeHealthyWhenHealthy() {
            dailyRewardMock.when(DailyRewardManager::isHealthy).thenReturn(true);
            dailyRewardMock.when(DailyRewardManager::getHealthInfo).thenReturn("Daily Reward OK");

            Map<String, HealthCheckManager.ComponentHealth> results =
                HealthCheckManager.checkAllSystems();

            assertThat(results.get("daily").getStatus())
                .isEqualTo(HealthCheckManager.SystemHealth.HEALTHY);
        }

        @Test
        @DisplayName("should handle exception in economy check gracefully")
        void shouldHandleEconomyException() {
            economyMock.when(EconomyManager::isHealthy).thenThrow(new RuntimeException("Simulated error"));

            Map<String, HealthCheckManager.ComponentHealth> results =
                HealthCheckManager.checkAllSystems();

            assertThat(results.get("economy").getStatus())
                .isEqualTo(HealthCheckManager.SystemHealth.UNHEALTHY);
            assertThat(results.get("economy").getMessage())
                .contains("Health-Check fehlgeschlagen");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // OVERALL HEALTH TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Overall Health Determination")
    class OverallHealthTests {

        @Test
        @DisplayName("getOverallHealth() should return HEALTHY when all systems healthy")
        void shouldReturnHealthyWhenAllHealthy() {
            // Already setup in @BeforeEach (all healthy)

            HealthCheckManager.SystemHealth overall = HealthCheckManager.getOverallHealth();

            assertThat(overall).isEqualTo(HealthCheckManager.SystemHealth.HEALTHY);
        }

        @Test
        @DisplayName("getOverallHealth() should return DEGRADED when at least one system degraded")
        void shouldReturnDegradedWhenOneDegraded() {
            economyMock.when(EconomyManager::isHealthy).thenReturn(false);
            economyMock.when(EconomyManager::getLastError).thenReturn("Using backup");

            HealthCheckManager.SystemHealth overall = HealthCheckManager.getOverallHealth();

            assertThat(overall).isEqualTo(HealthCheckManager.SystemHealth.DEGRADED);
        }

        @Test
        @DisplayName("getOverallHealth() should return UNHEALTHY when at least one system unhealthy")
        void shouldReturnUnhealthyWhenOneUnhealthy() {
            plotMock.when(PlotManager::isHealthy).thenReturn(false);
            plotMock.when(PlotManager::getLastError).thenReturn("Critical failure");

            HealthCheckManager.SystemHealth overall = HealthCheckManager.getOverallHealth();

            assertThat(overall).isEqualTo(HealthCheckManager.SystemHealth.UNHEALTHY);
        }

        @Test
        @DisplayName("UNHEALTHY should take priority over DEGRADED")
        void unhealthyShouldTakePriorityOverDegraded() {
            economyMock.when(EconomyManager::isHealthy).thenReturn(false);
            economyMock.when(EconomyManager::getLastError).thenReturn("Using backup");  // DEGRADED

            plotMock.when(PlotManager::isHealthy).thenReturn(false);
            plotMock.when(PlotManager::getLastError).thenReturn("Critical failure");  // UNHEALTHY

            HealthCheckManager.SystemHealth overall = HealthCheckManager.getOverallHealth();

            assertThat(overall).isEqualTo(HealthCheckManager.SystemHealth.UNHEALTHY);
        }

        @Test
        @DisplayName("isAllHealthy() should return true when all systems healthy")
        void isAllHealthyShouldReturnTrueWhenAllHealthy() {
            assertThat(HealthCheckManager.isAllHealthy()).isTrue();
        }

        @Test
        @DisplayName("isAllHealthy() should return false when any system degraded")
        void isAllHealthyShouldReturnFalseWhenDegraded() {
            economyMock.when(EconomyManager::isHealthy).thenReturn(false);
            economyMock.when(EconomyManager::getLastError).thenReturn("Using backup");

            assertThat(HealthCheckManager.isAllHealthy()).isFalse();
        }

        @Test
        @DisplayName("isAllHealthy() should return false when any system unhealthy")
        void isAllHealthyShouldReturnFalseWhenUnhealthy() {
            walletMock.when(WalletManager::isHealthy).thenReturn(false);
            walletMock.when(WalletManager::getLastError).thenReturn("Error");

            assertThat(HealthCheckManager.isAllHealthy()).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HEALTH REPORT TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Health Report Formatting")
    class HealthReportTests {

        @Test
        @DisplayName("getHealthReport() should include header")
        void getHealthReportShouldIncludeHeader() {
            String report = HealthCheckManager.getHealthReport();

            assertThat(report).contains("SYSTEM HEALTH REPORT");
        }

        @Test
        @DisplayName("getHealthReport() should list all systems")
        void getHealthReportShouldListAllSystems() {
            String report = HealthCheckManager.getHealthReport();

            assertThat(report).contains("Economy System");
            assertThat(report).contains("Plot System");
            assertThat(report).contains("Wallet System");
            assertThat(report).contains("Message System");
            assertThat(report).contains("Daily Reward System");
        }

        @Test
        @DisplayName("getHealthReport() should show summary counts")
        void getHealthReportShouldShowSummaryCounts() {
            economyMock.when(EconomyManager::isHealthy).thenReturn(false);
            economyMock.when(EconomyManager::getLastError).thenReturn("Using backup");

            plotMock.when(PlotManager::isHealthy).thenReturn(false);
            plotMock.when(PlotManager::getLastError).thenReturn("Error");

            String report = HealthCheckManager.getHealthReport();

            // 3 healthy, 1 degraded (economy with backup), 1 unhealthy (plot)
            assertThat(report).contains("Gesund: 3");
            assertThat(report).contains("Degradiert: 1");
            assertThat(report).contains("Ungesund: 1");
        }

        @Test
        @DisplayName("getHealthReport() should show all healthy when all systems OK")
        void getHealthReportShouldShowAllHealthyWhenOK() {
            String report = HealthCheckManager.getHealthReport();

            assertThat(report).contains("Gesund: 5");
            assertThat(report).contains("Degradiert: 0");
            assertThat(report).contains("Ungesund: 0");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // QUICK STATUS TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Quick Status")
    class QuickStatusTests {

        @Test
        @DisplayName("getQuickStatus() should show HEALTHY when all systems OK")
        void getQuickStatusShouldShowHealthy() {
            String status = HealthCheckManager.getQuickStatus();

            assertThat(status).contains("System Status:");
            assertThat(status).contains("GESUND");
        }

        @Test
        @DisplayName("getQuickStatus() should show DEGRADED when system degraded")
        void getQuickStatusShouldShowDegraded() {
            economyMock.when(EconomyManager::isHealthy).thenReturn(false);
            economyMock.when(EconomyManager::getLastError).thenReturn("Using backup");

            String status = HealthCheckManager.getQuickStatus();

            assertThat(status).contains("EINGESCHRÄNKT");
        }

        @Test
        @DisplayName("getQuickStatus() should show UNHEALTHY when system unhealthy")
        void getQuickStatusShouldShowUnhealthy() {
            plotMock.when(PlotManager::isHealthy).thenReturn(false);
            plotMock.when(PlotManager::getLastError).thenReturn("Error");

            String status = HealthCheckManager.getQuickStatus();

            assertThat(status).contains("UNGESUND");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LOG HEALTH CHECK TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Health Check Logging")
    class LogHealthCheckTests {

        @Test
        @DisplayName("logHealthCheck() should not throw exceptions")
        void logHealthCheckShouldNotThrow() {
            assertThatCode(() -> HealthCheckManager.logHealthCheck())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("logHealthCheck() should handle all healthy systems")
        void logHealthCheckShouldHandleAllHealthy() {
            assertThatCode(() -> HealthCheckManager.logHealthCheck())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("logHealthCheck() should handle degraded systems")
        void logHealthCheckShouldHandleDegraded() {
            economyMock.when(EconomyManager::isHealthy).thenReturn(false);
            economyMock.when(EconomyManager::getLastError).thenReturn("Using backup");

            assertThatCode(() -> HealthCheckManager.logHealthCheck())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("logHealthCheck() should handle unhealthy systems")
        void logHealthCheckShouldHandleUnhealthy() {
            plotMock.when(PlotManager::isHealthy).thenReturn(false);
            plotMock.when(PlotManager::getLastError).thenReturn("Critical error");

            assertThatCode(() -> HealthCheckManager.logHealthCheck())
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // INTEGRATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("complete workflow: all systems healthy")
        void completeWorkflowAllHealthy() {
            Map<String, HealthCheckManager.ComponentHealth> results =
                HealthCheckManager.checkAllSystems();

            HealthCheckManager.SystemHealth overall = HealthCheckManager.getOverallHealth();
            boolean allHealthy = HealthCheckManager.isAllHealthy();
            String report = HealthCheckManager.getHealthReport();
            String quickStatus = HealthCheckManager.getQuickStatus();

            assertThat(results).hasSize(5);
            assertThat(overall).isEqualTo(HealthCheckManager.SystemHealth.HEALTHY);
            assertThat(allHealthy).isTrue();
            assertThat(report).contains("Gesund: 5");
            assertThat(quickStatus).contains("GESUND");
        }

        @Test
        @DisplayName("complete workflow: mixed health status")
        void completeWorkflowMixedStatus() {
            economyMock.when(EconomyManager::isHealthy).thenReturn(false);
            economyMock.when(EconomyManager::getLastError).thenReturn("Using backup");

            plotMock.when(PlotManager::isHealthy).thenReturn(false);
            plotMock.when(PlotManager::getLastError).thenReturn("Critical failure");

            Map<String, HealthCheckManager.ComponentHealth> results =
                HealthCheckManager.checkAllSystems();

            HealthCheckManager.SystemHealth overall = HealthCheckManager.getOverallHealth();
            boolean allHealthy = HealthCheckManager.isAllHealthy();
            String report = HealthCheckManager.getHealthReport();

            assertThat(results).hasSize(5);
            assertThat(overall).isEqualTo(HealthCheckManager.SystemHealth.UNHEALTHY);
            assertThat(allHealthy).isFalse();
            assertThat(report).contains("Gesund: 3");
            assertThat(report).contains("Degradiert: 1");
            assertThat(report).contains("Ungesund: 1");
        }
    }
}
