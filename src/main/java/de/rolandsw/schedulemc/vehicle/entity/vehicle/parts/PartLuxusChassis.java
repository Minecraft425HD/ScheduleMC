package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import org.joml.Vector3d;
import de.rolandsw.schedulemc.vehicle.Main;
import de.maxhenkel.corelib.client.obj.OBJModel;
import net.minecraft.resources.ResourceLocation;

public class PartLuxusChassis extends PartBody {

    public PartLuxusChassis(ResourceLocation texture, String materialTranslationKey) {
        super(new OBJModel(ResourceLocation.fromNamespaceAndPath(Main.MODID, "models/entity/sport_body.obj")), texture, new Vector3d(0D, 4D / 16D, 0D), "sport", materialTranslationKey);
        this.wheelOffsets = new Vector3d[]{
                new Vector3d(9.5F / 16F, 4F / 16F, 8F / 16F),
                new Vector3d(9.5F / 16F, 4F / 16F, -9F / 16F),
                new Vector3d(-9.5F / 16F, 4F / 16F, 8F / 16F),
                new Vector3d(-9.5F / 16F, 4F / 16F, -9F / 16F)
        };
        this.playerOffsets = new Vector3d[]{
                new Vector3d(0D, -0.378D, 0D)
        };
        this.numberPlateOffset = new Vector3d(0D, 7D / 16D, 14.5D / 16D);
        this.width = 1.4F;
        this.height = 1.2F;
        this.minRotationSpeed = 1.1F;
        this.maxRotationSpeed = 5F;
        this.fuelEfficiency = () -> ModConfigHandler.VEHICLE_SERVER.luxusChassisFuelEfficiency.get().floatValue();
        this.acceleration = () -> ModConfigHandler.VEHICLE_SERVER.luxusChassisAcceleration.get().floatValue();
        this.maxSpeed = () -> ModConfigHandler.VEHICLE_SERVER.luxusChassisMaxSpeed.get().floatValue();
    }

    @Override
    public int getInternalInventorySize() {
        return 3; // Fixed: Luxus has 3 external inventory slots
    }

}
