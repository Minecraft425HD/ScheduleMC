package de.rolandsw.schedulemc.api;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.api.economy.IEconomyAPI;
import de.rolandsw.schedulemc.api.plot.IPlotAPI;
import de.rolandsw.schedulemc.api.production.IProductionAPI;
import de.rolandsw.schedulemc.api.npc.INPCAPI;
import de.rolandsw.schedulemc.api.police.IPoliceAPI;
import de.rolandsw.schedulemc.api.warehouse.IWarehouseAPI;
import de.rolandsw.schedulemc.api.messaging.IMessagingAPI;
import de.rolandsw.schedulemc.api.smartphone.ISmartphoneAPI;
import de.rolandsw.schedulemc.api.vehicle.IVehicleAPI;
import de.rolandsw.schedulemc.api.achievement.IAchievementAPI;
import de.rolandsw.schedulemc.api.market.IMarketAPI;
import org.slf4j.Logger;

/**
 * ScheduleMC Public API - Zentraler Einstiegspunkt
 *
 * Ermöglicht externen Mods und Plugins Zugriff auf alle ScheduleMC-Features.
 *
 * Verwendung:
 * ```java
 * ScheduleMCAPI api = ScheduleMCAPI.getInstance();
 *
 * // Economy
 * IEconomyAPI economy = api.getEconomyAPI();
 * economy.deposit(playerUUID, 1000.0);
 *
 * // Plots
 * IPlotAPI plots = api.getPlotAPI();
 * Optional<PlotRegion> plot = plots.getPlotAt(blockPos);
 *
 * // Production
 * IProductionAPI production = api.getProductionAPI();
 * production.registerCustomPlant(...);
 * ```
 *
 * @version 3.2.0
 * @since 2025-12-20
 */
public class ScheduleMCAPI {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String VERSION = "3.2.0";

    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile ScheduleMCAPI instance;

    // API Implementations
    private IEconomyAPI economyAPI;
    private IPlotAPI plotAPI;
    private IProductionAPI productionAPI;
    private INPCAPI npcAPI;
    private IPoliceAPI policeAPI;
    private IWarehouseAPI warehouseAPI;
    private IMessagingAPI messagingAPI;
    private ISmartphoneAPI smartphoneAPI;
    private IVehicleAPI vehicleAPI;
    private IAchievementAPI achievementAPI;
    private IMarketAPI marketAPI;

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private ScheduleMCAPI() {
        LOGGER.info("ScheduleMC API v{} initialized", VERSION);
    }

