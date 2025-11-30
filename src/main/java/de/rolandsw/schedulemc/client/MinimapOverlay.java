package de.rolandsw.schedulemc.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
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
    private static final int RANGE = 32; // Sichtbereich in Blöcken

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Nicht anzeigen wenn F1 gedrückt (GUI versteckt)
        if (!mc.options.hideGui) {
            renderMinimap(event.getGuiGraphics(), mc);
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
        guiGraphics.fill(x - 2, y - 2, x + MINIMAP_SIZE + 2, y + MINIMAP_SIZE + 2, 0xCC000000);

        Level level = mc.player.level();
        BlockPos playerPos = mc.player.blockPosition();
        float playerYaw = mc.player.getYRot();

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // Rotation um Minimap-Zentrum
        poseStack.translate(centerX, centerY, 0);
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(playerYaw));
        poseStack.translate(-centerX, -centerY, 0);

        // Rendere Karte (rotiert)
        renderWorldMapCircular(guiGraphics, level, playerPos, x, y);

        poseStack.popPose();

        // Kreismaske (nicht rotiert)
        renderCircularMask(guiGraphics, centerX, centerY, MINIMAP_SIZE / 2);

        // Himmelsrichtungen (nicht rotiert)
        renderCardinalDirections(guiGraphics, mc, centerX, centerY, MINIMAP_SIZE / 2);

        // Spieler-Marker im Zentrum (nicht rotiert)
        guiGraphics.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, 0xFFFFFF00);

        // Optional: Weißes Dreieck für Blickrichtung
        drawDirectionArrow(guiGraphics, centerX, centerY);
    }

    /**
     * Rendert die Welt als kreisförmige Karte
     */
    private static void renderWorldMapCircular(GuiGraphics guiGraphics, Level level, BlockPos center, int x, int y) {
        int pixelSize = Math.max(1, MINIMAP_SIZE / (RANGE * 2));
        int radius = MINIMAP_SIZE / 2;

        for (int dx = -RANGE; dx < RANGE; dx++) {
            for (int dz = -RANGE; dz < RANGE; dz++) {
                int screenX = (dx + RANGE) * pixelSize;
                int screenY = (dz + RANGE) * pixelSize;

                // Prüfe ob im Kreis
                int distFromCenter = (int)Math.sqrt(
                    Math.pow(screenX - radius, 2) + Math.pow(screenY - radius, 2)
                );
                if (distFromCenter > radius) continue;

                BlockPos pos = center.offset(dx, 0, dz);
                int color = getTerrainColor(level, pos);

                guiGraphics.fill(x + screenX, y + screenY,
                               x + screenX + pixelSize, y + screenY + pixelSize, color);
            }
        }
    }

    /**
     * Holt die Terrain-Farbe für eine Position
     */
    private static int getTerrainColor(Level level, BlockPos pos) {
        // Finde obersten Block
        int topY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        BlockPos topPos = new BlockPos(pos.getX(), topY - 1, pos.getZ()); // -1 weil getHeight einen Block über Surface zurückgibt

        BlockState state = level.getBlockState(topPos);

        // Falls Luft, gehe weiter runter
        if (state.isAir()) {
            for (int i = 1; i < 10; i++) {
                BlockPos checkPos = topPos.below(i);
                BlockState checkState = level.getBlockState(checkPos);
                if (!checkState.isAir()) {
                    state = checkState;
                    topPos = checkPos;
                    break;
                }
            }
        }

        // Nutze MapColor
        MapColor mapColor = state.getMapColor(level, topPos);

        // Fallback zu manueller Farbe basierend auf Block-Typ
        if (mapColor == MapColor.NONE || mapColor.col == 0) {
            return getBlockTypeColor(state);
        }

        // Konvertiere MapColor ID zu RGB
        return getColorFromMapColor(mapColor.col);
    }

    /**
     * Konvertiert MapColor ID zu RGB
     */
    private static int getColorFromMapColor(int colorId) {
        switch (colorId) {
            case 0: return 0xFF000000; // Keine Farbe
            case 1: return 0xFF7FB238; // Gras
            case 2: return 0xFFB5651D; // Sand
            case 3: return 0xFFC7C7C7; // Wolle
            case 4: return 0xFFFF0000; // Feuer
            case 5: return 0xFFA0A0FF; // Eis
            case 6: return 0xFFA7A7A7; // Metall
            case 7: return 0xFF007C00; // Pflanzen
            case 8: return 0xFFFFFFFF; // Schnee
            case 9: return 0xFFA4A8B8; // Lehm
            case 10: return 0xFF976D4D; // Erde
            case 11: return 0xFF707070; // Stein
            case 12: return 0xFF4040FF; // Wasser
            case 13: return 0xFF8B7653; // Holz
            case 14: return 0xFFFFFFFF; // Quarz
            case 15: return 0xFFFF9800; // Orange
            case 16: return 0xFFE91E63; // Magenta
            case 17: return 0xFF2196F3; // Hellblau
            case 18: return 0xFFFDD835; // Gelb
            case 19: return 0xFF8BC34A; // Lime
            case 20: return 0xFFF48FB1; // Rosa
            case 21: return 0xFF757575; // Grau
            case 22: return 0xFFBDBDBD; // Hellgrau
            case 23: return 0xFF00BCD4; // Cyan
            case 24: return 0xFF9C27B0; // Lila
            case 25: return 0xFF3F51B5; // Blau
            case 26: return 0xFF795548; // Braun
            case 27: return 0xFF4CAF50; // Grün
            case 28: return 0xFFFF5722; // Rot
            case 29: return 0xFF212121; // Schwarz
            default: return 0xFF808080; // Fallback Grau
        }
    }

    /**
     * Fallback-Farbe basierend auf Block-Typ
     */
    private static int getBlockTypeColor(BlockState state) {
        // Wasser
        if (state.is(Blocks.WATER)) {
            return 0xFF4040FF;
        }
        // Lava
        if (state.is(Blocks.LAVA)) {
            return 0xFFFF4400;
        }
        // Gras
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS)) {
            return 0xFF7FB238;
        }
        // Stein
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.ANDESITE)
            || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)) {
            return 0xFF808080;
        }
        // Erde
        if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)) {
            return 0xFF976D4D;
        }
        // Sand
        if (state.is(Blocks.SAND) || state.is(Blocks.SANDSTONE)) {
            return 0xFFB5651D;
        }
        // Schnee
        if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK)) {
            return 0xFFFFFFFF;
        }
        // Holz
        if (state.is(Blocks.OAK_LOG) || state.is(Blocks.OAK_PLANKS) || state.is(Blocks.SPRUCE_LOG)
            || state.is(Blocks.BIRCH_LOG) || state.is(Blocks.JUNGLE_LOG)) {
            return 0xFF8B7653;
        }
        // Blätter
        if (state.is(Blocks.OAK_LEAVES) || state.is(Blocks.SPRUCE_LEAVES)
            || state.is(Blocks.BIRCH_LEAVES) || state.is(Blocks.JUNGLE_LEAVES)) {
            return 0xFF007C00;
        }

        // Standard: Dunkelgrau
        return 0xFF404040;
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
                                   centerX + x + 1, centerY + y + 1, 0xFF000000);
                }
            }
        }

        // Rand
        for (int angle = 0; angle < 360; angle += 2) {
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

    /**
     * Zeichnet einen Pfeil für die Blickrichtung
     */
    private static void drawDirectionArrow(GuiGraphics guiGraphics, int centerX, int centerY) {
        // Kleiner Pfeil nach oben (zeigt Norden relativ zum Spieler)
        guiGraphics.fill(centerX, centerY - 4, centerX + 1, centerY - 2, 0xFFFFFFFF);
        guiGraphics.fill(centerX - 1, centerY - 3, centerX, centerY - 2, 0xFFFFFFFF);
        guiGraphics.fill(centerX + 1, centerY - 3, centerX + 2, centerY - 2, 0xFFFFFFFF);
    }
}
