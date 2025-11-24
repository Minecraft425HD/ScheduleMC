package de.rolandsw.schedulemc.npc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.client.model.CustomNPCModel;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Renderer für Custom NPCs mit Player-Skin Support
 */
@OnlyIn(Dist.CLIENT)
public class CustomNPCRenderer extends MobRenderer<CustomNPCEntity, CustomNPCModel> {

    private static final ResourceLocation DEFAULT_SKIN =
        new ResourceLocation(ScheduleMC.MOD_ID, "textures/entity/npc/default.png");

    public CustomNPCRenderer(EntityRendererProvider.Context context) {
        super(context, new CustomNPCModel(context.bakeLayer(CustomNPCModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CustomNPCEntity entity) {
        String skinFileName = entity.getSkinFileName();

        // Versuche, den Skin aus dem config Ordner zu laden
        try {
            // Check if custom skin exists
            Path skinPath = Paths.get("config", ScheduleMC.MOD_ID, "npc_skins", skinFileName);
            File skinFile = skinPath.toFile();

            if (skinFile.exists()) {
                // Erstelle ResourceLocation für custom skin
                // Diese werden dynamisch geladen via SkinManager
                return new ResourceLocation(ScheduleMC.MOD_ID, "npc_skins/" + skinFileName);
            }
        } catch (Exception e) {
            ScheduleMC.LOGGER.warn("Failed to load custom NPC skin: " + skinFileName, e);
        }

        // Fallback zu default skin
        return DEFAULT_SKIN;
    }

    @Override
    public void render(CustomNPCEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Name-Tag immer anzeigen
        if (this.shouldShowName(entity)) {
            this.renderNameTag(entity, entity.getName(), poseStack, buffer, packedLight);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected boolean shouldShowName(CustomNPCEntity entity) {
        return true; // Name immer anzeigen
    }
}
