package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Packet für Item-Kauf von Verkäufer-NPCs
 */
public class PurchaseItemPacket {
    private final int merchantEntityId;
    private final int itemIndex;
    private final int quantity;

    public PurchaseItemPacket(int merchantEntityId, int itemIndex, int quantity) {
        this.merchantEntityId = merchantEntityId;
        this.itemIndex = itemIndex;
        this.quantity = quantity;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(merchantEntityId);
        buf.writeInt(itemIndex);
        buf.writeInt(quantity);
    }

    public static PurchaseItemPacket decode(FriendlyByteBuf buf) {
        return new PurchaseItemPacket(
            buf.readInt(),
            buf.readInt(),
            buf.readInt()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(merchantEntityId);
                if (entity instanceof CustomNPCEntity merchant) {
                    processPurchase(player, merchant, itemIndex, quantity);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Verarbeitet den Kauf
     */
    private void processPurchase(ServerPlayer player, CustomNPCEntity merchant, int itemIndex, int quantity) {
        List<NPCData.ShopEntry> shopItems = merchant.getNpcData().getBuyShop().getEntries();

        if (itemIndex < 0 || itemIndex >= shopItems.size()) {
            player.sendSystemMessage(Component.literal("§cUngültiges Item!"));
            return;
        }

        NPCData.ShopEntry entry = shopItems.get(itemIndex);
        int totalPrice = entry.getPrice() * quantity;

        // Prüfe Lagerbestand (nutze Warehouse-Integration)
        if (!merchant.getNpcData().canSellItemFromWarehouse(player.level(), entry, quantity)) {
            // Hole verfügbare Menge für Fehlermeldung
            int available;
            if (entry.isUnlimited()) {
                available = Integer.MAX_VALUE;
            } else if (merchant.getNpcData().hasWarehouse()) {
                var warehouse = merchant.getNpcData().getWarehouseEntity(player.level());
                available = warehouse != null ? warehouse.getStock(entry.getItem().getItem()) : entry.getStock();
            } else {
                available = entry.getStock();
            }
            player.sendSystemMessage(Component.literal("§cNicht genug auf Lager! Verfügbar: " + available));
            return;
        }

        // Prüfe ob Spieler genug Geld hat
        double playerBalance = EconomyManager.getBalance(player.getUUID());
        if (playerBalance < totalPrice) {
            player.sendSystemMessage(Component.literal("§cNicht genug Geld! Du brauchst " + totalPrice + "$"));
            return;
        }

        // Prüfe ob Spieler genug Platz im Inventar hat
        ItemStack itemToGive = entry.getItem().copy();
        itemToGive.setCount(quantity);

        if (!canAddItemToInventory(player, itemToGive)) {
            player.sendSystemMessage(Component.literal("§cNicht genug Platz im Inventar!"));
            return;
        }

        // Transaktion durchführen
        if (EconomyManager.withdraw(player.getUUID(), totalPrice)) {
            player.getInventory().add(itemToGive);

            // Reduziere Lagerbestand (nutze Warehouse-Integration)
            merchant.getNpcData().onItemSoldFromWarehouse(player.level(), entry, quantity, totalPrice);

            player.sendSystemMessage(Component.literal("§aGekauft: " + quantity + "x " +
                entry.getItem().getHoverName().getString() + " für " + totalPrice + "$"));
        } else {
            player.sendSystemMessage(Component.literal("§cFehler beim Abbuchung! Kauf abgebrochen."));
        }
    }

    /**
     * Prüft ob ein Item zum Inventar hinzugefügt werden kann
     */
    private boolean canAddItemToInventory(ServerPlayer player, ItemStack stack) {
        // Vereinfachte Prüfung - kann später erweitert werden
        int emptySlots = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack slotStack = player.getInventory().getItem(i);
            if (slotStack.isEmpty()) {
                emptySlots++;
            } else if (ItemStack.isSameItemSameTags(slotStack, stack)) {
                int spaceInSlot = slotStack.getMaxStackSize() - slotStack.getCount();
                if (spaceInSlot > 0) {
                    return true;
                }
            }
        }
        return emptySlots > 0;
    }
}
