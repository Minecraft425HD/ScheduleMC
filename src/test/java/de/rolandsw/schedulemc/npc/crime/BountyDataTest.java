package de.rolandsw.schedulemc.npc.crime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for BountyData (Bounty System)
 *
 * Tests cover:
 * - Bounty creation and initialization
 * - Amount increase functionality
 * - Claim mechanics (once only)
 * - Expiration logic (time-based)
 * - Active state calculation
 * - Date formatting (dd.MM.yyyy HH:mm)
 * - Bounty ID generation (UUID)
 * - JSON serialization/deserialization
 * - Edge cases: zero amounts, negative amounts, expired bounties
 */
@DisplayName("BountyData Tests")
class BountyDataTest {

    private static final UUID TEST_TARGET = UUID.randomUUID();
    private static final UUID TEST_PLACER = UUID.randomUUID();
    private static final UUID TEST_HUNTER = UUID.randomUUID();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR AND INITIALIZATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Constructor and Initialization Tests")
    class ConstructorAndInitializationTests {

        @Test
        @DisplayName("Bounty should be created with all required fields")
        void bountyShouldBeCreatedWithAllFields() {
            BountyData bounty = new BountyData(TEST_TARGET, 5000.0, TEST_PLACER, "Murder");

            assertThat(bounty.getTargetUUID()).isEqualTo(TEST_TARGET);
            assertThat(bounty.getAmount()).isEqualTo(5000.0);
            assertThat(bounty.getPlacedBy()).isEqualTo(TEST_PLACER);
            assertThat(bounty.getReason()).isEqualTo("Murder");
        }

        @Test
        @DisplayName("Bounty should generate unique bounty ID")
        void bountyShouldGenerateUniqueBountyId() {
            BountyData bounty1 = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");
            BountyData bounty2 = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            assertThat(bounty1.getBountyId()).isNotNull();
            assertThat(bounty2.getBountyId()).isNotNull();
            assertThat(bounty1.getBountyId()).isNotEqualTo(bounty2.getBountyId());
        }

        @Test
        @DisplayName("Bounty ID should be valid UUID format")
        void bountyIdShouldBeValidUUID() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            // Should not throw exception
            assertThatCode(() -> UUID.fromString(bounty.getBountyId()))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Bounty should have current timestamp")
        void bountyShouldHaveCurrentTimestamp() {
            long before = System.currentTimeMillis();
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");
            long after = System.currentTimeMillis();

            assertThat(bounty.getTimestamp())
                .isGreaterThanOrEqualTo(before)
                .isLessThanOrEqualTo(after);
        }

        @Test
        @DisplayName("Bounty should not be claimed initially")
        void bountyShouldNotBeClaimedInitially() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            assertThat(bounty.isClaimed()).isFalse();
            assertThat(bounty.getClaimedBy()).isNull();
            assertThat(bounty.getClaimedAt()).isZero();
        }

        @Test
        @DisplayName("Bounty should not expire by default (expiresAt = 0)")
        void bountyShouldNotExpireByDefault() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            assertThat(bounty.getExpiresAt()).isZero();
            assertThat(bounty.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Bounty should be active initially")
        void bountyShouldBeActiveInitially() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            assertThat(bounty.isActive()).isTrue();
        }

        @Test
        @DisplayName("Bounty should handle null placer (automatic police bounty)")
        void bountyShouldHandleNullPlacer() {
            BountyData bounty = new BountyData(TEST_TARGET, 2000.0, null, "Wanted Level 3+");

            assertThat(bounty.getPlacedBy()).isNull();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // AMOUNT MANAGEMENT TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Amount Management Tests")
    class AmountManagementTests {

        @ParameterizedTest
        @ValueSource(doubles = {0.01, 100.0, 1000.0, 5000.0, 10000.0, 100000.0})
        @DisplayName("Bounty should accept various positive amounts")
        void bountyShouldAcceptVariousAmounts(double amount) {
            BountyData bounty = new BountyData(TEST_TARGET, amount, TEST_PLACER, "Test");

            assertThat(bounty.getAmount()).isEqualTo(amount);
        }

        @Test
        @DisplayName("Bounty amount should increase correctly")
        void bountyAmountShouldIncrease() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            bounty.increaseAmount(500.0);

            assertThat(bounty.getAmount()).isEqualTo(1500.0);
        }

        @Test
        @DisplayName("Multiple amount increases should accumulate")
        void multipleIncreasesShouldAccumulate() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            bounty.increaseAmount(200.0);
            bounty.increaseAmount(300.0);
            bounty.increaseAmount(500.0);

            assertThat(bounty.getAmount()).isEqualTo(2000.0);
        }

