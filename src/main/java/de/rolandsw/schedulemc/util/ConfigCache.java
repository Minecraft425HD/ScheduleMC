package de.rolandsw.schedulemc.util;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraftforge.common.ForgeConfigSpec;

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

    // Cache für Police-AI Werte
    private static int policeDetectionRadius = -1;
    private static int policeArrestCooldownSeconds = -1;
    private static double policeArrestDistance = -1;

    // Cache für Warehouse Werte
    private static int warehouseDeliveryIntervalDays = -1;

    // Cache für Economy Werte
    private static double economyStartBalance = -1;
    private static double overdraftLimit = -1;
    private static double taxRate = -1;

    // Cache für Production Werte
    private static int productionGrowthTicks = -1;
    private static double productionYieldMultiplier = -1;

    // Cache für Vehicle Werte
    private static double vehicleFuelConsumptionRate = -1;
    private static int vehicleMaxSpeed = -1;

    // Cache für NPC Werte
    private static int npcSalaryAmount = -1;
    private static int npcSalaryIntervalDays = -1;

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
            policeArrestCooldownSeconds = getConfigSafe(ModConfigHandler.COMMON.POLICE_ARREST_COOLDOWN_SECONDS, 5);
            policeArrestDistance = getConfigSafe(ModConfigHandler.COMMON.POLICE_ARREST_DISTANCE, 2.5);

            // Warehouse Werte
            warehouseDeliveryIntervalDays = getConfigSafe(ModConfigHandler.COMMON.WAREHOUSE_DELIVERY_INTERVAL_DAYS, 1);

            // Economy Werte
            economyStartBalance = getConfigSafe(ModConfigHandler.COMMON.START_BALANCE, 1000.0);
            overdraftLimit = getConfigSafe(ModConfigHandler.COMMON.OVERDRAFT_LIMIT, -500.0);
            taxRate = getConfigSafe(ModConfigHandler.COMMON.TAX_RATE, 0.1);

            // Production Werte
            productionGrowthTicks = getConfigSafe(ModConfigHandler.COMMON.PRODUCTION_GROWTH_TICKS, 1200);
            productionYieldMultiplier = getConfigSafe(ModConfigHandler.COMMON.PRODUCTION_YIELD_MULTIPLIER, 1.0);

            // Vehicle Werte
            vehicleFuelConsumptionRate = getConfigSafe(ModConfigHandler.COMMON.VEHICLE_FUEL_CONSUMPTION_RATE, 1.0);
            vehicleMaxSpeed = getConfigSafe(ModConfigHandler.COMMON.VEHICLE_MAX_SPEED, 200);

            // NPC Werte
            npcSalaryAmount = getConfigSafe(ModConfigHandler.COMMON.NPC_SALARY_AMOUNT, 100);
            npcSalaryIntervalDays = getConfigSafe(ModConfigHandler.COMMON.NPC_SALARY_INTERVAL_DAYS, 1);

            needsRefresh = false;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getConfigSafe(ForgeConfigSpec.ConfigValue<T> config, T defaultValue) {
        try {
            T value = config.get();
            return value != null ? value : defaultValue;
        } catch (Exception e) {
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

    public static double getOverdraftLimit() {
        refreshIfNeeded();
        return overdraftLimit;
    }

    public static double getTaxRate() {
        refreshIfNeeded();
        return taxRate;
    }

    // ═══════════════════════════════════════════════════════════
    // PRODUCTION GETTER
    // ═══════════════════════════════════════════════════════════

    public static int getProductionGrowthTicks() {
        refreshIfNeeded();
        return productionGrowthTicks;
    }

    public static double getProductionYieldMultiplier() {
        refreshIfNeeded();
        return productionYieldMultiplier;
    }

    // ═══════════════════════════════════════════════════════════
    // VEHICLE GETTER
    // ═══════════════════════════════════════════════════════════

    public static double getVehicleFuelConsumptionRate() {
        refreshIfNeeded();
        return vehicleFuelConsumptionRate;
    }

    public static int getVehicleMaxSpeed() {
        refreshIfNeeded();
        return vehicleMaxSpeed;
    }

    // ═══════════════════════════════════════════════════════════
    // NPC GETTER
    // ═══════════════════════════════════════════════════════════

    public static int getNpcSalaryAmount() {
        refreshIfNeeded();
        return npcSalaryAmount;
    }

    public static int getNpcSalaryIntervalDays() {
        refreshIfNeeded();
        return npcSalaryIntervalDays;
    }

    public static long getNpcSalaryIntervalTicks() {
        return getNpcSalaryIntervalDays() * 24000L; // 1 day = 24000 ticks
    }

    private ConfigCache() {
        throw new UnsupportedOperationException("Utility class");
    }
}
