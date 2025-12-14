package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.client.obj.OBJModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class PartRegistry {

    // Engines (Motors)
    public static final Part NORMAL_MOTOR = new PartNormalMotor();
    public static final Part PERFORMANCE_MOTOR = new PartPerformanceMotor();
    public static final Part INDUSTRIAL_MOTOR = new PartIndustrialMotor();

    // Bodies (Chassis)
    public static final Part LIMOUSINE_CHASSIS = new PartLimousineChassis(new ResourceLocation(Main.MODID, "textures/entity/vehicle_wood_oak.png"), "oak");
    public static final Part VAN_CHASSIS = new PartVanChassis(new ResourceLocation(Main.MODID, "textures/entity/vehicle_big_wood_oak.png"), "oak");
    public static final Part LUXUS_CHASSIS = new PartLuxusChassis(new ResourceLocation(Main.MODID, "textures/entity/vehicle_sport_white.png"), "white");
    public static final Part LKW_CHASSIS = new PartLkwChassis(new ResourceLocation(Main.MODID, "textures/entity/vehicle_transporter_white.png"), "white");
    public static final Part OFFROAD_CHASSIS = new PartOffroadChassis(new ResourceLocation(Main.MODID, "textures/entity/vehicle_suv_white.png"), "white");

    // Wheels (Tires)
    public static final Part STANDARD_TIRE = new PartStandardTire(
            new OBJModel(new ResourceLocation(Main.MODID, "models/entity/wheel.obj")),
            new ResourceLocation(Main.MODID, "textures/entity/wheel.png"),
            120F,
            0.5F
    );

    public static final Part OFFROAD_TIRE = new PartOffroadTire(
            new OBJModel(new ResourceLocation(Main.MODID, "models/entity/big_wheel.obj")),
            new ResourceLocation(Main.MODID, "textures/entity/big_wheel.png"),
            105F,
            1F
    );

    // Fuel Tanks
    public static final Part TANK_15L = new PartTank(() -> ModConfigHandler.VEHICLE_SERVER.tankSmallMaxFuel.get());
    public static final Part TANK_30L = new PartTank(() -> ModConfigHandler.VEHICLE_SERVER.tankMediumMaxFuel.get());
    public static final Part TANK_50L = new PartTank(() -> ModConfigHandler.VEHICLE_SERVER.tankLargeMaxFuel.get());

    // Modules & Accessories
    public static final Part CARGO_MODULE = new PartContainer(new ResourceLocation(Main.MODID, "textures/entity/container_white.png"));
    public static final Part FLUID_MODULE = new PartTankContainer(new ResourceLocation(Main.MODID, "textures/entity/tank_container_white.png"));
    public static final Part STANDARD_FRONT_FENDER = new PartBumper(new ResourceLocation("textures/block/oak_planks.png"));
    public static final Part LICENSE_PLATE_HOLDER = new PartLicensePlateHolder(new ResourceLocation("textures/block/iron_block.png"));

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
