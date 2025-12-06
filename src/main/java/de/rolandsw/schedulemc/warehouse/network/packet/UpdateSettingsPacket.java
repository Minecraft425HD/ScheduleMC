package de.rolandsw.schedulemc.warehouse.network.packet;

import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum Update der Warehouse-Einstellungen
 */
public class UpdateSettingsPacket {

    private final BlockPos pos;
    private final String shopId;

    public UpdateSettingsPacket(BlockPos pos, String shopId) {
        this.pos = pos;
        this.shopId = shopId;
    }

    public static void encode(UpdateSettingsPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeBoolean(msg.shopId != null);
        if (msg.shopId != null) {
            buffer.writeUtf(msg.shopId);
        }
    }

    public static UpdateSettingsPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        String shopId = buffer.readBoolean() ? buffer.readUtf() : null;
        return new UpdateSettingsPacket(pos, shopId);
    }

    public static void handle(UpdateSettingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Admin check
            if (!player.hasPermissions(2)) {
                player.sendSystemMessage(Component.literal("§cNur Admins können Einstellungen ändern!"));
                return;
            }

            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) return;

            warehouse.setShopId(msg.shopId);
            warehouse.setChanged();

            player.sendSystemMessage(Component.literal("§aEinstellungen aktualisiert!"));
        });
        ctx.get().setPacketHandled(true);
    }
}
