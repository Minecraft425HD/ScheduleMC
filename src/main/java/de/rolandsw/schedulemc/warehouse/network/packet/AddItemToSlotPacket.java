package de.rolandsw.schedulemc.warehouse.network.packet;

import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

            // WICHTIG: Füge Item auch zum NPC-Shop hinzu
            boolean addedToShop = addItemToLinkedNPCShops(warehouse, item, player.serverLevel());

            warehouse.syncToClient();

            if (addedToShop) {
                player.sendSystemMessage(
                    Component.literal("§a✓ Item hinzugefügt!")
                        .append(Component.literal("\n§7Item: §e" + item.getDescription().getString()))
                        .append(Component.literal("\n§7Slot: §e#" + emptySlotIndex))
                        .append(Component.literal("\n§7Status: §eLager (Stock-basiert)"))
                );
            } else {
                player.sendSystemMessage(
                    Component.literal("§a✓ Item zum Warehouse hinzugefügt!")
                        .append(Component.literal("\n§7Item: §e" + item.getDescription().getString()))
                        .append(Component.literal("\n§7Slot: §e#" + emptySlotIndex))
                        .append(Component.literal("\n§cWarnung: Kein NPC-Shop aktualisiert!"))
                );
            }
        });
        ctx.get().setPacketHandled(true);
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
            // Finde den NPC
            CustomNPCEntity npc = null;
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof CustomNPCEntity customNpc) {
                    if (customNpc.getUUID().equals(sellerId)) {
                        npc = customNpc;
                        break;
                    }
                }
            }

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
