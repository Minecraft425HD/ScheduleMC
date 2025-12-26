package de.rolandsw.schedulemc.mapview.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
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

        // Convert to BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = (x + y * width) * 4;
                int r = buffer.get(index) & 0xFF;
                int g = buffer.get(index + 1) & 0xFF;
                int b = buffer.get(index + 2) & 0xFF;
                int a = buffer.get(index + 3) & 0xFF;

                // Convert RGBA to ABGR for BufferedImage
                int pixel = (a << 24) | (b << 16) | (g << 8) | r;
                image.setRGB(x, y, pixel);
            }
        }

        // Call the consumer with the result
        resultConsumer.accept(image);
    }
}
