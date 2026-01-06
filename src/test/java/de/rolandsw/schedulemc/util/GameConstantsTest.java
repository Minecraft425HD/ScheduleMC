package de.rolandsw.schedulemc.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for GameConstants
 *
 * @since 1.0
 */
@DisplayName("GameConstants Tests")
class GameConstantsTest {

    // ========== Time Constants Tests ==========

    @Test
    @DisplayName("Time Constants - TICKS_PER_SECOND should be 20")
    void testTicksPerSecond() {
        assertThat(GameConstants.TICKS_PER_SECOND).isEqualTo(20);
    }

    @Test
    @DisplayName("Time Constants - TICKS_PER_MINUTE should be 1200")
    void testTicksPerMinute() {
        assertThat(GameConstants.TICKS_PER_MINUTE).isEqualTo(1200);
    }

    @Test
    @DisplayName("Time Constants - TICKS_PER_DAY should be 24000")
    void testTicksPerDay() {
        assertThat(GameConstants.TICKS_PER_DAY).isEqualTo(24000);
    }

    @Test
    @DisplayName("Time Constants - TICKS_PER_HOUR should be 1000")
    void testTicksPerHour() {
        assertThat(GameConstants.TICKS_PER_HOUR).isEqualTo(1000);
    }

    @Test
    @DisplayName("Time Constants - MILLIS_PER_SECOND should be 1000")
    void testMillisPerSecond() {
        assertThat(GameConstants.MILLIS_PER_SECOND).isEqualTo(1000L);
    }

    // ========== Distance Constants Tests ==========

    @Test
    @DisplayName("Distance Constants - INTERACTION_RANGE should be 4.0")
    void testInteractionRange() {
        assertThat(GameConstants.INTERACTION_RANGE).isEqualTo(4.0);
    }

    @Test
    @DisplayName("Distance Constants - CHUNK_SIZE should be 16")
    void testChunkSize() {
        assertThat(GameConstants.CHUNK_SIZE).isEqualTo(16);
    }

    // ========== Economic Constants Tests ==========

    @Test
    @DisplayName("Economic Constants - MIN_TRANSACTION_AMOUNT should be 0.01")
    void testMinTransactionAmount() {
        assertThat(GameConstants.MIN_TRANSACTION_AMOUNT).isEqualTo(0.01);
    }

    @Test
    @DisplayName("Economic Constants - MAX_TRANSACTION_AMOUNT should be 1000000")
    void testMaxTransactionAmount() {
        assertThat(GameConstants.MAX_TRANSACTION_AMOUNT).isEqualTo(1_000_000.0);
    }

    @Test
    @DisplayName("Economic Constants - DEFAULT_START_BALANCE should be 1000")
    void testDefaultStartBalance() {
        assertThat(GameConstants.DEFAULT_START_BALANCE).isEqualTo(1000.0);
    }

    // ========== Rate Limiting Constants Tests ==========

    @Test
    @DisplayName("Rate Limiting - RATE_LIMIT_WINDOW_MS should be 1000")
    void testRateLimitWindow() {
        assertThat(GameConstants.RATE_LIMIT_WINDOW_MS).isEqualTo(1000L);
    }

    @Test
    @DisplayName("Rate Limiting - MAX_CRITICAL_OPS_PER_WINDOW should be 5")
    void testMaxCriticalOps() {
        assertThat(GameConstants.MAX_CRITICAL_OPS_PER_WINDOW).isEqualTo(5);
    }

    // ========== Cache Constants Tests ==========

    @Test
    @DisplayName("Cache Constants - DEFAULT_CACHE_TTL_MS should be 5000")
    void testDefaultCacheTTL() {
        assertThat(GameConstants.DEFAULT_CACHE_TTL_MS).isEqualTo(5000L);
    }

    @Test
    @DisplayName("Cache Constants - SHORT_CACHE_TTL_MS should be 1000")
    void testShortCacheTTL() {
        assertThat(GameConstants.SHORT_CACHE_TTL_MS).isEqualTo(1000L);
    }

    @Test
    @DisplayName("Cache Constants - LONG_CACHE_TTL_MS should be 60000")
    void testLongCacheTTL() {
        assertThat(GameConstants.LONG_CACHE_TTL_MS).isEqualTo(60_000L);
    }

    // ========== Capacity Constants Tests ==========

    @Test
    @DisplayName("Capacity Constants - MAX_TRANSACTION_HISTORY should be 1000")
    void testMaxTransactionHistory() {
        assertThat(GameConstants.MAX_TRANSACTION_HISTORY).isEqualTo(1000);
    }

    @Test
    @DisplayName("Capacity Constants - MAX_CACHE_ENTRIES should be 10000")
    void testMaxCacheEntries() {
        assertThat(GameConstants.MAX_CACHE_ENTRIES).isEqualTo(10_000);
    }

