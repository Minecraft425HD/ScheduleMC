package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;

import org.joml.Vector3d;
import de.rolandsw.schedulemc.vehicle.Main;
import de.maxhenkel.corelib.client.obj.OBJModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class PartContainer extends PartTransporterBack {

    public PartContainer(ResourceLocation texture) {
        super(new OBJModel(ResourceLocation.fromNamespaceAndPath(Main.MODID, "models/entity/container.obj")),
                texture, new Vector3d(0D / 16D, 17D / 16D, 5.5D / 16D));
    }

    /**
     * Returns the number of inventory slots this container provides.
     * @return Number of slots (fixed: 12)
     */
    public int getSlotCount() {
        return 12; // Fixed: Container provides 12 inventory slots
    }

    /**
     * Checks if this container can be mounted on the given chassis.
     * Item containers can only be mounted on Truck chassis.
     * @param chassis The chassis to check
     * @return true if the chassis is a Truck, false otherwise
     */
    public boolean canMountOn(PartBody chassis) {
        return chassis instanceof PartTruckChassis;
    }

    @Override
    public boolean validate(List<Part> parts, List<Component> messages) {
        if (Part.getAmount(parts, part -> part instanceof PartTruckChassis) != 1) {
            messages.add(Component.translatable("message.parts.no_body_for_container"));
            return false;
        }
        return true;
    }

}
