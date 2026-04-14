package de.rolandsw.schedulemc.vehicle;

import de.rolandsw.schedulemc.vehicle.vehicle.VehicleOwnershipTracker;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VehicleOwnershipTrackerTest {

    @Test
    @DisplayName("registerVehiclePurchase should increment plate number for same player")
    void registerVehiclePurchaseIncrementsForSamePlayer() {
        VehicleOwnershipTracker tracker = new VehicleOwnershipTracker();
        Player player = mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUUID()).thenReturn(playerId);

        int first = tracker.registerVehiclePurchase(player, "MIN");
        int second = tracker.registerVehiclePurchase(player, "MIN");

        assertThat(first).isEqualTo(1);
        assertThat(second).isEqualTo(2);
    }

    @Test
    @DisplayName("registerVehiclePurchase should allocate distinct offsets for shared prefix")
    void registerVehiclePurchaseAllocatesOffsetForSharedPrefix() {
        VehicleOwnershipTracker tracker = new VehicleOwnershipTracker();
        Player playerOne = mock(Player.class);
        Player playerTwo = mock(Player.class);

        when(playerOne.getUUID()).thenReturn(UUID.randomUUID());
        when(playerTwo.getUUID()).thenReturn(UUID.randomUUID());

        int firstPlayerPlate = tracker.registerVehiclePurchase(playerOne, "MIN");
        int secondPlayerPlate = tracker.registerVehiclePurchase(playerTwo, "MIN");

        assertThat(firstPlayerPlate).isEqualTo(1);
        assertThat(secondPlayerPlate).isEqualTo(11);
    }

    @Test
    @DisplayName("registerVehiclePurchase should support plate numbers above 99")
    void registerVehiclePurchaseSupportsPlateNumbersAboveNinetyNine() {
        VehicleOwnershipTracker tracker = new VehicleOwnershipTracker();
        Player player = mock(Player.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        int lastPlate = 0;
        for (int i = 0; i < 105; i++) {
            lastPlate = tracker.registerVehiclePurchase(player, "MIN");
        }

        assertThat(lastPlate).isEqualTo(105);
    }
}
