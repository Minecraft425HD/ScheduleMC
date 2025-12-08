package de.rolandsw.schedulemc.vehicle.network;

import de.rolandsw.schedulemc.vehicle.component.attribute.BatteryComponent;
import de.rolandsw.schedulemc.vehicle.component.engine.EngineComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from client to server to start/stop engine.
 */
public class VehicleStartEnginePacket {

    private final int vehicleId;

    public VehicleStartEnginePacket(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public static void encode(VehicleStartEnginePacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.vehicleId);
    }

    public static VehicleStartEnginePacket decode(FriendlyByteBuf buf) {
        return new VehicleStartEnginePacket(buf.readInt());
    }

    public static void handle(VehicleStartEnginePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                net.minecraft.world.entity.Entity entity = player.level().getEntity(packet.vehicleId);

                if (entity instanceof VehicleEntity vehicle) {
                    EngineComponent engine = vehicle.getComponent(
                            ComponentType.ENGINE, EngineComponent.class);
                    BatteryComponent battery = vehicle.getComponent(
                            ComponentType.BATTERY, BatteryComponent.class);

                    if (engine != null) {
                        if (!engine.isRunning()) {
                            // Try to start engine
                            if (battery != null && battery.canStartEngine()) {
                                battery.consumeStartCharge();
                                engine.setRunning(true);
                                player.displayClientMessage(
                                        Component.translatable("message.vehicle.engine_started"),
                                        true);
                            } else {
                                player.displayClientMessage(
                                        Component.translatable("message.vehicle.battery_dead"),
                                        true);
                            }
                        } else {
                            // Stop engine
                            engine.setRunning(false);
                            engine.setCurrentRpm(0);
                            player.displayClientMessage(
                                    Component.translatable("message.vehicle.engine_stopped"),
                                    true);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
