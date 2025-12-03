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

        // Erstelle Kopie der Entries
        List<NPCData.ShopEntry> entriesToProcess = new ArrayList<>(originalEntries);

        WarehouseSlot[] slots = warehouse.getSlots();
        int itemsAdded = 0;

        // Verarbeite jedes Item - synchronisiere mit Warehouse
        for (NPCData.ShopEntry entry : entriesToProcess) {
            if (!entry.getItem().isEmpty()) {
                Item item = entry.getItem().getItem();
                boolean isUnlimited = entry.isUnlimited();

                // Finde oder erstelle Slot für dieses Item
                WarehouseSlot targetSlot = null;
                for (WarehouseSlot slot : slots) {
                    if (!slot.isEmpty() && slot.getAllowedItem() == item) {
                        targetSlot = slot;
                        break;
                    }
                }

                // Wenn Item unlimited ist, entferne es aus Warehouse oder setze Flag
                if (isUnlimited) {
                    if (targetSlot != null) {
                        // Item existiert im Warehouse - markiere als unlimited
                        targetSlot.setUnlimited(true);
                    }
                    // Unlimited Items werden NICHT zum Warehouse hinzugefügt wenn sie nicht existieren
                } else {
                    // Lager-Item
                    if (targetSlot != null) {
                        // Aktualisiere Flag
                        targetSlot.setUnlimited(false);
                    } else {
                        // Füge neues Lager-Item hinzu
                        for (WarehouseSlot slot : slots) {
                            if (slot.isEmpty()) {
                                slot.addStock(item, 0);
                                slot.setUnlimited(false);
                                itemsAdded++;
                                break;
                            }
                        }
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
