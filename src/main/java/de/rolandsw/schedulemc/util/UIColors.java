package de.rolandsw.schedulemc.util;

/**
 * Centralized UI Color Palette for ScheduleMC
 *
 * Provides semantic, named constants for all UI colors used throughout the mod.
 * This ensures consistency, maintainability, and easy theming.
 *
 * Color Format: 0xAARRGGBB (Alpha-Red-Green-Blue)
 *
 * @author ScheduleMC Team
 * @since 3.2.0
 */
public final class UIColors {

    // ═══════════════════════════════════════════════════════════════════════
    // BASIC COLORS
    // ═══════════════════════════════════════════════════════════════════════

    /** Pure white (opaque) */
    public static final int WHITE = 0xFFFFFFFF;

    /** Pure black (opaque) */
    public static final int BLACK = 0xFF000000;

    /** Transparent black */
    public static final int TRANSPARENT = 0x00000000;

    /** Semi-transparent white (27% opacity) */
    public static final int WHITE_SEMI_TRANSPARENT = 0x44FFFFFF;

    /** Semi-transparent white (67% opacity) */
    public static final int WHITE_TRANSPARENT_67 = 0xAAFFFFFF;

    /** Fully transparent white (for masking) */
    public static final int WHITE_FULLY_TRANSPARENT = 0x00FFFFFF;

    // ═══════════════════════════════════════════════════════════════════════
    // DARK THEME - BACKGROUNDS
    // ═══════════════════════════════════════════════════════════════════════

    /** Very dark background (darkest) */
    public static final int BACKGROUND_DARKEST = 0xFF1A1A1A;

    /** Dark background (darker) */
    public static final int BACKGROUND_DARKER = 0xFF1C1C1C;

    /** Dark background (dark) */
    public static final int BACKGROUND_DARK = 0xFF1E1E1E;

    /** Medium dark background */
    public static final int BACKGROUND_MEDIUM_DARK = 0xFF2A2A2A;

    /** Medium background */
    public static final int BACKGROUND_MEDIUM = 0xFF2B2B2B;

    /** Light dark background */
    public static final int BACKGROUND_LIGHT = 0xFF333333;

    /** Lighter background */
    public static final int BACKGROUND_LIGHTER = 0xFF373737;

    /** Panel background */
    public static final int BACKGROUND_PANEL = 0xFF4C4C4C;

    // ═══════════════════════════════════════════════════════════════════════
    // DARK THEME - OVERLAYS
    // ═══════════════════════════════════════════════════════════════════════

    /** Semi-transparent black overlay (11% opacity) */
    public static final int OVERLAY_BLACK_11 = 0x1B000000;

    /** Semi-transparent dark overlay (27% opacity) */
    public static final int OVERLAY_DARK_27 = 0x44333333;

    /** Semi-transparent red overlay (27% opacity) */
    public static final int OVERLAY_RED_27 = 0x44AA0000;

    /** Semi-transparent white overlay (40% opacity) */
    public static final int OVERLAY_WHITE_40 = 0x66FFFFFF;

    /** Semi-transparent green overlay (27% opacity) - for forest/nature */
    public static final int OVERLAY_GREEN_27 = 0x44228B22;

    // ═══════════════════════════════════════════════════════════════════════
    // GRAYS & NEUTRALS
    // ═══════════════════════════════════════════════════════════════════════

    /** Dark gray */
    public static final int GRAY_DARK = 0xFF555555;

    /** Medium gray */
    public static final int GRAY_MEDIUM = 0xFF666666;

    /** Gray */
    public static final int GRAY = 0xFF888888;

    /** Light gray */
    public static final int GRAY_LIGHT = 0xFF8B8B8B;

    /** Very light gray */
    public static final int GRAY_VERY_LIGHT = 0xFFAAAAAA;

    // ═══════════════════════════════════════════════════════════════════════
    // ACCENT COLORS - PRIMARY
    // ═══════════════════════════════════════════════════════════════════════

    /** Primary blue accent (info, links) */
    public static final int ACCENT_BLUE = 0xFF4A90E2;

    /** Primary green accent (success, confirm) */
    public static final int ACCENT_GREEN = 0xFF4CAF50;

    /** Bright green accent */
    public static final int ACCENT_GREEN_BRIGHT = 0xFF00AA00;

