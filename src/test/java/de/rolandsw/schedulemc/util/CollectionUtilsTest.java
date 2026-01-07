package de.rolandsw.schedulemc.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for CollectionUtils
 *
 * @since 1.0
 */
@DisplayName("CollectionUtils Tests")
class CollectionUtilsTest {

    @Test
    @DisplayName("newArrayListWithCapacity - Should create pre-sized ArrayList")
    void testNewArrayListWithCapacity() {
        // Act
        ArrayList<String> list = CollectionUtils.newArrayListWithCapacity(100);

        // Assert
        assertThat(list).isNotNull();
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("newArrayListWithCapacity - Should handle zero capacity")
    void testNewArrayListWithCapacityZero() {
        // Act
        ArrayList<String> list = CollectionUtils.newArrayListWithCapacity(0);

        // Assert
        assertThat(list).isNotNull();
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("newHashMapWithCapacity - Should create pre-sized HashMap")
    void testNewHashMapWithCapacity() {
        // Act
        HashMap<String, Integer> map = CollectionUtils.newHashMapWithCapacity(100);

        // Assert
        assertThat(map).isNotNull();
        assertThat(map).isEmpty();
    }

    @Test
    @DisplayName("newHashSetWithCapacity - Should create pre-sized HashSet")
    void testNewHashSetWithCapacity() {
        // Act
        HashSet<String> set = CollectionUtils.newHashSetWithCapacity(100);

        // Assert
        assertThat(set).isNotNull();
        assertThat(set).isEmpty();
    }

    @Test
    @DisplayName("filterInPlace - Should filter list in-place")
    void testFilterInPlace() {
        // Arrange
        List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));

        // Act - Keep only even numbers
        CollectionUtils.filterInPlace(numbers, n -> n % 2 == 0);

        // Assert
        assertThat(numbers).containsExactly(2, 4, 6);
    }

    @Test
    @DisplayName("filterInPlace - Should handle empty list")
    void testFilterInPlaceEmpty() {
        // Arrange
        List<Integer> empty = new ArrayList<>();

        // Act
        CollectionUtils.filterInPlace(empty, n -> n > 0);

        // Assert
        assertThat(empty).isEmpty();
    }

    @Test
    @DisplayName("findFirst - Should find first matching element")
    void testFindFirst() {
        // Arrange
        List<String> items = Arrays.asList("apple", "banana", "cherry");

        // Act
        String found = CollectionUtils.findFirst(items, s -> s.startsWith("b"));

        // Assert
        assertThat(found).isEqualTo("banana");
    }

    @Test
    @DisplayName("findFirst - Should return null when no match")
    void testFindFirstNoMatch() {
        // Arrange
        List<String> items = Arrays.asList("apple", "banana", "cherry");

        // Act
        String found = CollectionUtils.findFirst(items, s -> s.startsWith("z"));

        // Assert
        assertThat(found).isNull();
    }

    @Test
    @DisplayName("anyMatch - Should return true when element matches")
    void testAnyMatch() {
        // Arrange
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

        // Act
        boolean matches = CollectionUtils.anyMatch(numbers, n -> n > 3);

        // Assert
        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("anyMatch - Should return false when no element matches")
    void testAnyMatchNoMatch() {
        // Arrange
        List<Integer> numbers = Arrays.asList(1, 2, 3);

        // Act
        boolean matches = CollectionUtils.anyMatch(numbers, n -> n > 10);

        // Assert
        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("count - Should count matching elements")
    void testCount() {
        // Arrange
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);

        // Act
        long count = CollectionUtils.count(numbers, n -> n % 2 == 0);

        // Assert
        assertThat(count).isEqualTo(3); // 2, 4, 6
    }

    @Test
    @DisplayName("count - Should return zero when no matches")
    void testCountNoMatches() {
        // Arrange
        List<Integer> numbers = Arrays.asList(1, 3, 5);

        // Act
        long count = CollectionUtils.count(numbers, n -> n % 2 == 0);

        // Assert
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("partition - Should partition collection correctly")
    void testPartition() {
        // Arrange
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);

        // Act
        CollectionUtils.PartitionResult<Integer> result =
            CollectionUtils.partition(numbers, n -> n % 2 == 0);

        // Assert
        assertThat(result.matching).containsExactly(2, 4, 6);
        assertThat(result.nonMatching).containsExactly(1, 3, 5);
    }

    @Test
    @DisplayName("partition - Should handle all matching")
    void testPartitionAllMatching() {
        // Arrange
        List<Integer> numbers = Arrays.asList(2, 4, 6);

        // Act
        CollectionUtils.PartitionResult<Integer> result =
            CollectionUtils.partition(numbers, n -> n % 2 == 0);

        // Assert
        assertThat(result.matching).hasSize(3);
        assertThat(result.nonMatching).isEmpty();
    }

    @Test
    @DisplayName("isNullOrEmpty - Should detect null collection")
    void testIsNullOrEmptyNull() {
        // Act
        boolean result = CollectionUtils.isNullOrEmpty((Collection<?>) null);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isNullOrEmpty - Should detect empty collection")
    void testIsNullOrEmptyEmpty() {
        // Act
        boolean result = CollectionUtils.isNullOrEmpty(new ArrayList<>());

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isNullOrEmpty - Should return false for non-empty")
    void testIsNullOrEmptyNonEmpty() {
        // Act
        boolean result = CollectionUtils.isNullOrEmpty(Arrays.asList(1, 2, 3));

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isNullOrEmpty (Map) - Should detect null map")
    void testIsNullOrEmptyMapNull() {
        // Act
        boolean result = CollectionUtils.isNullOrEmpty((Map<?, ?>) null);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isNullOrEmpty (Map) - Should detect empty map")
    void testIsNullOrEmptyMapEmpty() {
        // Act
        boolean result = CollectionUtils.isNullOrEmpty(new HashMap<>());

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("getFirstOrDefault - Should get first element")
    void testGetFirstOrDefault() {
        // Arrange
        List<String> items = Arrays.asList("first", "second", "third");

        // Act
        String first = CollectionUtils.getFirstOrDefault(items, "default");

        // Assert
        assertThat(first).isEqualTo("first");
    }

    @Test
    @DisplayName("getFirstOrDefault - Should return default for empty list")
    void testGetFirstOrDefaultEmpty() {
        // Arrange
        List<String> empty = new ArrayList<>();

        // Act
        String first = CollectionUtils.getFirstOrDefault(empty, "default");

        // Assert
        assertThat(first).isEqualTo("default");
    }

    @Test
    @DisplayName("getLastOrDefault - Should get last element")
    void testGetLastOrDefault() {
        // Arrange
        List<String> items = Arrays.asList("first", "second", "third");

        // Act
        String last = CollectionUtils.getLastOrDefault(items, "default");

        // Assert
        assertThat(last).isEqualTo("third");
    }

    @Test
    @DisplayName("getLastOrDefault - Should return default for empty list")
    void testGetLastOrDefaultEmpty() {
        // Arrange
        List<String> empty = new ArrayList<>();

        // Act
        String last = CollectionUtils.getLastOrDefault(empty, "default");

        // Assert
        assertThat(last).isEqualTo("default");
    }
}
