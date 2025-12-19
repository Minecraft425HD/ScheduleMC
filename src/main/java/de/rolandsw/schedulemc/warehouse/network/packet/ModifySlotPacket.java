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
 * Packet zum Hinzufügen/Entfernen von Items in einem Slot
 */
public class ModifySlotPacket {

    private final BlockPos pos;
    private final int slotIndex;
    private final int amount; // Positive = Hinzufügen, Negative = Entfernen

    public ModifySlotPacket(BlockPos pos, int slotIndex, int amount) {
        this.pos = pos;
        this.slotIndex = slotIndex;
        this.amount = amount;
    }

    public static void encode(ModifySlotPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeInt(msg.slotIndex);
        buffer.writeInt(msg.amount);
    }

    public static ModifySlotPacket decode(FriendlyByteBuf buffer) {
        return new ModifySlotPacket(
            buffer.readBlockPos(),
            buffer.readInt(),
            buffer.readInt()
        );
    }

    public static void handle(ModifySlotPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleAdminPacket(ctx, 2, player -> {
            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) return;

            WarehouseSlot[] slots = warehouse.getSlots();
            if (msg.slotIndex < 0 || msg.slotIndex >= slots.length) return;

            WarehouseSlot slot = slots[msg.slotIndex];
            if (slot.isEmpty() || slot.getAllowedItem() == null) {
                player.sendSystemMessage(Component.literal("§cSlot ist leer!"));
                return;
            }

            if (msg.amount > 0) {
                // Hinzufügen - verwende warehouse.addItem() für korrekte Synchronisation
                int added = warehouse.addItem(slot.getAllowedItem(), msg.amount);
                player.sendSystemMessage(Component.literal(
                    "§a" + added + "x " + slot.getAllowedItem().getDescription().getString() + " hinzugefügt"
                ));
            } else if (msg.amount < 0) {
                // Entfernen - verwende warehouse.removeItem() für korrekte Synchronisation
                int removed = warehouse.removeItem(slot.getAllowedItem(), -msg.amount);
                player.sendSystemMessage(Component.literal(
                    "§a" + removed + "x " + slot.getAllowedItem().getDescription().getString() + " entfernt"
                ));
            }
        });
    }
}
