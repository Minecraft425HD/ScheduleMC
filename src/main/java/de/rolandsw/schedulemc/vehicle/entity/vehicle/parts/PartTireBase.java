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
import java.util.Arrays;
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
        PartBody body = null;

        for (Part part : vehicle.getModelParts()) {
            if (part instanceof PartBody) {
                body = (PartBody) part;
                wheelOffsets = body.getWheelOffsets();
            }
        }

        // Compute Ackermann geometry from wheel layout
        final float wheelbase;
        final float trackWidth;
        if (body != null) {
            wheelbase = body.getWheelbase();
            trackWidth = body.getTrackWidth();
        } else {
            wheelbase = 1.0F;
            trackWidth = 1.0F;
        }

        // Front wheels = those at the minimum Z offset (vehicles face -Z in model space)
        final double frontZ = wheelOffsets.length > 0
                ? Arrays.stream(wheelOffsets).mapToDouble(v -> v.z).min().orElse(0)
                : 0;

        List<PartTireBase> wheels = new ArrayList<>();

        for (Part part : vehicle.getModelParts()) {
            if (part instanceof PartTireBase) {
                wheels.add((PartTireBase) part);
            }
        }

        for (int i = 0; i < wheelOffsets.length && i < wheels.size(); i++) {
            final Vector3d offset = wheelOffsets[i];
            final boolean isFrontWheel = offset.z <= frontZ + 0.001;
            // Vehicle faces -Z in model space → X- is the right side, X+ is the left side
            final boolean isRightWheel = offset.x < 0;

            list.add(new OBJModelInstance<>(wheels.get(i).model, new OBJModelOptions<>(wheels.get(i).texture, offset, null, (c, matrixStack, partialTicks) -> {
                if (isFrontWheel) {
                    float steering = c.getSteeringAngle(partialTicks);
                    if (Math.abs(steering) > 0.01F) {
                        float angle = ackermannAngle(steering, isRightWheel, wheelbase, trackWidth);
                        matrixStack.mulPose(Axis.YP.rotationDegrees(-angle));
                    }
                }
                matrixStack.mulPose(Axis.XP.rotationDegrees(-c.getWheelRotation(partialTicks)));
            })));
        }

        return list;
    }

    /**
     * Returns the Ackermann-corrected steering angle for one front wheel.
     * Inner wheel (same side as turn) gets a larger angle, outer wheel a smaller one.
     *
     * @param steeringAngle center steering angle in degrees (positive = right turn)
     * @param isRightWheel  true for the right-side wheel (X > 0)
     * @param wheelbase     distance between front and rear axle in blocks
     * @param trackWidth    distance between left and right wheels in blocks
     */
    private static float ackermannAngle(float steeringAngle, boolean isRightWheel, float wheelbase, float trackWidth) {
        double absAngle = Math.abs(steeringAngle);
        double steeringRad = Math.toRadians(absAngle);

        // Turning radius at vehicle center from rear axle
        double R = wheelbase / Math.tan(steeringRad);
        double halfW = trackWidth / 2.0;

        // Inner wheel is on the same side as the turn direction
        boolean turningRight = steeringAngle > 0;
        boolean isInner = (turningRight && isRightWheel) || (!turningRight && !isRightWheel);

        double innerR = R - halfW;
        double outerR = R + halfW;

        double angle;
        if (isInner) {
            // Guard against degenerate geometry
            angle = innerR > 0.001 ? Math.toDegrees(Math.atan(wheelbase / innerR)) : absAngle;
        } else {
            angle = Math.toDegrees(Math.atan(wheelbase / outerR));
        }

        return (float) Math.copySign(angle, steeringAngle);
    }

}
