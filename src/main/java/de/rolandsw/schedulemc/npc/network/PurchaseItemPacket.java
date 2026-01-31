package de.rolandsw.schedulemc.npc.network;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager;
import de.rolandsw.schedulemc.vehicle.fuel.FuelStationRegistry;
import de.rolandsw.schedulemc.vehicle.items.ItemSpawnVehicle;
import de.rolandsw.schedulemc.vehicle.vehicle.VehiclePurchaseHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.NPCLifeSystemIntegration;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.economy.TradeEventHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet für Item-Kauf von Verkäufer-NPCs
 */
public class PurchaseItemPacket {
    private static final Logger LOGGER = LogUtils.getLogger();

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
        // ═══════════════════════════════════════════════════════════
        // NPC LIFE SYSTEM INTEGRATION: Prüfe ob NPC handeln möchte
        // ═══════════════════════════════════════════════════════════
        if (!merchant.isWillingToTrade()) {
            NPCLifeData lifeData = merchant.getLifeData();
            if (lifeData != null) {
                EmotionState emotion = lifeData.getEmotions().getCurrentEmotion();
                if (emotion == EmotionState.FEARFUL) {
                    player.sendSystemMessage(Component.translatable("message.npc.too_scared_to_trade")
                        .withStyle(ChatFormatting.RED));
                } else if (lifeData.getNeeds().getEnergy() < 10) {
                    player.sendSystemMessage(Component.translatable("message.npc.too_tired_to_trade")
                        .withStyle(ChatFormatting.YELLOW));
                } else {
                    player.sendSystemMessage(Component.translatable("message.npc.not_willing_to_trade")
                        .withStyle(ChatFormatting.GRAY));
                }
            }
            return;
        }

        // Null-Safety: Prüfe ob NPC-Daten und Shop vorhanden sind
        if (merchant.getNpcData() == null || merchant.getNpcData().getBuyShop() == null) {
            player.sendSystemMessage(Component.translatable("message.npc.shop_unavailable")
                .withStyle(ChatFormatting.RED));
            return;
        }

        // WICHTIG: Für Tankstellen müssen wir die Bill-Items auch hier hinzufügen, damit die Indizes stimmen!
        List<NPCData.ShopEntry> shopItems = new ArrayList<>(merchant.getNpcData().getBuyShop().getEntries());

        // Spezialbehandlung für Tankstelle: Füge unbezahlte Rechnungen hinzu (wie in OpenMerchantShopPacket)
        if (merchant.getMerchantCategory() == MerchantCategory.TANKSTELLE) {
            List<NPCData.ShopEntry> billEntries = createBillEntries(player);
            shopItems.addAll(0, billEntries); // Am Anfang einfügen - GLEICHE LOGIK WIE BEIM ÖFFNEN!
        }

        if (itemIndex < 0 || itemIndex >= shopItems.size()) {
            player.sendSystemMessage(Component.translatable("message.common.invalid_item"));
            return;
        }

        NPCData.ShopEntry entry = shopItems.get(itemIndex);

        // SICHERHEIT: Integer Overflow Prevention
        // Maximale Menge pro Transaktion begrenzen
        final int MAX_QUANTITY = 10000;
        int safeQuantity = Math.min(quantity, MAX_QUANTITY);
        if (quantity > MAX_QUANTITY) {
            player.sendSystemMessage(Component.translatable("message.purchase.max_quantity", MAX_QUANTITY));
            return;
        }

        // ═══════════════════════════════════════════════════════════
        // NPC LIFE SYSTEM INTEGRATION: Dynamische Preisanpassung
        // ═══════════════════════════════════════════════════════════
        float priceModifier = merchant.getPersonalPriceModifier();

