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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renderer für Custom NPCs mit Player-Skin Support
 * OPTIMIERT: Cached ResourceLocations für bessere Performance
 */
@OnlyIn(Dist.CLIENT)
public class CustomNPCRenderer extends MobRenderer<CustomNPCEntity, CustomNPCModel> {

    // Cached ResourceLocations (Performance-Optimierung)
    private static final ResourceLocation DEFAULT_SKIN =
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "textures/entity/npc/default.png");
    private static final ResourceLocation STEVE_SKIN =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");
    private static final ResourceLocation ALEX_SKIN =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/slim/alex.png");

    // Cache für Custom Skins (verhindert wiederholtes Laden)
    private static final Map<String, ResourceLocation> customSkinCache = new ConcurrentHashMap<>();

    public CustomNPCRenderer(EntityRendererProvider.Context context) {
        super(context, new CustomNPCModel(context.bakeLayer(CustomNPCModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CustomNPCEntity entity) {
        String skinFileName = entity.getSkinFileName();

        // Prüfe auf Standard-Minecraft-Skins (gecached)
        if ("steve".equals(skinFileName)) {
            return STEVE_SKIN;
        } else if ("alex".equals(skinFileName)) {
            return ALEX_SKIN;
        }

        // Prüfe, ob es ein Custom Skin ist (endet mit .png)
        if (skinFileName.endsWith(".png")) {
            // Lade den Custom Skin dynamisch (mit Cache)
            return customSkinCache.computeIfAbsent(skinFileName,
                CustomSkinManager::loadCustomSkin);
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
