package de.rolandsw.schedulemc.warehouse.network.packet;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet zum Hinzufügen eines Verkäufers zu einem Warehouse
 */
public class AddSellerPacket {

    private final BlockPos pos;
    private final UUID sellerId;

    public AddSellerPacket(BlockPos pos, UUID sellerId) {
        this.pos = pos;
        this.sellerId = sellerId;
    }

    public static void encode(AddSellerPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeUUID(msg.sellerId);
    }

    public static AddSellerPacket decode(FriendlyByteBuf buffer) {
        return new AddSellerPacket(
            buffer.readBlockPos(),
            buffer.readUUID()
        );
    }

    public static void handle(AddSellerPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Admin check
            if (!player.hasPermissions(2)) {
                player.sendSystemMessage(Component.literal("§cNur Admins können Verkäufer hinzufügen!"));
                return;
            }

            ServerLevel level = player.serverLevel();
            BlockEntity be = level.getBlockEntity(msg.pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) {
                player.sendSystemMessage(Component.literal("§cWarehouse nicht gefunden!"));
                return;
            }

            // Finde den NPC
            Entity entity = level.getEntity(msg.sellerId);
            if (!(entity instanceof CustomNPCEntity npc)) {
                player.sendSystemMessage(Component.literal("§cNPC nicht gefunden!"));
                return;
            }

            // Prüfe ob NPC bereits mit einem Warehouse verknüpft ist
            BlockPos existingWarehouse = npc.getNpcData().getAssignedWarehouse();
            if (existingWarehouse != null) {
                // Entferne alte Verknüpfung
                BlockEntity oldBe = level.getBlockEntity(existingWarehouse);
                if (oldBe instanceof WarehouseBlockEntity oldWarehouse) {
                    oldWarehouse.removeSeller(msg.sellerId);
                    oldWarehouse.setChanged();
                }
            }

            // Erstelle bidirektionale Verknüpfung
            npc.getNpcData().setAssignedWarehouse(msg.pos);
            warehouse.addSeller(msg.sellerId);
            warehouse.setChanged();

            player.sendSystemMessage(
                Component.literal("§a✓ NPC mit Warehouse verknüpft!")
                    .append(Component.literal("\n§7NPC: §e" + npc.getNpcName()))
                    .append(Component.literal("\n§7Warehouse: §f" + msg.pos.toShortString()))
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