        @Test
        @DisplayName("Zero amount increase should not change amount")
        void zeroIncreaseShouldNotChangeAmount() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            bounty.increaseAmount(0.0);

            assertThat(bounty.getAmount()).isEqualTo(1000.0);
        }

        @Test
        @DisplayName("Bounty should handle zero initial amount")
        void bountyShouldHandleZeroInitialAmount() {
            BountyData bounty = new BountyData(TEST_TARGET, 0.0, TEST_PLACER, "Test");

            assertThat(bounty.getAmount()).isZero();
        }

        @Test
        @DisplayName("Bounty should handle very large amounts")
        void bountyShouldHandleVeryLargeAmounts() {
            double largeAmount = 1_000_000.0;
            BountyData bounty = new BountyData(TEST_TARGET, largeAmount, TEST_PLACER, "Test");

            assertThat(bounty.getAmount()).isEqualTo(largeAmount);
        }

        @Test
        @DisplayName("Amount should preserve decimal precision")
        void amountShouldPreserveDecimalPrecision() {
            BountyData bounty = new BountyData(TEST_TARGET, 1234.567, TEST_PLACER, "Test");

            assertThat(bounty.getAmount()).isEqualTo(1234.567);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CLAIM MECHANICS TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Claim Mechanics Tests")
    class ClaimMechanicsTests {

        @Test
        @DisplayName("Bounty should be claimable once")
        void bountyShouldBeClaimableOnce() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            boolean claimed = bounty.claim(TEST_HUNTER);

            assertThat(claimed).isTrue();
            assertThat(bounty.isClaimed()).isTrue();
            assertThat(bounty.getClaimedBy()).isEqualTo(TEST_HUNTER);
            assertThat(bounty.getClaimedAt()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Bounty should not be claimable twice")
        void bountyShouldNotBeClaimableTwice() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            bounty.claim(TEST_HUNTER);
            boolean secondClaim = bounty.claim(UUID.randomUUID());

            assertThat(secondClaim).isFalse();
            assertThat(bounty.getClaimedBy()).isEqualTo(TEST_HUNTER); // Should still be first claimer
        }

        @Test
        @DisplayName("Claimed bounty should have claim timestamp")
        void claimedBountyShouldHaveClaimTimestamp() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            long before = System.currentTimeMillis();
            bounty.claim(TEST_HUNTER);
            long after = System.currentTimeMillis();

            assertThat(bounty.getClaimedAt())
                .isGreaterThanOrEqualTo(before)
                .isLessThanOrEqualTo(after);
        }

        @Test
        @DisplayName("Claimed bounty should not be active")
        void claimedBountyShouldNotBeActive() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            bounty.claim(TEST_HUNTER);

            assertThat(bounty.isActive()).isFalse();
        }

        @Test
        @DisplayName("Claiming should not change bounty amount")
        void claimingShouldNotChangeAmount() {
            BountyData bounty = new BountyData(TEST_TARGET, 5000.0, TEST_PLACER, "Test");

            bounty.claim(TEST_HUNTER);

            assertThat(bounty.getAmount()).isEqualTo(5000.0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EXPIRATION LOGIC TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Expiration Logic Tests")
    class ExpirationLogicTests {

        @Test
        @DisplayName("Bounty should not expire by default (expiresAt = 0)")
        void bountyShouldNotExpireByDefault() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            assertThat(bounty.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Bounty should expire when expiresAt timestamp is passed")
        void bountyShouldExpireWhenTimestampPassed() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            // Set expiration to past
            long pastTime = System.currentTimeMillis() - 10000;
            bounty.setExpiresAt(pastTime);

            assertThat(bounty.isExpired()).isTrue();
        }

        @Test
        @DisplayName("Bounty should not expire when expiresAt is in future")
        void bountyShouldNotExpireWhenTimestampInFuture() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            // Set expiration to future
            long futureTime = System.currentTimeMillis() + 100000;
            bounty.setExpiresAt(futureTime);

            assertThat(bounty.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Expired bounty should not be active")
        void expiredBountyShouldNotBeActive() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            bounty.setExpiresAt(System.currentTimeMillis() - 1000);

            assertThat(bounty.isActive()).isFalse();
        }

        @Test
        @DisplayName("Setting expiresAt to 0 should make bounty never expire")
        void settingExpiresAtToZeroShouldMakeNeverExpire() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            bounty.setExpiresAt(System.currentTimeMillis() - 1000); // Expired
            bounty.setExpiresAt(0); // Reset to never expire

            assertThat(bounty.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Expired bounty can still be claimed")
        void expiredBountyCanStillBeClaimed() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            bounty.setExpiresAt(System.currentTimeMillis() - 1000);
            boolean claimed = bounty.claim(TEST_HUNTER);

            assertThat(claimed).isTrue(); // Can still be claimed
            assertThat(bounty.isActive()).isFalse(); // But not active (both expired and claimed)
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ACTIVE STATE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Active State Tests")
    class ActiveStateTests {

        @Test
        @DisplayName("Fresh bounty should be active")
        void freshBountyShouldBeActive() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            assertThat(bounty.isActive()).isTrue();
        }

        @Test
        @DisplayName("Claimed bounty should not be active")
        void claimedBountyShouldNotBeActive() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");
            bounty.claim(TEST_HUNTER);

            assertThat(bounty.isActive()).isFalse();
        }

        @Test
        @DisplayName("Expired bounty should not be active")
        void expiredBountyShouldNotBeActive() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");
            bounty.setExpiresAt(System.currentTimeMillis() - 1000);

            assertThat(bounty.isActive()).isFalse();
        }

        @Test
        @DisplayName("Bounty that is both claimed and expired should not be active")
        void claimedAndExpiredBountyShouldNotBeActive() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");
            bounty.setExpiresAt(System.currentTimeMillis() - 1000);
            bounty.claim(TEST_HUNTER);

            assertThat(bounty.isActive()).isFalse();
        }

        @Test
        @DisplayName("Non-expired and unclaimed bounty should be active")
        void nonExpiredUnclaimedBountyShouldBeActive() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");
            bounty.setExpiresAt(System.currentTimeMillis() + 100000);

            assertThat(bounty.isActive()).isTrue();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DATE FORMATTING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Date Formatting Tests")
    class DateFormattingTests {

        @Test
        @DisplayName("Formatted date should match expected pattern")
        void formattedDateShouldMatchPattern() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            String formattedDate = bounty.getFormattedDate();

            // Should match pattern: dd.MM.yyyy HH:mm
            assertThat(formattedDate).matches("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}");
        }

        @Test
        @DisplayName("Formatted expiration should show 'Nie' for non-expiring bounty")
        void formattedExpirationShouldShowNeverForNonExpiring() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            String formattedExpires = bounty.getFormattedExpiresAt();

            assertThat(formattedExpires).contains("Nie");
        }

        @Test
        @DisplayName("Formatted expiration should show date for expiring bounty")
        void formattedExpirationShouldShowDateForExpiring() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");
            bounty.setExpiresAt(System.currentTimeMillis() + 100000);

            String formattedExpires = bounty.getFormattedExpiresAt();

            // Should be a date, not "Nie"
            assertThat(formattedExpires).doesNotContain("Nie");
            assertThat(formattedExpires).matches(".*\\d{2}\\.\\d{2}\\.\\d{4}.*");
        }

        @Test
        @DisplayName("Formatted date should be deterministic for same bounty")
        void formattedDateShouldBeDeterministic() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            String date1 = bounty.getFormattedDate();
            String date2 = bounty.getFormattedDate();

            assertThat(date1).isEqualTo(date2);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PLACER STRING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Placer String Tests")
    class PlacerStringTests {

        @Test
        @DisplayName("Null placer should show 'Polizei (Automatisch)'")
        void nullPlacerShouldShowPolice() {
            BountyData bounty = new BountyData(TEST_TARGET, 2000.0, null, "Wanted 3+");

            String placerString = bounty.getPlacedByString();

            assertThat(placerString).contains("Polizei");
            assertThat(placerString).contains("Automatisch");
        }

        @Test
        @DisplayName("Non-null placer should show unknown player when no server provided")
        void nonNullPlacerShouldShowUnknownWhenNoServer() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            String placerString = bounty.getPlacedByString();

            assertThat(placerString).contains("Unknown Player");
        }

        @Test
        @DisplayName("PlacedBy string with null server should handle gracefully")
        void placedByStringWithNullServerShouldHandleGracefully() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            assertThatCode(() -> bounty.getPlacedByString(null))
                .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FORMATTED DESCRIPTION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Formatted Description Tests")
    class FormattedDescriptionTests {

        @Test
        @DisplayName("Formatted description should include all components")
        void formattedDescriptionShouldIncludeAllComponents() {
            BountyData bounty = new BountyData(TEST_TARGET, 5000.0, TEST_PLACER, "Murder");

            String description = bounty.getFormattedDescription();

            assertThat(description)
                .contains("KOPFGELD")
                .contains("5000")  // amount
                .contains("Murder") // reason
                .contains("Aktiv"); // status
        }

        @Test
        @DisplayName("Formatted description should show 'Aktiv' for active bounty")
        void formattedDescriptionShouldShowActiveForActiveBounty() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            String description = bounty.getFormattedDescription();

            assertThat(description).contains("Aktiv");
        }

        @Test
        @DisplayName("Formatted description should show 'Eingelöst' for claimed bounty")
        void formattedDescriptionShouldShowClaimedForClaimedBounty() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");
            bounty.claim(TEST_HUNTER);

            String description = bounty.getFormattedDescription();

            assertThat(description).contains("Eingelöst");
        }

        @Test
        @DisplayName("Formatted description should show 'Abgelaufen' for expired bounty")
        void formattedDescriptionShouldShowExpiredForExpiredBounty() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");
            bounty.setExpiresAt(System.currentTimeMillis() - 1000);

            String description = bounty.getFormattedDescription();

            assertThat(description).contains("Abgelaufen");
        }

        @Test
        @DisplayName("Formatted description should include Minecraft color codes")
        void formattedDescriptionShouldIncludeColorCodes() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            String description = bounty.getFormattedDescription();

            assertThat(description)
                .contains("§6")  // Gold
                .contains("§7")  // Gray
                .contains("§a"); // Green
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TOSTRING METHOD TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toString() Method Tests")
    class ToStringMethodTests {

        @Test
        @DisplayName("toString should include key bounty information")
        void toStringShouldIncludeKeyInfo() {
            BountyData bounty = new BountyData(TEST_TARGET, 5000.0, TEST_PLACER, "Test");

            String str = bounty.toString();

            assertThat(str)
                .contains("BountyData")
                .contains(bounty.getBountyId())
                .contains(TEST_TARGET.toString())
                .contains("5000");
        }

        @Test
        @DisplayName("toString should indicate active state")
        void toStringShouldIndicateActiveState() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            assertThat(bounty.toString()).contains("active=true");

            bounty.claim(TEST_HUNTER);

            assertThat(bounty.toString()).contains("active=false");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // JSON SERIALIZATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JSONSerializationTests {

        @Test
        @DisplayName("Bounty should serialize to JSON successfully")
        void bountyShouldSerializeToJSON() {
            BountyData bounty = new BountyData(TEST_TARGET, 5000.0, TEST_PLACER, "Murder");

            String json = gson.toJson(bounty);

            assertThat(json)
                .isNotNull()
                .isNotEmpty()
                .contains("\"amount\"")
                .contains("\"targetUUID\"")
                .contains("\"reason\"");
        }

        @Test
        @DisplayName("Bounty should deserialize from JSON successfully")
        void bountyShouldDeserializeFromJSON() {
            BountyData original = new BountyData(TEST_TARGET, 5000.0, TEST_PLACER, "Murder");

            String json = gson.toJson(original);
            BountyData deserialized = gson.fromJson(json, BountyData.class);

            assertThat(deserialized).isNotNull();
            assertThat(deserialized.getAmount()).isEqualTo(original.getAmount());
            assertThat(deserialized.getTargetUUID()).isEqualTo(original.getTargetUUID());
            assertThat(deserialized.getReason()).isEqualTo(original.getReason());
        }

        @Test
        @DisplayName("Serialization should preserve bounty ID")
        void serializationShouldPreserveBountyId() {
            BountyData original = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            String json = gson.toJson(original);
            BountyData deserialized = gson.fromJson(json, BountyData.class);

            assertThat(deserialized.getBountyId()).isEqualTo(original.getBountyId());
        }

        @Test
        @DisplayName("Serialization should preserve claimed state")
        void serializationShouldPreserveClaimedState() {
            BountyData original = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");
            original.claim(TEST_HUNTER);

            String json = gson.toJson(original);
            BountyData deserialized = gson.fromJson(json, BountyData.class);

            assertThat(deserialized.isClaimed()).isTrue();
            assertThat(deserialized.getClaimedBy()).isEqualTo(TEST_HUNTER);
        }

        @Test
        @DisplayName("Serialization should preserve null placer")
        void serializationShouldPreserveNullPlacer() {
            BountyData original = new BountyData(TEST_TARGET, 2000.0, null, "Auto");

            String json = gson.toJson(original);
            BountyData deserialized = gson.fromJson(json, BountyData.class);

            assertThat(deserialized.getPlacedBy()).isNull();
        }

        @Test
        @DisplayName("JSON should use custom field names (SerializedName annotations)")
        void jsonShouldUseCustomFieldNames() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            String json = gson.toJson(bounty);

            // Should use SerializedName annotations
            assertThat(json)
                .contains("\"bountyId\"")
                .contains("\"targetUUID\"")
                .contains("\"amount\"")
                .contains("\"placedBy\"")
                .contains("\"reason\"")
                .contains("\"timestamp\"")
                .contains("\"expiresAt\"")
                .contains("\"claimed\"");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Bounty should handle empty reason string")
        void bountyShouldHandleEmptyReason() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "");

            assertThat(bounty.getReason()).isEmpty();
        }

        @Test
        @DisplayName("Bounty should handle very long reason string")
        void bountyShouldHandleVeryLongReason() {
            String longReason = "A".repeat(1000);
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, longReason);

            assertThat(bounty.getReason()).isEqualTo(longReason);
        }

        @Test
        @DisplayName("Bounty should handle special characters in reason")
        void bountyShouldHandleSpecialCharactersInReason() {
            String specialReason = "Test €$£¥ 中文 🎉 <>&\"'";
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, specialReason);

            assertThat(bounty.getReason()).isEqualTo(specialReason);
        }

        @Test
        @DisplayName("Bounty should handle negative amount increase")
        void bountyShouldHandleNegativeAmountIncrease() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            bounty.increaseAmount(-200.0);

            // Should decrease amount (implementation may vary)
            assertThat(bounty.getAmount()).isEqualTo(800.0);
        }

        @Test
        @DisplayName("Bounty should handle very large timestamp values")
        void bountyShouldHandleVeryLargeTimestamps() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            bounty.setExpiresAt(Long.MAX_VALUE);

            assertThatCode(() -> bounty.isExpired()).doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // IMMUTABILITY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Core bounty fields should be immutable")
        void coreBountyFieldsShouldBeImmutable() {
            BountyData bounty = new BountyData(TEST_TARGET, 1000.0, TEST_PLACER, "Test");

            // Verify core fields don't change
            UUID originalTarget = bounty.getTargetUUID();
            UUID originalPlacer = bounty.getPlacedBy();
            String originalReason = bounty.getReason();
            long originalTimestamp = bounty.getTimestamp();

            bounty.increaseAmount(500.0);
            bounty.claim(TEST_HUNTER);

            assertThat(bounty.getTargetUUID()).isEqualTo(originalTarget);
            assertThat(bounty.getPlacedBy()).isEqualTo(originalPlacer);
            assertThat(bounty.getReason()).isEqualTo(originalReason);
            assertThat(bounty.getTimestamp()).isEqualTo(originalTimestamp);
        }
    }
}
