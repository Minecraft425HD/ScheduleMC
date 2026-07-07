package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache für häufig abgefragte Config-Werte
 *
 * PERFORMANCE: Vermeidet wiederholte Config-Lookups jeden Tick.
 * Config-Werte werden einmalig geladen und nur bei Änderungen aktualisiert.
 *
 * Verwendung:
 * <pre>
 * // Statt: ModConfigHandler.COMMON.POLICE_DETECTION_RADIUS.get()
 * // Nutze: ConfigCache.getPoliceDetectionRadius()
 * </pre>
 */
public class ConfigCache {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Cache für Police-AI Werte
    private static volatile int policeDetectionRadius = -1;
    private static volatile int policeArrestCooldownSeconds = -1;
    private static volatile double policeArrestDistance = -1;
    // Police-Search/Hiding (heißester Pfad: pro Polizist × Spieler)
    private static volatile boolean policeIndoorHidingEnabled = true;
    private static volatile int policeSearchDurationSeconds = 60;
    private static volatile int policeSearchTargetUpdateSeconds = 10;
    private static volatile int policeSearchRadius = 50;

    // Cache für Warehouse Werte
    private static volatile int warehouseDeliveryIntervalDays = -1;

    // Cache für Economy Werte
    private static volatile double economyStartBalance = -1;

    // Invalidation flag
    private static volatile boolean needsRefresh = true;

    /**
     * Markiert den Cache als ungültig (bei Config-Reload)
     */
    public static void invalidate() {
        needsRefresh = true;
    }

    /**
     * Lädt alle gecachten Werte neu
     */
    private static void refreshIfNeeded() {
        if (!needsRefresh) return;

        synchronized (ConfigCache.class) {
            if (!needsRefresh) return;

            // Police-AI Werte
            policeDetectionRadius = getConfigSafe(ModConfigHandler.COMMON.POLICE_DETECTION_RADIUS, 32);
            policeArrestCooldownSeconds = getConfigSafe(ModConfigHandler.COMMON.POLICE_ARREST_COOLDOWN_SECONDS, 6);
            policeArrestDistance = getConfigSafe(ModConfigHandler.COMMON.POLICE_ARREST_DISTANCE, 2.5);
            policeIndoorHidingEnabled = getConfigSafe(ModConfigHandler.COMMON.POLICE_INDOOR_HIDING_ENABLED, true);
            policeSearchDurationSeconds = getConfigSafe(ModConfigHandler.COMMON.POLICE_SEARCH_DURATION_SECONDS, 60);
            policeSearchTargetUpdateSeconds = getConfigSafe(ModConfigHandler.COMMON.POLICE_SEARCH_TARGET_UPDATE_SECONDS, 10);
            policeSearchRadius = getConfigSafe(ModConfigHandler.COMMON.POLICE_SEARCH_RADIUS, 50);

            // Warehouse Werte
            warehouseDeliveryIntervalDays = getConfigSafe(ModConfigHandler.COMMON.WAREHOUSE_DELIVERY_INTERVAL_DAYS, 1);

            // Economy Werte
            economyStartBalance = getConfigSafe(ModConfigHandler.COMMON.START_BALANCE, 1000.0);

            needsRefresh = false;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getConfigSafe(ForgeConfigSpec.ConfigValue<T> config, T defaultValue) {
        try {
            T value = config.get();
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            LOGGER.debug("Config access failed, using default value: {}", defaultValue, e);
            return defaultValue;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // POLICE-AI GETTER
    // ═══════════════════════════════════════════════════════════

    public static int getPoliceDetectionRadius() {
        refreshIfNeeded();
        return policeDetectionRadius;
    }

    public static int getPoliceArrestCooldownSeconds() {
        refreshIfNeeded();
        return policeArrestCooldownSeconds;
    }

    public static double getPoliceArrestDistance() {
        refreshIfNeeded();
        return policeArrestDistance;
    }

    public static long getPoliceArrestCooldownTicks() {
        return getPoliceArrestCooldownSeconds() * 20L;
    }

    public static boolean isPoliceIndoorHidingEnabled() {
        refreshIfNeeded();
        return policeIndoorHidingEnabled;
    }

    public static int getPoliceSearchDurationSeconds() {
        refreshIfNeeded();
        return policeSearchDurationSeconds;
    }

    public static int getPoliceSearchTargetUpdateSeconds() {
        refreshIfNeeded();
        return policeSearchTargetUpdateSeconds;
    }

    public static int getPoliceSearchRadius() {
        refreshIfNeeded();
        return policeSearchRadius;
    }

    // ═══════════════════════════════════════════════════════════
    // WAREHOUSE GETTER
    // ═══════════════════════════════════════════════════════════

    public static int getWarehouseDeliveryIntervalDays() {
        refreshIfNeeded();
        return warehouseDeliveryIntervalDays;
    }

    // ═══════════════════════════════════════════════════════════
    // ECONOMY GETTER
    // ═══════════════════════════════════════════════════════════

    public static double getEconomyStartBalance() {
        refreshIfNeeded();
        return economyStartBalance;
    }

    private ConfigCache() {
        throw new UnsupportedOperationException("Utility class");
    }
}
