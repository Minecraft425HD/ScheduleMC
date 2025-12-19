package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.tobacco.blockentity.MediumPackagingTableBlockEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paket für Medium Packaging Table Pack-Anfragen
 * weight: 10 oder -1 (für Unpack)
 */
public class MediumPackageRequestPacket {
    private final BlockPos pos;
    private final int weight;

    public MediumPackageRequestPacket(BlockPos pos, int weight) {
        this.pos = pos;
        this.weight = weight;
    }

    public static void encode(MediumPackageRequestPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.weight);
    }

    public static MediumPackageRequestPacket decode(FriendlyByteBuf buf) {
        return new MediumPackageRequestPacket(buf.readBlockPos(), buf.readInt());
    }

    public static void handle(MediumPackageRequestPacket packet, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof MediumPackagingTableBlockEntity packagingTable) {
                if (packet.weight == -1) {
                    packagingTable.unpackAll();
                } else if (packet.weight == 10) {
                    packagingTable.packageTobacco10g();
                }
            }
        });
    }
}
