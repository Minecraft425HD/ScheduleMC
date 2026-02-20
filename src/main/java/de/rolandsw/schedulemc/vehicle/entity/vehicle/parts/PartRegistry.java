package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.client.obj.OBJModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class PartRegistry {

    private static final Map<String, Part> ID_TO_PART = new HashMap<>();
    private static final Map<Part, String> PART_TO_ID = new IdentityHashMap<>();

    private static <T extends Part> T register(String id, T part) {
        ID_TO_PART.put(id, part);
        PART_TO_ID.put(part, id);
        return part;
    }

    public static Part getById(String id) {
        return ID_TO_PART.get(id);
    }

    public static String getIdFor(Part part) {
        return PART_TO_ID.get(part);
    }

    // Engines (Motors)
    public static final Part NORMAL_MOTOR = register("normal_motor", new PartNormalMotor());
    public static final Part PERFORMANCE_MOTOR = register("performance_motor", new PartPerformanceMotor());
    public static final Part PERFORMANCE_2_MOTOR = register("performance_2_motor", new PartPerformance2Motor());

    // Bodies (Chassis)
    public static final Part LIMOUSINE_CHASSIS = register("limousine_chassis", new PartLimousineChassis(ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/vehicle_wood_oak.png"), "white"));
    public static final Part VAN_CHASSIS = register("van_chassis", new PartVanChassis(ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/vehicle_big_wood_oak.png"), "white"));
    public static final Part LUXUS_CHASSIS = register("luxus_chassis", new PartLuxusChassis(ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/vehicle_sport_white.png"), "white"));
    public static final Part TRUCK_CHASSIS = register("truck_chassis", new PartTruckChassis(ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/vehicle_transporter_white.png"), "white"));
    public static final Part OFFROAD_CHASSIS = register("offroad_chassis", new PartOffroadChassis(ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/vehicle_suv_white.png"), "white"));

    // Wheels (Tires) - Normal Vehicles
    public static final Part STANDARD_TIRE = register("standard_tire", new PartStandardTire(
            new OBJModel(ResourceLocation.fromNamespaceAndPath(Main.MODID, "models/entity/wheel.obj")),
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/wheel.png"),
            120F,
            0.5F
    ));

    public static final Part SPORT_TIRE = register("sport_tire", new PartSportTire(
            new OBJModel(ResourceLocation.fromNamespaceAndPath(Main.MODID, "models/entity/wheel.obj")),
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/sport_wheel.png"),
            130F,
            0.6F
    ));

    public static final Part PREMIUM_TIRE = register("premium_tire", new PartPremiumTire(
            new OBJModel(ResourceLocation.fromNamespaceAndPath(Main.MODID, "models/entity/wheel.obj")),
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/premium_wheel.png"),
            140F,
            0.7F
    ));

    // Wheels (Tires) - Trucks
    public static final Part OFFROAD_TIRE = register("offroad_tire", new PartOffroadTire(
            new OBJModel(ResourceLocation.fromNamespaceAndPath(Main.MODID, "models/entity/big_wheel.obj")),
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/big_wheel.png"),
            105F,
            1F
    ));

    public static final Part ALLTERRAIN_TIRE = register("allterrain_tire", new PartAllterrainTire(
            new OBJModel(ResourceLocation.fromNamespaceAndPath(Main.MODID, "models/entity/big_wheel.obj")),
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/allterrain_wheel.png"),
            115F,
            1.2F
    ));

    public static final Part HEAVY_DUTY_TIRE = register("heavyduty_tire", new PartHeavyDutyTire(
            new OBJModel(ResourceLocation.fromNamespaceAndPath(Main.MODID, "models/entity/big_wheel.obj")),
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/heavyduty_wheel.png"),
            125F,
            1.4F
    ));

    // Fuel Tanks
    public static final Part TANK_15L = register("tank_15l", new PartTank(() -> ModConfigHandler.VEHICLE_SERVER.tankSmallMaxFuel.get()));
    public static final Part TANK_30L = register("tank_30l", new PartTank(() -> ModConfigHandler.VEHICLE_SERVER.tankMediumMaxFuel.get()));
    public static final Part TANK_50L = register("tank_50l", new PartTank(() -> ModConfigHandler.VEHICLE_SERVER.tankLargeMaxFuel.get()));

    // Modules & Accessories
    public static final Part CARGO_MODULE = register("cargo_module", new PartContainer(ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/container_white.png")));
    public static final Part FLUID_MODULE = register("fluid_module", new PartTankContainer(ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/tank_container_white.png")));
    public static final Part LICENSE_PLATE_HOLDER = register("license_sign_mount", new PartLicensePlateHolder(ResourceLocation.parse("textures/block/iron_block.png")));

    // Front Fenders (Bumpers)
    public static final Part FENDER_BASIC = register("fender_basic", new PartBumper(ResourceLocation.parse("textures/block/oak_planks.png")));
    public static final Part FENDER_CHROME = register("fender_chrome", new PartChromeBumper(ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/parts/fender_chrome.png")));
    public static final Part FENDER_SPORT = register("fender_sport", new PartSportBumper(ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/parts/fender_sport.png")));

    public static boolean isValid(EntityGenericVehicle vehicle, List<Component> messages) {
        return isValid(vehicle.getModelParts(), messages);
    }

    public static boolean isValid(List<Part> modelParts, List<Component> messages) {
        int bodyAmount = Part.getAmount(modelParts, part -> part instanceof PartBody);
        if (bodyAmount <= 0) {
            messages.add(Component.translatable("message.parts.no_body"));
            return false;
        } else if (bodyAmount > 1) {
            messages.add(Component.translatable("message.parts.too_many_bodies"));
            return false;
        }

        List<Part> unmodifiableList = Collections.unmodifiableList(modelParts);

        boolean flag = true;

        for (Part part : modelParts) {
            if (!part.validate(unmodifiableList, messages)) {
                flag = false;
            }
        }
        return flag;
    }

}
