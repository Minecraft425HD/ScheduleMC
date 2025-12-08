package de.rolandsw.schedulemc.vehicle.network;

import de.rolandsw.schedulemc.vehicle.component.control.ControlComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from client to server to update vehicle controls.
 */
public class VehicleControlPacket {

    private final int vehicleId;
    private final boolean forward;
    private final boolean backward;
    private final boolean left;
    private final boolean right;
    private final boolean brake;

    public VehicleControlPacket(int vehicleId, boolean forward, boolean backward,
                               boolean left, boolean right, boolean brake) {
        this.vehicleId = vehicleId;
        this.forward = forward;
        this.backward = backward;
        this.left = left;
        this.right = right;
        this.brake = brake;
    }

    public static void encode(VehicleControlPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.vehicleId);
        buf.writeBoolean(packet.forward);
        buf.writeBoolean(packet.backward);
        buf.writeBoolean(packet.left);
        buf.writeBoolean(packet.right);
        buf.writeBoolean(packet.brake);
    }

    public static VehicleControlPacket decode(FriendlyByteBuf buf) {
        return new VehicleControlPacket(
                buf.readInt(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean()
        );
    }

    public static void handle(VehicleControlPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                net.minecraft.world.entity.Entity entity = player.level().getEntity(packet.vehicleId);

                if (entity instanceof VehicleEntity vehicle) {
                    ControlComponent controls = vehicle.getComponent(
                            ComponentType.CONTROLS, ControlComponent.class);

                    if (controls != null) {
                        controls.setForwardPressed(packet.forward);
                        controls.setBackwardPressed(packet.backward);
                        controls.setLeftPressed(packet.left);
                        controls.setRightPressed(packet.right);
                        controls.setBrakePressed(packet.brake);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
