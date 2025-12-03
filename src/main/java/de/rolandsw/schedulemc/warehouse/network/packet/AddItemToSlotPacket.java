package de.rolandsw.schedulemc.warehouse.network.packet;

import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum Hinzufügen eines Items zu einem leeren Warehouse-Slot
 */
public class AddItemToSlotPacket {

    private final BlockPos pos;
    private final String itemId;

    public AddItemToSlotPacket(BlockPos pos, String itemId) {
        this.pos = pos;
        this.itemId = itemId;
    }

    public static void encode(AddItemToSlotPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeUtf(msg.itemId);
    }

    public static AddItemToSlotPacket decode(FriendlyByteBuf buffer) {
        return new AddItemToSlotPacket(
            buffer.readBlockPos(),
            buffer.readUtf()
        );
    }

    public static void handle(AddItemToSlotPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Admin check
            if (!player.hasPermissions(2)) {
                player.sendSystemMessage(Component.literal("§cNur Admins können Items hinzufügen!"));
                return;
            }

            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) {
                player.sendSystemMessage(Component.literal("§cWarehouse nicht gefunden!"));
                return;
            }

            // Parse item ID
            ResourceLocation itemLoc = new ResourceLocation(msg.itemId);
            Item item = BuiltInRegistries.ITEM.get(itemLoc);

            if (item == null || item == net.minecraft.world.item.Items.AIR) {
                player.sendSystemMessage(Component.literal("§cUngültiges Item: " + msg.itemId));
                return;
            }

            // Finde einen leeren Slot
            WarehouseSlot[] slots = warehouse.getSlots();
            int emptySlotIndex = -1;

            for (int i = 0; i < slots.length; i++) {
                if (slots[i].isEmpty()) {
                    emptySlotIndex = i;
                    break;
                }
            }

            if (emptySlotIndex == -1) {
                player.sendSystemMessage(Component.literal("§cKein leerer Slot verfügbar!"));
                return;
            }

            // Setze das Item im Slot (mit 0 Stock, nur um das allowed item zu setzen)
            slots[emptySlotIndex].addStock(item, 0);
            warehouse.setChanged();
            warehouse.syncToClient();

            player.sendSystemMessage(
                Component.literal("§a✓ Item hinzugefügt!")
                    .append(Component.literal("\n§7Item: §e" + item.getDescription().getString()))
                    .append(Component.literal("\n§7Slot: §e#" + emptySlotIndex))
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
