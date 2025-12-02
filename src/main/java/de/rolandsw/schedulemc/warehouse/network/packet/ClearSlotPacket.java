package de.rolandsw.schedulemc.warehouse.network.packet;

import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum Leeren eines Slots
 */
public class ClearSlotPacket {

    private final BlockPos pos;
    private final int slotIndex;

    public ClearSlotPacket(BlockPos pos, int slotIndex) {
        this.pos = pos;
        this.slotIndex = slotIndex;
    }

    public static void encode(ClearSlotPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeInt(msg.slotIndex);
    }

    public static ClearSlotPacket decode(FriendlyByteBuf buffer) {
        return new ClearSlotPacket(
            buffer.readBlockPos(),
            buffer.readInt()
        );
    }

    public static void handle(ClearSlotPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Admin check
            if (!player.hasPermissions(2)) {
                player.sendSystemMessage(Component.literal("§cNur Admins können das Warehouse bearbeiten!"));
                return;
            }

            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) return;

            WarehouseSlot[] slots = warehouse.getSlots();
            if (msg.slotIndex < 0 || msg.slotIndex >= slots.length) return;

            WarehouseSlot slot = slots[msg.slotIndex];
            String itemName = slot.getAllowedItem() != null ?
                slot.getAllowedItem().getDescription().getString() : "Unknown";

            slot.clear();
            warehouse.setChanged();
            warehouse.syncToClient(); // Synchronisiere zum Client für GUI-Update

            player.sendSystemMessage(Component.literal("§aSlot geleert: " + itemName));
        });
        ctx.get().setPacketHandled(true);
    }
}
