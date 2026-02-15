package de.rolandsw.schedulemc.config;

import de.maxhenkel.corelib.config.ConfigBase;
import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig extends ConfigBase {

    public final ForgeConfigSpec.DoubleValue vehicleVolume;
    public final ForgeConfigSpec.BooleanValue thirdPersonEnter;
    public final ForgeConfigSpec.BooleanValue tempInFarenheit;
    public final ForgeConfigSpec.DoubleValue vehicleZoom;
    public final ForgeConfigSpec.BooleanValue debugLogging;

    public ClientConfig(ForgeConfigSpec.Builder builder) {
        super(builder);
        thirdPersonEnter = builder.define("vehicle.third_person_when_enter_vehicle", true);
        tempInFarenheit = builder.comment("True if the vehicle temperature should be displayed in farenheit").define("vehicle.temp_farenheit", false);
        vehicleVolume = builder.defineInRange("vehicle.vehicle_volume", 0.25D, 0D, 1D);
        vehicleZoom = builder.defineInRange("vehicle.third_person_zoom", 6D, 1D, 20D);

        builder.comment("Debug Settings").push("debug");
        debugLogging = builder
                .comment("Enable debug logging to console (shows detailed save operations, vehicle state, etc.)")
                .define("enable_debug_logging", false);
        builder.pop();
    }
}
