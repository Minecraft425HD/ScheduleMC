package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.tobacco.blockentity.LargePackagingTableBlockEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paket für Large Packaging Table Pack-Anfragen
 * weight: 20 oder -1 (für Unpack)
 */
public class LargePackageRequestPacket {
    private final BlockPos pos;
    private final int weight;

    public LargePackageRequestPacket(BlockPos pos, int weight) {
        this.pos = pos;
        this.weight = weight;
    }

    public static void encode(LargePackageRequestPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.weight);
    }

    public static LargePackageRequestPacket decode(FriendlyByteBuf buf) {
        return new LargePackageRequestPacket(buf.readBlockPos(), buf.readInt());
    }

    public static void handle(LargePackageRequestPacket packet, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof LargePackagingTableBlockEntity packagingTable) {
                if (packet.weight == -1) {
                    packagingTable.unpackAll();
                } else if (packet.weight == 20) {
                    packagingTable.packageTobacco20g();
                }
            }
        });
    }
}
