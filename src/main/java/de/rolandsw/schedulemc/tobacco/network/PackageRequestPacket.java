package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.tobacco.blockentity.PackagingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für Tabak-Verpackungs-Request
 */
public class PackageRequestPacket {
    private final BlockPos pos;
    private final int weight;

    public PackageRequestPacket(BlockPos pos, int weight) {
        this.pos = pos;
        this.weight = weight;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(weight);
    }

    public static PackageRequestPacket decode(FriendlyByteBuf buf) {
        return new PackageRequestPacket(
            buf.readBlockPos(),
            buf.readInt()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                BlockEntity be = player.level().getBlockEntity(pos);
                if (be instanceof PackagingTableBlockEntity packagingTable) {
                    packagingTable.packageTobacco(weight);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
