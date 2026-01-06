package de.rolandsw.schedulemc.npc.personality;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for NPCRelationshipManager
 *
 * Tests comprehensive NPC relationship scenarios:
 * - Relationship Creation & Management
 * - Affinity Tracking & Changes
 * - Interaction History
 * - Relationship Queries
 * - Top Relationships (Friendly/Hostile)
 * - Bulk Operations
 *
 * @since 1.0
 */
@DisplayName("NPCRelationship Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NPCRelationshipIntegrationTest {

    private NPCRelationshipManager manager;
    private UUID npcA;
    private UUID npcB;
    private UUID npcC;
    private UUID playerA;
    private UUID playerB;

    @BeforeEach
    void setUp() {
        manager = NPCRelationshipManager.getInstance();

        // Create test entities
        npcA = UUID.randomUUID();
        npcB = UUID.randomUUID();
        npcC = UUID.randomUUID();
        playerA = UUID.randomUUID();
        playerB = UUID.randomUUID();
    }

    @Test
    @Order(1)
    @DisplayName("Relationship Creation - Should create relationship")
    void testCreateRelationship() {
        // Act
        NPCRelationship relationship = manager.getOrCreateRelationship(npcA, playerA);

        // Assert
        assertThat(relationship).isNotNull();
        assertThat(relationship.getNpcId()).isEqualTo(npcA);
        assertThat(relationship.getPlayerId()).isEqualTo(playerA);
        assertThat(relationship.getAffinity()).isEqualTo(0); // Default neutral
    }

    @Test
    @Order(2)
    @DisplayName("Relationship Creation - Should return existing relationship")
    void testGetExistingRelationship() {
        // Arrange
        NPCRelationship created = manager.getOrCreateRelationship(npcA, playerA);
        created.modifyAffinity(50);

        // Act
        NPCRelationship retrieved = manager.getOrCreateRelationship(npcA, playerA);

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getAffinity()).isEqualTo(50); // Same relationship
    }

    @Test
    @Order(3)
    @DisplayName("Affinity - Should modify affinity correctly")
    void testModifyAffinity() {
        // Arrange
        NPCRelationship relationship = manager.getOrCreateRelationship(npcA, playerA);

        // Act
        relationship.modifyAffinity(30);
        relationship.modifyAffinity(20);

        // Assert
        assertThat(relationship.getAffinity()).isEqualTo(50);
    }

    @Test
    @Order(4)
    @DisplayName("Affinity - Should clamp affinity to valid range")
    void testAffinityBounds() {
        // Arrange
        NPCRelationship relationship = manager.getOrCreateRelationship(npcA, playerA);

        // Act - Try to exceed maximum
        relationship.setAffinity(150);

        // Assert
        assertThat(relationship.getAffinity()).isLessThanOrEqualTo(100);

        // Act - Try to go below minimum
        relationship.setAffinity(-150);

        // Assert
        assertThat(relationship.getAffinity()).isGreaterThanOrEqualTo(-100);
    }

    @Test
    @Order(5)
    @DisplayName("Affinity - Should track negative relationships")
    void testNegativeAffinity() {
        // Arrange
        NPCRelationship relationship = manager.getOrCreateRelationship(npcA, playerA);

        // Act
        relationship.modifyAffinity(-30);
        relationship.modifyAffinity(-20);

        // Assert
        assertThat(relationship.getAffinity()).isEqualTo(-50);
        assertThat(relationship.isHostile()).isTrue();
    }

    @Test
    @Order(6)
    @DisplayName("Relationship Queries - Should get player relationships")
    void testGetPlayerRelationships() {
        // Arrange
        manager.getOrCreateRelationship(npcA, playerA);
        manager.getOrCreateRelationship(npcB, playerA);
        manager.getOrCreateRelationship(npcC, playerA);
        manager.getOrCreateRelationship(npcA, playerB); // Different player

        // Act
        List<NPCRelationship> playerARelationships = manager.getPlayerRelationships(playerA);

        // Assert
        assertThat(playerARelationships).hasSize(3);
        assertThat(playerARelationships).allMatch(r -> r.getPlayerId().equals(playerA));
    }

    @Test
    @Order(7)
    @DisplayName("Relationship Queries - Should get NPC relationships")
    void testGetNPCRelationships() {
        // Arrange
        manager.getOrCreateRelationship(npcA, playerA);
        manager.getOrCreateRelationship(npcA, playerB);
        manager.getOrCreateRelationship(npcB, playerA); // Different NPC

        // Act
        List<NPCRelationship> npcARelationships = manager.getNPCRelationships(npcA);

        // Assert
        assertThat(npcARelationships).hasSize(2);
        assertThat(npcARelationships).allMatch(r -> r.getNpcId().equals(npcA));
    }

    @Test
    @Order(8)
    @DisplayName("Top Relationships - Should get top friendly relationships")
    void testGetTopFriendlyRelationships() {
        // Arrange
        NPCRelationship r1 = manager.getOrCreateRelationship(npcA, playerA);
        NPCRelationship r2 = manager.getOrCreateRelationship(npcB, playerA);
        NPCRelationship r3 = manager.getOrCreateRelationship(npcC, playerA);

        r1.setAffinity(80); // Most friendly
        r2.setAffinity(50); // Second
        r3.setAffinity(20); // Third

        // Act
        List<NPCRelationship> topFriendly = manager.getTopFriendlyRelationships(playerA, 2);

        // Assert
        assertThat(topFriendly).hasSize(2);
        assertThat(topFriendly.get(0).getAffinity()).isEqualTo(80);
        assertThat(topFriendly.get(1).getAffinity()).isEqualTo(50);
    }

    @Test
    @Order(9)
    @DisplayName("Top Relationships - Should get top hostile relationships")
    void testGetTopHostileRelationships() {
        // Arrange
        NPCRelationship r1 = manager.getOrCreateRelationship(npcA, playerA);
        NPCRelationship r2 = manager.getOrCreateRelationship(npcB, playerA);
        NPCRelationship r3 = manager.getOrCreateRelationship(npcC, playerA);

        r1.setAffinity(-80); // Most hostile
        r2.setAffinity(-50); // Second
        r3.setAffinity(-20); // Third

        // Act
        List<NPCRelationship> topHostile = manager.getTopHostileRelationships(playerA, 2);

        // Assert
        assertThat(topHostile).hasSize(2);
        assertThat(topHostile.get(0).getAffinity()).isEqualTo(-80);
        assertThat(topHostile.get(1).getAffinity()).isEqualTo(-50);
    }

    @Test
    @Order(10)
    @DisplayName("Interaction History - Should track interactions")
    void testInteractionTracking() {
        // Arrange
        NPCRelationship relationship = manager.getOrCreateRelationship(npcA, playerA);

        // Act
        relationship.recordInteraction();
        relationship.recordInteraction();
        relationship.recordInteraction();

        // Assert
        assertThat(relationship.getInteractionCount()).isEqualTo(3);
        assertThat(relationship.getLastInteractionTime()).isGreaterThan(0);
    }

    @Test
    @Order(11)
    @DisplayName("Relationship Status - Should determine relationship status")
    void testRelationshipStatus() {
        // Arrange
        NPCRelationship friendly = manager.getOrCreateRelationship(npcA, playerA);
        NPCRelationship hostile = manager.getOrCreateRelationship(npcB, playerA);
        NPCRelationship neutral = manager.getOrCreateRelationship(npcC, playerA);

        // Act
        friendly.setAffinity(60);
        hostile.setAffinity(-60);
        neutral.setAffinity(10);

        // Assert
        assertThat(friendly.isFriendly()).isTrue();
        assertThat(hostile.isHostile()).isTrue();
        assertThat(neutral.isNeutral()).isTrue();
    }

    @Test
    @Order(12)
    @DisplayName("Relationship Removal - Should remove single relationship")
    void testRemoveRelationship() {
        // Arrange
        manager.getOrCreateRelationship(npcA, playerA);

        // Act
        manager.removeRelationship(npcA, playerA);

        // Assert
        NPCRelationship removed = manager.getRelationship(npcA, playerA);
        assertThat(removed).isNull();
    }

    @Test
    @Order(13)
    @DisplayName("Bulk Removal - Should remove all player relationships")
    void testRemovePlayerRelationships() {
        // Arrange
        manager.getOrCreateRelationship(npcA, playerA);
        manager.getOrCreateRelationship(npcB, playerA);
        manager.getOrCreateRelationship(npcC, playerA);

        // Act
        manager.removePlayerRelationships(playerA);

        // Assert
        List<NPCRelationship> relationships = manager.getPlayerRelationships(playerA);
        assertThat(relationships).isEmpty();
    }

    @Test
    @Order(14)
    @DisplayName("Bulk Removal - Should remove all NPC relationships")
    void testRemoveNPCRelationships() {
        // Arrange
        manager.getOrCreateRelationship(npcA, playerA);
        manager.getOrCreateRelationship(npcA, playerB);

        // Act
        manager.removeNPCRelationships(npcA);

        // Assert
        List<NPCRelationship> relationships = manager.getNPCRelationships(npcA);
        assertThat(relationships).isEmpty();
    }

    @Test
    @Order(15)
    @DisplayName("Statistics - Should count total relationships")
    void testGetTotalRelationships() {
        // Arrange
        manager.getOrCreateRelationship(npcA, playerA);
        manager.getOrCreateRelationship(npcB, playerA);
        manager.getOrCreateRelationship(npcC, playerB);

        // Act
        int total = manager.getTotalRelationships();

        // Assert
        assertThat(total).isEqualTo(3);
    }

    @Test
    @Order(16)
    @DisplayName("Empty Manager - Should handle empty relationship manager")
    void testEmptyManager() {
        // Arrange - Clear all relationships
        manager.removePlayerRelationships(playerA);
        manager.removePlayerRelationships(playerB);

        // Act
        NPCRelationship nonExistent = manager.getRelationship(npcA, playerA);
        List<NPCRelationship> empty = manager.getPlayerRelationships(UUID.randomUUID());

        // Assert
        assertThat(nonExistent).isNull();
        assertThat(empty).isEmpty();
    }

    @Test
    @Order(17)
    @DisplayName("Multiple Players - Should handle multiple player relationships")
    void testMultiplePlayerRelationships() {
        // Arrange
        NPCRelationship r1 = manager.getOrCreateRelationship(npcA, playerA);
        NPCRelationship r2 = manager.getOrCreateRelationship(npcA, playerB);

        // Act
        r1.setAffinity(70);
        r2.setAffinity(-40);

        // Assert
        assertThat(r1.getAffinity()).isEqualTo(70);
        assertThat(r2.getAffinity()).isEqualTo(-40);
        assertThat(r1.getNpcId()).isEqualTo(r2.getNpcId()); // Same NPC
        assertThat(r1.getPlayerId()).isNotEqualTo(r2.getPlayerId()); // Different players
    }

    @AfterEach
    void tearDown() {
        // Cleanup all test relationships
        manager.removePlayerRelationships(playerA);
        manager.removePlayerRelationships(playerB);
        manager.removeNPCRelationships(npcA);
        manager.removeNPCRelationships(npcB);
        manager.removeNPCRelationships(npcC);
    }
}
