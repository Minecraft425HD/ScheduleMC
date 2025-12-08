package de.rolandsw.schedulemc.vehicle.client;

import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import de.rolandsw.schedulemc.vehicle.network.VehicleControlPacket;
import de.rolandsw.schedulemc.vehicle.network.VehicleHornPacket;
import de.rolandsw.schedulemc.vehicle.network.VehicleNetwork;
import de.rolandsw.schedulemc.vehicle.network.VehicleStartEnginePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles client-side input for vehicle controls.
 */
@Mod.EventBusSubscriber(modid = "schedulemc", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VehicleInputHandler {

    /**
     * Registers key mappings.
     */
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(VehicleKeys.FORWARD);
        event.register(VehicleKeys.BACKWARD);
        event.register(VehicleKeys.LEFT);
        event.register(VehicleKeys.RIGHT);
        event.register(VehicleKeys.START_ENGINE);
        event.register(VehicleKeys.HORN);
        event.register(VehicleKeys.BRAKE);
        event.register(VehicleKeys.INVENTORY);
    }

    /**
     * Forge event handler for input.
     */
    @Mod.EventBusSubscriber(modid = "schedulemc", value = Dist.CLIENT)
    public static class ForgeEvents {

        private static boolean wasStartEnginePressed = false;
        private static boolean wasHornPressed = false;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if (player == null || player.getVehicle() == null) {
                wasStartEnginePressed = false;
                wasHornPressed = false;
                return;
            }

            // Only handle input if riding a vehicle
            if (player.getVehicle() instanceof VehicleEntity vehicle) {
                handleVehicleInput(vehicle);
            }
        }

        private static void handleVehicleInput(VehicleEntity vehicle) {
            // Get current key states
            boolean forward = VehicleKeys.FORWARD.isDown();
            boolean backward = VehicleKeys.BACKWARD.isDown();
            boolean left = VehicleKeys.LEFT.isDown();
            boolean right = VehicleKeys.RIGHT.isDown();
            boolean brake = VehicleKeys.BRAKE.isDown();

            // Send control packet
            VehicleNetwork.sendToServer(new VehicleControlPacket(
                    vehicle.getId(),
                    forward,
                    backward,
                    left,
                    right,
                    brake
            ));

            // Handle start engine (toggle on press)
            boolean startEnginePressed = VehicleKeys.START_ENGINE.isDown();
            if (startEnginePressed && !wasStartEnginePressed) {
                VehicleNetwork.sendToServer(new VehicleStartEnginePacket(vehicle.getId()));
            }
            wasStartEnginePressed = startEnginePressed;

            // Handle horn (toggle on press)
            boolean hornPressed = VehicleKeys.HORN.isDown();
            if (hornPressed && !wasHornPressed) {
                VehicleNetwork.sendToServer(new VehicleHornPacket(vehicle.getId()));
            }
            wasHornPressed = hornPressed;

            // Handle inventory
            if (VehicleKeys.INVENTORY.consumeClick()) {
                // TODO: Open vehicle inventory GUI
            }
        }
    }
}
