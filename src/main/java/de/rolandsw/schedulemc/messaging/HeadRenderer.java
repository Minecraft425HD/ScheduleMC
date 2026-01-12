package de.rolandsw.schedulemc.messaging;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Utility class for rendering player and NPC heads in GUIs
 */
public class HeadRenderer {

    /**
     * Renders a player head in the GUI using UUID
     * @param guiGraphics Graphics context
     * @param x X position
     * @param y Y position
     * @param size Size of the head
     * @param playerUuid Player UUID (can be null for Steve skin)
     */
    public static void renderPlayerHead(GuiGraphics guiGraphics, int x, int y, int size, UUID playerUuid) {
        ResourceLocation skinTexture = null;
        if (playerUuid != null) {
            skinTexture = DefaultPlayerSkin.getSkin(playerUuid);
        }
        renderPlayerHead(guiGraphics, x, y, size, skinTexture);
    }

    /**
     * Renders a player head in the GUI
     * @param guiGraphics Graphics context
     * @param x X position
     * @param y Y position
     * @param size Size of the head
     * @param skinTexture Skin texture location (can be null for Steve skin)
     */
    public static void renderPlayerHead(GuiGraphics guiGraphics, int x, int y, int size, ResourceLocation skinTexture) {
        if (skinTexture == null) {
            skinTexture = DefaultPlayerSkin.getDefaultSkin();
        }

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // Bind skin texture
        RenderSystem.setShaderTexture(0, skinTexture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw head front (face)
        int textureSize = 64;
        int headTexX = 8;
        int headTexY = 8;
        int headTexWidth = 8;
        int headTexHeight = 8;

        // Main head layer
        guiGraphics.blit(skinTexture, x, y, size, size,
            headTexX, headTexY, headTexWidth, headTexHeight,
            textureSize, textureSize);

        // Overlay layer (hat/hair)
        int overlayTexX = 40;
        int overlayTexY = 8;
        guiGraphics.blit(skinTexture, x, y, size, size,
            overlayTexX, overlayTexY, headTexWidth, headTexHeight,
            textureSize, textureSize);

        poseStack.popPose();
    }

    /**
     * Renders an NPC head using skin file name
     */
    public static void renderNPCHead(GuiGraphics guiGraphics, int x, int y, int size, String skinFileName) {
        ResourceLocation skinTexture = null;

        if (skinFileName != null && !skinFileName.isEmpty()) {
            skinTexture = ResourceLocation.fromNamespaceAndPath("schedulemc", "textures/entity/npc/" + skinFileName);
        }

        renderPlayerHead(guiGraphics, x, y, size, skinTexture);
    }

    /**
     * Renders a 3D entity head (more advanced)
     */
    public static void renderEntityHead(GuiGraphics guiGraphics, int x, int y, int size, LivingEntity entity) {
        if (entity == null) return;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x + size / 2.0, y + size / 2.0, 50.0);

        float scale = size / 2.0f;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(20.0F));

        Lighting.setupForEntityInInventory();

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, poseStack, bufferSource, 15728880);
        bufferSource.endBatch();

        Lighting.setupFor3DItems();

        poseStack.popPose();
    }

}
