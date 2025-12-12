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

    public static final Part ENGINE_3_CYLINDER = new PartEngine3Cylinder();
    public static final Part ENGINE_6_CYLINDER = new PartEngine6Cylinder();
    public static final Part ENGINE_TRUCK = new PartEngineTruck();

    public static final Part IRON_LICENSE_PLATE_HOLDER = new PartLicensePlateHolder(new ResourceLocation("textures/block/iron_block.png"));

    public static final Part OAK_BODY = new PartBodyWood(new ResourceLocation(Main.MODID, "textures/entity/car_wood_oak.png"), "oak");
    public static final Part BIG_OAK_BODY = new PartBodyBigWood(new ResourceLocation(Main.MODID, "textures/entity/car_big_wood_oak.png"), "oak");
    public static final Part WHITE_SPORT_BODY = new PartBodySport(new ResourceLocation(Main.MODID, "textures/entity/car_sport_white.png"), "white");
    public static final Part WHITE_TRANSPORTER_BODY = new PartBodyTransporter(new ResourceLocation(Main.MODID, "textures/entity/car_transporter_white.png"), "white");
    public static final Part WHITE_SUV_BODY = new PartBodySUV(new ResourceLocation(Main.MODID, "textures/entity/car_suv_white.png"), "white");
    public static final Part WHITE_CONTAINER = new PartContainer(new ResourceLocation(Main.MODID, "textures/entity/container_white.png"));
    public static final Part WHITE_TANK_CONTAINER = new PartTankContainer(new ResourceLocation(Main.MODID, "textures/entity/tank_container_white.png"));

    public static final Part OAK_BUMPER = new PartBumper(new ResourceLocation("textures/block/oak_planks.png"));

    public static final Part SMALL_TANK = new PartTank(() -> ModConfigHandler.CAR_SERVER.tankSmallMaxFuel.get());
    public static final Part MEDIUM_TANK = new PartTank(() -> ModConfigHandler.CAR_SERVER.tankMediumMaxFuel.get());
    public static final Part LARGE_TANK = new PartTank(() -> ModConfigHandler.CAR_SERVER.tankLargeMaxFuel.get());

    public static final Part WHEEL = new PartWheel(
            new OBJModel(new ResourceLocation(Main.MODID, "models/entity/wheel.obj")),
            new ResourceLocation(Main.MODID, "textures/entity/wheel.png"),
            120F,
            0.5F
    );

    public static final Part BIG_WHEEL = new PartWheelBig(
            new OBJModel(new ResourceLocation(Main.MODID, "models/entity/big_wheel.obj")),
            new ResourceLocation(Main.MODID, "textures/entity/big_wheel.png"),
            105F,
            1F
    );

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
