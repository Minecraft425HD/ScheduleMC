package de.rolandsw.schedulemc.warehouse.network.packet;

import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum Aktualisieren der maximalen Kapazität eines Slots
 */
public class UpdateSlotCapacityPacket {

    private final BlockPos pos;
    private final int slotIndex;
    private final int newCapacity;

    public UpdateSlotCapacityPacket(BlockPos pos, int slotIndex, int newCapacity) {
        this.pos = pos;
        this.slotIndex = slotIndex;
        this.newCapacity = newCapacity;
    }

    public static void encode(UpdateSlotCapacityPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeInt(msg.slotIndex);
        buffer.writeInt(msg.newCapacity);
    }

    public static UpdateSlotCapacityPacket decode(FriendlyByteBuf buffer) {
        return new UpdateSlotCapacityPacket(
            buffer.readBlockPos(),
            buffer.readInt(),
            buffer.readInt()
        );
    }

    public static void handle(UpdateSlotCapacityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleAdminPacket(ctx, 2, player -> {
            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) return;

            WarehouseSlot[] slots = warehouse.getSlots();
            if (msg.slotIndex < 0 || msg.slotIndex >= slots.length) return;

            WarehouseSlot slot = slots[msg.slotIndex];
            if (slot.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.warehouse.slot_empty"));
                return;
            }

            // Aktualisiere die maximale Kapazität
            slot.setMaxCapacity(msg.newCapacity);
            warehouse.setChanged();
            warehouse.syncToClient(); // Synchronisiere zum Client für GUI-Update

            player.sendSystemMessage(Component.translatable("message.warehouse.capacity_updated", msg.newCapacity));
        });
    }
}
