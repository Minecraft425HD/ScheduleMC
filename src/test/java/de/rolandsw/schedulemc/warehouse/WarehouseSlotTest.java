package de.rolandsw.schedulemc.warehouse;

import net.minecraft.world.item.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests für WarehouseSlot — Slot-Verwaltung, Kapazitätsgrenzen und unlimited-Flag.
 *
 * Hinweis: Item-Referenzgleichheit (==) wird intern genutzt — Mockito-Mocks sind
 * ausreichend, da keine Item-Methoden aufgerufen werden (nur Referenzvergleich).
 */
public class WarehouseSlotTest {

    private static final int CAPACITY = 100;

    private WarehouseSlot slot;
    private Item itemA;
    private Item itemB;

    @BeforeEach
    void setUp() {
        slot = new WarehouseSlot(CAPACITY);
        itemA = mock(Item.class);
        itemB = mock(Item.class);
    }

    // ── Initialer Zustand ─────────────────────────────────────────────────────

    @Test
    void testInitialState_IsEmpty() {
        assertThat(slot.isEmpty()).isTrue();
    }

    @Test
    void testInitialState_IsNotFull() {
        assertThat(slot.isFull()).isFalse();
    }

    @Test
    void testInitialState_StockIsZero() {
        assertThat(slot.getStock()).isEqualTo(0);
    }

    @Test
    void testInitialState_AllowedItemIsNull() {
        assertThat(slot.getAllowedItem()).isNull();
    }

    @Test
    void testInitialState_AvailableSpaceEqualsCapacity() {
        assertThat(slot.getAvailableSpace()).isEqualTo(CAPACITY);
    }

    // ── canAccept ─────────────────────────────────────────────────────────────

    @Test
    void testCanAccept_EmptySlot_AcceptsAnyItem() {
        assertThat(slot.canAccept(itemA)).isTrue();
        assertThat(slot.canAccept(itemB)).isTrue();
    }

    @Test
    void testCanAccept_SlotWithItem_AcceptsSameItem() {
        slot.addStock(itemA, 1);
        assertThat(slot.canAccept(itemA)).isTrue();
    }

    @Test
    void testCanAccept_SlotWithItem_RejectsDifferentItem() {
        slot.addStock(itemA, 1);
        assertThat(slot.canAccept(itemB)).isFalse();
    }

    // ── addStock ──────────────────────────────────────────────────────────────

    @Test
    void testAddStock_AddsCorrectAmount() {
        int added = slot.addStock(itemA, 10);
        assertThat(added).isEqualTo(10);
        assertThat(slot.getStock()).isEqualTo(10);
    }

    @Test
    void testAddStock_SetsAllowedItemOnFirstAdd() {
        slot.addStock(itemA, 1);
        assertThat(slot.getAllowedItem()).isSameAs(itemA);
    }

    @Test
    void testAddStock_CappsAtMaxCapacity() {
        int added = slot.addStock(itemA, CAPACITY + 50);
        assertThat(added).isEqualTo(CAPACITY);
        assertThat(slot.getStock()).isEqualTo(CAPACITY);
    }

    @Test
    void testAddStock_AddsOnlyRemainingSpace() {
        slot.addStock(itemA, 80);
        int added = slot.addStock(itemA, 40);
        assertThat(added).isEqualTo(20); // Nur 20 Platz übrig
        assertThat(slot.getStock()).isEqualTo(CAPACITY);
    }

    @Test
    void testAddStock_RejectsWrongItem() {
        slot.addStock(itemA, 10);
        int added = slot.addStock(itemB, 10);
        assertThat(added).isEqualTo(0);
        assertThat(slot.getStock()).isEqualTo(10);
    }

    @Test
    void testAddStock_ReturnsZeroWhenFull() {
        slot.addStock(itemA, CAPACITY);
        int added = slot.addStock(itemA, 5);
        assertThat(added).isEqualTo(0);
    }

    @Test
    void testAddStock_IsFullAfterFillingToCapacity() {
        slot.addStock(itemA, CAPACITY);
        assertThat(slot.isFull()).isTrue();
    }

    @Test
    void testAddStock_IsNotEmptyAfterAdding() {
        slot.addStock(itemA, 1);
        assertThat(slot.isEmpty()).isFalse();
    }

    // ── removeStock ───────────────────────────────────────────────────────────

    @Test
    void testRemoveStock_RemovesCorrectAmount() {
        slot.addStock(itemA, 50);
        int removed = slot.removeStock(20);
        assertThat(removed).isEqualTo(20);
        assertThat(slot.getStock()).isEqualTo(30);
    }

