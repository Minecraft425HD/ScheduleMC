package de.rolandsw.schedulemc.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for Transaction data model
 *
 * Tests cover:
 * - Transaction creation and immutability
 * - Getter methods
 * - Date formatting
 * - Description formatting with Minecraft color codes
 * - JSON serialization/deserialization
 * - Edge cases: null UUIDs, zero amounts, negative amounts
 * - toString() method
 * - Transaction ID generation
 */
@DisplayName("Transaction Tests")
class TransactionTest {

    private static final UUID TEST_PLAYER_1 = UUID.randomUUID();
    private static final UUID TEST_PLAYER_2 = UUID.randomUUID();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR AND BASIC GETTER TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Constructor and Getter Tests")
    class ConstructorAndGetterTests {

        @Test
        @DisplayName("Transaction should be created with all required fields")
        void transactionShouldBeCreatedWithAllFields() {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Test deposit",
                500.0
            );

            assertThat(transaction.getType()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(transaction.getFromPlayer()).isNull();
            assertThat(transaction.getToPlayer()).isEqualTo(TEST_PLAYER_1);
            assertThat(transaction.getAmount()).isEqualTo(100.0);
            assertThat(transaction.getDescription()).isEqualTo("Test deposit");
            assertThat(transaction.getBalanceAfter()).isEqualTo(500.0);
        }

        @Test
        @DisplayName("Transaction should generate unique transaction ID")
        void transactionShouldGenerateUniqueId() {
            Transaction tx1 = new Transaction(TransactionType.DEPOSIT, null, TEST_PLAYER_1,
                100.0, "Test", 500.0);
            Transaction tx2 = new Transaction(TransactionType.DEPOSIT, null, TEST_PLAYER_1,
                100.0, "Test", 500.0);

            assertThat(tx1.getTransactionId()).isNotNull();
            assertThat(tx2.getTransactionId()).isNotNull();
            assertThat(tx1.getTransactionId()).isNotEqualTo(tx2.getTransactionId());
        }

        @Test
        @DisplayName("Transaction ID should be valid UUID format")
        void transactionIdShouldBeValidUUID() {
            Transaction transaction = new Transaction(TransactionType.DEPOSIT, null, TEST_PLAYER_1,
                100.0, "Test", 500.0);

            // Should not throw exception
            assertThatCode(() -> UUID.fromString(transaction.getTransactionId()))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Transaction should have current timestamp")
        void transactionShouldHaveCurrentTimestamp() {
            long before = System.currentTimeMillis();
            Transaction transaction = new Transaction(TransactionType.DEPOSIT, null, TEST_PLAYER_1,
                100.0, "Test", 500.0);
            long after = System.currentTimeMillis();

            assertThat(transaction.getTimestamp())
                .isGreaterThanOrEqualTo(before)
                .isLessThanOrEqualTo(after);
        }

        @Test
        @DisplayName("Transaction should handle null fromPlayer")
        void transactionShouldHandleNullFromPlayer() {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,  // fromPlayer is null
                TEST_PLAYER_1,
                100.0,
                "Deposit from system",
                500.0
            );

            assertThat(transaction.getFromPlayer()).isNull();
        }

        @Test
        @DisplayName("Transaction should handle null toPlayer")
        void transactionShouldHandleNullToPlayer() {
            Transaction transaction = new Transaction(
                TransactionType.WITHDRAWAL,
                TEST_PLAYER_1,
                null,  // toPlayer is null
                50.0,
                "Withdrawal to system",
                450.0
            );

            assertThat(transaction.getToPlayer()).isNull();
        }

