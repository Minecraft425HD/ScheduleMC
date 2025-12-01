package de.rolandsw.schedulemc.warehouse;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Konfiguration für das Warehouse-System
 * Nutzt die zentrale Config von ModConfigHandler
 */
public class WarehouseConfig {

    public static ForgeConfigSpec.IntValue SLOT_COUNT;
    public static ForgeConfigSpec.IntValue MAX_CAPACITY_PER_SLOT;
    public static ForgeConfigSpec.IntValue DELIVERY_INTERVAL_DAYS;

    /**
     * Initialisiert die Config-Referenzen nach dem Laden der Mod-Config
     */
    public static void init() {
        SLOT_COUNT = ModConfigHandler.COMMON.WAREHOUSE_SLOT_COUNT;
        MAX_CAPACITY_PER_SLOT = ModConfigHandler.COMMON.WAREHOUSE_MAX_CAPACITY_PER_SLOT;
        DELIVERY_INTERVAL_DAYS = ModConfigHandler.COMMON.WAREHOUSE_DELIVERY_INTERVAL_DAYS;
    }
}
