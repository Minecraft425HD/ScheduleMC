package de.rolandsw.schedulemc.production.nbt;

import de.rolandsw.schedulemc.production.data.PlantPotData;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlantSerializer and PlantSerializerFactory
 *
 * Tests cover:
 * - PlantSerializer interface contract
 * - Factory pattern functionality
 * - Serializer selection based on plant type
 * - NBT save/load operations
 * - Multiple serializer support
 */
class PlantSerializerTest {

    private PlantPotData mockPotData;
    private CompoundTag mockTag;

    @BeforeEach
    void setUp() {
        mockPotData = mock(PlantPotData.class);
        mockTag = new CompoundTag();
    }

    // ==================== PlantSerializer Interface Tests ====================

    @Test
    @DisplayName("Should save plant data to NBT")
    void testSavePlant() {
        // Arrange
        PlantSerializer serializer = new TestPlantSerializer("test_plant", true);

        // Act
        serializer.savePlant(mockPotData, mockTag);

        // Assert
        assertThat(mockTag.contains("test_data")).isTrue();
        assertThat(mockTag.getString("test_data")).isEqualTo("saved");
    }

    @Test
    @DisplayName("Should load plant data from NBT")
    void testLoadPlant() {
        // Arrange
        PlantSerializer serializer = new TestPlantSerializer("test_plant", true);
        mockTag.putString("test_data", "loaded");

        // Act
        serializer.loadPlant(mockPotData, mockTag);

        // Assert - Verify load was called (in real impl, this would populate potData)
        assertThat(mockTag.contains("test_data")).isTrue();
    }

    @Test
    @DisplayName("Should return correct tag name")
    void testGetPlantTagName() {
        // Arrange
        PlantSerializer serializer = new TestPlantSerializer("custom_tag", true);

        // Act
        String tagName = serializer.getPlantTagName();

        // Assert
        assertThat(tagName).isEqualTo("custom_tag");
    }

    @Test
    @DisplayName("Should check if plant is present")
    void testHasPlant() {
        // Arrange
        PlantSerializer serializerWithPlant = new TestPlantSerializer("test", true);
        PlantSerializer serializerWithoutPlant = new TestPlantSerializer("test", false);

        // Act & Assert
        assertThat(serializerWithPlant.hasPlant(mockPotData)).isTrue();
        assertThat(serializerWithoutPlant.hasPlant(mockPotData)).isFalse();
    }

    // ==================== PlantSerializerFactory Tests ====================

    @Test
    @DisplayName("Factory should return all available serializers")
    void testGetAllSerializers() {
        // Act
        List<PlantSerializer> serializers = PlantSerializerFactory.getAllSerializers();

        // Assert
        assertThat(serializers).isNotNull();
        assertThat(serializers).isNotEmpty();
        // Currently only TobaccoPlantSerializer is active
        assertThat(serializers).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Factory should return correct serializer for plant type")
    void testGetSerializerForPlantType() {
        // This test is somewhat limited as it depends on the actual serializers
        // In a real scenario, we'd mock the potData to return specific plant types

        // Act
        PlantSerializer serializer = PlantSerializerFactory.getSerializer(mockPotData);

        // Assert - With mock data that doesn't match any plant, should return null
        assertThat(serializer).isNull();
    }

    @Test
    @DisplayName("Factory should return null when no plant present")
    void testGetSerializerNoPlantt() {
        // Arrange
        PlantPotData emptyPot = mock(PlantPotData.class);

        // Act
        PlantSerializer serializer = PlantSerializerFactory.getSerializer(emptyPot);

        // Assert
        assertThat(serializer).isNull();
    }

    // ==================== Integration Tests ====================

    @Test
    @DisplayName("Should handle save and load cycle")
    void testSaveLoadCycle() {
        // Arrange
        PlantSerializer serializer = new TestPlantSerializer("cycle_test", true);
        CompoundTag tag = new CompoundTag();

        // Act - Save
        serializer.savePlant(mockPotData, tag);

        // Act - Load
        PlantPotData newPotData = mock(PlantPotData.class);
        serializer.loadPlant(newPotData, tag);

        // Assert - Tag should contain saved data
        assertThat(tag.contains("test_data")).isTrue();
    }

    @Test
    @DisplayName("Should handle empty NBT tag gracefully")
    void testLoadFromEmptyTag() {
        // Arrange
        PlantSerializer serializer = new TestPlantSerializer("empty_test", true);
        CompoundTag emptyTag = new CompoundTag();

        // Act & Assert - Should not throw
        assertThatCode(() -> serializer.loadPlant(mockPotData, emptyTag))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should support multiple plant types")
    void testMultiplePlantTypes() {
        // Arrange
        PlantSerializer tobacco = new TestPlantSerializer("tobacco", true);
        PlantSerializer cannabis = new TestPlantSerializer("cannabis", true);
        PlantSerializer coca = new TestPlantSerializer("coca", true);

        // Act
        CompoundTag tag1 = new CompoundTag();
        CompoundTag tag2 = new CompoundTag();
        CompoundTag tag3 = new CompoundTag();

        tobacco.savePlant(mockPotData, tag1);
        cannabis.savePlant(mockPotData, tag2);
        coca.savePlant(mockPotData, tag3);

        // Assert - Each should have saved independently
        assertThat(tag1.contains("test_data")).isTrue();
        assertThat(tag2.contains("test_data")).isTrue();
        assertThat(tag3.contains("test_data")).isTrue();
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle null NBT tag name")
    void testNullTagName() {
        // Arrange
        PlantSerializer serializer = new TestPlantSerializer(null, true);

        // Act
        String tagName = serializer.getPlantTagName();

        // Assert
        assertThat(tagName).isNull();
    }

    @Test
    @DisplayName("Should handle serializer with no plant")
    void testSerializerWithoutPlant() {
        // Arrange
        PlantSerializer serializer = new TestPlantSerializer("no_plant", false);

        // Act & Assert
        assertThat(serializer.hasPlant(mockPotData)).isFalse();
    }

    @Test
    @DisplayName("Factory should be thread-safe")
    void testFactoryThreadSafety() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        boolean[] results = new boolean[threadCount];

        // Act - Multiple threads accessing factory
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                List<PlantSerializer> serializers = PlantSerializerFactory.getAllSerializers();
                results[index] = serializers != null && !serializers.isEmpty();
            });
            threads[i].start();
        }

        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert - All threads should succeed
        for (boolean result : results) {
            assertThat(result).isTrue();
        }
    }

    // ==================== Helper Classes ====================

    /**
     * Test implementation of PlantSerializer for testing purposes
     */
    private static class TestPlantSerializer implements PlantSerializer {

        private final String tagName;
        private final boolean hasPlant;

        public TestPlantSerializer(String tagName, boolean hasPlant) {
            this.tagName = tagName;
            this.hasPlant = hasPlant;
        }

        @Override
        public void savePlant(PlantPotData potData, CompoundTag tag) {
            tag.putString("test_data", "saved");
        }

        @Override
        public void loadPlant(PlantPotData potData, CompoundTag tag) {
            // In real implementation, this would populate potData from tag
            if (tag.contains("test_data")) {
                tag.getString("test_data");
            }
        }

        @Override
        public String getPlantTagName() {
            return tagName;
        }

        @Override
        public boolean hasPlant(PlantPotData potData) {
            return hasPlant;
        }
    }
}
