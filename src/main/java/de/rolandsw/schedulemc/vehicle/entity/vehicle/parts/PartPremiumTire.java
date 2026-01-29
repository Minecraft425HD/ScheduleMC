package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;

import de.maxhenkel.corelib.client.obj.OBJModel;
import net.minecraft.resources.ResourceLocation;

public class PartPremiumTire extends PartTireBase {

    public PartPremiumTire(OBJModel model, ResourceLocation texture, float rotationModifier, float stepHeight) {
        super(model, texture, rotationModifier, stepHeight, TireSeasonType.SUMMER);
    }

}
