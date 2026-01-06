package de.rolandsw.schedulemc.util;

/**
 * Game-wide constants to replace magic numbers.
 *
 * <p>Centralizes commonly used values for better maintainability
 * and code clarity. Using named constants instead of magic numbers
 * improves readability and reduces errors.</p>
 *
 * <p><strong>Benefits:</strong></p>
 * <ul>
 *   <li>Single source of truth for game constants</li>
 *   <li>Self-documenting code (no need to guess what "24000" means)</li>
 *   <li>Easy to update game balance</li>
 *   <li>Type-safe (compile-time checking)</li>
 * </ul>
 *
 * @since 1.0
 */
public final class GameConstants {

    // ═══════════════════════════════════════════════════════════
    // TIME CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Ticks per second (Minecraft runs at 20 TPS) */
    public static final int TICKS_PER_SECOND = 20;

    /** Ticks per minute */
    public static final int TICKS_PER_MINUTE = TICKS_PER_SECOND * 60;

    /** Ticks per Minecraft day (24000 ticks = 20 minutes real-time) */
    public static final int TICKS_PER_DAY = 24000;

    /** Ticks per Minecraft hour */
    public static final int TICKS_PER_HOUR = TICKS_PER_DAY / 24;

    /** Milliseconds per second */
    public static final long MILLIS_PER_SECOND = 1000L;

    /** Milliseconds per minute */
    public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;

    // ═══════════════════════════════════════════════════════════
    // DISTANCE & RANGE CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Standard interaction range in blocks */
    public static final double INTERACTION_RANGE = 4.0;

    /** Extended interaction range for special items */
    public static final double EXTENDED_INTERACTION_RANGE = 6.0;

    /** Chunk size (16x16 blocks) */
    public static final int CHUNK_SIZE = 16;

    /** Render distance multiplier for entities */
    public static final double ENTITY_RENDER_DISTANCE_MULTIPLIER = 1.5;

    // ═══════════════════════════════════════════════════════════
    // ECONOMIC CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Minimum transaction amount (prevents micro-transactions) */
    public static final double MIN_TRANSACTION_AMOUNT = 0.01;

    /** Maximum transaction amount (prevents overflow) */
    public static final double MAX_TRANSACTION_AMOUNT = 1_000_000.0;

    /** Default starting balance for new players */
    public static final double DEFAULT_START_BALANCE = 1000.0;

    /** Default overdraft limit */
    public static final double DEFAULT_OVERDRAFT_LIMIT = -500.0;

    // ═══════════════════════════════════════════════════════════
    // RATE LIMITING CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Default rate limit window in milliseconds */
    public static final long RATE_LIMIT_WINDOW_MS = 1000L;

    /** Maximum operations per window for critical operations */
    public static final int MAX_CRITICAL_OPS_PER_WINDOW = 5;

    /** Maximum operations per window for normal operations */
    public static final int MAX_NORMAL_OPS_PER_WINDOW = 20;

    // ═══════════════════════════════════════════════════════════
    // CACHE CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Default cache TTL in milliseconds (5 seconds) */
    public static final long DEFAULT_CACHE_TTL_MS = 5000L;

    /** Short cache TTL (1 second) for frequently changing data */
    public static final long SHORT_CACHE_TTL_MS = 1000L;

    /** Long cache TTL (1 minute) for rarely changing data */
    public static final long LONG_CACHE_TTL_MS = 60_000L;

    // ═══════════════════════════════════════════════════════════
    // CAPACITY CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Default initial capacity for small collections */
    public static final int SMALL_COLLECTION_CAPACITY = 8;

    /** Default initial capacity for medium collections */
    public static final int MEDIUM_COLLECTION_CAPACITY = 32;

    /** Default initial capacity for large collections */
    public static final int LARGE_COLLECTION_CAPACITY = 128;

    /** Maximum transaction history size per player */
    public static final int MAX_TRANSACTION_HISTORY = 1000;

    /** Maximum cache entries before cleanup */
    public static final int MAX_CACHE_ENTRIES = 10_000;

    // ═══════════════════════════════════════════════════════════
    // NETWORK CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Maximum packet size in bytes (1 MB) */
    public static final int MAX_PACKET_SIZE = 1_048_576;

    /** Network timeout in milliseconds */
    public static final int NETWORK_TIMEOUT_MS = 5000;

    // ═══════════════════════════════════════════════════════════
    // VALIDATION CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Maximum string length for player input */
    public static final int MAX_INPUT_LENGTH = 256;

    /** Maximum string length for descriptions */
    public static final int MAX_DESCRIPTION_LENGTH = 1000;

    /** Minimum password length */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /** Maximum password length */
    public static final int MAX_PASSWORD_LENGTH = 128;

    // ═══════════════════════════════════════════════════════════
    // GAME MECHANICS CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Maximum stack size for items */
    public static final int MAX_STACK_SIZE = 64;

    /** Player inventory size (slots) */
    public static final int PLAYER_INVENTORY_SIZE = 36;

    /** Container default size (slots) */
    public static final int CONTAINER_DEFAULT_SIZE = 27;

    // ═══════════════════════════════════════════════════════════
    // PRODUCTION CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Default growth ticks for plants */
    public static final int DEFAULT_GROWTH_TICKS = 1200;

    /** Fast growth ticks (for boosted growth) */
    public static final int FAST_GROWTH_TICKS = 600;

    /** Slow growth ticks (for difficult plants) */
    public static final int SLOW_GROWTH_TICKS = 2400;

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Converts real-time seconds to Minecraft ticks.
     *
     * @param seconds Real-time seconds
     * @return Equivalent ticks
     */
    public static int secondsToTicks(int seconds) {
        return seconds * TICKS_PER_SECOND;
    }

    /**
     * Converts Minecraft ticks to real-time seconds.
     *
     * @param ticks Minecraft ticks
     * @return Equivalent seconds
     */
    public static double ticksToSeconds(int ticks) {
        return (double) ticks / TICKS_PER_SECOND;
    }

    /**
     * Converts Minecraft days to ticks.
     *
     * @param days Number of Minecraft days
     * @return Equivalent ticks
     */
    public static long daysToTicks(int days) {
        return (long) days * TICKS_PER_DAY;
    }

    /**
     * Converts ticks to Minecraft days.
     *
     * @param ticks Number of ticks
     * @return Equivalent days
     */
    public static int ticksToDays(long ticks) {
        return (int) (ticks / TICKS_PER_DAY);
    }

    private GameConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}
