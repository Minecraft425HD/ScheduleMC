package de.rolandsw.schedulemc.mapview.util;

// GlTexture doesn't exist in 1.20.1
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.system.MemoryUtil;

import java.lang.reflect.Field;

public class DynamicMoveableTexture extends DynamicTexture {
    private final Object bufferLock = new Object();
    private static Field pixelsField = null;
    private static boolean pixelsFieldInitialized = false;

    static {
        // Use reflection to access the private pixels field for performance
        try {
            pixelsField = NativeImage.class.getDeclaredField("pixels");
            pixelsField.setAccessible(true);
            pixelsFieldInitialized = true;
        } catch (Exception e) {
            System.err.println("[MapDataManager] Warning: Could not access NativeImage.pixels field, using slower fallback");
            pixelsFieldInitialized = true;
        }
    }

    public DynamicMoveableTexture(int width, int height, boolean clear) {
        super(width, height, clear);
    }

    private long getPixelsPointer() {
        if (pixelsField == null) return 0L;
        try {
            return (long) pixelsField.get(this.getPixels());
        } catch (Exception e) {
            return 0L;
        }
    }

    public int getWidth() {
        return this.getPixels().getWidth();
    }

    public int getHeight() {
        return this.getPixels().getHeight();
    }

    public int getIndex() {
        return this.getId();
    }

    public void moveX(int offset) {
        synchronized (this.bufferLock) {
            if (offset == 0) return;

            int width = this.getPixels().getWidth();
            int height = this.getPixels().getHeight();
            long pointer = getPixelsPointer();

            // Use fast bulk memory copy if we have access to the pixels pointer
            if (pointer != 0L) {
                int bytesPerPixel = 4; // RGBA

                if (offset > 0) {
                    // Shift right: copy pixels from right to left to avoid overlap
                    for (int y = 0; y < height; y++) {
                        long srcAddr = pointer + (y * width + offset) * bytesPerPixel;
                        long dstAddr = pointer + (y * width) * bytesPerPixel;
                        int copyBytes = (width - offset) * bytesPerPixel;
                        MemoryUtil.memCopy(srcAddr, dstAddr, copyBytes);
                    }
                } else if (offset < 0) {
                    // Shift left: copy pixels from left to right to avoid overlap
                    int absOffset = -offset;
                    for (int y = 0; y < height; y++) {
                        long srcAddr = pointer + (y * width) * bytesPerPixel;
                        long dstAddr = pointer + (y * width + absOffset) * bytesPerPixel;
                        int copyBytes = (width - absOffset) * bytesPerPixel;
                        MemoryUtil.memCopy(srcAddr, dstAddr, copyBytes);
                    }
                }
            } else {
                // Fallback: Use row-based buffer (slower but still better than pixel-by-pixel)
                int[] rowBuffer = new int[width];

                if (offset > 0) {
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            rowBuffer[x] = this.getPixels().getPixelRGBA(x, y);
                        }
                        for (int x = 0; x < width - offset; x++) {
                            this.getPixels().setPixelRGBA(x, y, rowBuffer[x + offset]);
                        }
                    }
                } else if (offset < 0) {
                    int absOffset = -offset;
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            rowBuffer[x] = this.getPixels().getPixelRGBA(x, y);
                        }
                        for (int x = absOffset; x < width; x++) {
                            this.getPixels().setPixelRGBA(x, y, rowBuffer[x - absOffset]);
                        }
                    }
                }
            }
        }
    }

    public void moveY(int offset) {
        synchronized (this.bufferLock) {
            if (offset == 0) return;

            int width = this.getPixels().getWidth();
            int height = this.getPixels().getHeight();
            long pointer = getPixelsPointer();

            // Use fast bulk memory copy if we have access to the pixels pointer
            if (pointer != 0L) {
                int bytesPerPixel = 4; // RGBA
                int rowBytes = width * bytesPerPixel;

                if (offset > 0) {
                    // Shift UP: player moved south, shift map content upward
                    // Copy row by row from TOP to BOTTOM to avoid overlap issues
                    for (int y = 0; y < height - offset; y++) {
                        long srcAddr = pointer + (y + offset) * rowBytes;
                        long dstAddr = pointer + y * rowBytes;
                        MemoryUtil.memCopy(srcAddr, dstAddr, rowBytes);
                    }
                } else if (offset < 0) {
                    // Shift DOWN: player moved north, shift map content downward
                    // Copy row by row from BOTTOM to TOP to avoid overlap issues
                    int absOffset = -offset;
                    for (int y = height - 1; y >= absOffset; y--) {
                        long srcAddr = pointer + (y - absOffset) * rowBytes;
                        long dstAddr = pointer + y * rowBytes;
                        MemoryUtil.memCopy(srcAddr, dstAddr, rowBytes);
                    }
                }
            } else {
                // Fallback: Use row-based buffer (slower but still better than pixel-by-pixel)
                int[] rowBuffer = new int[width];

                if (offset > 0) {
                    // Shift UP: copy from bottom rows to top rows
                    for (int y = 0; y < height - offset; y++) {
                        for (int x = 0; x < width; x++) {
                            rowBuffer[x] = this.getPixels().getPixelRGBA(x, y + offset);
                        }
                        for (int x = 0; x < width; x++) {
                            this.getPixels().setPixelRGBA(x, y, rowBuffer[x]);
                        }
                    }
                } else if (offset < 0) {
                    // Shift DOWN: copy from top rows to bottom rows
                    int absOffset = -offset;
                    for (int y = height - 1; y >= absOffset; y--) {
                        for (int x = 0; x < width; x++) {
                            rowBuffer[x] = this.getPixels().getPixelRGBA(x, y - absOffset);
                        }
                        for (int x = 0; x < width; x++) {
                            this.getPixels().setPixelRGBA(x, y, rowBuffer[x]);
                        }
                    }
                }
            }
        }
    }

    public void setRGB(int x, int y, int color24) {
        // Input is ABGR format (from ColorCalculationService): 0xAABBGGRR
        // NativeImage.setPixelRGBA also expects ABGR format
        // So we just pass through with full alpha
        int color = 0xFF000000 | (color24 & 0x00FFFFFF);
        this.getPixels().setPixelRGBA(x, y, color);
    }
}
