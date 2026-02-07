package de.rolandsw.schedulemc.npc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.maxhenkel.corelib.client.obj.OBJModel;
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
 * RenderLayer das eine Limousine um den NPC rendert wenn dieser faehrt.
 *
 * Das OBJ-Modell ist das gleiche wie bei PartLimousineChassis (wood_body.obj).
 * Es wird nur gerendert wenn der NPC im Fahrmodus ist (IS_DRIVING = true).
 */
@OnlyIn(Dist.CLIENT)
public class NPCVehicleLayer extends RenderLayer<CustomNPCEntity, CustomNPCModel> {

    private static final ResourceLocation VEHICLE_MODEL_LOCATION =
            ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "models/entity/wood_body.obj");

    /** Standard-Textur (weiss) */
    private static final ResourceLocation TEXTURE_WHITE =
            ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "textures/entity/vehicle_wood_white.png");

    /** Texturen fuer verschiedene Farben (0-4: weiss, schwarz, rot, blau, gelb) */
    private static final ResourceLocation[] COLOR_TEXTURES = new ResourceLocation[] {
            ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "textures/entity/vehicle_wood_white.png"),
            ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "textures/entity/vehicle_wood_black.png"),
            ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "textures/entity/vehicle_wood_red.png"),
            ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "textures/entity/vehicle_wood_blue.png"),
            ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "textures/entity/vehicle_wood_yellow.png")
    };

    /** Gecachtes OBJ-Modell (lazy-loaded, shared) */
    private static OBJModel cachedModel;

    /** Skalierungsfaktor (gleich wie bei echten Fahrzeugen) */
    private static final float SCALE_FACTOR = 0.7f;

    /** Y-Offset: NPC sitzt in der Limousine (playerOffset.y = -0.378) */
    private static final double SEAT_Y_OFFSET = 0.378;

    public NPCVehicleLayer(RenderLayerParent<CustomNPCEntity, CustomNPCModel> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       CustomNPCEntity npc, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // Nur rendern wenn der NPC faehrt
        if (!npc.isDriving()) {
            return;
        }

        // Lazy-load OBJ Model
        if (cachedModel == null) {
            cachedModel = new OBJModel(VEHICLE_MODEL_LOCATION);
        }

        // Fahrzeugfarbe bestimmen
        int colorIndex = npc.getVehicleColor();
        ResourceLocation texture = (colorIndex >= 0 && colorIndex < COLOR_TEXTURES.length)
                ? COLOR_TEXTURES[colorIndex]
                : TEXTURE_WHITE;

        poseStack.pushPose();

        // 1. Hebe den NPC an damit er in der Limousine sitzt
        poseStack.translate(0.0, SEAT_Y_OFFSET, 0.0);

        // 2. Rotiere das Fahrzeug nach der Fahrtrichtung
        float vehicleYaw = npc.getVehicleYaw();
        poseStack.mulPose(Axis.YP.rotationDegrees(-vehicleYaw));

        // 3. Skaliere auf Fahrzeugroesse
        poseStack.scale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);

        // 4. Verschiebe nach unten (Fahrzeug-Body Offset: 4/16 = 0.25)
        poseStack.translate(0.0, -4.0 / 16.0, 0.0);

        // 5. Rendere das OBJ-Modell
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(texture));
        cachedModel.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);

        poseStack.popPose();
    }
}
