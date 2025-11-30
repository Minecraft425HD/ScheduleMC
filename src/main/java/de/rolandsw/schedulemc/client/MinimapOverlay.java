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

    private static final int MINIMAP_SIZE = 80;
    private static final int MARGIN = 10;
    private static final int RANGE = 24; // Reduziert für Performance
    private static final int UPDATE_INTERVAL = 10; // Update alle 10 Ticks statt jeden Frame

    // Cache für Performance
    private static int[][] cachedColors = new int[RANGE * 2][RANGE * 2];
    private static BlockPos lastCachePos = BlockPos.ZERO;
    private static int tickCounter = 0;

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

        Level level = mc.player.level();
        BlockPos playerPos = mc.player.blockPosition();
        float playerYaw = mc.player.getYRot();

        // Update Cache nur alle paar Ticks
        tickCounter++;
        if (tickCounter >= UPDATE_INTERVAL || !playerPos.equals(lastCachePos)) {
            updateCache(level, playerPos);
            lastCachePos = playerPos;
            tickCounter = 0;
        }

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // Rotation um Minimap-Zentrum
        poseStack.translate(centerX, centerY, 0);
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(playerYaw));
        poseStack.translate(-centerX, -centerY, 0);

        // Rendere gecachte Karte (rotiert)
        renderCachedMap(guiGraphics, x, y);

        poseStack.popPose();

        // Kreismaske (nicht rotiert)
        renderCircularMask(guiGraphics, centerX, centerY, MINIMAP_SIZE / 2);

        // Himmelsrichtungen (nicht rotiert)
        renderCardinalDirections(guiGraphics, mc, centerX, centerY, MINIMAP_SIZE / 2);

        // Spieler-Marker im Zentrum (nicht rotiert)
        guiGraphics.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, 0xFFFFFF00);
        guiGraphics.fill(centerX - 1, centerY - 1, centerX + 1, centerY + 1, 0xFFFFFFFF);
    }

    /**
     * Update Cache mit aktuellen Terrain-Farben
     */
    private static void updateCache(Level level, BlockPos center) {
        for (int dx = -RANGE; dx < RANGE; dx++) {
            for (int dz = -RANGE; dz < RANGE; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                cachedColors[dx + RANGE][dz + RANGE] = getTerrainColorSimple(level, pos);
            }
        }
    }

    /**
     * Rendert die gecachte Karte
     */
    private static void renderCachedMap(GuiGraphics guiGraphics, int x, int y) {
        int pixelSize = Math.max(1, MINIMAP_SIZE / (RANGE * 2));
        int radius = MINIMAP_SIZE / 2;

        for (int dx = 0; dx < RANGE * 2; dx++) {
            for (int dz = 0; dz < RANGE * 2; dz++) {
                int screenX = dx * pixelSize;
                int screenY = dz * pixelSize;

                // Prüfe ob im Kreis
                int distFromCenter = (int)Math.sqrt(
                    Math.pow(screenX - radius, 2) + Math.pow(screenY - radius, 2)
                );
                if (distFromCenter > radius) continue;

                int color = cachedColors[dx][dz];
                guiGraphics.fill(x + screenX, y + screenY,
                               x + screenX + pixelSize, y + screenY + pixelSize, color);
            }
        }
    }

    /**
     * Vereinfachte Terrain-Farbe (Performance-optimiert)
     */
    private static int getTerrainColorSimple(Level level, BlockPos pos) {
        // Nutze Heightmap für schnellere Berechnung
        int topY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
        BlockPos topPos = new BlockPos(pos.getX(), topY - 1, pos.getZ());

        BlockState state = level.getBlockState(topPos);

        // Schnelle Block-Type Checks
        if (state.is(Blocks.WATER)) return 0xFF3030DD;
        if (state.is(Blocks.LAVA)) return 0xFFDD3030;
        if (state.is(Blocks.GRASS_BLOCK)) return 0xFF60A040;
        if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)) return 0xFF8B6914;
        if (state.is(Blocks.SAND)) return 0xFFDDDD88;
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)) return 0xFF888888;
        if (state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW)) return 0xFFFFFFFF;
        if (state.is(Blocks.OAK_LEAVES) || state.is(Blocks.SPRUCE_LEAVES)
            || state.is(Blocks.BIRCH_LEAVES)) return 0xFF228B22;
        if (state.is(Blocks.OAK_LOG) || state.is(Blocks.SPRUCE_LOG)) return 0xFF8B4513;

        // MapColor als Fallback (schneller als vorher)
        MapColor mapColor = state.getMapColor(level, topPos);
        if (mapColor != MapColor.NONE) {
            int id = mapColor.col;
            if (id == 12) return 0xFF3030DD; // Wasser
            if (id == 1 || id == 7) return 0xFF60A040; // Gras/Pflanzen
            if (id == 10) return 0xFF8B6914; // Erde
            if (id == 11) return 0xFF888888; // Stein
            if (id == 2) return 0xFFDDDD88; // Sand
            if (id == 8) return 0xFFFFFFFF; // Schnee
        }

        // Standard grau
        return 0xFF505050;
    }

    /**
     * Kreismaske über der Minimap
     */
    private static void renderCircularMask(GuiGraphics guiGraphics, int centerX, int centerY, int radius) {
        // Zeichne Maske außerhalb des Kreises
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                int distSq = x * x + y * y;
                if (distSq > radius * radius) {
                    guiGraphics.fill(centerX + x, centerY + y,
                                   centerX + x + 1, centerY + y + 1, 0xCC1A1A1A);
                }
            }
        }

        // Weißer Rand
        for (int angle = 0; angle < 360; angle += 3) {
            double rad = Math.toRadians(angle);
            int x = centerX + (int)(Math.cos(rad) * radius);
            int y = centerY + (int)(Math.sin(rad) * radius);
            guiGraphics.fill(x, y, x + 1, y + 1, 0xFFFFFFFF);
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
