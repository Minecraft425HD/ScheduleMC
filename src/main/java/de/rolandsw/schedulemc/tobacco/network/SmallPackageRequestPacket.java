package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.tobacco.blockentity.SmallPackagingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paket für Small Packaging Table Pack-Anfragen
 * weight: 1 (für Tüten) oder 5 (für Gläser) oder -1 (für Unpack)
 */
public class SmallPackageRequestPacket {
    private final BlockPos pos;
    private final int weight;

    public SmallPackageRequestPacket(BlockPos pos, int weight) {
        this.pos = pos;
        this.weight = weight;
    }

    public static void encode(SmallPackageRequestPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.weight);
    }

    public static SmallPackageRequestPacket decode(FriendlyByteBuf buf) {
        return new SmallPackageRequestPacket(buf.readBlockPos(), buf.readInt());
    }

    public static void handle(SmallPackageRequestPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                BlockEntity be = player.level().getBlockEntity(packet.pos);
                if (be instanceof SmallPackagingTableBlockEntity packagingTable) {
                    if (packet.weight == -1) {
                        // Unpack
                        packagingTable.unpackAll();
                    } else if (packet.weight == 1) {
                        packagingTable.packageTobacco1g();
                    } else if (packet.weight == 5) {
                        packagingTable.packageTobacco5g();
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
