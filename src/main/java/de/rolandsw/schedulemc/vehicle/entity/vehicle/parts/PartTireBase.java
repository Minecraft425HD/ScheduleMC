package de.rolandsw.schedulemc.vehicle.entity.vehicle.parts;

import com.mojang.math.Axis;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.client.obj.OBJModel;
import de.maxhenkel.corelib.client.obj.OBJModelInstance;
import de.maxhenkel.corelib.client.obj.OBJModelOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class PartTireBase extends PartModel {

    protected float rotationModifier;
    protected float stepHeight;
    protected TireSeasonType seasonType;

    public PartTireBase(OBJModel model, ResourceLocation texture, float rotationModifier, float stepHeight) {
        this(model, texture, rotationModifier, stepHeight, TireSeasonType.ALL_SEASON);
    }

    public PartTireBase(OBJModel model, ResourceLocation texture, float rotationModifier, float stepHeight, TireSeasonType seasonType) {
        super(model, texture);
        this.rotationModifier = rotationModifier;
        this.stepHeight = stepHeight;
        this.seasonType = seasonType;
    }

    public float getStepHeight() {
        return stepHeight;
    }

    public float getRotationModifier() {
        return rotationModifier;
    }

    public TireSeasonType getSeasonType() {
        return seasonType;
    }

    @Override
    public boolean validate(List<Part> parts, List<Component> messages) {
        for (Part part : parts) {
            if (part instanceof PartBody) {
                PartBody body = (PartBody) part;
                if (!body.canFitWheel(this)) {
                    messages.add(Component.translatable("message.parts.wrong_wheel_type"));
                    return false;
                }
            }
        }

        return super.validate(parts, messages);
    }

    @Override
    public List<OBJModelInstance<EntityGenericVehicle>> getInstances(EntityGenericVehicle vehicle) {
        List<OBJModelInstance<EntityGenericVehicle>> list = new ArrayList<>();

        Vector3d[] wheelOffsets = new Vector3d[0];

        for (Part part : vehicle.getModelParts()) {
            if (part instanceof PartBody) {
                wheelOffsets = ((PartBody) part).getWheelOffsets();
            }
        }

        List<PartTireBase> wheels = new ArrayList<>();

        for (Part part : vehicle.getModelParts()) {
            if (part instanceof PartTireBase) {
                wheels.add((PartTireBase) part);
            }
        }

        for (int i = 0; i < wheelOffsets.length && i < wheels.size(); i++) {
            list.add(new OBJModelInstance<>(wheels.get(i).model, new OBJModelOptions<>(wheels.get(i).texture, wheelOffsets[i], null, (c, matrixStack, partialTicks) -> {
                matrixStack.mulPose(Axis.XP.rotationDegrees(-vehicle.getWheelRotation(partialTicks)));
            })));
        }

        return list;
    }

}
