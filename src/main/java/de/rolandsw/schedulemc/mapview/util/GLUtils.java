package de.rolandsw.schedulemc.mapview.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class GLUtils {
    /**
     * 1.20.1 Port: Read texture contents using OpenGL directly instead of GPU APIs
     * Reads the currently bound texture and converts it to a BufferedImage
     */
    public static void readTextureContentsToBufferedImage(int textureId, Consumer<BufferedImage> resultConsumer) {
        RenderSystem.assertOnRenderThread();

        // Bind the texture
        GlStateManager._bindTexture(textureId);

        // Get texture dimensions
        int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);

        if (width <= 0 || height <= 0) {
            // Invalid texture, skip
            return;
        }

        // Read texture data from GPU
        int bufferSize = width * height * 4; // 4 bytes per pixel (RGBA)
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);

        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        // FIX (2026-09-27, Teil 33): Der vorherige Code rief BufferedImage.setRGB(x, y, pixel)
        // EINZELN pro Pixel auf (width*height Aufrufe). Jeder einzelne setRGB()-Aufruf geht durch
        // Raster-Bounds-Checks + ColorModel-Komponentenkonvertierung - bei einem großen Textur-
        // Atlas (dieser Mod registriert hunderte eigene Block-/Item-Texturen) summiert sich das
        // auf mehrere Sekunden CPU-Zeit, GAR NICHT durch GPU-Treiber-Wartezeit verursacht (wurde
        // per Diagnose-Logging widerlegt: eine 5-Sekunden-Verzögerung vor dem Aufruf hat die
        // gemessene Dauer nicht reduziert - 6246ms nach der Verzögerung vs. 6819ms ohne). Fix:
        // ein einziger Bulk-setRGB(...)-Aufruf mit einem fertig vorbereiteten int[]-Array
        // (Standard-JDK-Idiom für schnelles Pixel-Schreiben) statt width*height Einzelaufrufen.
        // TYPE_INT_ARGB statt TYPE_4BYTE_ABGR, da die Bulk-Variante für genau dieses Format den
        // schnellsten (Passthrough-)Pfad nimmt. Alle Aufrufer von terrainBuff lesen ausschließlich
        // über das formatunabhängige BufferedImage.getRGB(x,y) - der Bildtyp-Wechsel ist für sie
        // unsichtbar (verifiziert per Grep, siehe CLAUDE.md Teil 33).
        int[] pixels = new int[width * height];
        for (int i = 0; i < pixels.length; i++) {
            int index = i * 4;
            int r = buffer.get(index) & 0xFF;
            int g = buffer.get(index + 1) & 0xFF;
            int b = buffer.get(index + 2) & 0xFF;
            int a = buffer.get(index + 3) & 0xFF;
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);

        // Call the consumer with the result
        resultConsumer.accept(image);
    }
}
