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

    // PERFORMANCE: Cache wanted text per level to avoid per-frame Component allocation
    private static final String[] WANTED_TEXTS = new String[6];
    // PERFORMANCE: Cache last escape text to avoid per-frame allocation
    private static int lastCachedEscapeSeconds = -1;
    private static String cachedEscapeText = "";

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
                // PERFORMANCE: Cache wanted text per level (only 5 possible values)
                int idx = Math.min(wantedLevel, 5);
                if (WANTED_TEXTS[idx] == null) {
                    String stars = getStarString(wantedLevel);
                    WANTED_TEXTS[idx] = net.minecraft.network.chat.Component.translatable("hud.wanted.wanted", stars).getString();
                }
                String wantedText = WANTED_TEXTS[idx];

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
                // PERFORMANCE: Only recompute string when seconds value changes
                if (secondsRemaining != lastCachedEscapeSeconds) {
                    lastCachedEscapeSeconds = secondsRemaining;
                    cachedEscapeText = net.minecraft.network.chat.Component.translatable("hud.wanted.hidden", secondsRemaining).getString();
                }
                String escapeText = cachedEscapeText;

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

    // PERFORMANCE: Star-Strings vorberechnet statt StringBuilder pro Frame
    private static final String[] STAR_STRINGS = new String[6];
    static {
        for (int level = 0; level <= 5; level++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < level; i++) sb.append("★");
            for (int i = level; i < 5; i++) sb.append("§8☆");
            STAR_STRINGS[level] = sb.toString();
        }
    }

    /**
     * Erzeugt Stern-String basierend auf Wanted-Level.
     * PERFORMANCE: Nutzt vorberechnetes Array statt StringBuilder pro Frame.
     */
    private static String getStarString(int level) {
        if (level >= 0 && level < STAR_STRINGS.length) {
            return STAR_STRINGS[level];
        }
        return STAR_STRINGS[5]; // Max 5 Sterne
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
