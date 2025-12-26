package de.rolandsw.schedulemc.lightmap.util;

/**
 * Compatibility class for ARGB color operations
 * Replaces net.minecraft.util.ARGB which doesn't exist in MC 1.20.1
 */
public class ARGBCompat {

    /**
     * Creates an ARGB color from float components (0.0-1.0 range)
     */
    public static int colorFromFloat(float alpha, float red, float green, float blue) {
        int a = (int) (alpha * 255.0F) & 0xFF;
        int r = (int) (red * 255.0F) & 0xFF;
        int g = (int) (green * 255.0F) & 0xFF;
        int b = (int) (blue * 255.0F) & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Converts ARGB color to ABGR format (swaps R and B channels)
     */
    public static int toABGR(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    /**
     * Creates an ARGB color from byte components
     */
    public static int color(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * Extracts alpha component
     */
    public static int alpha(int argb) {
        return (argb >> 24) & 0xFF;
    }

    /**
     * Extracts red component
     */
    public static int red(int argb) {
        return (argb >> 16) & 0xFF;
    }

    /**
     * Extracts green component
     */
    public static int green(int argb) {
        return (argb >> 8) & 0xFF;
    }

    /**
     * Extracts blue component
     */
    public static int blue(int argb) {
        return argb & 0xFF;
    }
}
