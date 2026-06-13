package de.rolandsw.schedulemc.npc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import de.rolandsw.schedulemc.npc.client.model.CustomNPCModel;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Rendert das Haupthand-Item eines NPCs (z.B. die Polizei-Waffe) sichtbar
 * in der rechten Hand.
 */
public class NPCHeldItemLayer extends RenderLayer<CustomNPCEntity, CustomNPCModel> {

    private final ItemRenderer itemRenderer;

    public NPCHeldItemLayer(RenderLayerParent<CustomNPCEntity, CustomNPCModel> parent,
                            ItemRenderer itemRenderer) {
        super(parent);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       CustomNPCEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack stack = entity.getMainHandItem();
        if (stack.isEmpty()) return;

        poseStack.pushPose();
        this.getParentModel().translateToHand(poseStack);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0, 0.125, -0.625);

        this.itemRenderer.renderStatic(entity, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
            false, poseStack, buffer, entity.level(), packedLight,
            net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, entity.getId());

        poseStack.popPose();
    }
}
