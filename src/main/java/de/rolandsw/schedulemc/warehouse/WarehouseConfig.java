package de.rolandsw.schedulemc.warehouse;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Konfiguration für das Warehouse-System
 */
public class WarehouseConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue SLOT_COUNT;
    public static final ForgeConfigSpec.IntValue MAX_CAPACITY_PER_SLOT;
    public static final ForgeConfigSpec.IntValue DELIVERY_INTERVAL_DAYS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Warehouse System Settings").push("warehouse");

        SLOT_COUNT = builder
            .comment("Anzahl verschiedener Item-Slots pro Warehouse")
            .defineInRange("slotCount", 32, 8, 128);

        MAX_CAPACITY_PER_SLOT = builder
            .comment("Maximale Item-Menge pro Slot (16 Stacks = 1024)")
            .defineInRange("maxCapacityPerSlot", 1024, 64, 10000);

        DELIVERY_INTERVAL_DAYS = builder
            .comment("Lieferungs-Intervall in Minecraft-Tagen")
            .defineInRange("deliveryIntervalDays", 3, 1, 30);

        builder.pop();
        SPEC = builder.build();
    }
}
