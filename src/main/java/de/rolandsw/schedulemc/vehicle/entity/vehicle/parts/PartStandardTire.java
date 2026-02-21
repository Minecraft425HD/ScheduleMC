package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;

import de.maxhenkel.corelib.client.obj.OBJModel;
import net.minecraft.resources.ResourceLocation;

public class PartStandardTire extends PartTireBase {

    public PartStandardTire(OBJModel model, ResourceLocation texture, float rotationModifier, float stepHeight) {
        super(model, texture, rotationModifier, stepHeight, TireSeasonType.SUMMER);
    }

}
