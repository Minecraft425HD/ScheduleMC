package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import org.joml.Vector3d;
import de.rolandsw.schedulemc.vehicle.Main;
import de.maxhenkel.corelib.client.obj.OBJModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class PartTankContainer extends PartTransporterBack {

    public PartTankContainer(ResourceLocation texture) {
        super(new OBJModel(ResourceLocation.fromNamespaceAndPath(Main.MODID, "models/entity/tank_container.obj")), texture, new Vector3d(0D / 16D, 17D / 16D, 5.5D / 16D));
    }

    /**
     * Returns the fluid capacity of this tank container in mB (millibuckets).
     * @return Fluid capacity (configurable, default: 100000 mB = 100 Buckets)
     */
    public int getFluidAmount() {
        return ModConfigHandler.VEHICLE_SERVER.fluidContainerCapacity.get();
    }

    /**
     * Checks if this container can be mounted on the given chassis.
     * Fluid containers can only be mounted on Truck chassis.
     * @param chassis The chassis to check
     * @return true if the chassis is a Truck, false otherwise
     */
    public boolean canMountOn(PartBody chassis) {
        return chassis instanceof PartTruckChassis;
    }

    @Override
    public boolean validate(List<Part> parts, List<Component> messages) {
        if (Part.getAmount(parts, part -> part instanceof PartTruckChassis) != 1) {
            messages.add(Component.translatable("message.parts.no_body_for_tank_container"));
            return false;
        }
        return true;
    }

}
