package de.rolandsw.schedulemc.vehicle.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.rolandsw.schedulemc.vehicle.client.model.OBJVehicleModel;
import de.rolandsw.schedulemc.vehicle.component.body.BodyComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for vehicles using OBJ models
 */
public class VehicleRenderer extends EntityRenderer<VehicleEntity> {

    private final OBJVehicleModel objModel;

    public VehicleRenderer(EntityRendererProvider.Context context) {
        super(context);
        // Load the OBJ model
        this.objModel = new OBJVehicleModel(new ResourceLocation("schedulemc", "models/entity/sedan.obj"));
    }

    @Override
    public void render(VehicleEntity entity, float entityYaw, float partialTicks,
                      PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        poseStack.pushPose();

        // Rotate to face the correct direction
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

        // Flip for correct orientation
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        // Get texture and create vertex consumer
        ResourceLocation texture = getTextureLocation(entity);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));

        // Render the OBJ model
        objModel.renderToBuffer(poseStack, vertexConsumer, packedLight,
            net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
            1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(VehicleEntity vehicle) {
        BodyComponent body = vehicle.getComponent(ComponentType.BODY, BodyComponent.class);
        if (body != null && body.getSpecification() != null) {
            return body.getSpecification().getTexturePath();
        }
        return new ResourceLocation("schedulemc", "textures/entity/body_sedan.png");
    }
}
