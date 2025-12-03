package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Packet zum Aktualisieren der Shop-Items eines Verkäufer-NPCs
 * Nur für Admins!
 */
public class UpdateShopItemsPacket {
    private final int merchantEntityId;
    private final List<ItemStack> items;
    private final List<Integer> prices;
    private final List<Boolean> unlimited;
    private final List<Integer> stock;

    public UpdateShopItemsPacket(int merchantEntityId, List<ItemStack> items, List<Integer> prices,
                                  List<Boolean> unlimited, List<Integer> stock) {
        this.merchantEntityId = merchantEntityId;
        this.items = items;
        this.prices = prices;
        this.unlimited = unlimited;
        this.stock = stock;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(merchantEntityId);
        buf.writeInt(items.size());
        for (int i = 0; i < items.size(); i++) {
            buf.writeItem(items.get(i));
            buf.writeInt(prices.get(i));
            buf.writeBoolean(unlimited.get(i));
            buf.writeInt(stock.get(i));
        }
    }

    public static UpdateShopItemsPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        int count = buf.readInt();
        List<ItemStack> items = new ArrayList<>();
        List<Integer> prices = new ArrayList<>();
        List<Boolean> unlimited = new ArrayList<>();
        List<Integer> stock = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            items.add(buf.readItem());
            prices.add(buf.readInt());
            unlimited.add(buf.readBoolean());
            stock.add(buf.readInt());
        }

        return new UpdateShopItemsPacket(entityId, items, prices, unlimited, stock);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.hasPermissions(2)) { // Admin-Check!
                Entity entity = player.level().getEntity(merchantEntityId);
                if (entity instanceof CustomNPCEntity npc) {
                    // Lösche alte Shop-Items
                    npc.getNpcData().getBuyShop().clear();

                    // Füge neue Items hinzu
                    for (int i = 0; i < items.size(); i++) {
                        ItemStack item = items.get(i);
                        int price = prices.get(i);
                        boolean isUnlimited = unlimited.get(i);
                        int itemStock = stock.get(i);
                        if (!item.isEmpty() && price > 0) {
                            npc.getNpcData().getBuyShop().addEntry(
                                new NPCData.ShopEntry(item, price, isUnlimited, itemStock));
                        }
                    }

                    // NPC-Daten werden automatisch über NBT persistiert

                    // WAREHOUSE SYNCHRONISATION
                    syncShopToWarehouse(npc, player);

                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§aShop erfolgreich aktualisiert! " + items.size() + " Items hinzugefügt."));
                }
            } else {
                if (player != null) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§cFehler: Keine Berechtigung!"));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Synchronisiert die Shop-Items des NPCs mit dem verknüpften Warehouse
     */
    private static void syncShopToWarehouse(CustomNPCEntity npc, ServerPlayer player) {
        BlockPos warehousePos = npc.getNpcData().getAssignedWarehouse();
        if (warehousePos == null) {
            return; // Kein Warehouse verknüpft
        }

        BlockEntity be = player.level().getBlockEntity(warehousePos);
        if (!(be instanceof WarehouseBlockEntity warehouse)) {
            return; // Warehouse nicht gefunden
        }

        // Hole Shop-Items
        List<NPCData.ShopEntry> shopEntries = npc.getNpcData().getBuyShop().getEntries();
        List<Item> shopItems = new ArrayList<>();
        for (NPCData.ShopEntry entry : shopEntries) {
            if (!entry.getItem().isEmpty()) {
                shopItems.add(entry.getItem().getItem());
            }
        }

        // Warehouse-Slots durchgehen
        WarehouseSlot[] slots = warehouse.getSlots();

        // 1. Entferne Items aus dem Warehouse, die nicht mehr im Shop sind
        for (int i = 0; i < slots.length; i++) {
            WarehouseSlot slot = slots[i];
            if (!slot.isEmpty()) {
                Item slotItem = slot.getAllowedItem();
                if (!shopItems.contains(slotItem)) {
                    // Item nicht mehr im Shop -> entfernen
                    slot.clear();
                }
            }
        }

        // 2. Füge neue Shop-Items zum Warehouse hinzu (mit Stock 0)
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
                        break;
                    }
                }
            }
        }

        // Synchronisiere Warehouse zum Client
        warehouse.setChanged();
        warehouse.syncToClient();

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§aWarehouse synchronisiert: " + shopItems.size() + " Items."));
    }
}
