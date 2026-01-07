package de.rolandsw.schedulemc.npc.crime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for CrimeType enum
 *
 * @since 1.0
 */
@DisplayName("CrimeType Tests")
class CrimeTypeTest {

    // ========== Minor Crimes Tests ==========

    @Test
    @DisplayName("THEFT - Should have correct properties")
    void testTheft() {
        assertThat(CrimeType.THEFT.getWantedStars()).isEqualTo(1);
        assertThat(CrimeType.THEFT.getFine()).isEqualTo(500.0);
        assertThat(CrimeType.THEFT.getPrisonDays()).isEqualTo(1);
        assertThat(CrimeType.THEFT.getDisplayName()).isEqualTo("Diebstahl");
    }

    @Test
    @DisplayName("TRESPASSING - Should have correct properties")
    void testTrespassing() {
        assertThat(CrimeType.TRESPASSING.getWantedStars()).isEqualTo(1);
        assertThat(CrimeType.TRESPASSING.getFine()).isEqualTo(300.0);
        assertThat(CrimeType.TRESPASSING.getPrisonDays()).isEqualTo(1);
    }

    @Test
    @DisplayName("BLACK_MARKET - Should have correct properties")
    void testBlackMarket() {
        assertThat(CrimeType.BLACK_MARKET.getWantedStars()).isEqualTo(1);
        assertThat(CrimeType.BLACK_MARKET.getFine()).isEqualTo(1000.0);
        assertThat(CrimeType.BLACK_MARKET.getPrisonDays()).isEqualTo(2);
    }

    // ========== Moderate Crimes Tests ==========

    @Test
    @DisplayName("ASSAULT - Should have correct properties")
    void testAssault() {
        assertThat(CrimeType.ASSAULT.getWantedStars()).isEqualTo(2);
        assertThat(CrimeType.ASSAULT.getFine()).isEqualTo(2000.0);
        assertThat(CrimeType.ASSAULT.getPrisonDays()).isEqualTo(3);
    }

    @Test
    @DisplayName("DRUG_TRAFFICKING - Should have correct properties")
    void testDrugTrafficking() {
        assertThat(CrimeType.DRUG_TRAFFICKING.getWantedStars()).isEqualTo(2);
        assertThat(CrimeType.DRUG_TRAFFICKING.getFine()).isEqualTo(3000.0);
        assertThat(CrimeType.DRUG_TRAFFICKING.getPrisonDays()).isEqualTo(4);
    }

    // ========== Serious Crimes Tests ==========

    @Test
    @DisplayName("MURDER - Should have correct properties")
    void testMurder() {
        assertThat(CrimeType.MURDER.getWantedStars()).isEqualTo(3);
        assertThat(CrimeType.MURDER.getFine()).isEqualTo(10000.0);
        assertThat(CrimeType.MURDER.getPrisonDays()).isEqualTo(7);
    }

    @Test
    @DisplayName("GANG_VIOLENCE - Should have correct properties")
    void testGangViolence() {
        assertThat(CrimeType.GANG_VIOLENCE.getWantedStars()).isEqualTo(3);
        assertThat(CrimeType.GANG_VIOLENCE.getFine()).isEqualTo(5000.0);
        assertThat(CrimeType.GANG_VIOLENCE.getPrisonDays()).isEqualTo(5);
    }

    // ========== Extreme Crimes Tests ==========

    @Test
    @DisplayName("PRISON_ESCAPE - Should have correct properties")
    void testPrisonEscape() {
        assertThat(CrimeType.PRISON_ESCAPE.getWantedStars()).isEqualTo(4);
        assertThat(CrimeType.PRISON_ESCAPE.getFine()).isEqualTo(20000.0);
        assertThat(CrimeType.PRISON_ESCAPE.getPrisonDays()).isEqualTo(10);
    }

    @Test
    @DisplayName("POLICE_ASSAULT - Should have correct properties")
    void testPoliceAssault() {
        assertThat(CrimeType.POLICE_ASSAULT.getWantedStars()).isEqualTo(5);
        assertThat(CrimeType.POLICE_ASSAULT.getFine()).isEqualTo(50000.0);
        assertThat(CrimeType.POLICE_ASSAULT.getPrisonDays()).isEqualTo(14);
    }

    @Test
    @DisplayName("TERRORISM - Should have correct properties")
    void testTerrorism() {
        assertThat(CrimeType.TERRORISM.getWantedStars()).isEqualTo(5);
        assertThat(CrimeType.TERRORISM.getFine()).isEqualTo(100000.0);
        assertThat(CrimeType.TERRORISM.getPrisonDays()).isEqualTo(20);
    }

    // ========== Severity Tests ==========

    @Test
    @DisplayName("getSeverity - Should match wanted stars")
    void testGetSeverity() {
        assertThat(CrimeType.THEFT.getSeverity()).isEqualTo(1);
        assertThat(CrimeType.ASSAULT.getSeverity()).isEqualTo(2);
        assertThat(CrimeType.MURDER.getSeverity()).isEqualTo(3);
        assertThat(CrimeType.PRISON_ESCAPE.getSeverity()).isEqualTo(4);
        assertThat(CrimeType.TERRORISM.getSeverity()).isEqualTo(5);
    }

    // ========== Formatted Info Tests ==========

    @Test
    @DisplayName("getFormattedInfo - Should include stars and details")
    void testGetFormattedInfo() {
        String info = CrimeType.THEFT.getFormattedInfo();

        assertThat(info).contains("Diebstahl");
        assertThat(info).contains("500");
        assertThat(info).contains("1");
    }

