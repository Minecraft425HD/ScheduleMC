package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.menu.MerchantShopMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

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
                        var shopItems = npc.getNpcData().getBuyShop().getEntries();
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
                            }
                        });
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
