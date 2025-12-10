package de.rolandsw.schedulemc.vehicle.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.rolandsw.schedulemc.vehicle.client.model.SedanModel;
import de.rolandsw.schedulemc.vehicle.component.body.BodyComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for vehicles using native Minecraft model system (Blockbench Java export)
 */
public class VehicleRenderer extends EntityRenderer<VehicleEntity> {

    private final SedanModel<VehicleEntity> sedanModel;

    public VehicleRenderer(EntityRendererProvider.Context context) {
        super(context);
        // Load the baked model from the registered layer
        this.sedanModel = new SedanModel<>(context.bakeLayer(SedanModel.LAYER_LOCATION));
    }

    @Override
    public void render(VehicleEntity entity, float entityYaw, float partialTicks,
                      PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        poseStack.pushPose();

        // Flip model right-side up (Blockbench exports upside down)
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        // Move model up after flip (model pivot is at Y=24, so move up 1.5 blocks)
        poseStack.translate(0.0F, -1.5F, 0.0F);

        // Rotate to face the correct direction
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

        // Get texture and create vertex consumer
        ResourceLocation texture = getTextureLocation(entity);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));

        // Render the model
        sedanModel.renderToBuffer(poseStack, vertexConsumer, packedLight,
            OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

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
