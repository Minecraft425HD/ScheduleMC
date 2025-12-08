package de.rolandsw.schedulemc.npc.network;

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
     * TODO: Re-implement for new vehicle system with FuelBillManager and GasStationRegistry
     */
    private List<NPCData.ShopEntry> createBillEntries(ServerPlayer player) {
        List<NPCData.ShopEntry> billEntries = new ArrayList<>();

        // TODO: Implement fuel bill system for new vehicle architecture
        // Temporarily returning empty list until fuel/gas station system is reimplemented

        return billEntries;
    }
}
