package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager;
import de.rolandsw.schedulemc.vehicle.fuel.GasStationRegistry;
import de.rolandsw.schedulemc.vehicle.items.VehicleSpawnTool;
import de.rolandsw.schedulemc.vehicle.purchase.VehiclePurchaseHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        // WICHTIG: Für Tankstellen müssen wir die Bill-Items auch hier hinzufügen, damit die Indizes stimmen!
        List<NPCData.ShopEntry> shopItems = new ArrayList<>(merchant.getNpcData().getBuyShop().getEntries());

        // Spezialbehandlung für Tankstelle: Füge unbezahlte Rechnungen hinzu (wie in OpenMerchantShopPacket)
        if (merchant.getMerchantCategory() == MerchantCategory.TANKSTELLE) {
            List<NPCData.ShopEntry> billEntries = createBillEntries(player);
            shopItems.addAll(0, billEntries); // Am Anfang einfügen - GLEICHE LOGIK WIE BEIM ÖFFNEN!
        }

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

        // Spezialbehandlung für Tankrechnungen (Tankstelle)
        if (merchant.getMerchantCategory() == MerchantCategory.TANKSTELLE &&
            entry.getItem().hasTag()) {

            String billType = entry.getItem().getTag().getString("BillType");

            // Prüfe ob es "Keine Rechnungen" ist
            if ("NoBill".equals(billType)) {
                player.sendSystemMessage(Component.literal("✓ Sie haben keine offenen Rechnungen!")
                    .withStyle(ChatFormatting.GREEN));
                return;
            }

            // Prüfe ob es eine echte Rechnung ist
            if ("FuelBill".equals(billType)) {
                // Rechnung bezahlen
                processFuelBillPayment(player, merchant, entry, totalPrice);
                return;
            }
        }

        // Spezialbehandlung für Fahrzeuge (Autohändler)
        if (merchant.getMerchantCategory() == MerchantCategory.AUTOHAENDLER &&
            entry.getItem().getItem() instanceof VehicleSpawnTool) {

            // Handle vehicle purchase
            if (VehiclePurchaseHandler.purchaseVehicle(player, merchant.getUUID(), entry.getItem(), totalPrice)) {
                // Update warehouse/shop accounting
                merchant.getNpcData().onItemSoldFromWarehouse(player.level(), entry, quantity, totalPrice);
            }
            return;
        }

        // Normale Items: Prüfe ob Spieler genug Platz im Inventar hat
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

    /**
     * Verarbeitet die Bezahlung einer Tankrechnung
     */
    private void processFuelBillPayment(ServerPlayer player, CustomNPCEntity merchant, NPCData.ShopEntry entry, int price) {
        CompoundTag tag = entry.getItem().getTag();
        if (tag == null || !tag.contains("GasStationId")) {
            player.sendSystemMessage(Component.literal("§cUngültige Rechnung!"));
            return;
        }

        UUID gasStationId = tag.getUUID("GasStationId");
        String stationName = GasStationRegistry.getDisplayName(gasStationId);

        // Check if player has enough money
        double balance = EconomyManager.getBalance(player.getUUID());
        if (balance < price) {
            player.sendSystemMessage(Component.literal("§cNicht genug Geld! Benötigt: " + price + "€"));
            return;
        }

        // Withdraw money
        if (!EconomyManager.withdraw(player.getUUID(), price)) {
            player.sendSystemMessage(Component.literal("§cFehler beim Abbuchung!"));
            return;
        }

        // Mark bills as paid
        FuelBillManager.payBills(player.getUUID(), gasStationId);

        // Send success message
        player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("✓ ").withStyle(ChatFormatting.GREEN)
            .append(Component.literal("RECHNUNG BEZAHLT").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        player.sendSystemMessage(Component.literal("Tankstelle: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(stationName).withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.literal("Betrag: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(price + "€").withStyle(ChatFormatting.GOLD)));
        player.sendSystemMessage(Component.literal("Restguthaben: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID()))).withStyle(ChatFormatting.YELLOW)));
        player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));
    }

    /**
     * Erstellt Shop-Einträge für unbezahlte Rechnungen (kopiert von OpenMerchantShopPacket)
     */
    private List<NPCData.ShopEntry> createBillEntries(ServerPlayer player) {
        List<NPCData.ShopEntry> billEntries = new ArrayList<>();

        // Get all unpaid bills for this player
        List<FuelBillManager.UnpaidBill> unpaidBills = FuelBillManager.getUnpaidBills(player.getUUID());

        if (unpaidBills.isEmpty()) {
            // Show "Keine Rechnungen" entry
            ItemStack noBillItem = new ItemStack(Items.PAPER);
            CompoundTag tag = noBillItem.getOrCreateTag();
            tag.putString("BillType", "NoBill");
            noBillItem.setHoverName(Component.literal("✓ Keine offenen Rechnungen")
                .withStyle(ChatFormatting.GREEN));
            billEntries.add(new NPCData.ShopEntry(noBillItem, 0, true, Integer.MAX_VALUE));
        } else {
            // Group bills by gas station
            Map<UUID, List<FuelBillManager.UnpaidBill>> billsByStation = new HashMap<>();
            for (FuelBillManager.UnpaidBill bill : unpaidBills) {
                billsByStation.computeIfAbsent(bill.gasStationId, k -> new ArrayList<>()).add(bill);
            }

            // Create an entry for each gas station
            for (Map.Entry<UUID, List<FuelBillManager.UnpaidBill>> entry : billsByStation.entrySet()) {
                UUID stationId = entry.getKey();
                List<FuelBillManager.UnpaidBill> stationBills = entry.getValue();

                // Calculate total amount for this station
                double totalAmount = stationBills.stream()
                    .mapToDouble(bill -> bill.totalCost)
                    .sum();

                // Get station name
                String stationName = GasStationRegistry.getDisplayName(stationId);

                // Create bill item
                ItemStack billItem = new ItemStack(Items.PAPER);
                CompoundTag tag = billItem.getOrCreateTag();
                tag.putString("BillType", "FuelBill");
                tag.putUUID("GasStationId", stationId);
                billItem.setHoverName(Component.literal("Tankrechnung - " + stationName)
                    .withStyle(ChatFormatting.YELLOW));

                billEntries.add(new NPCData.ShopEntry(billItem, (int) Math.ceil(totalAmount), true, Integer.MAX_VALUE));
            }
        }

        return billEntries;
    }
}
