package de.rolandsw.schedulemc.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.client.screen.apps.MapAppScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * HUD Overlay für runde Minimap (oben rechts)
 * - Zeigt umgebende Welt in Echtzeit
 * - Rotiert mit Spieler-Blickrichtung
 * - Himmelsrichtungen (N, S, O, W) fixiert
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MinimapOverlay {

    private static final int MINIMAP_SIZE = 60; // Reduziert von 80 auf 60
    private static final int MARGIN = 10;
    private static final int RANGE = 40; // In Blöcken - größerer Bereich da wir gecachte Daten nutzen

    // TEXTURE-CACHING für Performance (Lösung 1!)
    private static NativeImage minimapImage = null;
    private static DynamicTexture minimapTexture = null;
    private static ResourceLocation minimapTextureLocation = null;
    private static int textureUpdateCounter = 0;
    private static final int TEXTURE_UPDATE_INTERVAL = 20; // Update Texture alle 20 Ticks (1 Sekunde)

    // Map-Update Counter (für Background-Updates)
    private static int mapUpdateCounter = 0;
    private static final int MAP_UPDATE_INTERVAL = 40; // Update Map alle 40 Ticks (2 Sekunden)

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Initialisiere Texture beim ersten Aufruf
        if (minimapTexture == null) {
            initMinimapTexture(mc);
        }

        // Update Texture nur alle 20 Ticks (1 Sekunde) - PERFORMANCE-TRICK!
        textureUpdateCounter++;
        if (textureUpdateCounter >= TEXTURE_UPDATE_INTERVAL) {
            updateMinimapTexture(mc.player.blockPosition());
            textureUpdateCounter = 0;
        }

        // Rendere Minimap (zeichnet nur die gecachte Texture - SEHR SCHNELL!)
        renderMinimap(event.getGuiGraphics(), mc);
    }

    /**
     * Background-Update für Map-Daten - läuft kontinuierlich auch wenn Map geschlossen ist
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Update Map alle 40 Ticks (2 Sekunden)
        mapUpdateCounter++;
        if (mapUpdateCounter >= MAP_UPDATE_INTERVAL) {
            try {
                MapAppScreen.updateMapDataStatic(mc.level, mc.player.blockPosition());
            } catch (Exception e) {
                // Fehler silent ignorieren - Map-Update ist nicht kritisch
            }
            mapUpdateCounter = 0;
        }
    }

    /**
     * Initialisiert die Minimap-Texture (nur 1x beim ersten Aufruf)
     */
    private static void initMinimapTexture(Minecraft mc) {
        if (minimapImage == null) {
            minimapImage = new NativeImage(MINIMAP_SIZE, MINIMAP_SIZE, false);
            minimapTexture = new DynamicTexture(minimapImage);
            minimapTextureLocation = mc.getTextureManager().register("minimap_overlay", minimapTexture);
        }
    }

    /**
     * Rendert Minimap in die NativeImage Texture (nur alle 20 Ticks = 1 Sekunde!)
     * Das ist der PERFORMANCE-TRICK: Rendering nur 1x pro Sekunde statt 60x!
     */
    private static void updateMinimapTexture(BlockPos playerPos) {
        if (minimapImage == null) return;

        // Clear image - schwarzer Hintergrund
        for (int x = 0; x < MINIMAP_SIZE; x++) {
            for (int y = 0; y < MINIMAP_SIZE; y++) {
                minimapImage.setPixelRGBA(x, y, 0xFF1A1A1A);
            }
        }

        // Rendere Map in NativeImage - GLEICHE LOGIK wie MapAppScreen!
        float scale = (float) MINIMAP_SIZE / (RANGE * 2);
        int viewCenterX = playerPos.getX();
        int viewCenterZ = playerPos.getZ();

        int viewMinWorldX = viewCenterX - RANGE;
        int viewMaxWorldX = viewCenterX + RANGE;
        int viewMinWorldZ = viewCenterZ - RANGE;
        int viewMaxWorldZ = viewCenterZ + RANGE;

        int minChunkX = viewMinWorldX >> 4;
        int maxChunkX = viewMaxWorldX >> 4;
        int minChunkZ = viewMinWorldZ >> 4;
        int maxChunkZ = viewMaxWorldZ >> 4;

        // Rendere nur sichtbare Chunks
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                long chunkKey = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
                byte[] chunkData = MapAppScreen.getExploredChunkData(chunkKey);

                if (chunkData == null) continue;

                for (int localX = 0; localX < 16; localX++) {
                    for (int localZ = 0; localZ < 16; localZ++) {
                        int worldX = (chunkX << 4) + localX;
                        int worldZ = (chunkZ << 4) + localZ;

                        byte colorId = chunkData[localX + localZ * 16];
                        if (colorId == 0) continue;

                        int color = MapAppScreen.getMaterialColorStatic(colorId);

                        int dx = worldX - viewCenterX;
                        int dz = worldZ - viewCenterZ;

                        int imgX = (MINIMAP_SIZE / 2) + (int)(dx * scale);
                        int imgY = (MINIMAP_SIZE / 2) + (int)(dz * scale);

                        // Setze Pixel in NativeImage
                        if (imgX >= 0 && imgX < MINIMAP_SIZE && imgY >= 0 && imgY < MINIMAP_SIZE) {
                            minimapImage.setPixelRGBA(imgX, imgY, color);
                        }
                    }
                }
            }
        }

        // Upload zu GPU - ab jetzt kann die Texture gezeichnet werden
        minimapTexture.upload();
    }

    private static void renderMinimap(GuiGraphics guiGraphics, Minecraft mc) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        // Position: Oben rechts
        int x = screenWidth - MINIMAP_SIZE - MARGIN;
        int y = MARGIN;

        int centerX = x + MINIMAP_SIZE / 2;
        int centerY = y + MINIMAP_SIZE / 2;

        // Hintergrund
        guiGraphics.fill(x - 2, y - 2, x + MINIMAP_SIZE + 2, y + MINIMAP_SIZE + 2, 0xCC1A1A1A);

        float playerYaw = mc.player.getYRot();

        // Zeichne gecachte Texture - SEHR SCHNELL! (nur 1 Texture-Blit statt tausende Pixel-Fills!)
        if (minimapTextureLocation != null) {
            guiGraphics.blit(minimapTextureLocation, x, y, 0, 0, MINIMAP_SIZE, MINIMAP_SIZE, MINIMAP_SIZE, MINIMAP_SIZE);
        }

        // Einfacher Rahmen
        guiGraphics.fill(x - 1, y - 1, x + MINIMAP_SIZE + 1, y, 0xFFFFFFFF); // Top
        guiGraphics.fill(x - 1, y + MINIMAP_SIZE, x + MINIMAP_SIZE + 1, y + MINIMAP_SIZE + 1, 0xFFFFFFFF); // Bottom
        guiGraphics.fill(x - 1, y, x, y + MINIMAP_SIZE, 0xFFFFFFFF); // Left
        guiGraphics.fill(x + MINIMAP_SIZE, y, x + MINIMAP_SIZE + 1, y + MINIMAP_SIZE, 0xFFFFFFFF); // Right

        // Himmelsrichtungen (fixiert - N oben, S unten, O rechts, W links)
        renderCardinalDirections(guiGraphics, mc, centerX, centerY, MINIMAP_SIZE / 2);

        // Spieler-Marker als DREIECK - zeigt in Laufrichtung
        renderPlayerTriangle(guiGraphics, centerX, centerY, playerYaw);
    }

    /**
     * Rendert Spieler als Dreieck das in Laufrichtung zeigt
     */
    private static void renderPlayerTriangle(GuiGraphics guiGraphics, int centerX, int centerY, float yaw) {
        // Dreieck-Größe
        int size = 2;

        // Yaw zu Radians (0° = Süden in Minecraft, -90° = Osten, 90° = Westen, 180° = Norden)
        // Wir wollen dass 0° nach Norden zeigt, also addieren wir 180°
        double angle = Math.toRadians(yaw + 180);

        // Dreieck-Punkte (Spitze zeigt nach oben/Norden bei angle=0)
        // Spitze
        int x1 = centerX + (int)(Math.sin(angle) * size);
        int y1 = centerY - (int)(Math.cos(angle) * size);

        // Linke Ecke
        int x2 = centerX + (int)(Math.sin(angle + Math.toRadians(140)) * size);
        int y2 = centerY - (int)(Math.cos(angle + Math.toRadians(140)) * size);

        // Rechte Ecke
        int x3 = centerX + (int)(Math.sin(angle - Math.toRadians(140)) * size);
        int y3 = centerY - (int)(Math.cos(angle - Math.toRadians(140)) * size);

        // Gelber Hintergrund (größer)
        fillTriangle(guiGraphics, x1, y1, x2, y2, x3, y3, 0xFFFFFF00);

        // Weißer Rand (kleiner)
        int innerSize = size - 1;
        x1 = centerX + (int)(Math.sin(angle) * innerSize);
        y1 = centerY - (int)(Math.cos(angle) * innerSize);
        x2 = centerX + (int)(Math.sin(angle + Math.toRadians(140)) * innerSize);
        y2 = centerY - (int)(Math.cos(angle + Math.toRadians(140)) * innerSize);
        x3 = centerX + (int)(Math.sin(angle - Math.toRadians(140)) * innerSize);
        y3 = centerY - (int)(Math.cos(angle - Math.toRadians(140)) * innerSize);

        fillTriangle(guiGraphics, x1, y1, x2, y2, x3, y3, 0xFFFFFFFF);
    }

    /**
     * Füllt ein Dreieck (einfache Implementierung)
     */
    private static void fillTriangle(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        // Zeichne Linien zwischen den Punkten (einfache Version)
        drawLine(guiGraphics, x1, y1, x2, y2, color);
        drawLine(guiGraphics, x2, y2, x3, y3, color);
        drawLine(guiGraphics, x3, y3, x1, y1, color);

        // Fülle das Dreieck grob (zentrierter Punkt)
        int centerTriX = (x1 + x2 + x3) / 3;
        int centerTriY = (y1 + y2 + y3) / 3;
        guiGraphics.fill(centerTriX - 1, centerTriY - 1, centerTriX + 2, centerTriY + 2, color);
    }

    /**
     * Zeichnet eine Linie zwischen zwei Punkten
     */
    private static void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            guiGraphics.fill(x1, y1, x1 + 1, y1 + 1, color);

            if (x1 == x2 && y1 == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    /**
     * Himmelsrichtungen um die Minimap
     */
    private static void renderCardinalDirections(GuiGraphics guiGraphics, Minecraft mc, int centerX, int centerY, int radius) {
        int offset = radius + 10;

        // N (Norden)
        guiGraphics.drawCenteredString(mc.font, "§fN", centerX, centerY - offset, 0xFFFFFF);

        // S (Süden)
        guiGraphics.drawCenteredString(mc.font, "§7S", centerX, centerY + offset - 8, 0xFFFFFF);

        // O (Osten)
        guiGraphics.drawString(mc.font, "§7O", centerX + offset - 4, centerY - 4, 0xFFFFFF);

        // W (Westen)
        guiGraphics.drawString(mc.font, "§7W", centerX - offset - 4, centerY - 4, 0xFFFFFF);
    }
}