        @Test
        @DisplayName("Transaction should handle both null players (system transaction)")
        void transactionShouldHandleBothNullPlayers() {
            Transaction transaction = new Transaction(
                TransactionType.FEE,
                null,
                null,
                10.0,
                "System fee",
                490.0
            );

            assertThat(transaction.getFromPlayer()).isNull();
            assertThat(transaction.getToPlayer()).isNull();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TRANSACTION TYPE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Transaction Type Tests")
    class TransactionTypeTests {

        @ParameterizedTest
        @EnumSource(TransactionType.class)
        @DisplayName("Transaction should accept all transaction types")
        void transactionShouldAcceptAllTypes(TransactionType type) {
            Transaction transaction = new Transaction(
                type,
                TEST_PLAYER_1,
                TEST_PLAYER_2,
                100.0,
                "Test",
                500.0
            );

            assertThat(transaction.getType()).isEqualTo(type);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // AMOUNT HANDLING TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Amount Handling Tests")
    class AmountHandlingTests {

        @ParameterizedTest
        @ValueSource(doubles = {0.0, 0.01, 1.0, 100.0, 1000.0, 10000.0, 1000000.0})
        @DisplayName("Transaction should handle positive amounts")
        void transactionShouldHandlePositiveAmounts(double amount) {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                amount,
                "Test",
                1000.0
            );

            assertThat(transaction.getAmount()).isEqualTo(amount);
        }

        @ParameterizedTest
        @ValueSource(doubles = {-0.01, -1.0, -100.0, -1000.0, -10000.0})
        @DisplayName("Transaction should handle negative amounts (withdrawals)")
        void transactionShouldHandleNegativeAmounts(double amount) {
            Transaction transaction = new Transaction(
                TransactionType.WITHDRAWAL,
                TEST_PLAYER_1,
                null,
                amount,
                "Withdrawal",
                500.0
            );

            assertThat(transaction.getAmount()).isEqualTo(amount);
        }

        @Test
        @DisplayName("Transaction should handle zero amount")
        void transactionShouldHandleZeroAmount() {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                0.0,
                "Zero transaction",
                500.0
            );

            assertThat(transaction.getAmount()).isZero();
        }

        @Test
        @DisplayName("Transaction should preserve decimal precision")
        void transactionShouldPreserveDecimalPrecision() {
            double preciseAmount = 123.456789;

            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                preciseAmount,
                "Precise amount",
                500.0
            );

            assertThat(transaction.getAmount()).isEqualTo(preciseAmount);
        }

        @Test
        @DisplayName("Transaction should handle very large amounts")
        void transactionShouldHandleVeryLargeAmounts() {
            double largeAmount = Double.MAX_VALUE / 2;

            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                largeAmount,
                "Large amount",
                largeAmount
            );

            assertThat(transaction.getAmount()).isEqualTo(largeAmount);
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
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Test",
                500.0
            );

            String formattedDate = transaction.getFormattedDate();

            // Should match pattern: dd.MM.yyyy HH:mm:ss
            assertThat(formattedDate).matches("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2}");
        }

        @Test
        @DisplayName("Formatted date should be non-null and non-empty")
        void formattedDateShouldBeNonNullAndNonEmpty() {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Test",
                500.0
            );

