package de.rolandsw.schedulemc.vehicle.network;

import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from client to server to honk horn.
 */
public class VehicleHornPacket {

    private final int vehicleId;

    public VehicleHornPacket(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public static void encode(VehicleHornPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.vehicleId);
    }

    public static VehicleHornPacket decode(FriendlyByteBuf buf) {
        return new VehicleHornPacket(buf.readInt());
    }

    public static void handle(VehicleHornPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                net.minecraft.world.entity.Entity entity = player.level().getEntity(packet.vehicleId);

                if (entity instanceof VehicleEntity vehicle) {
                    // Play horn sound
                    vehicle.level().playSound(null, vehicle.blockPosition(),
                            SoundEvents.NOTE_BLOCK_BELL.get(), // Placeholder sound
                            SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
