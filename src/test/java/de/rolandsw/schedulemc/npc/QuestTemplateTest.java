package de.rolandsw.schedulemc.npc;

import de.rolandsw.schedulemc.npc.life.quest.QuestManager.QuestTemplate;
import de.rolandsw.schedulemc.npc.life.quest.QuestReward;
import de.rolandsw.schedulemc.npc.life.quest.QuestType;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für QuestTemplate Builder und Defaults.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QuestTemplateTest {

    // ── Builder Defaults ──────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Builder: Minimale Konfiguration mit ID")
    void testMinimalBuilder() {
        QuestTemplate t = QuestTemplate.builder("test_quest")
            .type(QuestType.DELIVERY)
            .title("Test Quest")
            .baseReward(QuestReward.create().money(50))
            .build();

        assertEquals("test_quest", t.getId());
        assertEquals(QuestType.DELIVERY, t.getType());
        assertEquals("Test Quest", t.getTitle());
        assertNotNull(t.getBaseReward());
    }

    @Test
    @Order(2)
    @DisplayName("Builder: Standard-Difficulty ist 1")
    void testDefaultDifficulty() {
        QuestTemplate t = QuestTemplate.builder("test")
            .type(QuestType.DELIVERY)
            .title("T")
            .baseReward(QuestReward.create())
            .build();

        assertEquals(1, t.getDifficulty());
    }

    @Test
    @Order(3)
    @DisplayName("Builder: Standard-Repeatable ist true")
    void testDefaultRepeatable() {
        QuestTemplate t = QuestTemplate.builder("test")
            .type(QuestType.COLLECTION)
            .title("T")
            .baseReward(QuestReward.create())
            .build();

        assertTrue(t.isRepeatable());
    }

    @Test
    @Order(4)
    @DisplayName("Builder: Standard-Fraktion ist null")
    void testDefaultFaction() {
        QuestTemplate t = QuestTemplate.builder("test")
            .type(QuestType.DELIVERY)
            .title("T")
            .baseReward(QuestReward.create())
            .build();

        assertNull(t.getFaction());
    }

    @Test
    @Order(5)
    @DisplayName("Builder: Standard-TimeLimit ist 0 (kein Limit)")
    void testDefaultTimeLimit() {
        QuestTemplate t = QuestTemplate.builder("test")
            .type(QuestType.DELIVERY)
            .title("T")
            .baseReward(QuestReward.create())
            .build();

        assertEquals(0, t.getTimeLimit());
    }

    // ── Builder Konfiguration ─────────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("Builder: Difficulty wird korrekt gesetzt")
    void testSetDifficulty() {
        QuestTemplate t = QuestTemplate.builder("test")
            .type(QuestType.ELIMINATION)
            .title("Hard Quest")
            .difficulty(4)
            .baseReward(QuestReward.create().money(500))
            .build();

        assertEquals(4, t.getDifficulty());
    }

    @Test
    @Order(7)
    @DisplayName("Builder: Faction-Reputation-Anforderung wird gesetzt")
    void testSetMinFactionRep() {
        QuestTemplate t = QuestTemplate.builder("faction_test")
            .type(QuestType.ESCORT)
            .title("Faction Quest")
            .faction(Faction.HAENDLER)
            .minFactionRep(25)
            .baseReward(QuestReward.create().money(200))
            .build();

        assertEquals(Faction.HAENDLER, t.getFaction());
        assertEquals(25, t.getMinFactionRep());
    }

    @Test
    @Order(8)
    @DisplayName("Builder: TimeLimit wird gesetzt")
    void testSetTimeLimit() {
        QuestTemplate t = QuestTemplate.builder("timed")
            .type(QuestType.DELIVERY)
            .title("Urgent")
            .timeLimit(2)
            .baseReward(QuestReward.create().money(150))
            .build();

        assertEquals(2, t.getTimeLimit());
    }

    @Test
    @Order(9)
    @DisplayName("Builder: repeatable(false) setzt Wert")
    void testSetNotRepeatable() {
        QuestTemplate t = QuestTemplate.builder("unique")
            .type(QuestType.NEGOTIATION)
            .title("One-time Quest")
            .repeatable(false)
            .baseReward(QuestReward.create().money(1000))
            .build();

        assertFalse(t.isRepeatable());
    }

    // ── QuestReward ────────────────────────────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("QuestReward: money() setzt Geldbelohnung")
    void testQuestRewardMoney() {
        QuestReward reward = QuestReward.create().money(200);
        assertEquals(200, reward.getMoney());
    }

    @Test
    @Order(11)
    @DisplayName("QuestReward: experience() setzt XP-Belohnung")
    void testQuestRewardExperience() {
        QuestReward reward = QuestReward.create().experience(100);
        assertEquals(100, reward.getExperience());
    }

    @Test
    @Order(12)
    @DisplayName("QuestReward: scale() multipliziert Werte korrekt")
    void testQuestRewardScale() {
        QuestReward base = QuestReward.create().money(100).experience(50);
        QuestReward scaled = base.scale(2.0f);

        assertEquals(200, scaled.getMoney());
        assertEquals(100, scaled.getExperience());
    }

    @Test
    @Order(13)
    @DisplayName("QuestReward: scale(1.0) ändert nichts")
    void testQuestRewardScaleIdentity() {
        QuestReward base = QuestReward.create().money(150);
        QuestReward scaled = base.scale(1.0f);

        assertEquals(150, scaled.getMoney());
    }

    // ── QuestType ─────────────────────────────────────────────────────────────

    @Test
    @Order(14)
    @DisplayName("Alle QuestType-Werte sind vorhanden")
    void testQuestTypeValues() {
        QuestType[] types = QuestType.values();
        assertTrue(types.length >= 6, "Expected at least 6 quest types");
    }

    @Test
    @Order(15)
    @DisplayName("DELIVERY QuestType existiert")
    void testDeliveryQuestType() {
        assertNotNull(QuestType.DELIVERY);
    }
}
