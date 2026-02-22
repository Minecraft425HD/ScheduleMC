package de.rolandsw.schedulemc.vehicle;

import de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unit-Tests für FuelBillManager — testet die In-Memory-Logik ohne Datei-I/O.
 */
public class FuelBillManagerTest {

    private UUID playerA;
    private UUID playerB;
    private UUID stationX;
    private UUID stationY;

    @BeforeEach
    void setUp() throws Exception {
        // playerBills-Map via Reflection zurücksetzen
        Field field = FuelBillManager.class.getDeclaredField("playerBills");
        field.setAccessible(true);
        ((Map<?, ?>) field.get(null)).clear();

        playerA = UUID.randomUUID();
        playerB = UUID.randomUUID();
        stationX = UUID.randomUUID();
        stationY = UUID.randomUUID();
    }

    @Test
    void testCreateBill_AppearsInUnpaidList() {
        FuelBillManager.createBill(playerA, stationX, 500, 25.0);

        List<FuelBillManager.UnpaidBill> bills = FuelBillManager.getUnpaidBills(playerA);
        assertThat(bills).hasSize(1);
        assertThat(bills.get(0).amountFueled).isEqualTo(500);
        assertThat(bills.get(0).totalCost).isEqualTo(25.0);
        assertThat(bills.get(0).paid).isFalse();
    }

    @Test
    void testGetUnpaidBills_IgnoresPaidBills() {
        FuelBillManager.createBill(playerA, stationX, 200, 10.0);
        FuelBillManager.payBills(playerA, stationX);

        List<FuelBillManager.UnpaidBill> bills = FuelBillManager.getUnpaidBills(playerA);
        assertThat(bills).isEmpty();
    }

    @Test
    void testGetTotalUnpaidAmount_SumsCorrectly() {
        FuelBillManager.createBill(playerA, stationX, 100, 5.0);
        FuelBillManager.createBill(playerA, stationX, 200, 10.0);

        double total = FuelBillManager.getTotalUnpaidAmount(playerA);
        assertThat(total).isEqualTo(15.0);
    }

    @Test
    void testGetTotalUnpaidAmount_ForStation_FiltersCorrectly() {
        FuelBillManager.createBill(playerA, stationX, 100, 5.0);
        FuelBillManager.createBill(playerA, stationY, 200, 20.0);

        double totalX = FuelBillManager.getTotalUnpaidAmount(playerA, stationX);
        double totalY = FuelBillManager.getTotalUnpaidAmount(playerA, stationY);

        assertThat(totalX).isEqualTo(5.0);
        assertThat(totalY).isEqualTo(20.0);
    }

    @Test
    void testPayBills_MarksAsPaid() {
        FuelBillManager.createBill(playerA, stationX, 300, 15.0);
        FuelBillManager.createBill(playerA, stationY, 100, 5.0);

        FuelBillManager.payBills(playerA, stationX);

        assertThat(FuelBillManager.getUnpaidBills(playerA, stationX)).isEmpty();
        assertThat(FuelBillManager.getUnpaidBills(playerA, stationY)).hasSize(1);
    }

    @Test
    void testCleanupOldBills_RemovesOldPaidBills() {
        FuelBillManager.createBill(playerA, stationX, 100, 5.0);
        FuelBillManager.payBills(playerA, stationX);

        // Timestamp der Rechnung auf "vor 8 Tagen" setzen
        try {
            Field field = FuelBillManager.class.getDeclaredField("playerBills");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, List<FuelBillManager.UnpaidBill>> bills =
                (Map<UUID, List<FuelBillManager.UnpaidBill>>) field.get(null);
            long eightDaysAgo = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000);
            bills.get(playerA).get(0).timestamp = eightDaysAgo;
        } catch (Exception e) {
            fail("Reflection fehlgeschlagen: " + e.getMessage());
        }

        FuelBillManager.cleanupOldBills();

        assertThat(FuelBillManager.getUnpaidBills(playerA)).isEmpty();
        // getUnpaidBills filtert ohnehin nach !paid, aber auch die Liste sollte leer sein
        try {
            Field field = FuelBillManager.class.getDeclaredField("playerBills");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, List<FuelBillManager.UnpaidBill>> bills =
                (Map<UUID, List<FuelBillManager.UnpaidBill>>) field.get(null);
            List<FuelBillManager.UnpaidBill> remaining = bills.get(playerA);
            assertThat(remaining == null || remaining.isEmpty()).isTrue();
        } catch (Exception e) {
            fail("Reflection fehlgeschlagen: " + e.getMessage());
        }
    }

    @Test
    void testCleanupOldBills_KeepsRecentBills() {
        FuelBillManager.createBill(playerA, stationX, 100, 5.0);
        FuelBillManager.payBills(playerA, stationX);
        // Timestamp bleibt frisch (jetzt) → soll nicht gelöscht werden

        FuelBillManager.cleanupOldBills();

        try {
            Field field = FuelBillManager.class.getDeclaredField("playerBills");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, List<FuelBillManager.UnpaidBill>> bills =
                (Map<UUID, List<FuelBillManager.UnpaidBill>>) field.get(null);
            List<FuelBillManager.UnpaidBill> remaining = bills.get(playerA);
            assertThat(remaining).hasSize(1);
        } catch (Exception e) {
            fail("Reflection fehlgeschlagen: " + e.getMessage());
        }
    }

    @Test
    void testGetUnpaidBills_UnknownPlayer_ReturnsEmpty() {
        List<FuelBillManager.UnpaidBill> bills = FuelBillManager.getUnpaidBills(UUID.randomUUID());
        assertThat(bills).isEmpty();
    }

    @Test
    void testGetTotalUnpaidAmount_UnknownPlayer_ReturnsZero() {
        double total = FuelBillManager.getTotalUnpaidAmount(UUID.randomUUID());
        assertThat(total).isEqualTo(0.0);
    }

    @Test
    void testMultiplePlayers_AreIsolated() {
        FuelBillManager.createBill(playerA, stationX, 100, 5.0);
        FuelBillManager.createBill(playerB, stationX, 200, 10.0);

        assertThat(FuelBillManager.getTotalUnpaidAmount(playerA)).isEqualTo(5.0);
        assertThat(FuelBillManager.getTotalUnpaidAmount(playerB)).isEqualTo(10.0);
    }
}
