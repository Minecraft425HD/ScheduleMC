package de.rolandsw.schedulemc.config;

import de.maxhenkel.corelib.config.ConfigBase;
import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig extends ConfigBase {

    public final ForgeConfigSpec.DoubleValue carVolume;
    public final ForgeConfigSpec.BooleanValue thirdPersonEnter;
    public final ForgeConfigSpec.BooleanValue tempInFarenheit;
    public final ForgeConfigSpec.DoubleValue carZoom;

    public ClientConfig(ForgeConfigSpec.Builder builder) {
        super(builder);
        thirdPersonEnter = builder.define("vehicle.third_person_when_enter_car", true);
        tempInFarenheit = builder.comment("True if the vehicle temperature should be displayed in farenheit").define("vehicle.temp_farenheit", false);
        carVolume = builder.defineInRange("vehicle.car_volume", 0.25D, 0D, 1D);
        carZoom = builder.defineInRange("vehicle.third_person_zoom", 6D, 1D, 20D);
    }
}
