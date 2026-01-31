package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        List<ItemStack> items = new ArrayList<>(count);
        List<Integer> prices = new ArrayList<>(count);
        List<Boolean> unlimited = new ArrayList<>(count);
        List<Integer> stock = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            items.add(buf.readItem());
            prices.add(buf.readInt());
            unlimited.add(buf.readBoolean());
            stock.add(buf.readInt());
        }

        return new UpdateShopItemsPacket(entityId, items, prices, unlimited, stock);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleAdminPacket(ctx, 2, player -> {
            Entity entity = player.level().getEntity(merchantEntityId);
            if (entity instanceof CustomNPCEntity npc) {
                // Null-Safety: Prüfe ob NPC-Daten und Shop vorhanden sind
                if (npc.getNpcData() == null || npc.getNpcData().getBuyShop() == null) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§cFehler: NPC-Daten nicht verfügbar!"));
                    return;
                }

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

                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "network.shop_update.success",
                    String.valueOf(items.size())
                ));
            }
        });
    }

    /**
     * Synchronisiert die Shop-Items des NPCs mit dem verknüpften Warehouse
     * WICHTIG: Nur Lager-Items (unlimited=false) werden ins Warehouse übernommen!
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

        // Hole Shop-Items mit unlimited Status
        // OPTIMIERUNG: HashSet für O(1) contains statt O(n) mit List
        List<NPCData.ShopEntry> shopEntries = npc.getNpcData().getBuyShop().getEntries();
        Set<Item> allShopItems = new HashSet<>();
        Set<Item> unlimitedItems = new HashSet<>();

        for (NPCData.ShopEntry entry : shopEntries) {
            if (!entry.getItem().isEmpty()) {
                Item item = entry.getItem().getItem();
                allShopItems.add(item);
                if (entry.isUnlimited()) {
                    unlimitedItems.add(item);
                }
            }
        }

        // Warehouse-Slots durchgehen
        WarehouseSlot[] slots = warehouse.getSlots();

        // 1. Entferne Items die nicht mehr im Shop sind (O(1) lookup dank HashSet)
        for (int i = 0; i < slots.length; i++) {
            WarehouseSlot slot = slots[i];
            if (!slot.isEmpty()) {
                Item slotItem = slot.getAllowedItem();
                if (!allShopItems.contains(slotItem)) {
                    slot.clear(); // Item nicht mehr im Shop
                }
            }
        }

        // 2. Synchronisiere alle Shop-Items mit Warehouse
        // OPTIMIERUNG: HashMap für O(1) Slot-Lookup statt O(n) pro Item
        Map<Item, WarehouseSlot> slotByItem = new HashMap<>();
        List<WarehouseSlot> emptySlots = new ArrayList<>();

        for (WarehouseSlot slot : slots) {
            if (!slot.isEmpty()) {
                slotByItem.put(slot.getAllowedItem(), slot);
            } else {
                emptySlots.add(slot);
            }
        }

        int itemsAdded = 0;
        int emptySlotIndex = 0;

        for (NPCData.ShopEntry entry : shopEntries) {
            if (!entry.getItem().isEmpty()) {
                Item item = entry.getItem().getItem();
                boolean isUnlimited = entry.isUnlimited();

                // O(1) Slot-Lookup dank HashMap
                WarehouseSlot targetSlot = slotByItem.get(item);

                if (targetSlot != null) {
                    // Item existiert - aktualisiere unlimited Flag
                    targetSlot.setUnlimited(isUnlimited);
                } else if (!isUnlimited && emptySlotIndex < emptySlots.size()) {
                    // Nur Lager-Items werden neu hinzugefügt
                    WarehouseSlot emptySlot = emptySlots.get(emptySlotIndex++);
                    emptySlot.addStock(item, 0);
                    emptySlot.setUnlimited(false);
                    slotByItem.put(item, emptySlot); // Für spätere Lookups
                    itemsAdded++;
                }
            }
        }

        // Synchronisiere Warehouse zum Client
        warehouse.setChanged();
        warehouse.syncToClient();

        int stockItemCount = allShopItems.size() - unlimitedItems.size();
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§aWarehouse synchronisiert: " + stockItemCount + " Lager-Items, " +
            unlimitedItems.size() + " unlimited Items."));
    }
}
