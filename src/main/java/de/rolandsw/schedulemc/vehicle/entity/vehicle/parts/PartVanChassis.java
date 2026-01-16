package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import org.joml.Vector3d;
import de.rolandsw.schedulemc.vehicle.Main;
import de.maxhenkel.corelib.client.obj.OBJModel;
import net.minecraft.resources.ResourceLocation;

public class PartVanChassis extends PartChassisBase {

    public PartVanChassis(ResourceLocation texture, String materialTranslationKey) {
        super(new OBJModel(ResourceLocation.fromNamespaceAndPath(Main.MODID, "models/entity/wood_body_big.obj")), texture, new Vector3d(0D, 4D / 16D, 0D), "big_wood", materialTranslationKey);

        this.bumperOffset = new Vector3d(0D, 6D / 16D, -19.5D / 16D);
        this.wheelOffsets = new Vector3d[]{
                new Vector3d(12.5F / 16F, 4F / 16F, 10F / 16F),
                new Vector3d(12.5F / 16F, 4F / 16F, -13F / 16F),
                new Vector3d(-12.5F / 16F, 4F / 16F, 10F / 16F),
                new Vector3d(-12.5F / 16F, 4F / 16F, -13F / 16F)
        };
        this.playerOffsets = new Vector3d[]{
                new Vector3d(0.2D, -0.378D, 0D),
                new Vector3d(-0.5D, -0.378D, 0D)
        };
        this.numberPlateOffset = new Vector3d(0D, 7D / 16D, 16.5D / 16D);
        this.width = 1.625F;
        this.height = 1.6F;
        this.minRotationSpeed = 2F;
        this.maxRotationSpeed = 5F;
        this.fuelEfficiency = () -> ModConfigHandler.VEHICLE_SERVER.vanChassisFuelEfficiency.get().floatValue();
        this.acceleration = () -> ModConfigHandler.VEHICLE_SERVER.vanChassisAcceleration.get().floatValue();
        this.maxSpeed = () -> ModConfigHandler.VEHICLE_SERVER.vanChassisMaxSpeed.get().floatValue();
    }

    @Override
    public int getInternalInventorySize() {
        return 6; // Fixed: Van has 6 external inventory slots
    }

}
