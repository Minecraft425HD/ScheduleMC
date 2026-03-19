package de.rolandsw.schedulemc.production;

import de.rolandsw.schedulemc.production.core.GenericQuality;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für GenericQuality-Hilfsmethoden: getByLevel, getByName, canUpgrade/Downgrade.
 * Nutzt direkt konstruierte Tier-Arrays ohne Minecraft Component.translatable().
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GenericQualityLookupTest {

    private GenericQuality[] tiers;

    @BeforeEach
    void setUp() {
        // Custom tiers direkt über den Builder erstellen (ohne MC Component)
        tiers = new GenericQuality.Builder(4)
            .names("Schlecht", "Gut", "Sehr Gut", "Legendär")
            .colorCodes("§c", "§e", "§a", "§6")
            .priceMultipliers(0.7, 1.0, 1.5, 2.5)
            .descriptions("Low", "Standard", "High", "Premium")
            .build();
    }

    // ── getByLevel ────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("getByLevel: Findet korrektes Tier nach Level")
    void testGetByLevel_Found() {
        GenericQuality q = GenericQuality.getByLevel(tiers, 2);
        assertNotNull(q);
        assertEquals(2, q.getLevel());
        assertEquals("Sehr Gut", q.getDisplayName());
    }

    @Test
    @Order(2)
    @DisplayName("getByLevel: Level 0 gibt erstes Tier zurück")
    void testGetByLevel_Zero() {
        GenericQuality q = GenericQuality.getByLevel(tiers, 0);
        assertEquals(0, q.getLevel());
        assertEquals("Schlecht", q.getDisplayName());
    }

    @Test
    @Order(3)
    @DisplayName("getByLevel: Unbekanntes Level fällt auf Level 0 zurück")
    void testGetByLevel_Unknown() {
        GenericQuality q = GenericQuality.getByLevel(tiers, 99);
        assertEquals(0, q.getLevel()); // Fallback to worst
    }

    @Test
    @Order(4)
    @DisplayName("getByLevel: Negatives Level fällt auf Level 0 zurück")
    void testGetByLevel_Negative() {
        GenericQuality q = GenericQuality.getByLevel(tiers, -1);
        assertEquals(0, q.getLevel());
    }

    // ── getByName ─────────────────────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("getByName: Findet Tier nach Name (case-insensitive)")
    void testGetByName_Found() {
        GenericQuality q = GenericQuality.getByName(tiers, "gut");
        assertNotNull(q);
        assertEquals(1, q.getLevel());
    }

    @Test
    @Order(6)
    @DisplayName("getByName: Exakter Name funktioniert")
    void testGetByName_Exact() {
        GenericQuality q = GenericQuality.getByName(tiers, "Legendär");
        assertEquals(3, q.getLevel());
    }

    @Test
    @Order(7)
    @DisplayName("getByName: Unbekannter Name fällt auf Level 0 zurück")
    void testGetByName_Unknown() {
        GenericQuality q = GenericQuality.getByName(tiers, "UnbekannterName");
        assertEquals(0, q.getLevel());
    }

    // ── Upgrade/Downgrade ─────────────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("canUpgrade: Mittlere Tiers können upgraden")
    void testCanUpgrade_Middle() {
        assertTrue(tiers[1].canUpgrade());
        assertTrue(tiers[2].canUpgrade());
    }

    @Test
    @Order(9)
    @DisplayName("canUpgrade: Maximales Tier kann nicht upgraden")
    void testCanUpgrade_Max() {
        assertFalse(tiers[3].canUpgrade());
        assertTrue(tiers[3].isMaxQuality());
    }

    @Test
    @Order(10)
    @DisplayName("canDowngrade: Mittlere Tiers können downgraden")
    void testCanDowngrade_Middle() {
        assertTrue(tiers[1].canDowngrade());
        assertTrue(tiers[2].canDowngrade());
    }

    @Test
    @Order(11)
    @DisplayName("canDowngrade: Minimales Tier kann nicht downgraden")
    void testCanDowngrade_Min() {
        assertFalse(tiers[0].canDowngrade());
        assertTrue(tiers[0].isMinQuality());
    }

    @Test
    @Order(12)
    @DisplayName("upgrade/downgrade: Kette von Level 0 bis Max und zurück")
    void testUpgradeDowngradeChain() {
        GenericQuality q = tiers[0];
        q = (GenericQuality) q.upgrade();
        assertEquals(1, q.getLevel());
        q = (GenericQuality) q.upgrade();
        assertEquals(2, q.getLevel());
        q = (GenericQuality) q.upgrade();
        assertEquals(3, q.getLevel());
        // Max: upgrade bleibt auf 3
        q = (GenericQuality) q.upgrade();
        assertEquals(3, q.getLevel());

        // Downgrade
        q = (GenericQuality) q.downgrade();
        assertEquals(2, q.getLevel());
    }

    // ── Price Multiplier ──────────────────────────────────────────────────────

    @Test
    @Order(13)
    @DisplayName("Preis-Multiplikatoren sind aufsteigend")
    void testPriceMultipliersAscending() {
        for (int i = 0; i < tiers.length - 1; i++) {
            assertTrue(
                tiers[i].getPriceMultiplier() < tiers[i + 1].getPriceMultiplier(),
                "Tier " + i + " multiplier should be less than tier " + (i + 1)
            );
        }
    }

    @Test
    @Order(14)
    @DisplayName("getTotalTiers gibt korrekte Anzahl zurück")
    void testGetTotalTiers() {
        for (GenericQuality tier : tiers) {
            assertEquals(4, tier.getTotalTiers());
        }
    }

    // ── Custom Tier System ────────────────────────────────────────────────────

    @Test
    @Order(15)
    @DisplayName("createCustomTierSystem: 2-Tier System")
    void testCustom2TierSystem() {
        GenericQuality[] custom = GenericQuality.createCustomTierSystem(2, 0.5, 2.0);
        assertEquals(2, custom.length);
        assertEquals(0, custom[0].getLevel());
        assertEquals(1, custom[1].getLevel());
        assertEquals(0.5, custom[0].getPriceMultiplier(), 0.01);
        assertEquals(2.0, custom[1].getPriceMultiplier(), 0.01);
    }

    @Test
    @Order(16)
    @DisplayName("createCustomTierSystem: Ungültige Tier-Anzahl wirft Exception")
    void testCustomTierSystemInvalidCount() {
        assertThrows(IllegalArgumentException.class, () ->
            GenericQuality.createCustomTierSystem(1, 0.5, 2.0)
        );
        assertThrows(IllegalArgumentException.class, () ->
            GenericQuality.createCustomTierSystem(11, 0.5, 2.0)
        );
    }

    @Test
    @Order(17)
    @DisplayName("getAllTiers gibt alle Tiers zurück")
    void testGetAllTiers() {
        assertEquals(4, tiers[0].getAllTiers().size());
        assertEquals(4, tiers[3].getAllTiers().size());
    }
}