    // ========== Conversion Methods Tests ==========

    @Test
    @DisplayName("secondsToTicks - Should convert seconds to ticks correctly")
    void testSecondsToTicks() {
        // Act & Assert
        assertThat(GameConstants.secondsToTicks(1)).isEqualTo(20);
        assertThat(GameConstants.secondsToTicks(5)).isEqualTo(100);
        assertThat(GameConstants.secondsToTicks(60)).isEqualTo(1200);
    }

    @Test
    @DisplayName("secondsToTicks - Should handle zero")
    void testSecondsToTicksZero() {
        assertThat(GameConstants.secondsToTicks(0)).isEqualTo(0);
    }

    @Test
    @DisplayName("ticksToSeconds - Should convert ticks to seconds correctly")
    void testTicksToSeconds() {
        // Act & Assert
        assertThat(GameConstants.ticksToSeconds(20)).isEqualTo(1.0);
        assertThat(GameConstants.ticksToSeconds(100)).isEqualTo(5.0);
        assertThat(GameConstants.ticksToSeconds(1200)).isEqualTo(60.0);
    }

    @Test
    @DisplayName("ticksToSeconds - Should handle zero")
    void testTicksToSecondsZero() {
        assertThat(GameConstants.ticksToSeconds(0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("daysToTicks - Should convert days to ticks correctly")
    void testDaysToTicks() {
        // Act & Assert
        assertThat(GameConstants.daysToTicks(1)).isEqualTo(24000L);
        assertThat(GameConstants.daysToTicks(7)).isEqualTo(168000L);
        assertThat(GameConstants.daysToTicks(30)).isEqualTo(720000L);
    }

    @Test
    @DisplayName("daysToTicks - Should handle zero")
    void testDaysToTicksZero() {
        assertThat(GameConstants.daysToTicks(0)).isEqualTo(0L);
    }

    @Test
    @DisplayName("ticksToDays - Should convert ticks to days correctly")
    void testTicksToDays() {
        // Act & Assert
        assertThat(GameConstants.ticksToDays(24000L)).isEqualTo(1);
        assertThat(GameConstants.ticksToDays(168000L)).isEqualTo(7);
        assertThat(GameConstants.ticksToDays(720000L)).isEqualTo(30);
    }

    @Test
    @DisplayName("ticksToDays - Should handle zero")
    void testTicksToDaysZero() {
        assertThat(GameConstants.ticksToDays(0L)).isEqualTo(0);
    }

    @Test
    @DisplayName("ticksToDays - Should handle partial days")
    void testTicksToDaysPartial() {
        // 36000 ticks = 1.5 days, should truncate to 1
        assertThat(GameConstants.ticksToDays(36000L)).isEqualTo(1);
    }

    // ========== Round-trip Conversion Tests ==========

    @Test
    @DisplayName("Round-trip - seconds to ticks and back")
    void testSecondsRoundTrip() {
        int seconds = 42;
        int ticks = GameConstants.secondsToTicks(seconds);
        double backToSeconds = GameConstants.ticksToSeconds(ticks);
        assertThat(backToSeconds).isEqualTo(seconds);
    }

    @Test
    @DisplayName("Round-trip - days to ticks and back")
    void testDaysRoundTrip() {
        int days = 10;
        long ticks = GameConstants.daysToTicks(days);
        int backToDays = GameConstants.ticksToDays(ticks);
        assertThat(backToDays).isEqualTo(days);
    }

    // ========== Validation Tests ==========

    @Test
    @DisplayName("Validation - Transaction amount range is valid")
    void testTransactionAmountRange() {
        assertThat(GameConstants.MIN_TRANSACTION_AMOUNT)
            .isLessThan(GameConstants.MAX_TRANSACTION_AMOUNT);
    }

    @Test
    @DisplayName("Validation - Cache TTL values are ordered")
    void testCacheTTLOrdering() {
        assertThat(GameConstants.SHORT_CACHE_TTL_MS)
            .isLessThan(GameConstants.DEFAULT_CACHE_TTL_MS);
        assertThat(GameConstants.DEFAULT_CACHE_TTL_MS)
            .isLessThan(GameConstants.LONG_CACHE_TTL_MS);
    }

    @Test
    @DisplayName("Validation - Time constants are consistent")
    void testTimeConstantsConsistency() {
        assertThat(GameConstants.TICKS_PER_MINUTE)
            .isEqualTo(GameConstants.TICKS_PER_SECOND * 60);
        assertThat(GameConstants.TICKS_PER_DAY)
            .isEqualTo(GameConstants.TICKS_PER_HOUR * 24);
    }
}
