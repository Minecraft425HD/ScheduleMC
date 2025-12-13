package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;

import org.joml.Vector3d;
import de.maxhenkel.corelib.client.obj.OBJModel;
import net.minecraft.resources.ResourceLocation;

public abstract class PartBodyWoodBase extends PartBody {

    protected Vector3d bumperOffset;

    public PartBodyWoodBase(OBJModel model, ResourceLocation texture, Vector3d offset, String translationKey, String materialTranslationKey) {
        super(model, texture, offset, translationKey, materialTranslationKey);
    }

    public Vector3d getBumperOffset() {
        return bumperOffset;
    }

}
