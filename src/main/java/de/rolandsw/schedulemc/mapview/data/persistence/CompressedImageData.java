package de.rolandsw.schedulemc.mapview.data.persistence;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import de.rolandsw.schedulemc.mapview.data.persistence.CompressionUtils;
// GlTexture doesn't exist in 1.20.1
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import com.mojang.blaze3d.systems.RenderSystem;
// AddressMode and FilterMode don't exist in 1.20.1 - using GL constants directly when needed
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.DataFormatException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class CompressedImageData {
    // OPTIMIERUNG: LRU-Cache mit max. 8 ByteBuffers verhindert Memory Leaks
    private static final int MAX_BUFFER_CACHE_SIZE = 8;
    private static final Map<Integer, ByteBuffer> byteBuffers = new LinkedHashMap<Integer, ByteBuffer>(
            MAX_BUFFER_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, ByteBuffer> eldest) {
            return size() > MAX_BUFFER_CACHE_SIZE;
        }
    };
    private static final int DEFAULT_SIZE = 256;
    private static final ByteBuffer defaultSizeBuffer = ByteBuffer.allocateDirect(DEFAULT_SIZE * DEFAULT_SIZE * 4).order(ByteOrder.nativeOrder());

    // OPTIMIZATION: volatile for lock-free reads
    private volatile byte[] bytes;
    private final int width;
    private final int height;
    private final Object bufferLock = new Object();
    private volatile boolean isCompressed;
    private final boolean compressNotDelete;
    private final ResourceLocation location = ResourceLocation.fromNamespaceAndPath("schedulemc", "mapview/mapimage/" + UUID.randomUUID());
    private DynamicTexture texture;

    public CompressedImageData(int width, int height, int imageType) {
        this.width = width;
        this.height = height;
        this.bytes = new byte[width * height * 4];
        this.compressNotDelete = MapViewConstants.getLightMapInstance().getWorldMapDataOptions().isOutputImages();
    }

    public byte[] getData() {
        if (this.isCompressed) {
            this.decompress();
        }

        return this.bytes;
    }

    public ResourceLocation getTextureLocation() {
        return this.texture != null ? this.location : null;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void deleteTexture() {
        if (!RenderSystem.isOnRenderThread()) {
            MapViewConstants.getLogger().log(Level.WARN, "Texture unload call from wrong thread", new Exception());
            return;
        }
        if (this.texture != null) {
            Minecraft.getInstance().getTextureManager().release(location);
            this.texture = null;
        }
    }

    public void uploadToTexture() {
        if (!RenderSystem.isOnRenderThread()) {
            MapViewConstants.getLogger().log(Level.WARN, "Texture upload call from wrong thread", new Exception());
            return;
        }

        if (this.isCompressed) {
            this.decompress();
        }

        if (this.texture == null) {
            this.texture = new DynamicTexture(new NativeImage(Format.RGBA, width, height, false));
            Minecraft.getInstance().getTextureManager().register(location, texture);
        }

        ByteBuffer buffer = byteBuffers.get(this.width * this.height);
        if (buffer == null) {
            buffer = ByteBuffer.allocateDirect(this.width * this.height * 4).order(ByteOrder.nativeOrder());
            byteBuffers.put(this.width * this.height, buffer);
        }
        buffer.clear();
        synchronized (this.bufferLock) {
            buffer.put(this.bytes);
            buffer.position(0).limit(this.bytes.length);
        }

        // This needs to be rewritten for 1.20.1 compatibility
        // For now, using pixel-by-pixel upload (unoptimized but works)
        /* Commented out for compilation - needs 1.20.1 compatible implementation
        int imageBytes = width * height * this.texture.getPixelsRGBA().format().components();
        ByteBuffer outBuffer = MemoryUtil.memByteBuffer(this.texture.getPixelsRGBA().getPointer(), imageBytes);
        MemoryUtil.memCopy(buffer, outBuffer);
        */

        // Fallback to pixel-by-pixel upload
        buffer.position(0);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                byte r = buffer.get();
                byte g = buffer.get();
                byte b = buffer.get();
                byte a = buffer.get();
                int color = (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | b & 255;
                this.texture.getPixels().setPixelRGBA(x, y, color);
            }
        }

        this.texture.upload();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture.getId());
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        this.compress();
    }

    public void setRGB(int x, int y, int color) {
        if (this.isCompressed) {
            this.decompress();
        }

        int index = (x + y * this.getWidth()) * 4;
        synchronized (this.bufferLock) {
            int alpha = color >> 24 & 0xFF;
            this.bytes[index + 0] = (byte) ((color >> 16 & 0xFF) * alpha / 255);
            this.bytes[index + 1] = (byte) ((color >> 8 & 0xFF) * alpha / 255);
            this.bytes[index + 2] = (byte) ((color & 0xFF) * alpha / 255);
            this.bytes[index + 3] = -1;
        }
    }

    // OPTIMIZATION: Double-Checked Locking for minimal synchronization
    private void compress() {
        if (!this.isCompressed) {
            synchronized (bufferLock) {
                if (!this.isCompressed) {
                    if (this.compressNotDelete) {
                        byte[] compressedBytes = CompressionUtils.compress(this.bytes);
                        this.bytes = compressedBytes;
                    } else {
                        this.bytes = null;
                    }
                    this.isCompressed = true;
                }
            }
        }
    }

    // OPTIMIZATION: Double-Checked Locking for minimal synchronization
    private void decompress() {
        if (this.isCompressed) {
            synchronized (bufferLock) {
                if (this.isCompressed) {
                    if (this.compressNotDelete) {
                        try {
                            byte[] decompressedBytes = CompressionUtils.decompress(this.bytes);
                            this.bytes = decompressedBytes;
                        } catch (DataFormatException ignored) {
                        }
                    } else {
                        this.bytes = new byte[this.width * this.height * 4];
                    }
                    this.isCompressed = false;
                }
            }
        }
    }

    static {
        byteBuffers.put(DEFAULT_SIZE * DEFAULT_SIZE, defaultSizeBuffer);
    }
}