    public static ScheduleMCAPI getInstance() {
        if (instance == null) {
            synchronized (ScheduleMCAPI.class) {
                if (instance == null) {
                    instance = new ScheduleMCAPI();
                }
            }
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // API GETTERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Economy API - Konten, Transaktionen, Zinsen, etc.
     */
    public IEconomyAPI getEconomyAPI() {
        if (economyAPI == null) {
            throw new IllegalStateException("EconomyAPI not initialized! Call ScheduleMCAPI.initialize() first.");
        }
        return economyAPI;
    }

    /**
     * Plot API - Grundstücke, Besitz, Schutz, etc.
     */
    public IPlotAPI getPlotAPI() {
        if (plotAPI == null) {
            throw new IllegalStateException("PlotAPI not initialized! Call ScheduleMCAPI.initialize() first.");
        }
        return plotAPI;
    }

    /**
     * Production API - Pflanzen, Drogen, Qualität, etc.
     */
    public IProductionAPI getProductionAPI() {
        if (productionAPI == null) {
            throw new IllegalStateException("ProductionAPI not initialized! Call ScheduleMCAPI.initialize() first.");
        }
        return productionAPI;
    }

    /**
     * NPC API - NPCs, AI, Schedules, Shops, etc.
     */
    public INPCAPI getNPCAPI() {
        if (npcAPI == null) {
            throw new IllegalStateException("NPCAPI not initialized! Call ScheduleMCAPI.initialize() first.");
        }
        return npcAPI;
    }

    /**
     * Police API - Wanted System, Prison, Crime Detection, etc.
     */
    public IPoliceAPI getPoliceAPI() {
        if (policeAPI == null) {
            throw new IllegalStateException("PoliceAPI not initialized! Call ScheduleMCAPI.initialize() first.");
        }
        return policeAPI;
    }

    /**
     * Warehouse API - Lagerverwaltung, Lieferungen, etc.
     */
    public IWarehouseAPI getWarehouseAPI() {
        if (warehouseAPI == null) {
            throw new IllegalStateException("WarehouseAPI not initialized! Call ScheduleMCAPI.initialize() first.");
        }
        return warehouseAPI;
    }

    /**
     * Messaging API - Nachrichten, Chat, etc.
     */
    public IMessagingAPI getMessagingAPI() {
        if (messagingAPI == null) {
            throw new IllegalStateException("MessagingAPI not initialized! Call ScheduleMCAPI.initialize() first.");
        }
        return messagingAPI;
    }

    /**
     * Smartphone API - Apps, Notifications, etc.
     */
    public ISmartphoneAPI getSmartphoneAPI() {
        if (smartphoneAPI == null) {
            throw new IllegalStateException("SmartphoneAPI not initialized! Call ScheduleMCAPI.initialize() first.");
        }
        return smartphoneAPI;
    }

    /**
     * Vehicle API - Fahrzeuge, Fuel, Werkstatt, etc.
     */
    public IVehicleAPI getVehicleAPI() {
        if (vehicleAPI == null) {
            throw new IllegalStateException("VehicleAPI not initialized! Call ScheduleMCAPI.initialize() first.");
        }
        return vehicleAPI;
    }

    /**
     * Achievement API - Erfolge, Rewards, etc.
     */
    public IAchievementAPI getAchievementAPI() {
        if (achievementAPI == null) {
            throw new IllegalStateException("AchievementAPI not initialized! Call ScheduleMCAPI.initialize() first.");
        }
        return achievementAPI;
    }

    /**
     * Market API - Dynamischer Markt, Preise, Supply & Demand, etc.
     */
    public IMarketAPI getMarketAPI() {
        if (marketAPI == null) {
            throw new IllegalStateException("MarketAPI not initialized! Call ScheduleMCAPI.initialize() first.");
        }
        return marketAPI;
    }

    // ═══════════════════════════════════════════════════════════
    // INITIALIZATION (Internal - Called by Mod)
    // ═══════════════════════════════════════════════════════════

    /**
     * Initialisiert alle APIs (wird vom Mod aufgerufen)
     */
    public void initialize(
        IEconomyAPI economyAPI,
        IPlotAPI plotAPI,
        IProductionAPI productionAPI,
        INPCAPI npcAPI,
        IPoliceAPI policeAPI,
        IWarehouseAPI warehouseAPI,
        IMessagingAPI messagingAPI,
        ISmartphoneAPI smartphoneAPI,
        IVehicleAPI vehicleAPI,
        IAchievementAPI achievementAPI,
        IMarketAPI marketAPI
    ) {
        this.economyAPI = economyAPI;
        this.plotAPI = plotAPI;
        this.productionAPI = productionAPI;
        this.npcAPI = npcAPI;
        this.policeAPI = policeAPI;
        this.warehouseAPI = warehouseAPI;
        this.messagingAPI = messagingAPI;
        this.smartphoneAPI = smartphoneAPI;
        this.vehicleAPI = vehicleAPI;
        this.achievementAPI = achievementAPI;
        this.marketAPI = marketAPI;

        LOGGER.info("ScheduleMC API v{} fully initialized with {} subsystems (extended API)", VERSION, 11);
    }

    // ═══════════════════════════════════════════════════════════
    // METADATA
    // ═══════════════════════════════════════════════════════════

    /**
     * API Version
     */
    public String getVersion() {
        return VERSION;
    }

    /**
     * Prüft ob API initialisiert ist
     */
    public boolean isInitialized() {
        return economyAPI != null
            && plotAPI != null
            && productionAPI != null
            && npcAPI != null
            && policeAPI != null
            && warehouseAPI != null
            && messagingAPI != null
            && smartphoneAPI != null
            && vehicleAPI != null
            && achievementAPI != null
            && marketAPI != null;
    }

    /**
     * Gibt Status aller APIs zurück
     */
    public String getStatus() {
        return String.format(
            "ScheduleMC API v%s - %s\n" +
            "  Economy:      %s\n" +
            "  Plot:         %s\n" +
            "  Production:   %s\n" +
            "  NPC:          %s\n" +
            "  Police:       %s\n" +
            "  Warehouse:    %s\n" +
            "  Messaging:    %s\n" +
            "  Smartphone:   %s\n" +
            "  Vehicle:      %s\n" +
            "  Achievement:  %s\n" +
            "  Market:       %s",
            VERSION,
            isInitialized() ? "READY" : "NOT INITIALIZED",
            economyAPI != null ? "✓" : "✗",
            plotAPI != null ? "✓" : "✗",
            productionAPI != null ? "✓" : "✗",
            npcAPI != null ? "✓" : "✗",
            policeAPI != null ? "✓" : "✗",
            warehouseAPI != null ? "✓" : "✗",
            messagingAPI != null ? "✓" : "✗",
            smartphoneAPI != null ? "✓" : "✗",
            vehicleAPI != null ? "✓" : "✗",
            achievementAPI != null ? "✓" : "✗",
            marketAPI != null ? "✓" : "✗"
        );
    }
}
