package de.rolandsw.schedulemc.vehicle.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.rolandsw.schedulemc.vehicle.component.body.BodyComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for vehicles.
 * Uses component data to determine appearance.
 */
public class VehicleRenderer extends EntityRenderer<VehicleEntity> {

    public VehicleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(VehicleEntity vehicle, float entityYaw, float partialTicks,
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(vehicle, entityYaw, partialTicks, poseStack, buffer, packedLight);

        // Get body component for texture/model
        BodyComponent body = vehicle.getComponent(ComponentType.BODY, BodyComponent.class);

        if (body != null) {
            poseStack.pushPose();

            // TODO: Render vehicle model based on BodyComponent specification
            // For now, this is a placeholder

            poseStack.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(VehicleEntity vehicle) {
        BodyComponent body = vehicle.getComponent(ComponentType.BODY, BodyComponent.class);
        if (body != null && body.getSpecification() != null) {
            return body.getSpecification().getTexturePath();
        }

        return new ResourceLocation("schedulemc", "textures/entity/vehicle_default.png");
    }
}
