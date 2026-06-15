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
        this.addLayer(new NPCVehicleLayer(this));
        this.addLayer(new NPCSirenLayer(this)); // Feature 2: Polizei-Blaulicht
        this.addLayer(new NPCHeldItemLayer(this,
            net.minecraft.client.Minecraft.getInstance().getItemRenderer()));
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
        // Name + Persönlichkeit erst zeigen, wenn der Spieler den NPC kennt.
        // shouldShowName() ist false, damit super.render() nicht zusätzlich
        // das Vanilla-Nameplate über unser Persönlichkeits-Label zeichnet.
        if (this.entityRenderDispatcher.distanceToSqr(entity) <= 4096.0
                && !entity.isInvisible()) {
            // Client-safe: gespiegelte NPC-Data-UUID verwenden. Das echte
            // npcData-Objekt ist auf dem Client nicht synchron (frische Zufalls-UUID
            // aus dem Konstruktor) und würde nie zum ClientKnownNPCCache passen.
            java.util.UUID npcDataId = entity.getSyncedNpcDataUUID();
            boolean known = de.rolandsw.schedulemc.npc.client.ClientKnownNPCCache.isKnown(npcDataId);

            if (known) {
                net.minecraft.network.chat.Component nameLine = entity.getName();
                if (de.rolandsw.schedulemc.npc.client.ClientSupplyRequestCache.has(npcDataId)) {
                    nameLine = net.minecraft.network.chat.Component.literal("! ")
                        .withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD)
                        .append(nameLine);
                }
                // Persönlichkeits-Label an der Basis
                this.renderNameTag(entity,
                    net.minecraft.network.chat.Component.translatable(
                        entity.getPersonalityArchetype().getTranslationKey())
                        .withStyle(net.minecraft.ChatFormatting.GRAY),
                    poseStack, buffer, packedLight);
                // Name über der Persönlichkeit
                poseStack.pushPose();
                poseStack.translate(0.0, 0.32, 0.0);
                this.renderNameTag(entity, nameLine, poseStack, buffer, packedLight);
                poseStack.popPose();
            } else {
                this.renderNameTag(entity,
                    net.minecraft.network.chat.Component.translatable("entity.schedulemc.npc.unknown")
                        .withStyle(net.minecraft.ChatFormatting.DARK_GRAY),
                    poseStack, buffer, packedLight);
            }
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected boolean shouldShowName(CustomNPCEntity entity) {
        // false: wir rendern Name + Persönlichkeit selbst in render() —
        // sonst zeichnet LivingEntityRenderer das Nameplate doppelt
        // und der Name überdeckt das Persönlichkeits-Label.
        return false;
    }
}
