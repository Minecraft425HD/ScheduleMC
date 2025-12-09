package de.rolandsw.schedulemc.vehicle.network;

import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from server to client to sync vehicle component data.
 */
public class VehicleSyncPacket {

    private final int vehicleId;
    private final CompoundTag componentData;

    public VehicleSyncPacket(int vehicleId, CompoundTag componentData) {
        this.vehicleId = vehicleId;
        this.componentData = componentData;
    }

    public static void encode(VehicleSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.vehicleId);
        buf.writeNbt(packet.componentData);
    }

    public static VehicleSyncPacket decode(FriendlyByteBuf buf) {
        return new VehicleSyncPacket(buf.readInt(), buf.readNbt());
    }

    public static void handle(VehicleSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side handling
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level != null) {
                net.minecraft.world.entity.Entity entity = mc.level.getEntity(packet.vehicleId);
                if (entity instanceof VehicleEntity vehicle) {
                    // Sync component data
                    // TODO: Implement component sync from NBT
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
