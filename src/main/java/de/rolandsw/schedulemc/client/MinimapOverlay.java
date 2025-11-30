package de.rolandsw.schedulemc.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.client.screen.apps.MapAppScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
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

    // KEIN eigener Cache mehr - nutzt MapAppScreen.exploredChunks!

    // Map-Update Counter (für Background-Updates)
    private static int mapUpdateCounter = 0;
    private static final int MAP_UPDATE_INTERVAL = 40; // Update Map alle 40 Ticks (2 Sekunden)

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // Nur im Survival/Creative rendern
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Rendere die Minimap
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

    private static void renderMinimap(GuiGraphics guiGraphics, Minecraft mc) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        // Position: Oben rechts
        int x = screenWidth - MINIMAP_SIZE - MARGIN;
        int y = MARGIN;

        int centerX = x + MINIMAP_SIZE / 2;
        int centerY = y + MINIMAP_SIZE / 2;

        // Hintergrund
        guiGraphics.fill(x - 2, y - 2, x + MINIMAP_SIZE + 2, y + MINIMAP_SIZE + 2, 0xCC1A1A1A);

        BlockPos playerPos = mc.player.blockPosition();
        float playerYaw = mc.player.getYRot();

        // KEINE eigene Cache-Berechnung mehr! Nutzt MapAppScreen.exploredChunks

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // Rotation um Minimap-Zentrum
        poseStack.translate(centerX, centerY, 0);
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(playerYaw));
        poseStack.translate(-centerX, -centerY, 0);

        // Rendere direkt aus exploredChunks (rotiert)
        renderMapFromExploredChunks(guiGraphics, x, y, playerPos);

        poseStack.popPose();

        // Einfacher Rahmen statt teurer Kreismaske (Performance!)
        guiGraphics.fill(x - 1, y - 1, x + MINIMAP_SIZE + 1, y, 0xFFFFFFFF); // Top
        guiGraphics.fill(x - 1, y + MINIMAP_SIZE, x + MINIMAP_SIZE + 1, y + MINIMAP_SIZE + 1, 0xFFFFFFFF); // Bottom
        guiGraphics.fill(x - 1, y, x, y + MINIMAP_SIZE, 0xFFFFFFFF); // Left
        guiGraphics.fill(x + MINIMAP_SIZE, y, x + MINIMAP_SIZE + 1, y + MINIMAP_SIZE, 0xFFFFFFFF); // Right

        // Himmelsrichtungen (nicht rotiert)
        renderCardinalDirections(guiGraphics, mc, centerX, centerY, MINIMAP_SIZE / 2);

        // Spieler-Marker im Zentrum (nicht rotiert)
        guiGraphics.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, 0xFFFFFF00);
        guiGraphics.fill(centerX - 1, centerY - 1, centerX + 1, centerY + 1, 0xFFFFFFFF);
    }

    /**
     * Rendert Minimap direkt aus MapAppScreen.exploredChunks (gleiche Daten!)
     */
    private static void renderMapFromExploredChunks(GuiGraphics guiGraphics, int x, int y, BlockPos playerPos) {
        float pixelsPerBlock = (float) MINIMAP_SIZE / (RANGE * 2);

        // Rendere Blöcke im RANGE-Radius um den Spieler
        for (int dx = -RANGE; dx < RANGE; dx++) {
            for (int dz = -RANGE; dz < RANGE; dz++) {
                int worldX = playerPos.getX() + dx;
                int worldZ = playerPos.getZ() + dz;

                // Chunk-Koordinaten
                int chunkX = worldX >> 4;
                int chunkZ = worldZ >> 4;
                int localX = worldX & 15;
                int localZ = worldZ & 15;

                // Hole Farbe aus exploredChunks
                long chunkKey = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
                byte[] chunkData = MapAppScreen.getExploredChunkData(chunkKey);

                int color;
                if (chunkData != null) {
                    byte colorId = chunkData[localX + localZ * 16];
                    color = colorId != 0 ? MapAppScreen.getMaterialColorStatic(colorId) : 0xFF1A1A1A;
                } else {
                    color = 0xFF1A1A1A; // Schwarz für unerkundete Bereiche
                }

                // Screen-Position (zentriert auf Spieler)
                int screenX = x + (int)((dx + RANGE) * pixelsPerBlock);
                int screenY = y + (int)((dz + RANGE) * pixelsPerBlock);
                int pixelSize = Math.max(1, (int) pixelsPerBlock);

                guiGraphics.fill(screenX, screenY, screenX + pixelSize, screenY + pixelSize, color);
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
