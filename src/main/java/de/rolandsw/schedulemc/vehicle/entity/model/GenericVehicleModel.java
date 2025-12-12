package de.rolandsw.schedulemc.vehicle.entity.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.client.obj.OBJEntityRenderer;
import de.maxhenkel.corelib.client.obj.OBJModelInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.joml.Vector3d;

import java.util.List;

public class GenericVehicleModel extends OBJEntityRenderer<EntityGenericVehicle> {

    public GenericVehicleModel(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public List<OBJModelInstance<EntityGenericVehicle>> getModels(EntityGenericVehicle entity) {
        return entity.getModels();
    }

    @Override
    public void render(EntityGenericVehicle entity, float yaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, yaw, partialTicks, matrixStack, buffer, packedLight);
        matrixStack.pushPose();

        String text = entity.getLicensePlate();
        if (text != null && !text.isEmpty()) {
            matrixStack.pushPose();
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            drawLicensePlate(entity, text, matrixStack, buffer, partialTicks, packedLight);
            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    private void drawLicensePlate(EntityGenericVehicle vehicle, String txt, PoseStack matrixStack, MultiBufferSource buffer, float partialTicks, int packedLight) {
        matrixStack.pushPose();
        matrixStack.scale(1F, -1F, 1F);

        translateLicensePlate(vehicle, matrixStack, partialTicks);

        int textWidth = Minecraft.getInstance().font.width(txt);
        float textScale = 0.01F;

        matrixStack.translate(-(textScale * textWidth) / 2F, 0F, 0F);

        matrixStack.scale(textScale, textScale, textScale);

        Minecraft.getInstance().font.drawInBatch(txt, 0F, 0F, 0xFFFFFF, false, matrixStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, packedLight);

        matrixStack.popPose();
    }

    protected void translateLicensePlate(EntityGenericVehicle entity, PoseStack matrixStack, float partialTicks) {
        Vector3d offset = entity.getLicensePlateOffset();
        matrixStack.mulPose(Axis.YP.rotationDegrees(180F - entity.getViewYRot(partialTicks)));
        matrixStack.translate(offset.x, offset.y, offset.z);
    }

}