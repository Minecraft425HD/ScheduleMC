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
 * Packet zum automatischen Auffüllen aller Slots auf Maximum
 */
public class AutoFillPacket {

    private final BlockPos pos;

    public AutoFillPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(AutoFillPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
    }

    public static AutoFillPacket decode(FriendlyByteBuf buffer) {
        return new AutoFillPacket(buffer.readBlockPos());
    }

    public static void handle(AutoFillPacket msg, Supplier<NetworkEvent.Context> ctx) {
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

            int totalAdded = 0;
            int slotsFilled = 0;

            for (WarehouseSlot slot : warehouse.getSlots()) {
                if (slot.isEmpty() || slot.getAllowedItem() == null) continue;

                int restockAmount = slot.getRestockAmount();
                if (restockAmount > 0) {
                    int added = slot.addStock(slot.getAllowedItem(), restockAmount);
                    totalAdded += added;
                    if (added > 0) slotsFilled++;
                }
            }

            warehouse.setChanged();

            player.sendSystemMessage(Component.literal(
                "§aAuto-Fill abgeschlossen: " + totalAdded + " Items in " + slotsFilled + " Slots aufgefüllt"
            ));
        });
        ctx.get().setPacketHandled(true);
    }
}