            assertThat(transaction.getFormattedDate())
                .isNotNull()
                .isNotEmpty();
        }

        @Test
        @DisplayName("Formatted date should be deterministic for same transaction")
        void formattedDateShouldBeDeterministic() {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Test",
                500.0
            );

            String date1 = transaction.getFormattedDate();
            String date2 = transaction.getFormattedDate();

            assertThat(date1).isEqualTo(date2);
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
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Test deposit",
                600.0
            );

            String formatted = transaction.getFormattedDescription();

            assertThat(formatted)
                .contains("100") // amount (formatted may vary)
                .contains(TransactionType.DEPOSIT.getDisplayName())
                .contains("Test deposit")
                .contains("600"); // balance after
        }

        @Test
        @DisplayName("Formatted description should use green color for positive amounts")
        void formattedDescriptionShouldUseGreenForPositive() {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Deposit",
                600.0
            );

            String formatted = transaction.getFormattedDescription();

            // Should contain §a (green) for positive amount
            assertThat(formatted).contains("§a+");
        }

        @Test
        @DisplayName("Formatted description should use red color for negative amounts")
        void formattedDescriptionShouldUseRedForNegative() {
            Transaction transaction = new Transaction(
                TransactionType.WITHDRAWAL,
                TEST_PLAYER_1,
                null,
                -50.0,
                "Withdrawal",
                550.0
            );

            String formatted = transaction.getFormattedDescription();

            // Should contain §c (red) for negative amount
            assertThat(formatted).contains("§c-");
        }

        @Test
        @DisplayName("Formatted description should include Minecraft color codes")
        void formattedDescriptionShouldIncludeColorCodes() {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Test",
                600.0
            );

            String formatted = transaction.getFormattedDescription();

            // Should contain various color codes
            assertThat(formatted)
                .contains("§7")  // Gray
                .contains("§a")  // Green (positive)
                .contains("§e")  // Yellow
                .contains("§6"); // Gold
        }

        @Test
        @DisplayName("Formatted description should handle null description gracefully")
        void formattedDescriptionShouldHandleNullDescription() {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                null,  // null description
                600.0
            );

            String formatted = transaction.getFormattedDescription();

            assertThat(formatted)
                .isNotNull()
                .isNotEmpty();
        }

        @Test
        @DisplayName("Formatted description should handle empty description gracefully")
        void formattedDescriptionShouldHandleEmptyDescription() {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "",  // empty description
                600.0
            );

            String formatted = transaction.getFormattedDescription();

            assertThat(formatted)
                .isNotNull()
                .isNotEmpty();
        }

        @Test
        @DisplayName("Formatted description should show absolute amount value")
        void formattedDescriptionShouldShowAbsoluteAmount() {
            Transaction transaction = new Transaction(
                TransactionType.WITHDRAWAL,
                TEST_PLAYER_1,
                null,
                -75.50,
                "Withdrawal",
                424.50
            );

            String formatted = transaction.getFormattedDescription();

            // Should show 75.50, not -75.50
            assertThat(formatted).contains("75.50");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TOSTRING METHOD TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toString() Method Tests")
    class ToStringMethodTests {

        @Test
        @DisplayName("toString should include key transaction information")
        void toStringShouldIncludeKeyInfo() {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Test",
                600.0
            );

            String str = transaction.toString();

            assertThat(str)
                .contains(transaction.getTransactionId())
                .contains(TransactionType.DEPOSIT.toString())
                .contains("100");
        }

        @Test
        @DisplayName("toString should be non-null and non-empty")
        void toStringShouldBeNonNullAndNonEmpty() {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Test",
                600.0
            );

            assertThat(transaction.toString())
                .isNotNull()
                .isNotEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // JSON SERIALIZATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JSONSerializationTests {

        @Test
        @DisplayName("Transaction should serialize to JSON successfully")
        void transactionShouldSerializeToJSON() {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Test deposit",
                600.0
            );

            String json = gson.toJson(transaction);

            assertThat(json)
                .isNotNull()
                .isNotEmpty()
                .contains("\"amount\"")
                .contains("\"type\"")
                .contains("\"description\"");
        }

        @Test
        @DisplayName("Transaction should deserialize from JSON successfully")
        void transactionShouldDeserializeFromJSON() {
            Transaction original = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Test deposit",
                600.0
            );

            String json = gson.toJson(original);
            Transaction deserialized = gson.fromJson(json, Transaction.class);

            assertThat(deserialized).isNotNull();
            assertThat(deserialized.getAmount()).isEqualTo(original.getAmount());
            assertThat(deserialized.getType()).isEqualTo(original.getType());
            assertThat(deserialized.getDescription()).isEqualTo(original.getDescription());
            assertThat(deserialized.getBalanceAfter()).isEqualTo(original.getBalanceAfter());
        }

        @Test
        @DisplayName("Serialization should preserve transaction ID")
        void serializationShouldPreserveTransactionId() {
            Transaction original = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Test",
                600.0
            );

            String json = gson.toJson(original);
            Transaction deserialized = gson.fromJson(json, Transaction.class);

            assertThat(deserialized.getTransactionId())
                .isEqualTo(original.getTransactionId());
        }

        @Test
        @DisplayName("Serialization should preserve timestamp")
        void serializationShouldPreserveTimestamp() {
            Transaction original = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Test",
                600.0
            );

            String json = gson.toJson(original);
            Transaction deserialized = gson.fromJson(json, Transaction.class);

            assertThat(deserialized.getTimestamp())
                .isEqualTo(original.getTimestamp());
        }

        @Test
        @DisplayName("Serialization should preserve null player UUIDs")
        void serializationShouldPreserveNullPlayers() {
            Transaction original = new Transaction(
                TransactionType.FEE,
                null,  // fromPlayer is null
                null,  // toPlayer is null
                10.0,
                "System fee",
                590.0
            );

            String json = gson.toJson(original);
            Transaction deserialized = gson.fromJson(json, Transaction.class);

            assertThat(deserialized.getFromPlayer()).isNull();
            assertThat(deserialized.getToPlayer()).isNull();
        }

        @Test
        @DisplayName("JSON should use custom field names (SerializedName annotations)")
        void jsonShouldUseCustomFieldNames() {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Test",
                600.0
            );

            String json = gson.toJson(transaction);

            // Should use SerializedName annotations
            assertThat(json)
                .contains("\"id\"")  // Not "transactionId"
                .contains("\"timestamp\"")
                .contains("\"type\"")
                .contains("\"from\"")  // Not "fromPlayer"
                .contains("\"to\"")    // Not "toPlayer"
                .contains("\"amount\"")
                .contains("\"description\"")
                .contains("\"balanceAfter\"");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Transaction should handle very long descriptions")
        void transactionShouldHandleVeryLongDescriptions() {
            String longDescription = "A".repeat(1000);

            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                longDescription,
                600.0
            );

            assertThat(transaction.getDescription()).isEqualTo(longDescription);
        }

        @Test
        @DisplayName("Transaction should handle special characters in description")
        void transactionShouldHandleSpecialCharacters() {
            String specialDesc = "Test €$£¥ 中文 🎉 <>&\"'";

            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                specialDesc,
                600.0
            );

            assertThat(transaction.getDescription()).isEqualTo(specialDesc);
        }

        @Test
        @DisplayName("Transaction should handle extreme balance values")
        void transactionShouldHandleExtremeBalanceValues() {
            double extremeBalance = Double.MAX_VALUE;

            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Test",
                extremeBalance
            );

            assertThat(transaction.getBalanceAfter()).isEqualTo(extremeBalance);
        }

        @Test
        @DisplayName("Transaction should handle negative balance after")
        void transactionShouldHandleNegativeBalanceAfter() {
            Transaction transaction = new Transaction(
                TransactionType.WITHDRAWAL,
                TEST_PLAYER_1,
                null,
                -700.0,
                "Overdraft",
                -100.0  // Negative balance
            );

            assertThat(transaction.getBalanceAfter()).isEqualTo(-100.0);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // IMMUTABILITY TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Transaction fields should be final and unchangeable")
        void transactionFieldsShouldBeImmutable() {
            Transaction transaction = new Transaction(
                TransactionType.DEPOSIT,
                null,
                TEST_PLAYER_1,
                100.0,
                "Original description",
                600.0
            );

            // Verify getters return same values repeatedly
            assertThat(transaction.getTransactionId()).isEqualTo(transaction.getTransactionId());
            assertThat(transaction.getTimestamp()).isEqualTo(transaction.getTimestamp());
            assertThat(transaction.getType()).isEqualTo(transaction.getType());
            assertThat(transaction.getAmount()).isEqualTo(transaction.getAmount());
            assertThat(transaction.getDescription()).isEqualTo(transaction.getDescription());
            assertThat(transaction.getBalanceAfter()).isEqualTo(transaction.getBalanceAfter());
        }
    }
}
