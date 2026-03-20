package de.rolandsw.schedulemc.vehicle;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für das Fahrzeug-Ölsystem — prüft Verbrauch, Grenzwerte und Schaden-Logik
 * anhand eines einfachen POJO-Modells (ohne Minecraft-Server).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VehicleOilTest {

    /**
     * Vereinfachtes Oil-Modell, das die Logik aus DamageComponent abbildet.
     */
    static class OilModel {
        static final float OIL_MAX = 100.0f;
        static final float OIL_CONSUMPTION_PER_SECOND = 0.01f;
        static final float OIL_EMPTY_DAMAGE_PER_SECOND = 0.1f;
        static final float LOW_OIL_THRESHOLD = 20.0f;

        private float oilLevel = OIL_MAX;
        private float damage = 0.0f;

        /** Simuliert einen Tick bei laufendem Motor (1 Sekunde = 20 Ticks, hier pro Sekunde). */
        void tickRunning() {
            oilLevel = Math.max(0.0f, oilLevel - OIL_CONSUMPTION_PER_SECOND);
            if (oilLevel <= 0.0f) {
                damage = Math.min(100.0f, damage + OIL_EMPTY_DAMAGE_PER_SECOND);
            }
        }

        /** Setzt Öl auf Maximum zurück (Ölwechsel). */
        void resetOil() {
            oilLevel = OIL_MAX;
        }

        float getOilLevel() { return oilLevel; }
        float getOilPercentage() { return oilLevel / OIL_MAX; }
        float getDamage() { return damage; }
        boolean isOilLow() { return oilLevel < LOW_OIL_THRESHOLD; }

        /** Setzt Öl-Level direkt (für Tests). */
        void setOilLevel(float level) {
            oilLevel = Math.max(0, Math.min(OIL_MAX, level));
        }
    }

    private OilModel oil;

    @BeforeEach
    void setUp() {
        oil = new OilModel();
    }

    // ── Initialer Zustand ─────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Neues Fahrzeug: Öl ist voll")
    void testInitialOilFull() {
        assertEquals(100.0f, oil.getOilLevel(), 0.001f);
        assertEquals(1.0f, oil.getOilPercentage(), 0.001f);
    }

    @Test
    @Order(2)
    @DisplayName("Neues Fahrzeug: kein Schaden")
    void testInitialNoDamage() {
        assertEquals(0.0f, oil.getDamage(), 0.001f);
    }

    @Test
    @Order(3)
    @DisplayName("Öl nicht niedrig wenn voll")
    void testNotLowWhenFull() {
        assertFalse(oil.isOilLow());
    }

    // ── Verbrauch ─────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("Öl sinkt bei laufendem Motor")
    void testOilDecreases() {
        oil.tickRunning();
        assertTrue(oil.getOilLevel() < 100.0f);
    }

    @Test
    @Order(5)
    @DisplayName("Öl-Verbrauch ist konsistent: 0.01 pro Sekunde")
    void testOilConsumptionRate() {
        oil.tickRunning();
        assertEquals(100.0f - OilModel.OIL_CONSUMPTION_PER_SECOND, oil.getOilLevel(), 0.0001f);
    }

    @Test
    @Order(6)
    @DisplayName("Öl wird nach 10000 Sekunden leer (mathematisch)")
    void testOilDepletion() {
        // Mit 0.01/s Verbrauch dauert es 100/0.01 = 10000 Sekunden
        float expectedSeconds = OilModel.OIL_MAX / OilModel.OIL_CONSUMPTION_PER_SECOND;
        assertEquals(10000.0f, expectedSeconds, 0.01f);
    }

    @Test
    @Order(7)
    @DisplayName("Öl-Level kann nicht unter 0 fallen")
    void testOilNotNegative() {
        oil.setOilLevel(0.005f);
        oil.tickRunning(); // sollte 0 ergeben, nicht negativ
        assertEquals(0.0f, oil.getOilLevel(), 0.0001f);
    }

    // ── Niedrig-Schwelle ──────────────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("isOilLow wenn Öl unter 20%")
    void testOilLowThreshold() {
        oil.setOilLevel(19.9f);
        assertTrue(oil.isOilLow());
    }

    @Test
    @Order(9)
    @DisplayName("isOilLow nicht wenn Öl genau 20%")
    void testOilNotLowAt20() {
        oil.setOilLevel(20.0f);
        assertFalse(oil.isOilLow());
    }

    // ── Schaden bei leerem Öl ─────────────────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("Kein Schaden solange Öl vorhanden")
    void testNoDamageWithOil() {
        for (int i = 0; i < 100; i++) {
            oil.tickRunning();
        }
        // Nach 100 Sekunden ist noch Öl vorhanden
        assertTrue(oil.getOilLevel() > 0);
        assertEquals(0.0f, oil.getDamage(), 0.001f);
    }

    @Test
    @Order(11)
    @DisplayName("Schaden entsteht wenn Öl leer")
    void testDamageWhenOilEmpty() {
        oil.setOilLevel(0.0f);
        oil.tickRunning();
        assertTrue(oil.getDamage() > 0.0f);
    }

    @Test
    @Order(12)
    @DisplayName("Schaden bei leerem Öl: 0.1 pro Sekunde")
    void testDamageRateWhenEmpty() {
        oil.setOilLevel(0.0f);
        oil.tickRunning();
        assertEquals(OilModel.OIL_EMPTY_DAMAGE_PER_SECOND, oil.getDamage(), 0.0001f);
    }

    // ── Ölwechsel ─────────────────────────────────────────────────────────────

    @Test
    @Order(13)
    @DisplayName("Ölwechsel setzt Öl auf Maximum")
    void testOilReset() {
        oil.setOilLevel(10.0f);
        oil.resetOil();
        assertEquals(OilModel.OIL_MAX, oil.getOilLevel(), 0.001f);
    }

    @Test
    @Order(14)
    @DisplayName("Ölwechsel: kein Schaden nach Reset")
    void testNoDamagePossibleAfterReset() {
        oil.setOilLevel(0.0f);
        oil.resetOil();
        oil.tickRunning(); // Öl vorhanden, kein Schaden
        assertEquals(0.0f, oil.getDamage(), 0.001f);
    }

    @Test
    @Order(15)
    @DisplayName("Ölwechsel: isOilLow ist false nach Reset")
    void testNotLowAfterReset() {
        oil.setOilLevel(5.0f);
        assertTrue(oil.isOilLow());
        oil.resetOil();
        assertFalse(oil.isOilLow());
    }

    // ── Prozentualer Wert ─────────────────────────────────────────────────────

    @Test
    @Order(16)
    @DisplayName("getOilPercentage: 0% bei leerem Öl")
    void testOilPercentageEmpty() {
        oil.setOilLevel(0.0f);
        assertEquals(0.0f, oil.getOilPercentage(), 0.001f);
    }

    @Test
    @Order(17)
    @DisplayName("getOilPercentage: 50% bei halbvollem Tank")
    void testOilPercentageHalf() {
        oil.setOilLevel(50.0f);
        assertEquals(0.5f, oil.getOilPercentage(), 0.001f);
    }
}
