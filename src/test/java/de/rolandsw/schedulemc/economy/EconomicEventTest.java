package de.rolandsw.schedulemc.economy;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for EconomicEvent (Price Event System)
 *
 * Tests cover:
 * - Event creation and initialization
 * - Item affects checking
 * - Multiplier retrieval (default 1.0 for non-affected items)
 * - Expiration logic (time-based, ~20 minutes per MC day)
 * - Remaining time/days calculation
 * - toString() format
 * - Edge cases: expired events, empty multipliers, very long durations
 *
 * BUSINESS LOGIC:
 * - Events affect specific items with price multipliers
 * - Duration measured in Minecraft days (~20 minutes real-time each)
 * - Events expire automatically after duration
 */
@DisplayName("EconomicEvent Tests")
class EconomicEventTest {

    private static final long MC_DAY_MILLIS = 20L * 60 * 1000; // ~20 minutes

    // ═══════════════════════════════════════════════════════════
    // EVENT CREATION AND INITIALIZATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Event Creation and Initialization Tests")
    class EventCreationAndInitializationTests {

        @Test
        @DisplayName("Event should be created with name and multipliers")
        void eventShouldBeCreatedWithNameAndMultipliers() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Diamond Boom", multipliers, 3);

            assertThat(event.getName()).isEqualTo("Diamond Boom");
        }

