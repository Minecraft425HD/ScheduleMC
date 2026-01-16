package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import org.joml.Vector3d;
import de.rolandsw.schedulemc.vehicle.Main;
import de.maxhenkel.corelib.client.obj.OBJModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class PartTruckChassis extends PartBody {

    public PartTruckChassis(ResourceLocation texture, String materialTranslationKey) {
        super(new OBJModel(ResourceLocation.fromNamespaceAndPath(Main.MODID, "models/entity/transporter_body.obj")), texture, new Vector3d(0D, 4D / 16D, 0D), "transporter", materialTranslationKey);
        this.wheelOffsets = new Vector3d[]{
                new Vector3d(14.5F / 16F, 4F / 16F, 12F / 16F),
                new Vector3d(14.5F / 16F, 4F / 16F, -16F / 16F),
                new Vector3d(-14.5F / 16F, 4F / 16F, 12F / 16F),
                new Vector3d(-14.5F / 16F, 4F / 16F, -16F / 16F),
                new Vector3d(14.5F / 16F, 4F / 16F, 3F / 16F),
                new Vector3d(-14.5F / 16F, 4F / 16F, 3F / 16F)
        };
        this.playerOffsets = new Vector3d[]{
                new Vector3d(0.55D, -0.378D, -0.38D),
                new Vector3d(0.55D, -0.378D, 0.38D)
        };
        this.numberPlateOffset = new Vector3d(0D, 7D / 16D, 17.5D / 16D);
        this.width = 2F;
        this.height = 1.51F;
        this.minRotationSpeed = 2.0F;
        this.maxRotationSpeed = 5F;
        this.fuelEfficiency = () -> ModConfigHandler.VEHICLE_SERVER.truckChassisFuelEfficiency.get().floatValue();
        this.acceleration = () -> ModConfigHandler.VEHICLE_SERVER.truckChassisAcceleration.get().floatValue();
        this.maxSpeed = () -> ModConfigHandler.VEHICLE_SERVER.truckChassisMaxSpeed.get().floatValue();
    }

    @Override
    public int getInternalInventorySize() {
        return 0; // Fixed: Truck has 0 base slots (replaced by Container with 12 slots)
    }

    @Override
    public boolean validate(List<Part> parts, List<Component> messages) {
        if (getAmount(parts, part -> part instanceof PartTransporterBack) > 1) {
            messages.add(Component.translatable("message.parts.too_many_containers"));
            return false;
        }

        return super.validate(parts, messages);
    }

}
