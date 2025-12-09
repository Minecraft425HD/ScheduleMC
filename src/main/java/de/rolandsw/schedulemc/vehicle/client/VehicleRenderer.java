package de.rolandsw.schedulemc.vehicle.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.rolandsw.schedulemc.vehicle.client.model.SedanModel;
import de.rolandsw.schedulemc.vehicle.component.body.BodyComponent;
import de.rolandsw.schedulemc.vehicle.component.body.BodySpecification;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Renderer for vehicles.
 * Uses component data to determine appearance.
 */
public class VehicleRenderer extends EntityRenderer<VehicleEntity> {

    private final Map<String, EntityModel<VehicleEntity>> models = new HashMap<>();
    private final SedanModel<VehicleEntity> sedanModel;

    public VehicleRenderer(EntityRendererProvider.Context context) {
        super(context);

        // Initialize models
        this.sedanModel = new SedanModel<>(context.bakeLayer(SedanModel.LAYER_LOCATION));

        // Register models by body specification identifier
        models.put("sedan", sedanModel);
        // TODO: Add other vehicle models (sport, suv, truck, transporter)
    }

    @Override
    public void render(VehicleEntity vehicle, float entityYaw, float partialTicks,
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(vehicle, entityYaw, partialTicks, poseStack, buffer, packedLight);

        poseStack.pushPose();

        // Get body component for color
        BodyComponent body = vehicle.getComponent(ComponentType.BODY, BodyComponent.class);
        int color = body != null ? body.getColor() : 0xFFFFFF;

        if (body != null && body.getSpecification() != null) {
            poseStack.pushPose();

            // Position model at entity center
            poseStack.translate(0.0, 1.5, 0.0);

            // Rotate to face correct direction (Minecraft entities face south by default)
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

            // Scale if needed
            poseStack.scale(-1.0F, -1.0F, 1.0F);

            // Get the appropriate model for this body type
            EntityModel<VehicleEntity> model = getModelForBody(body.getSpecification());

            if (model != null) {
                // Setup animation (limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch)
                model.setupAnim(vehicle, 0.0F, 0.0F, vehicle.tickCount + partialTicks, 0.0F, 0.0F);

                // Get texture
                ResourceLocation texture = getTextureLocation(vehicle);
                VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(texture));

                // Render the model
                model.renderToBuffer(poseStack, vertexConsumer, packedLight,
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F);
            }

        // Offset to center the vehicle
        poseStack.translate(-1.0, 0.0, -0.75);

        // Render a simple colored box representing the vehicle
        // Dimensions: 2.0 wide, 1.5 tall, 1.5 deep (typical car size)
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());
        renderBox(poseStack, vertexConsumer, 0, 0, 0, 2.0f, 1.5f, 1.5f, red, green, blue, 1.0f);

        poseStack.popPose();
    }

    /**
     * Renders a wireframe box at the specified position with the given dimensions and color.
     */
    private void renderBox(PoseStack poseStack, VertexConsumer consumer,
                          float x, float y, float z,
                          float width, float height, float depth,
                          float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = poseStack.last();

        // Bottom face
        drawLine(pose, consumer, x, y, z, x + width, y, z, red, green, blue, alpha);
        drawLine(pose, consumer, x + width, y, z, x + width, y, z + depth, red, green, blue, alpha);
        drawLine(pose, consumer, x + width, y, z + depth, x, y, z + depth, red, green, blue, alpha);
        drawLine(pose, consumer, x, y, z + depth, x, y, z, red, green, blue, alpha);

        // Top face
        drawLine(pose, consumer, x, y + height, z, x + width, y + height, z, red, green, blue, alpha);
        drawLine(pose, consumer, x + width, y + height, z, x + width, y + height, z + depth, red, green, blue, alpha);
        drawLine(pose, consumer, x + width, y + height, z + depth, x, y + height, z + depth, red, green, blue, alpha);
        drawLine(pose, consumer, x, y + height, z + depth, x, y + height, z, red, green, blue, alpha);

        // Vertical edges
        drawLine(pose, consumer, x, y, z, x, y + height, z, red, green, blue, alpha);
        drawLine(pose, consumer, x + width, y, z, x + width, y + height, z, red, green, blue, alpha);
        drawLine(pose, consumer, x + width, y, z + depth, x + width, y + height, z + depth, red, green, blue, alpha);
        drawLine(pose, consumer, x, y, z + depth, x, y + height, z + depth, red, green, blue, alpha);
    }

    /**
     * Draws a line between two points.
     */
    private void drawLine(PoseStack.Pose pose, VertexConsumer consumer,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float red, float green, float blue, float alpha) {
        consumer.vertex(pose.pose(), x1, y1, z1)
                .color(red, green, blue, alpha)
                .normal(pose.normal(), x2 - x1, y2 - y1, z2 - z1)
                .endVertex();
        consumer.vertex(pose.pose(), x2, y2, z2)
                .color(red, green, blue, alpha)
                .normal(pose.normal(), x2 - x1, y2 - y1, z2 - z1)
                .endVertex();
    }

    /**
     * Gets the appropriate model for the given body specification.
     */
    private EntityModel<VehicleEntity> getModelForBody(BodySpecification specification) {
        return models.getOrDefault(specification.getIdentifier(), sedanModel);
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
