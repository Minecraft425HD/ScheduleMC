package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.menu.MerchantShopMenu;
import de.rolandsw.schedulemc.vehicle.fuel.FuelBillManager;
import de.rolandsw.schedulemc.vehicle.fuel.GasStationRegistry;
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
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(merchantEntityId);
                if (entity instanceof CustomNPCEntity npc) {
                    // Prüfe ob es ein Verkäufer ist
                    if (npc.getNpcType() == NPCType.VERKAEUFER) {
                        // Öffne Shop-GUI und sende Shop-Items zum Client
                        List<NPCData.ShopEntry> shopItems = new ArrayList<>(npc.getNpcData().getBuyShop().getEntries());

                        // Spezialbehandlung für Tankstelle: Füge unbezahlte Rechnungen hinzu
                        if (npc.getMerchantCategory() == MerchantCategory.TANKSTELLE) {
                            List<NPCData.ShopEntry> billEntries = createBillEntries(player);
                            shopItems.addAll(0, billEntries); // Am Anfang einfügen
                        }

                        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                            (id, playerInventory, p) -> new MerchantShopMenu(id, playerInventory, npc),
                            Component.literal(npc.getMerchantCategory().getDisplayName())
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
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Erstellt Shop-Einträge für unbezahlte Rechnungen
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
