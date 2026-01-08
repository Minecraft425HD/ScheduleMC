package de.rolandsw.schedulemc.client;
import de.rolandsw.schedulemc.util.EventHelper;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * HUD Overlay für Wanted-Level Anzeige (oben links)
 * - Zeigt Sterne (★) basierend auf Wanted-Level
 * - Zeigt Escape-Timer Countdown wenn aktiv
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class WantedLevelOverlay {

    private static final int HUD_X = 10;
    private static final int HUD_Y = 10;
    private static final float SCALE = 1.2f;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        EventHelper.handleEvent(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            int wantedLevel = CrimeManager.getClientWantedLevel();
            long escapeTime = CrimeManager.getClientEscapeTime();

            // Nur anzeigen wenn Wanted-Level > 0
            if (wantedLevel <= 0 && escapeTime <= 0) return;

            GuiGraphics guiGraphics = event.getGuiGraphics();

            int currentY = HUD_Y;

            // Berechne Hintergrund-Größe
            int bgWidth = 150;
            int bgHeight = escapeTime > 0 ? 40 : 20;

            // Halbtransparenter Hintergrund
            guiGraphics.fill(HUD_X - 5, currentY - 5, HUD_X + bgWidth, currentY + bgHeight, 0x88000000);

            // === WANTED LEVEL STERNE ===
            if (wantedLevel > 0) {
                String stars = getStarString(wantedLevel);
                String wantedText = net.minecraft.network.chat.Component.translatable("hud.wanted.wanted", stars).getString();

                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
                guiGraphics.drawString(mc.font, wantedText, (int)(HUD_X / SCALE), (int)(currentY / SCALE), 0xFF0000);
                guiGraphics.pose().popPose();

                currentY += 18;
            }

            // === ESCAPE TIMER ===
            if (escapeTime > 0) {
                // Konvertiere Ticks zu Sekunden
                int secondsRemaining = (int) Math.ceil(escapeTime / 20.0);
                String escapeText = net.minecraft.network.chat.Component.translatable("hud.wanted.hidden", secondsRemaining).getString();

                // Fortschrittsbalken
                float progress = (float) escapeTime / CrimeManager.ESCAPE_DURATION;
                int barWidth = 120;
                int barHeight = 6;

                // Balken-Hintergrund
                guiGraphics.fill(HUD_X, currentY + 12, HUD_X + barWidth, currentY + 12 + barHeight, 0xFF1A1A1A);

                // Gefüllter Teil (grün → gelb → rot je nach verbleibender Zeit)
                int filledWidth = (int) (barWidth * progress);
                int barColor = getProgressColor(progress);
                if (filledWidth > 0) {
                    guiGraphics.fill(HUD_X, currentY + 12, HUD_X + filledWidth, currentY + 12 + barHeight, barColor);
                }

                // Rahmen
                guiGraphics.fill(HUD_X - 1, currentY + 11, HUD_X + barWidth + 1, currentY + 12, 0xAAFFFFFF);
                guiGraphics.fill(HUD_X - 1, currentY + 12 + barHeight, HUD_X + barWidth + 1, currentY + 12 + barHeight + 1, 0xAAFFFFFF);
                guiGraphics.fill(HUD_X - 1, currentY + 12, HUD_X, currentY + 12 + barHeight, 0xAAFFFFFF);
                guiGraphics.fill(HUD_X + barWidth, currentY + 12, HUD_X + barWidth + 1, currentY + 12 + barHeight, 0xAAFFFFFF);

                // Text
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(0.9f, 0.9f, 1.0f);
                guiGraphics.drawString(mc.font, escapeText, (int)(HUD_X / 0.9f), (int)(currentY / 0.9f), 0xFFFFFF);
                guiGraphics.pose().popPose();
            }
        }, "onRenderGuiOverlay");
    }

    /**
     * Erzeugt Stern-String basierend auf Wanted-Level
     */
    private static String getStarString(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("★");
        }
        // Leere Sterne für restliche (max 5)
        for (int i = level; i < 5; i++) {
            sb.append("§8☆");
        }
        return sb.toString();
    }

    /**
     * Farbe für Fortschrittsbalken (grün → gelb → rot)
     */
    private static int getProgressColor(float progress) {
        if (progress > 0.66f) {
            return 0xFF4CAF50; // Grün
        } else if (progress > 0.33f) {
            return 0xFFFDD835; // Gelb
        } else {
            return 0xFFF44336; // Rot
        }
    }
}