    @Test
    @DisplayName("getFormattedInfo - Should show multiple stars for serious crimes")
    void testGetFormattedInfoMultipleStars() {
        String info = CrimeType.TERRORISM.getFormattedInfo();

        assertThat(info).contains("Terrorismus");
        assertThat(info).contains("100000");
        assertThat(info).contains("20");
    }

    // ========== Color Code Tests ==========

    @Test
    @DisplayName("getColorCode - Should return yellow for 1 star")
    void testGetColorCodeMinor() {
        assertThat(CrimeType.THEFT.getColorCode()).isEqualTo("§e");
        assertThat(CrimeType.TRESPASSING.getColorCode()).isEqualTo("§e");
    }

    @Test
    @DisplayName("getColorCode - Should return orange for 2 stars")
    void testGetColorCodeModerate() {
        assertThat(CrimeType.ASSAULT.getColorCode()).isEqualTo("§6");
        assertThat(CrimeType.DRUG_TRAFFICKING.getColorCode()).isEqualTo("§6");
    }

    @Test
    @DisplayName("getColorCode - Should return red for 3 stars")
    void testGetColorCodeSerious() {
        assertThat(CrimeType.MURDER.getColorCode()).isEqualTo("§c");
        assertThat(CrimeType.ARSON.getColorCode()).isEqualTo("§c");
    }

    @Test
    @DisplayName("getColorCode - Should return dark red for 4+ stars")
    void testGetColorCodeExtreme() {
        assertThat(CrimeType.PRISON_ESCAPE.getColorCode()).isEqualTo("§4");
        assertThat(CrimeType.POLICE_ASSAULT.getColorCode()).isEqualTo("§4");
        assertThat(CrimeType.TERRORISM.getColorCode()).isEqualTo("§4");
    }

    // ========== Severity Progression Tests ==========

    @Test
    @DisplayName("Severity - Should increase with wanted stars")
    void testSeverityProgression() {
        assertThat(CrimeType.THEFT.getWantedStars())
            .isLessThan(CrimeType.ASSAULT.getWantedStars());
        assertThat(CrimeType.ASSAULT.getWantedStars())
            .isLessThan(CrimeType.MURDER.getWantedStars());
        assertThat(CrimeType.MURDER.getWantedStars())
            .isLessThan(CrimeType.TERRORISM.getWantedStars());
    }

    @Test
    @DisplayName("Fine - Should increase with severity")
    void testFineProgression() {
        assertThat(CrimeType.THEFT.getFine())
            .isLessThan(CrimeType.ASSAULT.getFine());
        assertThat(CrimeType.ASSAULT.getFine())
            .isLessThan(CrimeType.MURDER.getFine());
        assertThat(CrimeType.MURDER.getFine())
            .isLessThan(CrimeType.TERRORISM.getFine());
    }

    @Test
    @DisplayName("Prison Days - Should increase with severity")
    void testPrisonDaysProgression() {
        assertThat(CrimeType.THEFT.getPrisonDays())
            .isLessThan(CrimeType.ASSAULT.getPrisonDays());
        assertThat(CrimeType.ASSAULT.getPrisonDays())
            .isLessThan(CrimeType.MURDER.getPrisonDays());
        assertThat(CrimeType.MURDER.getPrisonDays())
            .isLessThan(CrimeType.TERRORISM.getPrisonDays());
    }

    // ========== Enum Completeness Tests ==========

    @Test
    @DisplayName("All Crimes - Should have valid wanted stars")
    void testAllCrimesHaveValidWantedStars() {
        for (CrimeType crime : CrimeType.values()) {
            assertThat(crime.getWantedStars()).isBetween(1, 5);
        }
    }

    @Test
    @DisplayName("All Crimes - Should have positive fine")
    void testAllCrimesHavePositiveFine() {
        for (CrimeType crime : CrimeType.values()) {
            assertThat(crime.getFine()).isGreaterThan(0.0);
        }
    }

    @Test
    @DisplayName("All Crimes - Should have positive prison days")
    void testAllCrimesHavePositivePrisonDays() {
        for (CrimeType crime : CrimeType.values()) {
            assertThat(crime.getPrisonDays()).isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("All Crimes - Should have non-empty display name")
    void testAllCrimesHaveDisplayName() {
        for (CrimeType crime : CrimeType.values()) {
            assertThat(crime.getDisplayName()).isNotEmpty();
        }
    }

    // ========== Specific Crime Validation Tests ==========

    @Test
    @DisplayName("TERRORISM - Should be most severe")
    void testTerrorismMostSevere() {
        for (CrimeType crime : CrimeType.values()) {
            if (crime != CrimeType.TERRORISM) {
                assertThat(CrimeType.TERRORISM.getWantedStars())
                    .isGreaterThanOrEqualTo(crime.getWantedStars());
                assertThat(CrimeType.TERRORISM.getFine())
                    .isGreaterThanOrEqualTo(crime.getFine());
            }
        }
    }

    @Test
    @DisplayName("TRESPASSING - Should be least severe by fine")
    void testTrespassingLeastSevereFine() {
        assertThat(CrimeType.TRESPASSING.getFine()).isEqualTo(300.0);
        for (CrimeType crime : CrimeType.values()) {
            assertThat(crime.getFine())
                .isGreaterThanOrEqualTo(CrimeType.TRESPASSING.getFine());
        }
    }
}
