package de.rolandsw.schedulemc.warehouse.network.packet;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
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
        PacketHandler.handleAdminPacket(ctx, 2, player -> {
            ServerLevel level = player.serverLevel();
            BlockEntity be = level.getBlockEntity(msg.pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) return;

            // Entferne die Verknüpfung vom Warehouse
            warehouse.removeSeller(msg.sellerId);
            warehouse.setChanged();
            warehouse.syncToClient(); // Wichtig: Synchronisiere zum Client

            // Finde den NPC und entferne auch seine Warehouse-Zuweisung
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof CustomNPCEntity customNpc) {
                    if (customNpc.getUUID().equals(msg.sellerId)) {
                        customNpc.getNpcData().setAssignedWarehouse(null);
                        break;
                    }
                }
            }

            player.sendSystemMessage(Component.literal("§aVerkäufer entfernt!"));
        });
    }
}