    @Test
    void testRemoveStock_CapsAtCurrentStock() {
        slot.addStock(itemA, 10);
        int removed = slot.removeStock(100);
        assertThat(removed).isEqualTo(10);
        assertThat(slot.getStock()).isEqualTo(0);
    }

    @Test
    void testRemoveStock_ResetsSlotWhenEmpty() {
        slot.addStock(itemA, 10);
        slot.removeStock(10);
        assertThat(slot.isEmpty()).isTrue();
        assertThat(slot.getAllowedItem()).isNull();
    }

    @Test
    void testRemoveStock_SlotAcceptsNewItemAfterReset() {
        slot.addStock(itemA, 10);
        slot.removeStock(10);
        assertThat(slot.canAccept(itemB)).isTrue();
        int added = slot.addStock(itemB, 5);
        assertThat(added).isEqualTo(5);
    }

    @Test
    void testRemoveStock_PartialRemoval_ItemStaysLocked() {
        slot.addStock(itemA, 10);
        slot.removeStock(5);
        assertThat(slot.getAllowedItem()).isSameAs(itemA);
        assertThat(slot.isEmpty()).isFalse();
    }

    @Test
    void testRemoveStock_FromEmptySlot_ReturnsZero() {
        int removed = slot.removeStock(10);
        assertThat(removed).isEqualTo(0);
    }

    // ── getAvailableSpace ─────────────────────────────────────────────────────

    @Test
    void testGetAvailableSpace_DecreasesAfterAdding() {
        slot.addStock(itemA, 30);
        assertThat(slot.getAvailableSpace()).isEqualTo(CAPACITY - 30);
    }

    @Test
    void testGetAvailableSpace_ZeroWhenFull() {
        slot.addStock(itemA, CAPACITY);
        assertThat(slot.getAvailableSpace()).isEqualTo(0);
    }

    // ── getRestockAmount ──────────────────────────────────────────────────────

    @Test
    void testGetRestockAmount_EmptySlot_ReturnsZero() {
        assertThat(slot.getRestockAmount()).isEqualTo(0);
    }

    @Test
    void testGetRestockAmount_PartiallyFilled_ReturnsDeficit() {
        slot.addStock(itemA, 30);
        assertThat(slot.getRestockAmount()).isEqualTo(CAPACITY - 30);
    }

    @Test
    void testGetRestockAmount_FullSlot_ReturnsZero() {
        slot.addStock(itemA, CAPACITY);
        assertThat(slot.getRestockAmount()).isEqualTo(0);
    }

    @Test
    void testGetRestockAmount_UnlimitedSlot_ReturnsZero() {
        slot.addStock(itemA, 30);
        slot.setUnlimited(true);
        assertThat(slot.getRestockAmount()).isEqualTo(0);
    }

    // ── unlimited Flag ────────────────────────────────────────────────────────

    @Test
    void testUnlimited_DefaultIsFalse() {
        assertThat(slot.isUnlimited()).isFalse();
    }

    @Test
    void testUnlimited_CanBeSetToTrue() {
        slot.setUnlimited(true);
        assertThat(slot.isUnlimited()).isTrue();
    }

    @Test
    void testUnlimited_DoesNotAffectAddStockCapacity() {
        // unlimited beeinflusst nur getRestockAmount, nicht die tatsächliche Kapazitätsbegrenzung
        slot.setUnlimited(true);
        int added = slot.addStock(itemA, CAPACITY + 50);
        assertThat(added).isEqualTo(CAPACITY); // Immer noch auf CAPACITY begrenzt
    }

    // ── setMaxCapacity ────────────────────────────────────────────────────────

    @Test
    void testSetMaxCapacity_UpdatesCapacity() {
        slot.setMaxCapacity(200);
        assertThat(slot.getMaxCapacity()).isEqualTo(200);
    }

    @Test
    void testSetMaxCapacity_MinimumIsOne() {
        slot.setMaxCapacity(0);
        assertThat(slot.getMaxCapacity()).isEqualTo(1);

        slot.setMaxCapacity(-50);
        assertThat(slot.getMaxCapacity()).isEqualTo(1);
    }

    // ── clear ─────────────────────────────────────────────────────────────────

    @Test
    void testClear_ResetsAllState() {
        slot.addStock(itemA, 50);
        slot.setUnlimited(true);
        slot.clear();

        assertThat(slot.isEmpty()).isTrue();
        assertThat(slot.getStock()).isEqualTo(0);
        assertThat(slot.getAllowedItem()).isNull();
        assertThat(slot.isUnlimited()).isFalse();
    }

    @Test
    void testClear_AllowsNewItemAfterClear() {
        slot.addStock(itemA, 50);
        slot.clear();
        slot.addStock(itemB, 10);
        assertThat(slot.getAllowedItem()).isSameAs(itemB);
    }
}
