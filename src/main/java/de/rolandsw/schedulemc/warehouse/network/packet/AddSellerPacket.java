package de.rolandsw.schedulemc.warehouse.network.packet;

import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
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

            // Finde den NPC - durchsuche alle Entities in der Welt
            CustomNPCEntity npc = null;
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof CustomNPCEntity customNpc) {
                    if (customNpc.getUUID().equals(msg.sellerId)) {
                        npc = customNpc;
                        break;
                    }
                }
            }

            if (npc == null) {
                player.sendSystemMessage(Component.literal("§cNPC nicht gefunden! (UUID: " + msg.sellerId + ")"));
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

            // WICHTIG: Synchronisiere Shop-Items zum Warehouse
            int itemsAdded = syncNPCShopToWarehouse(npc, warehouse);

            warehouse.syncToClient(); // Wichtig: Synchronisiere zum Client damit GUI sich aktualisiert

            player.sendSystemMessage(
                Component.literal("§a✓ NPC mit Warehouse verknüpft!")
                    .append(Component.literal("\n§7NPC: §e" + npc.getNpcName()))
                    .append(Component.literal("\n§7Warehouse: §f" + msg.pos.toShortString()))
                    .append(Component.literal("\n§7Shop-Items hinzugefügt: §e" + itemsAdded))
            );
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Synchronisiert die Shop-Items des NPCs zum Warehouse
     * @return Anzahl der hinzugefügten Items
     */
    private static int syncNPCShopToWarehouse(CustomNPCEntity npc, WarehouseBlockEntity warehouse) {
        // Hole Shop-Items des NPCs
        NPCData.ShopInventory shop = npc.getNpcData().getBuyShop();
        List<NPCData.ShopEntry> originalEntries = shop.getEntries();
        if (originalEntries.isEmpty()) {
            return 0;
        }

        // WICHTIG: Konvertiere ALLE Shop-Items zu Lager-Items (unlimited=false)
        // Erstelle Kopie der Entries, dann baue Shop neu auf
        List<NPCData.ShopEntry> entriesToConvert = new ArrayList<>(originalEntries);
        shop.clear();

        List<Item> shopItems = new ArrayList<>();

        for (NPCData.ShopEntry entry : entriesToConvert) {
            if (!entry.getItem().isEmpty()) {
                shopItems.add(entry.getItem().getItem());

                // Füge Item als Lager-Item wieder hinzu
                shop.addEntry(new NPCData.ShopEntry(
                    entry.getItem(),
                    entry.getPrice(),
                    false, // unlimited=false -> Lager-Item!
                    0      // stock=0 (wird vom Warehouse verwaltet)
                ));
            }
        }

        WarehouseSlot[] slots = warehouse.getSlots();
        int itemsAdded = 0;

        // Füge alle Shop-Items zum Warehouse hinzu (mit Stock 0)
        for (Item shopItem : shopItems) {
            boolean existsInWarehouse = false;

            // Prüfe ob Item schon im Warehouse ist
            for (WarehouseSlot slot : slots) {
                if (!slot.isEmpty() && slot.getAllowedItem() == shopItem) {
                    existsInWarehouse = true;
                    break;
                }
            }

            // Wenn nicht vorhanden: Füge zu einem leeren Slot hinzu
            if (!existsInWarehouse) {
                for (WarehouseSlot slot : slots) {
                    if (slot.isEmpty()) {
                        slot.addStock(shopItem, 0); // Füge mit 0 Stock hinzu
                        itemsAdded++;
                        break;
                    }
                }
            }
        }

        if (itemsAdded > 0) {
            warehouse.setChanged();
        }

        return itemsAdded;
    }
}
