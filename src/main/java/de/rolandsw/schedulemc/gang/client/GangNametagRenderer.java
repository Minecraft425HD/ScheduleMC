package de.rolandsw.schedulemc.gang.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.rolandsw.schedulemc.gang.network.PlayerGangInfo;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * Rendert Gang-Tag, Sterne, Rang und Level-Fortschritt ueber Spielerkoepfen.
 *
 * Anzeige:
 *   §c[MAFIA §4★★★§c] §7Underboss
 *          Spielername              (Vanilla)
 *   §6Lv.18 §a████████░░ §772%
 *
 * Nutzt Forges RenderNameTagEvent um zusaetzliche Textzeilen
 * ueber und unter dem Vanilla-Nametag zu rendern.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "schedulemc")
public class GangNametagRenderer {

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        EventHelper.handleEvent(() -> {
            if (!(event.getEntity() instanceof Player player)) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // Eigenen Nametag nicht modifizieren (nur andere Spieler)
            if (player.getUUID().equals(mc.player.getUUID())) return;

            PlayerGangInfo info = ClientGangCache.getPlayerInfo(player.getUUID());
            if (info == null) return;

            // Vanilla-Nametag bleibt bestehen, wir rendern zusaetzliche Zeilen
            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource buffer = event.getMultiBufferSource();
            int packedLight = event.getPackedLight();
            Font font = mc.font;

            // Zeile ueber dem Nametag: [TAG ★★★] Rang
            String topLine = buildTopLine(info);
            if (!topLine.isEmpty()) {
                renderTextAbove(poseStack, buffer, font, topLine, 0.3f, packedLight);
            }

            // Zeile unter dem Nametag: Lv.X ████░░ XX%
            String bottomLine = info.getFormattedLevel();
            renderTextBelow(poseStack, buffer, font, bottomLine, -0.3f, packedLight);

        }, "onRenderNameTag");
    }

    /**
     * Baut die obere Zeile: [TAG ★★★] Rang
     */
    private static String buildTopLine(PlayerGangInfo info) {
        StringBuilder sb = new StringBuilder();
        if (info.isInGang()) {
            sb.append(info.getFormattedGangTag());
            sb.append(" ");
            sb.append(info.getRankColorCode()).append(info.getRankName());
        }
        return sb.toString();
    }

    /**
     * Rendert Text ueber dem Standard-Nametag.
     */
    private static void renderTextAbove(PoseStack poseStack, MultiBufferSource buffer,
                                         Font font, String text, float yOffset,
                                         int packedLight) {
        Component component = Component.literal(text);
        float textWidth = font.width(component);
        float x = -textWidth / 2.0f;

        poseStack.pushPose();
        poseStack.translate(0.0, yOffset, 0.0);

        Matrix4f matrix = poseStack.last().pose();

        // Hintergrund
        font.drawInBatch(component, x, 0, 0x20FFFFFF, false, matrix, buffer,
                Font.DisplayMode.SEE_THROUGH, 0x40000000, packedLight);
        // Text
        font.drawInBatch(component, x, 0, -1, false, matrix, buffer,
                Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();
    }

    /**
     * Rendert Text unter dem Standard-Nametag.
     */
    private static void renderTextBelow(PoseStack poseStack, MultiBufferSource buffer,
                                         Font font, String text, float yOffset,
                                         int packedLight) {
        Component component = Component.literal(text);
        float textWidth = font.width(component);
        float x = -textWidth / 2.0f;

        poseStack.pushPose();
        poseStack.translate(0.0, yOffset, 0.0);

        Matrix4f matrix = poseStack.last().pose();

        font.drawInBatch(component, x, 0, 0x20FFFFFF, false, matrix, buffer,
                Font.DisplayMode.SEE_THROUGH, 0x40000000, packedLight);
        font.drawInBatch(component, x, 0, -1, false, matrix, buffer,
                Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();
    }
}
