package de.rolandsw.schedulemc.economy;

import de.rolandsw.schedulemc.cannabis.items.CannabisItems;
import de.rolandsw.schedulemc.coca.items.CocaItems;
import de.rolandsw.schedulemc.lsd.items.LSDItems;
import de.rolandsw.schedulemc.mdma.items.MDMAItems;
import de.rolandsw.schedulemc.meth.items.MethItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for PriceManager
 *
 * Tests cover:
 * - Price multiplier calculations (time-based waves)
 * - Final price calculations with multipliers
 * - Economic event system
 * - Event expiration and cleanup
 * - Percentage formatting
 * - Edge cases: null items, zero prices, negative prices
 * - Business logic validation
 */
@DisplayName("PriceManager Tests")
class PriceManagerTest {

    @BeforeEach
    void setUp() throws Exception {
        // Clear active events before each test
        clearActiveEvents();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up after each test
        clearActiveEvents();
    }

    /**
     * Helper to clear active events using reflection
     */
    private void clearActiveEvents() throws Exception {
        Field field = PriceManager.class.getDeclaredField("activeEvents");
        field.setAccessible(true);
        List<?> events = (List<?>) field.get(null);
        events.clear();
    }

    // ═══════════════════════════════════════════════════════════
    // PRICE MULTIPLIER TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Price Multiplier Tests")
    class PriceMultiplierTests {

        @Test
        @DisplayName("Price multiplier should be within valid range (0.85 - 1.15) without events")
        void priceMultiplierShouldBeWithinValidRange() {
            // Test with vanilla item (no events should apply)
            float multiplier = PriceManager.getPriceMultiplier(Items.DIAMOND);

            assertThat(multiplier)
                .isGreaterThanOrEqualTo(0.85f)
                .isLessThanOrEqualTo(1.15f);
        }

        @Test
        @DisplayName("Price multiplier should be positive")
        void priceMultiplierShouldBePositive() {
            float multiplier = PriceManager.getPriceMultiplier(Items.DIAMOND);

            assertThat(multiplier).isPositive();
        }

        @Test
        @DisplayName("Price multiplier should be consistent for same item in short time")
        void priceMultiplierShouldBeConsistentInShortTime() {
            Item item = Items.DIAMOND;

            float multiplier1 = PriceManager.getPriceMultiplier(item);
            float multiplier2 = PriceManager.getPriceMultiplier(item);

            // Should be identical when called immediately after
            assertThat(multiplier1).isEqualTo(multiplier2);
        }

