package de.rolandsw.schedulemc.warehouse.network.packet;

import de.rolandsw.schedulemc.managers.NPCEntityRegistry;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
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

    /**
     * SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
     */
    public static AddItemToSlotPacket decode(FriendlyByteBuf buffer) {
        return new AddItemToSlotPacket(
            buffer.readBlockPos(),
            buffer.readUtf(256) // ResourceLocation max 256 chars
        );
    }

    public static void handle(AddItemToSlotPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleAdminPacket(ctx, 2, player -> {
            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) {
                PacketHandler.sendError(player, "Warehouse nicht gefunden!");
                return;
            }

            // Parse item ID
            ResourceLocation itemLoc = ResourceLocation.parse(msg.itemId);
            Item item = BuiltInRegistries.ITEM.get(itemLoc);

            if (item == null || item == net.minecraft.world.item.Items.AIR) {
                PacketHandler.sendError(player, "Ungültiges Item: " + msg.itemId);
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
                PacketHandler.sendError(player, "Kein leerer Slot verfügbar!");
                return;
            }

            // Setze das Item im Slot (mit 0 Stock, nur um das allowed item zu setzen)
            slots[emptySlotIndex].addStock(item, 0);
            warehouse.setChanged();

            // WICHTIG: Füge Item auch zum NPC-Shop hinzu
            boolean addedToShop = addItemToLinkedNPCShops(warehouse, item, player.serverLevel());

            warehouse.syncToClient();

            if (addedToShop) {
                player.sendSystemMessage(
                    Component.translatable("message.warehouse.item_added")
                        .append(Component.literal("\n§7Item: §e" + item.getDescription().getString()))
                        .append(Component.literal("\n§7Slot: §e#" + emptySlotIndex))
                        .append(Component.literal("\n§7Status: §eLager (Stock-basiert)"))
                );
            } else {
                PacketHandler.sendWarning(player, "Item zum Warehouse hinzugefügt, aber kein NPC-Shop aktualisiert!");
            }
        });
    }

    /**
     * Fügt das Item zu allen verknüpften NPC-Shops hinzu
     * @return true wenn mindestens ein Shop aktualisiert wurde
     */
    private static boolean addItemToLinkedNPCShops(WarehouseBlockEntity warehouse, Item item, ServerLevel level) {
        List<UUID> sellers = warehouse.getLinkedSellers();
        if (sellers.isEmpty()) {
            return false;
        }

        boolean updated = false;
        int defaultPrice = 100; // Standard-Preis für neue Items

        for (UUID sellerId : sellers) {
            // Performance-Optimierung: O(1) UUID Lookup statt O(n) getAllEntities() Iteration
            CustomNPCEntity npc = NPCEntityRegistry.getNPCByUUID(sellerId, level);

            if (npc != null) {
                NPCData.ShopInventory shop = npc.getNpcData().getBuyShop();

                // Prüfe ob Item bereits im Shop ist
                boolean itemExists = false;
                for (NPCData.ShopEntry entry : shop.getEntries()) {
                    if (!entry.getItem().isEmpty() && entry.getItem().getItem() == item) {
                        itemExists = true;
                        break;
                    }
                }

                // Füge Item hinzu wenn nicht vorhanden
                if (!itemExists) {
                    // WICHTIG: unlimited=false damit es vom Warehouse-Stock abhängt!
                    shop.addEntry(new NPCData.ShopEntry(
                        new ItemStack(item, 1),
                        defaultPrice,
                        false, // unlimited=false -> Lager-Item!
                        0      // stock=0 (wird vom Warehouse verwaltet)
                    ));
                    updated = true;
                }
            }
        }

        return updated;
    }
}