        // SICHERHEIT: Berechne mit long um Overflow zu erkennen
        long basePriceLong = (long) entry.getPrice() * safeQuantity;
        long totalPriceLong = (long) (basePriceLong * priceModifier);
        if (totalPriceLong > Integer.MAX_VALUE) {
            player.sendSystemMessage(Component.translatable("message.shop.total_too_high"));
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
            // Klare Meldung mit Item-Name, angeforderte Menge und verfügbare Menge
            player.sendSystemMessage(Component.translatable("message.shop.warehouse_shortage",
                entry.getItem().getHoverName().getString(),
                quantity,
                available));
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
                player.sendSystemMessage(Component.translatable("message.bank.no_outstanding_bills")
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

            LOGGER.info("Fahrzeugkauf-Paket empfangen: Spieler={}, Händler-Category={}, Item={}",
                player.getName().getString(), merchant.getMerchantCategory(), entry.getItem().getHoverName().getString());

            // Fahrzeug-Kauf über VehiclePurchaseHandler
            boolean success = VehiclePurchaseHandler.purchaseVehicle(
                player,
                merchant.getNpcData().getNpcUUID(),
                entry.getItem(),
                totalPrice
            );

            if (success) {
                LOGGER.info("Fahrzeugkauf erfolgreich, reduziere Lagerbestand");
                // Reduziere Lagerbestand (nutze Warehouse-Integration)
                merchant.getNpcData().onItemSoldFromWarehouse(player.level(), entry, quantity, totalPrice);
            } else {
                LOGGER.warn("Fahrzeugkauf fehlgeschlagen (VehiclePurchaseHandler gab false zurück)");
            }
            return;
        }

        // Normale Items: Prüfe ob Spieler genug Platz im Inventar hat
        ItemStack itemToGive = entry.getItem().copy();
        itemToGive.setCount(quantity);

        if (!canAddItemToInventory(player, itemToGive)) {
            player.sendSystemMessage(Component.translatable("message.purchase.inventory_full"));
            return;
        }

        // Transaktion durchführen - EconomyManager.withdraw() ist atomar und prüft Balance
        if (EconomyManager.withdraw(player.getUUID(), totalPrice)) {
            player.getInventory().add(itemToGive);

            // Reduziere Lagerbestand (nutze Warehouse-Integration)
            merchant.getNpcData().onItemSoldFromWarehouse(player.level(), entry, quantity, totalPrice);

            player.sendSystemMessage(Component.translatable("network.purchase.success",
                String.valueOf(quantity),
                entry.getItem().getHoverName().getString(),
                String.valueOf(totalPrice)
            ));

            // ═══════════════════════════════════════════════════════════
            // NPC LIFE SYSTEM INTEGRATION: Update Emotionen und Gedächtnis
            // ═══════════════════════════════════════════════════════════
            NPCLifeData lifeData = merchant.getLifeData();
            if (lifeData != null) {
                // NPC freut sich über den Verkauf (je größer der Betrag, desto mehr)
                float happinessAmount = Math.min(30.0f, totalPrice / 100.0f);
                lifeData.getEmotions().trigger(EmotionState.HAPPY, happinessAmount, 600);

                // Speichere im Gedächtnis
                lifeData.getMemory().addMemory(
                    player.getUUID(),
                    MemoryType.TRADED,
                    String.format("Kaufte %dx %s für %d",
                        quantity,
                        entry.getItem().getHoverName().getString(),
                        totalPrice),
                    totalPrice > 500 ? 4 : 2 // Große Käufe sind wichtiger
                );

                // Bei großen Einkäufen: Spieler als "guter Kunde" merken
                if (totalPrice > 1000) {
                    lifeData.getMemory().addPlayerTag(player.getUUID(), "GutKunde");
                }
            }

            // Trade Event für Economy-System melden
            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                // NPC Life System: Transaktion verarbeiten
                TradeEventHelper.processPurchase(merchant, player, entry.getItem(),
                    totalPrice, entry.getPrice() * safeQuantity, serverLevel);

                // WorldEventManager Preismodifikator
                de.rolandsw.schedulemc.npc.life.world.WorldEventManager worldEventManager =
                    de.rolandsw.schedulemc.npc.life.world.WorldEventManager.getManager(serverLevel);
                float worldModifier = worldEventManager.getCombinedPriceModifier(player.blockPosition());
                // Hinweis: Modifikator wird für zukünftige Preisberechnungen gespeichert

                // Cross-System Koordination via Life System Integration
                NPCLifeSystemIntegration integration = NPCLifeSystemIntegration.get(serverLevel);
                integration.onTradeCompleted(player, merchant, totalPrice);
            }
        } else {
            // Atomare Prüfung fehlgeschlagen - nicht genug Geld
            player.sendSystemMessage(Component.translatable("network.purchase.insufficient_funds",
                String.valueOf(totalPrice),
                String.format("%.2f", EconomyManager.getBalance(player.getUUID()))
            ));
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
            player.sendSystemMessage(Component.translatable("message.purchase.payment_failed"));
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
            .append(Component.translatable("message.fuel.bill_paid").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        player.sendSystemMessage(Component.translatable("message.fuel.pump_label").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(stationName).withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.translatable("message.fuel.refueled_label").withStyle(ChatFormatting.GRAY)
            .append(Component.translatable("message.fuel.amount_biodiesel", totalFueled).withStyle(ChatFormatting.YELLOW)));
        player.sendSystemMessage(Component.translatable("message.fuel.paid_label").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.format("%.2f€", totalCost)).withStyle(ChatFormatting.GOLD)));
        player.sendSystemMessage(Component.translatable("message.bank.remaining_credit_label").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID()))).withStyle(ChatFormatting.YELLOW)));
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
            noBillItem.setHoverName(Component.translatable("message.bank.no_open_bills")
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
