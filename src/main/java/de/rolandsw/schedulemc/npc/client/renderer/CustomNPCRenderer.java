package de.rolandsw.schedulemc.npc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.client.model.CustomNPCModel;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

        // Prüfe auf Standard-Minecraft-Skins
        if (skinFileName.equals("steve")) {
            return new ResourceLocation("minecraft", "textures/entity/player/wide/steve.png");
        } else if (skinFileName.equals("alex")) {
            return new ResourceLocation("minecraft", "textures/entity/player/slim/alex.png");
        }

        // Prüfe, ob es ein Custom Skin ist (endet mit .png)
        if (skinFileName.endsWith(".png")) {
            // Lade den Custom Skin dynamisch
            return CustomSkinManager.loadCustomSkin(skinFileName);
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