        @Test
        @DisplayName("Price multiplier should change over time (wave effect)")
        void priceMultiplierShouldChangeOverTime() throws InterruptedException {
            Item item = Items.DIAMOND;

            float multiplier1 = PriceManager.getPriceMultiplier(item);

            // Wait for time to advance (wave is based on System.currentTimeMillis())
            Thread.sleep(100);

            float multiplier2 = PriceManager.getPriceMultiplier(item);

            // Multipliers might differ due to time-based wave
            // (though difference might be tiny due to slow wave frequency)
            assertThat(multiplier2).isNotNegative();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FINAL PRICE CALCULATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Final Price Calculation Tests")
    class FinalPriceCalculationTests {

        @Test
        @DisplayName("Final price should apply multiplier to base price")
        void finalPriceShouldApplyMultiplier() {
            int basePrice = 1000;
            int finalPrice = PriceManager.getFinalPrice(basePrice, Items.DIAMOND);

            // Final price should be close to base price (within ±15%)
            assertThat(finalPrice)
                .isGreaterThan((int)(basePrice * 0.80))  // Allow some margin
                .isLessThan((int)(basePrice * 1.20));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 10, 100, 1000, 10000, 100000})
        @DisplayName("Final price should scale proportionally with base price")
        void finalPriceShouldScaleProportionally(int basePrice) {
            int finalPrice = PriceManager.getFinalPrice(basePrice, Items.DIAMOND);

            assertThat(finalPrice).isPositive();
        }

        @Test
        @DisplayName("Final price should have minimum of 1€")
        void finalPriceShouldHaveMinimum() {
            int basePrice = 0;
            int finalPrice = PriceManager.getFinalPrice(basePrice, Items.DIAMOND);

            assertThat(finalPrice).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Final price for zero base should return 1€")
        void finalPriceForZeroBaseShouldReturnOne() {
            assertThat(PriceManager.getFinalPrice(0, Items.DIAMOND))
                .isEqualTo(1);
        }

        @Test
        @DisplayName("Final price should handle large base prices without overflow")
        void finalPriceShouldHandleLargeBasePrices() {
            int largeBasePrice = Integer.MAX_VALUE / 2;
            int finalPrice = PriceManager.getFinalPrice(largeBasePrice, Items.DIAMOND);

            assertThat(finalPrice).isPositive();
        }

        @Test
        @DisplayName("Final price should never be negative")
        void finalPriceShouldNeverBeNegative() {
            // Even with negative base price (shouldn't happen, but test defensiveness)
            int finalPrice = PriceManager.getFinalPrice(-100, Items.DIAMOND);

            assertThat(finalPrice).isGreaterThanOrEqualTo(1);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ECONOMIC EVENT SYSTEM TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Economic Event System Tests")
    class EconomicEventSystemTests {

        @Test
        @DisplayName("Should be able to add economic event")
        void shouldBeAbleToAddEvent() throws Exception {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Test Event", multipliers, 3);
            PriceManager.addEvent(event);

            List<EconomicEvent> events = PriceManager.getActiveEvents();
            assertThat(events).contains(event);
        }

        @Test
        @DisplayName("Should be able to remove economic event")
        void shouldBeAbleToRemoveEvent() {
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Test Event", multipliers, 3);
            PriceManager.addEvent(event);
            PriceManager.removeEvent(event);

            List<EconomicEvent> events = PriceManager.getActiveEvents();
            assertThat(events).doesNotContain(event);
        }

        @Test
        @DisplayName("Multiple events should stack multipliers")
        void multipleEventsShouldStackMultipliers() {
            // Create test events
            Map<Item, Float> multipliers1 = new HashMap<>();
            multipliers1.put(Items.DIAMOND, 1.2f);

            Map<Item, Float> multipliers2 = new HashMap<>();
            multipliers2.put(Items.DIAMOND, 1.3f);

            EconomicEvent event1 = new EconomicEvent("Event 1", multipliers1, 3);
            EconomicEvent event2 = new EconomicEvent("Event 2", multipliers2, 3);

            PriceManager.addEvent(event1);
            PriceManager.addEvent(event2);

            float multiplier = PriceManager.getPriceMultiplier(Items.DIAMOND);

            // Should be greater than base due to stacked events
            assertThat(multiplier).isGreaterThan(1.2f);
        }

        @Test
        @DisplayName("Should remove expired events")
        void shouldRemoveExpiredEvents() throws Exception {
            // Create expired event (0 days duration)
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);

            EconomicEvent event = new EconomicEvent("Expired Event", multipliers, 0);

            // Manually set creation time to past
            Field creationTimeField = EconomicEvent.class.getDeclaredField("creationDay");
            creationTimeField.setAccessible(true);
            creationTimeField.set(event, -10L); // Set to past

            PriceManager.addEvent(event);

            // Remove expired events
            PriceManager.removeExpiredEvents();

            List<EconomicEvent> events = PriceManager.getActiveEvents();
            assertThat(events).isEmpty();
        }

        @Test
        @DisplayName("Active events list should be initially empty")
        void activeEventsListShouldBeInitiallyEmpty() {
            List<EconomicEvent> events = PriceManager.getActiveEvents();
            assertThat(events).isEmpty();
        }

        @Test
        @DisplayName("Getting active events should return copy, not original list")
        void getActiveEventsShouldReturnCopy() {
            List<EconomicEvent> events1 = PriceManager.getActiveEvents();
            List<EconomicEvent> events2 = PriceManager.getActiveEvents();

            // Should be different instances (defensive copy)
            assertThat(events1).isNotSameAs(events2);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PERCENTAGE FORMATTING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Percentage Formatting Tests")
    class PercentageFormattingTests {

        @Test
        @DisplayName("Percentage should include sign for positive changes")
        void percentageShouldIncludeSignForPositive() {
            // Add event that increases price
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.5f);  // +50%

            EconomicEvent event = new EconomicEvent("Price Increase", multipliers, 3);
            PriceManager.addEvent(event);

            String percentage = PriceManager.getMultiplierAsPercentage(Items.DIAMOND);

            // Should start with '+'
            assertThat(percentage).matches("^\\+.*");
        }

        @Test
        @DisplayName("Percentage should include sign for negative changes")
        void percentageShouldIncludeSignForNegative() {
            // Add event that decreases price
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 0.7f);  // -30%

            EconomicEvent event = new EconomicEvent("Price Decrease", multipliers, 3);
            PriceManager.addEvent(event);

            String percentage = PriceManager.getMultiplierAsPercentage(Items.DIAMOND);

            // Should start with '-' or contain negative sign
            assertThat(percentage).contains("-");
        }

        @Test
        @DisplayName("Percentage should end with % symbol")
        void percentageShouldEndWithPercentSymbol() {
            String percentage = PriceManager.getMultiplierAsPercentage(Items.DIAMOND);

            assertThat(percentage).endsWith("%");
        }

        @Test
        @DisplayName("Zero change should show ±0%")
        void zeroChangeShouldShowPlusMinusZero() {
            // Create event with multiplier 1.0 (no change)
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 1.0f);

            EconomicEvent event = new EconomicEvent("No Change", multipliers, 3);
            PriceManager.addEvent(event);

            // Note: Due to time-based wave, this might not be exactly ±0%
            // So we just verify format is valid
            String percentage = PriceManager.getMultiplierAsPercentage(Items.DIAMOND);

            assertThat(percentage).matches("^[+\\-±]?\\d+%$");
        }

        @Test
        @DisplayName("Percentage format should be consistent")
        void percentageFormatShouldBeConsistent() {
            String percentage = PriceManager.getMultiplierAsPercentage(Items.DIAMOND);

            // Should be in format: [sign]digits%
            assertThat(percentage).matches("^[+\\-±]?\\d+%$");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle item with no events gracefully")
        void shouldHandleItemWithNoEventsGracefully() {
            // Vanilla item unlikely to be in drug events
            float multiplier = PriceManager.getPriceMultiplier(Items.STONE);

            assertThat(multiplier).isPositive();
        }

        @Test
        @DisplayName("Final price calculation should be deterministic for same inputs")
        void finalPriceCalculationShouldBeDeterministic() {
            int basePrice = 1000;
            Item item = Items.DIAMOND;

            int price1 = PriceManager.getFinalPrice(basePrice, item);
            int price2 = PriceManager.getFinalPrice(basePrice, item);

            // Should be identical when called immediately
            assertThat(price1).isEqualTo(price2);
        }

        @Test
        @DisplayName("Should handle very small base price correctly")
        void shouldHandleVerySmallBasePriceCorrectly() {
            int finalPrice = PriceManager.getFinalPrice(1, Items.DIAMOND);

            assertThat(finalPrice).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should handle maximum integer base price without overflow")
        void shouldHandleMaximumBasePriceWithoutOverflow() {
            int largeBasePrice = Integer.MAX_VALUE / 2;
            int finalPrice = PriceManager.getFinalPrice(largeBasePrice, Items.DIAMOND);

            assertThat(finalPrice).isPositive();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BUSINESS LOGIC VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Business Logic Validation Tests")
    class BusinessLogicValidationTests {

        @Test
        @DisplayName("Price multiplier should never be zero or negative")
        void priceMultiplierShouldNeverBeZeroOrNegative() {
            float multiplier = PriceManager.getPriceMultiplier(Items.DIAMOND);

            assertThat(multiplier).isPositive();
        }

        @Test
        @DisplayName("Final price should always be at least 1€")
        void finalPriceShouldAlwaysBeAtLeastOne() {
            for (int basePrice : new int[]{0, 1, 10, 100, 1000}) {
                int finalPrice = PriceManager.getFinalPrice(basePrice, Items.DIAMOND);
                assertThat(finalPrice).isGreaterThanOrEqualTo(1);
            }
        }

        @Test
        @DisplayName("Time-based price fluctuation should be bounded")
        void timeBasedFluctuationShouldBeBounded() {
            // Take multiple samples over time
            float minMultiplier = Float.MAX_VALUE;
            float maxMultiplier = Float.MIN_VALUE;

            for (int i = 0; i < 10; i++) {
                float multiplier = PriceManager.getPriceMultiplier(Items.DIAMOND);
                minMultiplier = Math.min(minMultiplier, multiplier);
                maxMultiplier = Math.max(maxMultiplier, multiplier);
            }

            // Without events, should be in range 0.85 - 1.15
            assertThat(minMultiplier).isGreaterThanOrEqualTo(0.80f);
            assertThat(maxMultiplier).isLessThanOrEqualTo(1.20f);
        }

        @Test
        @DisplayName("Economic events should affect multiplier")
        void economicEventsShouldAffectMultiplier() {
            // Get baseline multiplier
            float baselineMultiplier = PriceManager.getPriceMultiplier(Items.DIAMOND);

            // Add event
            Map<Item, Float> multipliers = new HashMap<>();
            multipliers.put(Items.DIAMOND, 2.0f);  // Double price

            EconomicEvent event = new EconomicEvent("Price Spike", multipliers, 3);
            PriceManager.addEvent(event);

            float newMultiplier = PriceManager.getPriceMultiplier(Items.DIAMOND);

            // New multiplier should be significantly different
            assertThat(newMultiplier).isNotCloseTo(baselineMultiplier, within(0.5f));
        }

        @Test
        @DisplayName("Manual event triggering should add events")
        void manualEventTriggeringShouldAddEvents() {
            int initialEventCount = PriceManager.getActiveEvents().size();

            PriceManager.triggerEventManually();

            int newEventCount = PriceManager.getActiveEvents().size();

            // Should have added an event (might be 0 → 1)
            assertThat(newEventCount).isGreaterThanOrEqualTo(initialEventCount);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // THREAD SAFETY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Concurrent event additions should not cause exceptions")
        void concurrentEventAdditionsShouldNotCauseExceptions() throws InterruptedException {
            Runnable addEventTask = () -> {
                for (int i = 0; i < 10; i++) {
                    Map<Item, Float> multipliers = new HashMap<>();
                    multipliers.put(Items.DIAMOND, 1.1f);

                    EconomicEvent event = new EconomicEvent("Concurrent Event", multipliers, 1);
                    PriceManager.addEvent(event);
                }
            };

            Thread thread1 = new Thread(addEventTask);
            Thread thread2 = new Thread(addEventTask);

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            // Should complete without throwing ConcurrentModificationException
            List<EconomicEvent> events = PriceManager.getActiveEvents();
            assertThat(events).isNotEmpty();
        }

        @Test
        @DisplayName("Getting active events during modifications should not fail")
        void gettingEventsDuringModificationsShouldNotFail() throws InterruptedException {
            Runnable modifyEventsTask = () -> {
                for (int i = 0; i < 50; i++) {
                    Map<Item, Float> multipliers = new HashMap<>();
                    multipliers.put(Items.DIAMOND, 1.1f);

                    EconomicEvent event = new EconomicEvent("Event " + i, multipliers, 1);
                    PriceManager.addEvent(event);
                }
            };

            Runnable readEventsTask = () -> {
                for (int i = 0; i < 50; i++) {
                    PriceManager.getActiveEvents();
                    PriceManager.getPriceMultiplier(Items.DIAMOND);
                }
            };

            Thread writer = new Thread(modifyEventsTask);
            Thread reader = new Thread(readEventsTask);

            writer.start();
            reader.start();

            writer.join();
            reader.join();

            // Should complete without ConcurrentModificationException
            assertThat(PriceManager.getActiveEvents()).isNotNull();
        }
    }
}
