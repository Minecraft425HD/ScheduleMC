package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager;
import de.rolandsw.schedulemc.vehicle.fuel.FuelStationRegistry;
import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.menu.MerchantShopMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet zum Öffnen des Verkäufer-Shops
 */
public class OpenMerchantShopPacket {
    private final int merchantEntityId;

    public OpenMerchantShopPacket(int merchantEntityId) {
        this.merchantEntityId = merchantEntityId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(merchantEntityId);
    }

    public static OpenMerchantShopPacket decode(FriendlyByteBuf buf) {
        return new OpenMerchantShopPacket(buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            Entity entity = player.level().getEntity(merchantEntityId);
            if (entity instanceof CustomNPCEntity npc) {
                // Prüfe ob es ein Verkäufer oder Abschlepper ist
                if (npc.getNpcType() == NPCType.VERKAEUFER || npc.getNpcType() == NPCType.ABSCHLEPPER) {
                    // Prüfe ob NPC innerhalb der Arbeitszeiten ist
                    if (!npc.getNpcData().isWithinWorkingHours(player.level())) {
                        player.sendSystemMessage(Component.translatable("message.npc.outside_working_hours")
                            .withStyle(ChatFormatting.RED));
                        return;
                    }

                    // Öffne Shop-GUI und sende Shop-Items zum Client
                    List<NPCData.ShopEntry> shopItems = new ArrayList<>(npc.getNpcData().getBuyShop().getEntries());

                    // Spezialbehandlung für Tankstelle: Füge unbezahlte Rechnungen hinzu
                    if (npc.getMerchantCategory() == MerchantCategory.TANKSTELLE) {
                        List<NPCData.ShopEntry> billEntries = createFuelBillEntries(player);
                        shopItems.addAll(0, billEntries); // Am Anfang einfügen
                    }

                    // Spezialbehandlung für Abschlepper: Füge Towing-Rechnungen hinzu
                    if (npc.getNpcType() == NPCType.ABSCHLEPPER) {
                        List<NPCData.ShopEntry> towingBillEntries = createTowingBillEntries(player);
                        shopItems.addAll(0, towingBillEntries); // Am Anfang einfügen
                    }

                    // Determine display name based on NPC type
                    String displayName = npc.getNpcType() == NPCType.VERKAEUFER
                        ? npc.getMerchantCategory().getDisplayName()
                        : npc.getServiceCategory().getDisplayName();

                    NetworkHooks.openScreen(player, new SimpleMenuProvider(
                        (id, playerInventory, p) -> new MerchantShopMenu(id, playerInventory, npc),
                        Component.literal(displayName)
                    ), buf -> {
                        buf.writeInt(npc.getId());
                        // Sende Shop-Items
                        buf.writeInt(shopItems.size());
                        for (var entry : shopItems) {
                            buf.writeItem(entry.getItem());
                            buf.writeInt(entry.getPrice());
                            buf.writeBoolean(entry.isUnlimited());

                            // Stock aus Warehouse oder lokalem Entry
                            int actualStock;
                            if (entry.isUnlimited()) {
                                actualStock = Integer.MAX_VALUE;
                            } else if (npc.getNpcData().hasWarehouse()) {
                                // Hole Stock aus Warehouse
                                var warehouse = npc.getNpcData().getWarehouseEntity(player.level());
                                if (warehouse != null) {
                                    actualStock = warehouse.getStock(entry.getItem().getItem());
                                } else {
                                    actualStock = entry.getStock(); // Fallback
                                }
                            } else {
                                actualStock = entry.getStock();
                            }
                            buf.writeInt(actualStock);
                        }
                    });
                }
            }
        });
    }

    /**
     * Erstellt Shop-Einträge für unbezahlte Tankrechnungen
     */
    private List<NPCData.ShopEntry> createFuelBillEntries(ServerPlayer player) {
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

    /**
     * Erstellt Shop-Einträge für unbezahlte Abschlepprechnungen
     */
    private List<NPCData.ShopEntry> createTowingBillEntries(ServerPlayer player) {
        List<NPCData.ShopEntry> billEntries = new ArrayList<>();

        // Hole unbezahlte Abschlepprechnungen
        java.util.List<de.rolandsw.schedulemc.towing.TowingInvoiceData> unpaidInvoices =
            de.rolandsw.schedulemc.towing.TowingYardManager.getUnpaidInvoices(player.getUUID());

        if (!unpaidInvoices.isEmpty()) {
            for (de.rolandsw.schedulemc.towing.TowingInvoiceData invoice : unpaidInvoices) {
                // Erstelle Bill-Item
                ItemStack billItem = new ItemStack(Items.PAPER);
                CompoundTag tag = billItem.getOrCreateTag();
                tag.putString("BillType", "TowingBill");
                tag.putUUID("InvoiceId", invoice.getInvoiceId());
                tag.putDouble("TotalCost", invoice.getAmount());

                // Hole den Yard-Namen vom PlotManager
                String yardName = invoice.getTowingYardPlotId();
                de.rolandsw.schedulemc.region.PlotRegion plot =
                    de.rolandsw.schedulemc.region.PlotManager.getPlotById(yardName);
                if (plot != null && plot.getName() != null && !plot.getName().isEmpty()) {
                    yardName = plot.getName();
                }

                // Setze Namen mit Formatierung
                billItem.setHoverName(Component.literal("🚗 Abschlepprechnung - " + yardName)
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

                // Erstelle Shop-Entry
                NPCData.ShopEntry billEntry = new NPCData.ShopEntry(
                    billItem,
                    (int) Math.ceil(invoice.getAmount()), // Preis aufgerundet
                    true, // Unbegrenzt verfügbar
                    1     // Stock: 1
                );

                billEntries.add(billEntry);
            }
        } else {
            // Keine offenen Rechnungen
            ItemStack noBillItem = new ItemStack(Items.PAPER);
            CompoundTag tag = noBillItem.getOrCreateTag();
            tag.putString("BillType", "NoBill");
            tag.putDouble("TotalCost", 0.0);

            noBillItem.setHoverName(Component.translatable("towing.no_invoices")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

            NPCData.ShopEntry noBillEntry = new NPCData.ShopEntry(
                noBillItem,
                0,
                true,
                1
            );

            billEntries.add(noBillEntry);
        }

        return billEntries;
    }
}
