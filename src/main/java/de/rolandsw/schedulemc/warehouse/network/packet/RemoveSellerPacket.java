package de.rolandsw.schedulemc.warehouse.network.packet;

import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet zum Entfernen eines Verkäufers
 */
public class RemoveSellerPacket {

    private final BlockPos pos;
    private final UUID sellerId;

    public RemoveSellerPacket(BlockPos pos, UUID sellerId) {
        this.pos = pos;
        this.sellerId = sellerId;
    }

    public static void encode(RemoveSellerPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeUUID(msg.sellerId);
    }

    public static RemoveSellerPacket decode(FriendlyByteBuf buffer) {
        return new RemoveSellerPacket(
            buffer.readBlockPos(),
            buffer.readUUID()
        );
    }

    public static void handle(RemoveSellerPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Admin check
            if (!player.hasPermissions(2)) {
                player.sendSystemMessage(Component.literal("§cNur Admins können Verkäufer entfernen!"));
                return;
            }

            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) return;

            warehouse.removeSeller(msg.sellerId);
            warehouse.setChanged();

            player.sendSystemMessage(Component.literal("§aVerkäufer entfernt!"));
        });
        ctx.get().setPacketHandled(true);
    }
}