        @Test
        @DisplayName("Event should store all multipliers")
        void eventShouldStoreAllMultipliers() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);
            multipliers.put(Items.GOLD_INGOT, 2.0f);
            multipliers.put(Items.IRON_INGOT, 0.8f);

            EconomicEvent event = new EconomicEvent("Metal Market", multipliers, 5);

            assertThat(event.affectsItem(Items.DIAMOND)).isTrue();
            assertThat(event.affectsItem(Items.GOLD_INGOT)).isTrue();
            assertThat(event.affectsItem(Items.IRON_INGOT)).isTrue();
        }

        @Test
        @DisplayName("Event should handle empty multipliers map")
        void eventShouldHandleEmptyMultipliers() {
            Map<Item, Float> multipliers = new HashMap<>();

            EconomicEvent event = new EconomicEvent("No Effect Event", multipliers, 1);

            assertThat(event.getName()).isNotNull();
            assertThat(event.affectsItem(Items.DIAMOND)).isFalse();
        }

        @Test
        @DisplayName("Event should calculate expiry time based on duration")
        void eventShouldCalculateExpiryTime() {
            long beforeCreation = System.currentTimeMillis();
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Test Event", multipliers, 3);

            long afterCreation = System.currentTimeMillis();
            long expectedDuration = 3 * MC_DAY_MILLIS;

            // Remaining time should be approximately the duration
            assertThat(event.getRemainingTime())
                .isGreaterThanOrEqualTo(expectedDuration - (afterCreation - beforeCreation))
                .isLessThanOrEqualTo(expectedDuration);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 5, 10, 100})
        @DisplayName("Event should accept various duration lengths")
        void eventShouldAcceptVariousDurations(int durationDays) {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.0f);

            EconomicEvent event = new EconomicEvent("Test", multipliers, durationDays);

            assertThat(event.getRemainingDays()).isGreaterThanOrEqualTo(durationDays - 1);
            assertThat(event.getRemainingDays()).isLessThanOrEqualTo(durationDays);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ITEM AFFECTS TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Item Affects Tests")
    class ItemAffectsTests {

        @Test
        @DisplayName("Event should affect items in multipliers map")
        void eventShouldAffectItemsInMap() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);
            multipliers.put(Items.GOLD_INGOT, 2.0f);

            EconomicEvent event = new EconomicEvent("Precious Metals", multipliers, 3);

            assertThat(event.affectsItem(Items.DIAMOND)).isTrue();
            assertThat(event.affectsItem(Items.GOLD_INGOT)).isTrue();
        }

        @Test
        @DisplayName("Event should not affect items not in multipliers map")
        void eventShouldNotAffectItemsNotInMap() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Diamond Event", multipliers, 3);

            assertThat(event.affectsItem(Items.IRON_INGOT)).isFalse();
            assertThat(event.affectsItem(Items.GOLD_INGOT)).isFalse();
            assertThat(event.affectsItem(Items.COAL)).isFalse();
        }

        @Test
        @DisplayName("Event with no multipliers should not affect any items")
        void eventWithNoMultipliersShouldNotAffectAnyItems() {
            EconomicEvent event = new EconomicEvent("Empty Event", new HashMap<>(), 1);

            assertThat(event.affectsItem(Items.DIAMOND)).isFalse();
            assertThat(event.affectsItem(Items.GOLD_INGOT)).isFalse();
            assertThat(event.affectsItem(Items.IRON_INGOT)).isFalse();
        }

        @Test
        @DisplayName("affectsItem should handle null gracefully")
        void affectsItemShouldHandleNullGracefully() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Test", multipliers, 1);

            assertThatCode(() -> event.affectsItem(null))
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MULTIPLIER RETRIEVAL TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Multiplier Retrieval Tests")
    class MultiplierRetrievalTests {

        @ParameterizedTest
        @CsvSource({
            "0.5",   // 50% cheaper
            "0.8",   // 20% cheaper
            "1.0",   // No change
            "1.5",   // 50% more expensive
            "2.0",   // 100% more expensive
            "3.0"    // 200% more expensive
        })
        @DisplayName("Event should return correct multiplier for affected items")
        void eventShouldReturnCorrectMultiplier(float multiplier) {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, multiplier);

            EconomicEvent event = new EconomicEvent("Test", multipliers, 3);

            assertThat(event.getMultiplier(Items.DIAMOND)).isEqualTo(multiplier);
        }

        @Test
        @DisplayName("Event should return 1.0 for non-affected items")
        void eventShouldReturnDefaultMultiplierForNonAffectedItems() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Diamond Event", multipliers, 3);

            assertThat(event.getMultiplier(Items.IRON_INGOT)).isEqualTo(1.0f);
            assertThat(event.getMultiplier(Items.GOLD_INGOT)).isEqualTo(1.0f);
            assertThat(event.getMultiplier(Items.COAL)).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("Event should handle multiple different multipliers")
        void eventShouldHandleMultipleDifferentMultipliers() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 2.0f);
            multipliers.put(Items.GOLD_INGOT, 1.5f);
            multipliers.put(Items.IRON_INGOT, 0.7f);

            EconomicEvent event = new EconomicEvent("Complex Market", multipliers, 5);

            assertThat(event.getMultiplier(Items.DIAMOND)).isEqualTo(2.0f);
            assertThat(event.getMultiplier(Items.GOLD_INGOT)).isEqualTo(1.5f);
            assertThat(event.getMultiplier(Items.IRON_INGOT)).isEqualTo(0.7f);
        }

        @Test
        @DisplayName("Multiplier should handle very small values")
        void multiplierShouldHandleVerySmallValues() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 0.01f);

            EconomicEvent event = new EconomicEvent("Crash", multipliers, 1);

            assertThat(event.getMultiplier(Items.DIAMOND)).isEqualTo(0.01f);
        }

        @Test
        @DisplayName("Multiplier should handle very large values")
        void multiplierShouldHandleVeryLargeValues() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 100.0f);

            EconomicEvent event = new EconomicEvent("Mega Boom", multipliers, 1);

            assertThat(event.getMultiplier(Items.DIAMOND)).isEqualTo(100.0f);
        }

        @Test
        @DisplayName("getMultiplier should handle null gracefully")
        void getMultiplierShouldHandleNullGracefully() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Test", multipliers, 1);

            // Should return default 1.0 for null
            assertThat(event.getMultiplier(null)).isEqualTo(1.0f);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EXPIRATION LOGIC TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Expiration Logic Tests")
    class ExpirationLogicTests {

        @Test
        @DisplayName("New event should not be expired")
        void newEventShouldNotBeExpired() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Fresh Event", multipliers, 5);

            assertThat(event.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Event with zero duration should expire immediately")
        void eventWithZeroDurationShouldExpireImmediately() throws InterruptedException {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Instant Event", multipliers, 0);

            // Give it a tiny moment to expire
            Thread.sleep(10);

            assertThat(event.isExpired()).isTrue();
        }

        @Test
        @DisplayName("Event with very short duration should expire quickly")
        void eventWithShortDurationShouldExpireQuickly() throws InterruptedException {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            // Create event that lasts ~1ms (manipulating duration)
            // Note: This tests the expiration mechanism, not realistic game timing
            EconomicEvent event = new EconomicEvent("Quick Event", multipliers, 0);

            Thread.sleep(100); // Wait a bit

            assertThat(event.isExpired()).isTrue();
        }

        @Test
        @DisplayName("Event with long duration should not expire quickly")
        void eventWithLongDurationShouldNotExpireQuickly() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Long Event", multipliers, 100);

            assertThat(event.isExpired()).isFalse();
        }

        @Test
        @DisplayName("isExpired should be consistent when called multiple times")
        void isExpiredShouldBeConsistent() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Test Event", multipliers, 10);

            boolean result1 = event.isExpired();
            boolean result2 = event.isExpired();
            boolean result3 = event.isExpired();

            assertThat(result1).isEqualTo(result2);
            assertThat(result2).isEqualTo(result3);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // REMAINING TIME/DAYS TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Remaining Time/Days Tests")
    class RemainingTimeAndDaysTests {

        @Test
        @DisplayName("Remaining time should be positive for active event")
        void remainingTimeShouldBePositiveForActiveEvent() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Active Event", multipliers, 5);

            assertThat(event.getRemainingTime()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Remaining time should be zero for expired event")
        void remainingTimeShouldBeZeroForExpiredEvent() throws InterruptedException {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Expired Event", multipliers, 0);
            Thread.sleep(10);

            assertThat(event.getRemainingTime()).isZero();
        }

        @Test
        @DisplayName("Remaining days should match duration for new event")
        void remainingDaysShouldMatchDurationForNewEvent() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            int duration = 7;
            EconomicEvent event = new EconomicEvent("Week Event", multipliers, duration);

            // Should be close to duration (within 1 day due to rounding)
            assertThat(event.getRemainingDays())
                .isGreaterThanOrEqualTo(duration - 1)
                .isLessThanOrEqualTo(duration);
        }

        @Test
        @DisplayName("Remaining days should decrease over time")
        void remainingDaysShouldDecreaseOverTime() throws InterruptedException {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Test Event", multipliers, 10);

            long remaining1 = event.getRemainingTime();
            Thread.sleep(100); // Wait a bit
            long remaining2 = event.getRemainingTime();

            assertThat(remaining2).isLessThan(remaining1);
        }

        @Test
        @DisplayName("Remaining days should be zero for expired event")
        void remainingDaysShouldBeZeroForExpiredEvent() throws InterruptedException {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Quick Event", multipliers, 0);
            Thread.sleep(10);

            assertThat(event.getRemainingDays()).isZero();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 3, 5, 10, 30})
        @DisplayName("Remaining days should be calculated correctly for various durations")
        void remainingDaysShouldBeCalculatedCorrectly(int duration) {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Test", multipliers, duration);

            int remainingDays = event.getRemainingDays();

            // Should be within reasonable range
            assertThat(remainingDays).isGreaterThanOrEqualTo(duration - 1);
            assertThat(remainingDays).isLessThanOrEqualTo(duration);
        }

        @Test
        @DisplayName("Remaining time should never be negative")
        void remainingTimeShouldNeverBeNegative() throws InterruptedException {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Test", multipliers, 0);
            Thread.sleep(100);

            assertThat(event.getRemainingTime()).isGreaterThanOrEqualTo(0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TOSTRING METHOD TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toString() Method Tests")
    class ToStringMethodTests {

        @Test
        @DisplayName("toString should include event name")
        void toStringShouldIncludeEventName() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Diamond Boom", multipliers, 5);

            String str = event.toString();

            assertThat(str).contains("Diamond Boom");
        }

        @Test
        @DisplayName("toString should include remaining days")
        void toStringShouldIncludeRemainingDays() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Test Event", multipliers, 7);

            String str = event.toString();

            assertThat(str).containsPattern("\\d+\\s+Tage?");
        }

        @Test
        @DisplayName("toString should match expected format")
        void toStringShouldMatchExpectedFormat() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Market Crash", multipliers, 3);

            String str = event.toString();

            // Format: "Name (noch X Tage)"
            assertThat(str).matches(".*\\(noch \\d+ Tage?\\)");
        }

        @Test
        @DisplayName("toString should be non-null and non-empty")
        void toStringShouldBeNonNullAndNonEmpty() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Test", multipliers, 1);

            String str = event.toString();

            assertThat(str).isNotNull();
            assertThat(str).isNotEmpty();
        }

        @Test
        @DisplayName("toString should handle expired event gracefully")
        void toStringShouldHandleExpiredEventGracefully() throws InterruptedException {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Expired", multipliers, 0);
            Thread.sleep(10);

            assertThatCode(() -> event.toString()).doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Event should handle very long name")
        void eventShouldHandleVeryLongName() {
            String longName = "A".repeat(1000);
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent(longName, multipliers, 1);

            assertThat(event.getName()).isEqualTo(longName);
        }

        @Test
        @DisplayName("Event should handle empty name")
        void eventShouldHandleEmptyName() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("", multipliers, 1);

            assertThat(event.getName()).isEmpty();
        }

        @Test
        @DisplayName("Event should handle special characters in name")
        void eventShouldHandleSpecialCharactersInName() {
            String specialName = "€$£¥ 中文 🎉 <>&\"'";
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent(specialName, multipliers, 1);

            assertThat(event.getName()).isEqualTo(specialName);
        }

        @Test
        @DisplayName("Event should handle very large duration")
        void eventShouldHandleVeryLargeDuration() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Long Event", multipliers, 10000);

            assertThat(event.getRemainingDays()).isGreaterThan(0);
            assertThat(event.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Event should handle negative duration gracefully")
        void eventShouldHandleNegativeDuration() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Negative Event", multipliers, -5);

            // Should be immediately expired
            assertThat(event.isExpired()).isTrue();
        }

        @Test
        @DisplayName("Event should handle many items in multipliers map")
        void eventShouldHandleManyItems() {
            Map<Item, Float> multipliers = new HashMap<>();
            // Add many items
            multipliers.put(Items.DIAMOND, 1.1f);
            multipliers.put(Items.GOLD_INGOT, 1.2f);
            multipliers.put(Items.IRON_INGOT, 1.3f);
            multipliers.put(Items.COAL, 0.9f);
            multipliers.put(Items.EMERALD, 1.5f);
            multipliers.put(Items.REDSTONE, 0.8f);
            multipliers.put(Items.LAPIS_LAZULI, 1.1f);

            EconomicEvent event = new EconomicEvent("Complex Market", multipliers, 5);

            assertThat(event.affectsItem(Items.DIAMOND)).isTrue();
            assertThat(event.affectsItem(Items.EMERALD)).isTrue();
            assertThat(event.getMultiplier(Items.COAL)).isEqualTo(0.9f);
        }

        @Test
        @DisplayName("Event should handle zero multiplier")
        void eventShouldHandleZeroMultiplier() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 0.0f);

            EconomicEvent event = new EconomicEvent("Free Diamonds", multipliers, 1);

            assertThat(event.getMultiplier(Items.DIAMOND)).isZero();
        }

        @Test
        @DisplayName("Event should handle negative multiplier")
        void eventShouldHandleNegativeMultiplier() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, -1.5f);

            EconomicEvent event = new EconomicEvent("Weird Event", multipliers, 1);

            assertThat(event.getMultiplier(Items.DIAMOND)).isEqualTo(-1.5f);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BUSINESS LOGIC VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Business Logic Validation Tests")
    class BusinessLogicValidationTests {

        @Test
        @DisplayName("Minecraft day should be approximately 20 minutes")
        void minecraftDayShouldBeApproximatelyTwentyMinutes() {
            assertThat(MC_DAY_MILLIS).isEqualTo(20L * 60 * 1000);
        }

        @Test
        @DisplayName("Event duration calculation should be deterministic")
        void eventDurationCalculationShouldBeDeterministic() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            int duration = 5;
            EconomicEvent event = new EconomicEvent("Test", multipliers, duration);

            int days1 = event.getRemainingDays();
            int days2 = event.getRemainingDays();

            assertThat(days1).isEqualTo(days2);
        }

        @Test
        @DisplayName("Price multipliers should affect pricing correctly")
        void priceMultipliersShouldAffectPricingCorrectly() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f); // 50% more expensive
            multipliers.put(Items.IRON_INGOT, 0.8f); // 20% cheaper

            EconomicEvent event = new EconomicEvent("Market Shift", multipliers, 3);

            // Simulate price calculation
            double diamondBasePrice = 100.0;
            double ironBasePrice = 50.0;

            double diamondFinalPrice = diamondBasePrice * event.getMultiplier(Items.DIAMOND);
            double ironFinalPrice = ironBasePrice * event.getMultiplier(Items.IRON_INGOT);

            assertThat(diamondFinalPrice).isEqualTo(150.0); // 50% increase
            assertThat(ironFinalPrice).isEqualTo(40.0); // 20% decrease
        }

        @Test
        @DisplayName("Non-affected items should maintain original price")
        void nonAffectedItemsShouldMaintainOriginalPrice() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 2.0f);

            EconomicEvent event = new EconomicEvent("Diamond Boom", multipliers, 3);

            double coalBasePrice = 10.0;
            double coalFinalPrice = coalBasePrice * event.getMultiplier(Items.COAL);

            assertThat(coalFinalPrice).isEqualTo(coalBasePrice); // No change (1.0x)
        }
    }
}
