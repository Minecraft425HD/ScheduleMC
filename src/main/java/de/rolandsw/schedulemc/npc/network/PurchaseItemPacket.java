package de.rolandsw.schedulemc.npc.network;
nimport de.rolandsw.schedulemc.util.StringUtils;
nimport de.rolandsw.schedulemc.util.GameConstants;

import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.util.RateLimiter;
import de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager;
import de.rolandsw.schedulemc.vehicle.fuel.FuelStationRegistry;
import de.rolandsw.schedulemc.vehicle.items.ItemSpawnVehicle;
import de.rolandsw.schedulemc.vehicle.vehicle.VehiclePurchaseHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
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
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet für Item-Kauf von Verkäufer-NPCs
 *
 * SICHERHEIT: Rate Limiting gegen DoS/Spam-Angriffe
 */
public class PurchaseItemPacket {

    // Rate Limiting Constants
    private static final int PURCHASE_MAX_OPS_PER_SECOND = 20;
    private static final int PURCHASE_WINDOW_MS = 1000;

    // Purchase Configuration
    private static final int MAX_PURCHASE_QUANTITY = 10000;

    // SICHERHEIT: Rate Limiter - Max 20 Käufe pro Sekunde (verhindert Spam/Exploits)
    private static final RateLimiter PURCHASE_RATE_LIMITER = new RateLimiter("purchase", PURCHASE_MAX_OPS_PER_SECOND, PURCHASE_WINDOW_MS);
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
        PacketHandler.handleServerPacket(ctx, player -> {
            // SICHERHEIT: Rate Limiting - verhindere Spam/DoS-Angriffe
            if (!PURCHASE_RATE_LIMITER.allowOperation(player.getUUID())) {
                player.sendSystemMessage(Component.literal("⚠ Zu viele Kaufversuche! Bitte langsamer.")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            Entity entity = player.level().getEntity(merchantEntityId);
            if (entity instanceof CustomNPCEntity merchant) {
                processPurchase(player, merchant, itemIndex, quantity);
            }
        });
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

        // SICHERHEIT: Integer Overflow Prevention
        // Maximale Menge pro Transaktion begrenzen
        int safeQuantity = Math.min(quantity, MAX_PURCHASE_QUANTITY);
        if (quantity > MAX_PURCHASE_QUANTITY) {
            player.sendSystemMessage(Component.literal("§cMaximale Kaufmenge ist " + MAX_PURCHASE_QUANTITY + " pro Transaktion!"));
            return;
        }

        // SICHERHEIT: Berechne mit long um Overflow zu erkennen
        long totalPriceLong = (long) entry.getPrice() * safeQuantity;
        if (totalPriceLong > Integer.MAX_VALUE) {
            player.sendSystemMessage(Component.literal("§cGesamtpreis zu hoch! Kaufe weniger Items."));
            return;
        }
        int totalPrice = (int) totalPriceLong;

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

        // HINWEIS: Balance-Prüfung erfolgt atomar in EconomyManager.withdraw()
        // Separate Prüfung hier entfernt wegen TOCTOU Race Condition

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
            entry.getItem().getItem() instanceof ItemSpawnVehicle) {

            // Fahrzeug-Kauf über VehiclePurchaseHandler
            boolean success = VehiclePurchaseHandler.purchaseVehicle(
                player,
                merchant.getNpcData().getNpcUUID(),
                entry.getItem(),
                totalPrice
            );

            if (success) {
                // Reduziere Lagerbestand (nutze Warehouse-Integration)
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

        // Transaktion durchführen - EconomyManager.withdraw() ist atomar und prüft Balance
        if (EconomyManager.withdraw(player.getUUID(), totalPrice)) {
            player.getInventory().add(itemToGive);

            // Reduziere Lagerbestand (nutze Warehouse-Integration)
            merchant.getNpcData().onItemSoldFromWarehouse(player.level(), entry, quantity, totalPrice);

            player.sendSystemMessage(Component.literal("§aGekauft: " + quantity + "x " +
                entry.getItem().getHoverName().getString() + " für " + totalPrice + "$"));
        } else {
            // Atomare Prüfung fehlgeschlagen - nicht genug Geld
            player.sendSystemMessage(Component.literal("§cNicht genug Geld! Du brauchst " + totalPrice + "$ (aktuell: " +
                String.format("%.2f", EconomyManager.getBalance(player.getUUID())) + "$)"));
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
        ItemStack billItem = entry.getItem();

        // Lese Daten aus dem Bill-Item
        UUID fuelStationId = billItem.getTag().getUUID("FuelStationId");
        int totalFueled = billItem.getTag().getInt("TotalFueled");
        double totalCost = billItem.getTag().getDouble("TotalCost");

        // Prüfe ob Spieler genug Geld hat (bereits vorher geprüft, aber sicherheitshalber nochmal)
        if (!EconomyManager.withdraw(player.getUUID(), price)) {
            player.sendSystemMessage(Component.literal("§cFehler beim Abbuchung! Zahlung abgebrochen."));
            return;
        }

        // Markiere alle Rechnungen für diese Zapfsäule als bezahlt
        FuelBillManager.payBills(player.getUUID(), fuelStationId);
        FuelBillManager.save();

        // WICHTIG: Füge Umsatz zum Warehouse hinzu (7-Tage-Statistik)
        // Menge ist immer 1 (eine Rechnung), Preis ist der Rechnungsbetrag
        merchant.getNpcData().onItemSoldFromWarehouse(player.level(), entry, 1, price);

        // Erfolgs-Nachricht
        String stationName = FuelStationRegistry.getDisplayName(fuelStationId);
        player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("⛽ ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal("RECHNUNG BEZAHLT").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        player.sendSystemMessage(Component.literal("Zapfsäule: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(stationName).withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.literal("Getankt: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(totalFueled + " mB Bio-Diesel").withStyle(ChatFormatting.YELLOW)));
        player.sendSystemMessage(Component.literal("Gezahlt: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(StringUtils.formatMoney(totalCost)).withStyle(ChatFormatting.GOLD)));
        player.sendSystemMessage(Component.literal("Restguthaben: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(StringUtils.formatMoney(EconomyManager.getBalance(player.getUUID()))).withStyle(ChatFormatting.YELLOW)));
        player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));
    }

    /**
     * Erstellt Shop-Einträge für unbezahlte Rechnungen (kopiert von OpenMerchantShopPacket)
     */
    private List<NPCData.ShopEntry> createBillEntries(ServerPlayer player) {
        List<NPCData.ShopEntry> billEntries = new ArrayList<>();

        // Alle Tankstellen durchgehen
        for (UUID fuelStationId : FuelStationRegistry.getAllFuelStationIds()) {
            List<FuelBillManager.UnpaidBill> unpaidBills = FuelBillManager.getUnpaidBills(player.getUUID(), fuelStationId);

            if (!unpaidBills.isEmpty()) {
                // Summiere alle unbezahlten Rechnungen für diese Tankstelle
                int totalFueled = 0;
                double totalCost = 0.0;

                for (FuelBillManager.UnpaidBill bill : unpaidBills) {
                    totalFueled += bill.amountFueled;
                    totalCost += bill.totalCost;
                }

                // Erstelle Bill-Item
                String stationName = FuelStationRegistry.getDisplayName(fuelStationId);
                ItemStack billItem = new ItemStack(Items.PAPER);
                CompoundTag tag = billItem.getOrCreateTag();
                tag.putString("BillType", "FuelBill");
                tag.putUUID("FuelStationId", fuelStationId);
                tag.putInt("TotalFueled", totalFueled);
                tag.putDouble("TotalCost", totalCost);

                // Setze Namen mit Formatierung
                billItem.setHoverName(Component.literal("⛽ Tankrechnung - " + stationName)
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

                // Erstelle Shop-Entry (Preis ist die Rechnungssumme)
                NPCData.ShopEntry billEntry = new NPCData.ShopEntry(
                    billItem,
                    (int) Math.ceil(totalCost), // Preis aufgerundet
                    true, // Unbegrenzt verfügbar (ist ja eine Rechnung)
                    1     // Stock: 1
                );

                billEntries.add(billEntry);
            }
        }

        // WICHTIG: Wenn keine Rechnungen vorhanden, zeige trotzdem ein Papier-Item
        if (billEntries.isEmpty()) {
            ItemStack noBillItem = new ItemStack(Items.PAPER);
            CompoundTag tag = noBillItem.getOrCreateTag();
            tag.putString("BillType", "NoBill");
            tag.putDouble("TotalCost", 0.0);

            // Setze Namen: Keine offenen Rechnungen
            noBillItem.setHoverName(Component.literal("📄 Keine offenen Rechnungen")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

            // Erstelle Shop-Entry mit Preis 0
            NPCData.ShopEntry noBillEntry = new NPCData.ShopEntry(
                noBillItem,
                0, // Preis: 0€
                true,
                1
            );

            billEntries.add(noBillEntry);
        }

        return billEntries;
    }
}
