package de.rolandsw.schedulemc.npc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.client.model.CustomNPCModel;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Feature 2: Sirene und Blaulicht RenderLayer
 *
 * Rendert ein blaues Blinklicht auf dem Dach des Polizei-Fahrzeugs.
 * Wird nur angezeigt wenn:
 * - NPC faehrt (isDriving() == true)
 * - Sirene aktiv (isSirenActive() == true)
 *
 * Das Licht rotiert und pulsiert fuer realistischen Blaulicht-Effekt.
 */
@OnlyIn(Dist.CLIENT)
public class NPCSirenLayer extends RenderLayer<CustomNPCEntity, CustomNPCModel> {

    private static final ResourceLocation SIREN_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "textures/entity/police_siren.png");

    /** Groesse des Blaulichts */
    private static final float SIREN_SIZE = 0.15f;

    /** Hoehe ueber dem Fahrzeug */
    private static final float SIREN_HEIGHT = 1.2f;

    /** Rotationsgeschwindigkeit (Grad pro Tick) */
    private static final float ROTATION_SPEED = 15.0f;

    /** Blink-Frequenz (Ticks pro Blink-Zyklus) */
    private static final int BLINK_CYCLE = 10;

    public NPCSirenLayer(RenderLayerParent<CustomNPCEntity, CustomNPCModel> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       CustomNPCEntity npc, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // Nur rendern wenn fahrend UND Sirene aktiv
        if (!npc.isDriving() || !npc.isSirenActive()) {
            return;
        }

        // Blink-Effekt: Jedes 2. Intervall aus
        int blinkPhase = ((int) ageInTicks) % (BLINK_CYCLE * 2);
        if (blinkPhase >= BLINK_CYCLE) {
            return; // Blinklicht aus
        }

        poseStack.pushPose();

        // Position: Oben auf dem Fahrzeug
        float vehicleYaw = npc.getVehicleYaw();
        poseStack.translate(0.0, SIREN_HEIGHT, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-vehicleYaw));

        // Rotation des Blaulichts
        float rotation = ageInTicks * ROTATION_SPEED;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // Skalierung
        poseStack.scale(SIREN_SIZE, SIREN_SIZE, SIREN_SIZE);

        // Blaues Licht rendern (Emissive = leuchtet)
        VertexConsumer vertexConsumer = bufferSource.getBuffer(
            RenderType.entityTranslucentEmissive(SIREN_TEXTURE));

        // Einfaches Quad fuer Blaulicht
        float halfSize = 0.5f;
        int blue = 255;
        int green = 100;
        int red = 50;
        int alpha = 200;

        // Blaulicht-Intensitaet pulsiert
        float pulseIntensity = 0.7f + 0.3f * (float) Math.sin(ageInTicks * 0.5f);
        red = (int) (red * pulseIntensity);
        green = (int) (green * pulseIntensity);
        blue = (int) (blue * pulseIntensity);

        // Rendern als Billboard-Quad (immer zur Kamera gerichtet)
        var matrix = poseStack.last().pose();
        var normal = poseStack.last().normal();

        vertexConsumer.vertex(matrix, -halfSize, -halfSize, 0)
            .color(red, green, blue, alpha)
            .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(0xF000F0).normal(normal, 0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix, halfSize, -halfSize, 0)
            .color(red, green, blue, alpha)
            .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(0xF000F0).normal(normal, 0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix, halfSize, halfSize, 0)
            .color(red, green, blue, alpha)
            .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(0xF000F0).normal(normal, 0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix, -halfSize, halfSize, 0)
            .color(red, green, blue, alpha)
            .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(0xF000F0).normal(normal, 0, 1, 0).endVertex();

        poseStack.popPose();
    }
}
