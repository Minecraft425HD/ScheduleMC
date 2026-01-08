package de.rolandsw.schedulemc.npc.crime;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for CrimeRecord
 *
 * @since 1.0
 */
@DisplayName("CrimeRecord Tests")
class CrimeRecordTest {

    private UUID playerUUID;
    private BlockPos location;

    @BeforeEach
    void setUp() {
        playerUUID = UUID.randomUUID();
        location = new BlockPos(100, 64, 200);
    }

    // ========== Construction Tests ==========

    @Test
    @DisplayName("Constructor - Should create record with correct properties")
    void testConstructor() {
        // Act
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, location);

        // Assert
        assertThat(record.getPlayerUUID()).isEqualTo(playerUUID);
        assertThat(record.getType()).isEqualTo(CrimeType.THEFT);
        assertThat(record.getLocation()).isEqualTo(location);
        assertThat(record.getWantedLevelAdded()).isEqualTo(CrimeType.THEFT.getWantedStars());
        assertThat(record.getFineAmount()).isEqualTo(CrimeType.THEFT.getFine());
        assertThat(record.getPrisonDays()).isEqualTo(CrimeType.THEFT.getPrisonDays());
    }

    @Test
    @DisplayName("Constructor - Should generate unique record ID")
    void testUniqueRecordId() {
        // Act
        CrimeRecord record1 = new CrimeRecord(playerUUID, CrimeType.THEFT, location);
        CrimeRecord record2 = new CrimeRecord(playerUUID, CrimeType.THEFT, location);

        // Assert
        assertThat(record1.getRecordId()).isNotEqualTo(record2.getRecordId());
    }

    @Test
    @DisplayName("Constructor - Should set timestamp")
    void testTimestamp() {
        // Arrange
        long before = System.currentTimeMillis();

        // Act
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, location);
        long after = System.currentTimeMillis();

        // Assert
        assertThat(record.getTimestamp()).isBetween(before, after);
    }

    @Test
    @DisplayName("Constructor - Should handle null location")
    void testNullLocation() {
        // Act
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, null);

        // Assert
        assertThat(record.getLocation()).isNull();
        assertThat(record.getPlayerUUID()).isEqualTo(playerUUID);
    }

    @Test
    @DisplayName("Constructor - Should initialize as not served")
    void testInitiallyNotServed() {
        // Act
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, location);

        // Assert
        assertThat(record.isServed()).isFalse();
        assertThat(record.getServedTimestamp()).isEqualTo(0);
    }

    // ========== Crime Type Properties Tests ==========

    @Test
    @DisplayName("Properties - Should match minor crime values")
    void testMinorCrimeProperties() {
        // Act
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.TRESPASSING, location);

        // Assert
        assertThat(record.getWantedLevelAdded()).isEqualTo(1);
        assertThat(record.getFineAmount()).isEqualTo(300.0);
        assertThat(record.getPrisonDays()).isEqualTo(1);
    }

    @Test
    @DisplayName("Properties - Should match moderate crime values")
    void testModerateCrimeProperties() {
        // Act
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.ASSAULT, location);

        // Assert
        assertThat(record.getWantedLevelAdded()).isEqualTo(2);
        assertThat(record.getFineAmount()).isEqualTo(2000.0);
        assertThat(record.getPrisonDays()).isEqualTo(3);
    }

    @Test
    @DisplayName("Properties - Should match serious crime values")
    void testSeriousCrimeProperties() {
        // Act
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.MURDER, location);

        // Assert
        assertThat(record.getWantedLevelAdded()).isEqualTo(3);
        assertThat(record.getFineAmount()).isEqualTo(10000.0);
        assertThat(record.getPrisonDays()).isEqualTo(7);
    }

    @Test
    @DisplayName("Properties - Should match extreme crime values")
    void testExtremeCrimeProperties() {
        // Act
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.TERRORISM, location);

        // Assert
        assertThat(record.getWantedLevelAdded()).isEqualTo(5);
        assertThat(record.getFineAmount()).isEqualTo(100000.0);
        assertThat(record.getPrisonDays()).isEqualTo(20);
    }

    // ========== Served Status Tests ==========

    @Test
    @DisplayName("markServed - Should mark record as served")
    void testMarkServed() {
        // Arrange
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, location);
        long before = System.currentTimeMillis();

        // Act
        record.markServed();
        long after = System.currentTimeMillis();

        // Assert
        assertThat(record.isServed()).isTrue();
        assertThat(record.getServedTimestamp()).isBetween(before, after);
    }

    @Test
    @DisplayName("markServed - Should update served timestamp")
    void testMarkServedTimestamp() {
        // Arrange
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, location);
        assertThat(record.getServedTimestamp()).isEqualTo(0);

        // Act
        record.markServed();

        // Assert
        assertThat(record.getServedTimestamp()).isGreaterThan(0);
    }

    @Test
    @DisplayName("markServed - Should be idempotent")
    void testMarkServedIdempotent() {
        // Arrange
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, location);

        // Act
        record.markServed();
        long firstTimestamp = record.getServedTimestamp();

        record.markServed();
        long secondTimestamp = record.getServedTimestamp();

        // Assert - Timestamp should not change on second call
        assertThat(secondTimestamp).isGreaterThanOrEqualTo(firstTimestamp);
    }

    // ========== Formatted Time Tests ==========

    @Test
    @DisplayName("getFormattedTimestamp - Should return formatted time")
    void testGetFormattedTimestamp() {
        // Act
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, location);
        String formatted = record.getFormattedTimestamp();

        // Assert
        assertThat(formatted).isNotEmpty();
        assertThat(formatted).matches("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}");
    }

    @Test
    @DisplayName("getFormattedServedTime - Should return N/A when not served")
    void testGetFormattedServedTimeNotServed() {
        // Act
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, location);
        String formatted = record.getFormattedServedTime();

        // Assert
        assertThat(formatted).isEqualTo("N/A");
    }

    @Test
    @DisplayName("getFormattedServedTime - Should return formatted time when served")
    void testGetFormattedServedTimeServed() {
        // Arrange
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, location);
        record.markServed();

        // Act
        String formatted = record.getFormattedServedTime();

        // Assert
        assertThat(formatted).isNotEmpty();
        assertThat(formatted).isNotEqualTo("N/A");
        assertThat(formatted).matches("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}");
    }

    // ========== Location Tests ==========

    @Test
    @DisplayName("getFormattedLocation - Should format location coordinates")
    void testGetFormattedLocation() {
        // Act
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, location);
        String formatted = record.getFormattedLocation();

        // Assert
        assertThat(formatted).contains("100");
        assertThat(formatted).contains("64");
        assertThat(formatted).contains("200");
    }

    @Test
    @DisplayName("getFormattedLocation - Should return Unknown for null location")
    void testGetFormattedLocationNull() {
        // Act
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, null);
        String formatted = record.getFormattedLocation();

        // Assert
        assertThat(formatted).isEqualTo("Unbekannt");
    }

    // ========== Summary Tests ==========

    @Test
    @DisplayName("getSummary - Should include crime details")
    void testGetSummary() {
        // Act
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, location);
        String summary = record.getSummary();

        // Assert
        assertThat(summary).contains("Diebstahl");
        assertThat(summary).isNotEmpty();
    }

    @Test
    @DisplayName("getSummary - Should indicate served status")
    void testGetSummaryServed() {
        // Arrange
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, location);

        // Act - Not served
        String summaryNotServed = record.getSummary();

        record.markServed();
        String summaryServed = record.getSummary();

        // Assert
        assertThat(summaryNotServed).isNotEqualTo(summaryServed);
    }

    // ========== Multiple Crime Types Tests ==========

    @Test
    @DisplayName("Multiple Types - Should handle all crime types")
    void testAllCrimeTypes() {
        for (CrimeType type : CrimeType.values()) {
            // Act
            CrimeRecord record = new CrimeRecord(playerUUID, type, location);

            // Assert
            assertThat(record.getType()).isEqualTo(type);
            assertThat(record.getWantedLevelAdded()).isEqualTo(type.getWantedStars());
            assertThat(record.getFineAmount()).isEqualTo(type.getFine());
            assertThat(record.getPrisonDays()).isEqualTo(type.getPrisonDays());
        }
    }

    // ========== Edge Cases Tests ==========

    @Test
    @DisplayName("Edge Case - Location at origin")
    void testLocationAtOrigin() {
        // Act
        BlockPos origin = new BlockPos(0, 0, 0);
        CrimeRecord record = new CrimeRecord(playerUUID, CrimeType.THEFT, origin);

        // Assert
        assertThat(record.getLocation()).isEqualTo(origin);
        assertThat(record.getFormattedLocation()).contains("0");
    }

    @Test
    @DisplayName("Edge Case - Multiple crimes same player")
    void testMultipleCrimesSamePlayer() {
        // Act
        CrimeRecord record1 = new CrimeRecord(playerUUID, CrimeType.THEFT, location);
        CrimeRecord record2 = new CrimeRecord(playerUUID, CrimeType.ASSAULT, location);

        // Assert
        assertThat(record1.getPlayerUUID()).isEqualTo(record2.getPlayerUUID());
        assertThat(record1.getRecordId()).isNotEqualTo(record2.getRecordId());
        assertThat(record1.getType()).isNotEqualTo(record2.getType());
    }
}