    /** Lime green accent */
    public static final int ACCENT_LIME = 0xFF55FF55;

    /** Primary orange accent (warning, highlight) */
    public static final int ACCENT_ORANGE = 0xFFFFAA00;

    /** Magenta accent (special marker) */
    public static final int ACCENT_MAGENTA = 0xFEFF00FF;

    // ═══════════════════════════════════════════════════════════════════════
    // FUNCTIONAL COLORS - STATES
    // ═══════════════════════════════════════════════════════════════════════

    /** Success state color */
    public static final int SUCCESS = ACCENT_GREEN;

    /** Info state color */
    public static final int INFO = ACCENT_BLUE;

    /** Warning state color */
    public static final int WARNING = ACCENT_ORANGE;

    /** Error state color (to be defined per context) */
    public static final int ERROR = 0xFFE74C3C;

    // ═══════════════════════════════════════════════════════════════════════
    // UI COMPONENTS
    // ═══════════════════════════════════════════════════════════════════════

    /** Default button background */
    public static final int BUTTON_BACKGROUND = BACKGROUND_LIGHT;

    /** Button hover background */
    public static final int BUTTON_HOVER = BACKGROUND_LIGHTER;

    /** Button active/pressed background */
    public static final int BUTTON_ACTIVE = BACKGROUND_MEDIUM_DARK;

    /** Default text color */
    public static final int TEXT_DEFAULT = WHITE;

    /** Secondary text color */
    public static final int TEXT_SECONDARY = GRAY_LIGHT;

    /** Disabled text color */
    public static final int TEXT_DISABLED = GRAY_DARK;

    /** Border color */
    public static final int BORDER = GRAY_MEDIUM;

    /** Divider color */
    public static final int DIVIDER = BACKGROUND_MEDIUM;

    // ═══════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Extracts alpha component from ARGB color
     * @param argb Color in 0xAARRGGBB format
     * @return Alpha value (0-255)
     */
    public static int getAlpha(int argb) {
        return (argb >> 24) & 0xFF;
    }

    /**
     * Extracts red component from ARGB color
     * @param argb Color in 0xAARRGGBB format
     * @return Red value (0-255)
     */
    public static int getRed(int argb) {
        return (argb >> 16) & 0xFF;
    }

    /**
     * Extracts green component from ARGB color
     * @param argb Color in 0xAARRGGBB format
     * @return Green value (0-255)
     */
    public static int getGreen(int argb) {
        return (argb >> 8) & 0xFF;
    }

    /**
     * Extracts blue component from ARGB color
     * @param argb Color in 0xAARRGGBB format
     * @return Blue value (0-255)
     */
    public static int getBlue(int argb) {
        return argb & 0xFF;
    }

    /**
     * Creates ARGB color from components
     * @param alpha Alpha (0-255)
     * @param red Red (0-255)
     * @param green Green (0-255)
     * @param blue Blue (0-255)
     * @return Color in 0xAARRGGBB format
     */
    public static int argb(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * Creates RGB color with full opacity
     * @param red Red (0-255)
     * @param green Green (0-255)
     * @param blue Blue (0-255)
     * @return Color in 0xFFRRGGBB format
     */
    public static int rgb(int red, int green, int blue) {
        return argb(255, red, green, blue);
    }

    /**
     * Applies alpha to existing color
     * @param color Base color
     * @param alpha Alpha value (0-255)
     * @return Color with new alpha
     */
    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    /**
     * Blends two colors
     * @param color1 First color
     * @param color2 Second color
     * @param ratio Blend ratio (0.0 = color1, 1.0 = color2)
     * @return Blended color
     */
    public static int blend(int color1, int color2, float ratio) {
        ratio = Math.max(0.0f, Math.min(1.0f, ratio));

        int a1 = getAlpha(color1);
        int r1 = getRed(color1);
        int g1 = getGreen(color1);
        int b1 = getBlue(color1);

        int a2 = getAlpha(color2);
        int r2 = getRed(color2);
        int g2 = getGreen(color2);
        int b2 = getBlue(color2);

        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        return argb(a, r, g, b);
    }

    // Prevent instantiation
    private UIColors() {
        throw new UnsupportedOperationException("UIColors is a utility class");
    }
}
